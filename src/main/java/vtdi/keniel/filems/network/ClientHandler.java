package vtdi.keniel.filems.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vtdi.keniel.filems.dao.HibernateCaseDAO;

/**
 * Handles individual client connections on a separate thread.
 */
public class ClientHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(ClientHandler.class);
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    // We will use our DAO to interact with the database when the client asks us to
    private HibernateCaseDAO caseDAO;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.caseDAO = new HibernateCaseDAO();
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

            // Listen for incoming serialized objects [cite: 71]
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
                    // Example: Client wants all cases. We fetch them using the DAO.
                    logger.info("Processing GET_ALL_CASES command...");
                    return new NetworkMessage(NetworkMessage.Command.RESPONSE_OK, caseDAO.getAllCases());
                    
                // We will implement INSERT, UPDATE, DELETE here later
                    
                default:
                    logger.warn("Received unknown command: " + request.getCommand());
                    return new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Unknown Command");
            }
        } catch (Exception e) {
            logger.error("Error processing client request: " + e.getMessage(), e);
            return new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Internal Server Error");
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