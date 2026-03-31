package vtdi.keniel.filems.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vtdi.keniel.filems.dao.HibernateCaseDAO;
import vtdi.keniel.filems.models.CourtCase;
import vtdi.keniel.filems.dao.ICourtCaseDAO;

/**
 * Handles individual client connections on a separate thread.
 */
public class ClientHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(ClientHandler.class);
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    // We will use our DAO to interact with the database when the client asks us to
    private ICourtCaseDAO caseDAO;

    public ClientHandler(Socket socket, ICourtCaseDAO caseDAO) {
        this.socket = socket;
        this.caseDAO = caseDAO;
    }

    @Override
    public void run() {
        try {
            // Architecture Rule: ALWAYS initialize ObjectOutputStream before ObjectInputStream
            // If both sides initialize InputStream first, they will deadlock waiting for headers!
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            
            logger.info("Input/Output streams established for client: " + socket.getInetAddress().getHostAddress());

            // Listen for incoming serialized objects
            while (true) {
                // Read the serialized NetworkMessage from the client
                NetworkMessage request = (NetworkMessage) in.readObject();
                logger.info("Received request command: " + request.getCommand());

                // Process the request and generate a response
                NetworkMessage response = processRequest(request);
                
                // Send the serialized response back to the client
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

    /**
     * Determines what to do based on the command sent by the client.
     */
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
                        
                        logger.info("Successfully updated CourtCase with ID: " + updatedCase.getCaseId());
                        // Return the response instead of writing it directly
                        return new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Case updated successfully.");
                    } catch (Exception e) {
                        logger.error("Fatal error during UPDATE_CASE operation.", e);
                        return new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Failed to update case: " + e.getMessage());
                    }

                case DELETE_CASE:
                    try {
                        Integer caseIdToDelete = (Integer) request.getPayload();
                        // Convert the Integer to a String to satisfy your DAO's signature
                        caseDAO.deleteCase(String.valueOf(caseIdToDelete)); 
        
                        logger.info("Successfully deleted CourtCase with ID: " + caseIdToDelete);
                        return new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, "Case deleted successfully.");
                    } catch (Exception e) {
                        logger.error("Fatal error during DELETE_CASE operation for ID: " + request.getPayload(), e);
                        return new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Failed to delete case: " + e.getMessage());
                    }
                    
                default:
                    logger.warn("Received unknown command from client: " + request.getCommand());
                    return new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Unknown command.");
            }
        } catch (Exception e) {
            logger.fatal("Critical failure in ClientHandler switch block processing.", e);
            // Fallback return statement if the entire switch block fails
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