package vtdi.keniel.filems.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vtdi.keniel.filems.models.CourtCase;
import vtdi.keniel.filems.dao.ICourtCaseDAO;

public class ClientHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(ClientHandler.class);
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private ICourtCaseDAO caseDAO;

    public ClientHandler(Socket socket, ICourtCaseDAO caseDAO) {
        this.socket = socket;
        this.caseDAO = caseDAO;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            
            logger.info("Input/Output streams established for client: " + socket.getInetAddress().getHostAddress());

            while (true) {
                NetworkMessage request = (NetworkMessage) in.readObject();
                logger.info("Received request command: " + request.getCommand());

                NetworkMessage response = processRequest(request);
                
                out.writeObject(response);
                out.flush();
            }

        } catch (java.io.EOFException e) {
            logger.info("Client disconnected gracefully.");
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Connection error with client: " + e.getMessage(), e);
        } finally {
            closeConnections();
        }
    }

    private NetworkMessage processRequest(NetworkMessage request) {
        try {
            switch (request.getCommand()) {
                case GET_ALL_CASES:
                    logger.info("Processing GET_ALL_CASES command...");
                    return new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, caseDAO.getAllCases());
                
                case INSERT_CASE:
                    logger.info("Processing INSERT_CASE command...");
                    CourtCase newCase = (CourtCase) request.getPayload();
                    
                    boolean success = caseDAO.insertCase(newCase);
                    if (success) {
                        return new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Case inserted successfully.");
                    } else {
                        return new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Database insertion failed.");
                    }
                
                case UPDATE_CASE:
                    try {
                        CourtCase updatedCase = (CourtCase) request.getPayload();
                        caseDAO.updateCase(updatedCase); 
                        
                        // CHANGED: Logging the Case Number instead of Case ID
                        logger.info("Successfully updated CourtCase with Number: " + updatedCase.getCaseNumber());
                        return new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Case updated successfully.");
                    } catch (Exception e) {
                        logger.error("Fatal error during UPDATE_CASE operation.", e);
                        return new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Failed to update case: " + e.getMessage());
                    }

                case DELETE_CASE:
                    try {
                        String caseNumberToDelete = (String) request.getPayload();
                        
                        // CHANGED: Respecting the boolean returned by the DAO
                        boolean isDeleted = caseDAO.deleteCase(caseNumberToDelete); 
        
                        if (isDeleted) {
                            logger.info("Successfully deleted CourtCase with Number: " + caseNumberToDelete);
                            return new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Case deleted successfully.");
                        } else {
                            logger.warn("DAO failed to delete case: " + caseNumberToDelete);
                            return new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Case not found in database.");
                        }
                    } catch (Exception e) {
                        logger.error("Fatal error during DELETE_CASE operation for Number: " + request.getPayload(), e);
                        return new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Failed to delete case: " + e.getMessage());
                    }
                    
                case GET_ALL_JUDGES:
                    try (org.hibernate.Session session = vtdi.keniel.filems.utils.HibernateUtil.getSessionFactory().openSession()) {
                    java.util.List<vtdi.keniel.filems.models.Judge> judges = session.createQuery("FROM Judge", vtdi.keniel.filems.models.Judge.class).list();
                        return new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, judges);
                } catch (Exception e) {
                    logger.error("Error fetching judges: " + e.getMessage(), e);
                        return new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, e.getMessage());
                }

                case GET_ALL_PARTIES:
                    try (org.hibernate.Session session = vtdi.keniel.filems.utils.HibernateUtil.getSessionFactory().openSession()) {
                    java.util.List<vtdi.keniel.filems.models.InvolvedParty> parties = session.createQuery("FROM InvolvedParty", vtdi.keniel.filems.models.InvolvedParty.class).list();
                        return new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, parties);
                } catch (Exception e) {
                    logger.error("Error fetching parties: " + e.getMessage(), e);
                        return new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, e.getMessage());
                }
                    
                default:
                    logger.warn("Received unknown command from client: " + request.getCommand());
                    return new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Unknown command.");
            }
        } catch (Exception e) {
            logger.fatal("Critical failure in ClientHandler switch block processing.", e);
            return new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Critical server error processing request.");
        }
    }

    private void closeConnections() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            logger.info("Cleaned up and closed connections for client.");
        } catch (IOException e) {
            logger.error("Error while closing client connections: " + e.getMessage(), e);
        }
    }
}