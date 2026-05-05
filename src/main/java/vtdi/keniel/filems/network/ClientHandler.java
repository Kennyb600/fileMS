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
                            List<CourtCaseDTO> caseDTOs = EntityMapper.toCourtCaseDTOList(cases);
                            response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, caseDTOs);
                            break;

                        case GET_ALL_JUDGES:
                            List<Judge> judges = courtCaseDAO.getAllJudges();
                            List<JudgeDTO> judgeDTOs = EntityMapper.toJudgeDTOList(judges);
                            response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, judgeDTOs);
                            break;

                        case GET_ALL_PARTIES:
                            List<InvolvedParty> parties = courtCaseDAO.getAllParties();
                            List<InvolvedPartyDTO> partyDTOs = EntityMapper.toInvolvedPartyDTOList(parties);
                            response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, partyDTOs);
                            break;

                        case INSERT_JUDGE:
                            if (request.getPayload() instanceof JudgeDTO) {
                                JudgeDTO incomingDTO = (JudgeDTO) request.getPayload();
                                Judge newJudge = EntityMapper.toJudgeEntity(incomingDTO);
                                courtCaseDAO.saveJudge(newJudge);
                                response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Judge added successfully.");
                            } else {
                                response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Invalid payload.");
                            }
                            break;
                            
                        case INSERT_CASE:
                            if (request.getPayload() instanceof CourtCaseDTO) {
                                CourtCaseDTO incomingDTO = (CourtCaseDTO) request.getPayload();
                                CourtCase newCase = EntityMapper.toCourtCaseEntity(incomingDTO);
                                
                                // FIX: Use the boolean method to verify it actually saved to MySQL!
                                boolean success = courtCaseDAO.insertCase(newCase);
                                
                                if (success) {
                                    response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Court Case filed successfully.");
                                } else {
                                    response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Database failed to save the Court Case.");
                                }
                            } else {
                                response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Invalid payload for INSERT_CASE.");
                            }
                            break;
                            
                            case UPDATE_CASE:
                            if (request.getPayload() instanceof CourtCaseDTO) {
                                CourtCaseDTO incomingDTO = (CourtCaseDTO) request.getPayload();
                                CourtCase updatedCase = EntityMapper.toCourtCaseEntity(incomingDTO);
                                
                                boolean success = courtCaseDAO.updateCase(updatedCase);
                                
                                if (success) {
                                    response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Court Case updated successfully.");
                                } else {
                                    response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Database failed to update the case.");
                                }
                            } else {
                                response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Invalid payload for UPDATE_CASE.");
                            }
                            break;
                        
                            case UPDATE_PARTY:
            if (request.getPayload() instanceof InvolvedPartyDTO) {
                InvolvedPartyDTO incomingDTO = (InvolvedPartyDTO) request.getPayload();
                InvolvedParty updatedParty = EntityMapper.toInvolvedPartyEntity(incomingDTO);
                
                boolean success = courtCaseDAO.updateParty(updatedParty);
                if (success) {
                    response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Party updated successfully.");
                } else {
                    response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Database failed to update party.");
                }
            } else {
                response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Invalid payload.");
            }
            break;

        case DELETE_PARTY:
            if (request.getPayload() instanceof Integer) {
                int partyId = (Integer) request.getPayload();
                boolean success = courtCaseDAO.deleteParty(partyId);
                if (success) {
                    response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Party deleted successfully.");
                } else {
                    response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Database failed to delete party. It may be linked to a case.");
                }
            } else {
                response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Invalid payload. Expected an Integer ID.");
            }
            break;
            
            case UPDATE_JUDGE:
            if (request.getPayload() instanceof JudgeDTO) {
                JudgeDTO incomingDTO = (JudgeDTO) request.getPayload();
                Judge updatedJudge = EntityMapper.toJudgeEntity(incomingDTO);
                
                boolean success = courtCaseDAO.updateJudge(updatedJudge);
                if (success) {
                    response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Judge updated successfully.");
                } else {
                    response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Database failed to update judge.");
                }
            } else {
                response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Invalid payload.");
            }
            break;

        case DELETE_JUDGE:
            if (request.getPayload() instanceof Integer) {
                int judgeId = (Integer) request.getPayload();
                boolean success = courtCaseDAO.deleteJudge(judgeId);
                if (success) {
                    response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Judge deleted successfully.");
                } else {
                    response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Database failed to delete judge. They may be presiding over an active Court Case.");
                }
            } else {
                response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Invalid payload. Expected an Integer ID.");
            }
            break;
                            
                        case INSERT_PARTY:
                            if (request.getPayload() instanceof InvolvedPartyDTO) {
                                InvolvedPartyDTO incomingDTO = (InvolvedPartyDTO) request.getPayload();
                                InvolvedParty newParty = EntityMapper.toInvolvedPartyEntity(incomingDTO);
                                courtCaseDAO.saveParty(newParty);
                                response = new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Party registered successfully.");
                            } else {
                                response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Invalid payload.");
                            }
                            break;            
                        
                        // Default moved to the bottom!
                        default:
                            response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Unknown request type.");
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