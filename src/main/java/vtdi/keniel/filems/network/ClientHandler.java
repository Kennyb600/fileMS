package vtdi.keniel.filems.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import vtdi.keniel.filems.dao.ICourtCaseDAO;
import vtdi.keniel.filems.models.CourtCase;
import vtdi.keniel.filems.models.Judge;
import vtdi.keniel.filems.models.InvolvedParty;
import vtdi.keniel.filems.dto.CourtCaseDTO;
import vtdi.keniel.filems.dto.JudgeDTO;
import vtdi.keniel.filems.dto.InvolvedPartyDTO;
import vtdi.keniel.filems.dto.EntityMapper;

/**
 * Dedicated thread for handling continuous client socket connections.
 * Now utilizing the DTO mapping layer for secure, proxy-free network serialization.
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final ICourtCaseDAO courtCaseDAO;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket clientSocket, ICourtCaseDAO courtCaseDAO) {
        this.clientSocket = clientSocket;
        this.courtCaseDAO = courtCaseDAO;
    }

    @Override
    public void run() {
        try {
            // Output MUST be initialized first to flush network headers
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());

            System.out.println("Client connected: " + clientSocket.getInetAddress());

            while (!clientSocket.isClosed()) {
                try {
                    NetworkMessage request = (NetworkMessage) in.readObject();
                    
                    if (request == null) break;

                    NetworkMessage response;

                    switch (request.getCommand()) { 
                        case GET_ALL_CASES:
                            List<CourtCase> cases = courtCaseDAO.getAllCases();
                            // Pass through the DTO Shield
                            List<CourtCaseDTO> caseDTOs = EntityMapper.toCourtCaseDTOList(cases);
                            response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, caseDTOs);
                            break;

                        case GET_ALL_JUDGES:
                            List<Judge> judges = courtCaseDAO.getAllJudges();
                            // Pass through the DTO Shield
                            List<JudgeDTO> judgeDTOs = EntityMapper.toJudgeDTOList(judges);
                            response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, judgeDTOs);
                            break;

                        case GET_ALL_PARTIES:
                            List<InvolvedParty> parties = courtCaseDAO.getAllParties();
                            // Pass through the DTO Shield
                            List<InvolvedPartyDTO> partyDTOs = EntityMapper.toInvolvedPartyDTOList(parties);
                            response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, partyDTOs);
                            break;

                        default:
                            response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Unknown request type.");
                            break;
                            
                        case INSERT_JUDGE:
        if (request.getPayload() instanceof JudgeDTO) {
            JudgeDTO incomingDTO = (JudgeDTO) request.getPayload();
            // 1. Translate DTO to Entity
            Judge newJudge = EntityMapper.toJudgeEntity(incomingDTO);
            // 2. Save to Database
            courtCaseDAO.saveJudge(newJudge);
            // 3. Send Success Response
            response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Judge added successfully.");
        } else {
            response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Invalid payload.");
        }
        break;
                    
                    
                    case INSERT_CASE:
        if (request.getPayload() instanceof CourtCaseDTO) {
            CourtCaseDTO incomingDTO = (CourtCaseDTO) request.getPayload();
            // 1. Translate DTO back to Entity
            CourtCase newCase = EntityMapper.toCourtCaseEntity(incomingDTO);
            // 2. Save to Database
            courtCaseDAO.saveCase(newCase);
            // 3. Send Success Response
            response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Court Case filed successfully.");
        } else {
            response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Invalid payload for INSERT_CASE.");
        }
        break;
        
        case INSERT_PARTY:
                            if (request.getPayload() instanceof InvolvedPartyDTO) {
                                InvolvedPartyDTO incomingDTO = (InvolvedPartyDTO) request.getPayload();
                                // Translate and Save
                                InvolvedParty newParty = EntityMapper.toInvolvedPartyEntity(incomingDTO);
                                courtCaseDAO.saveParty(newParty);
                                response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Party registered successfully.");
                            } else {
                                response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Invalid payload.");
                            }
                            break;            
                    
                    }
                    out.writeObject(response);
                    out.flush();

                } catch (ClassNotFoundException e) {
                    System.err.println("Received unidentifiable object from client.");
                    break; 
                } catch (IOException e) {
                    System.out.println("Client disconnected gracefully.");
                    break; 
                }
            }

        } catch (IOException e) {
            System.err.println("Error initializing streams: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
    