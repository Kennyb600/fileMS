package vtdi.keniel.filems.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vtdi.keniel.filems.models.CourtCase;

/**
 * The TCP/IP Client that communicates with the FileMSServer.
 */
public class FileMSClient {

    // Strict Rule: Log4j Logger
    private static final Logger logger = LogManager.getLogger(FileMSClient.class);
    
    private static final String SERVER_IP = "127.0.0.1"; // Localhost
    private static final int SERVER_PORT = 8888;

    /**
     * Connects to the server, sends a NetworkMessage, and returns the server's response.
     */
    public NetworkMessage sendRequest(NetworkMessage request) {
        NetworkMessage response = null;

        // Fulfills the "Setup TCP/IP Client" requirement
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             // Architecture Rule: Output stream MUST be initialized before Input stream
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            logger.info("Successfully connected to the Server at " + SERVER_IP + ":" + SERVER_PORT);

            // Send the serialized request object over the network stream
            out.writeObject(request);
            out.flush();
            logger.info("Sent request to server: " + request.getCommand());

            // Wait for and read the serialized response object from the server
            response = (NetworkMessage) in.readObject();
            logger.info("Received response from server: " + response.getCommand());

        } catch (IOException e) {
            // Strict Rule: Manage and Log All Exceptions
            logger.error("Network communication error: " + e.getMessage(), e);
            response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Connection Failed");
        } catch (ClassNotFoundException e) {
            logger.fatal("Received an unknown object format from the server: " + e.getMessage(), e);
            response = new NetworkMessage(NetworkMessage.Command.RESPONSE_ERROR, "Class Cast Exception");
        }

        return response;
    }

    // Quick test method to run the client standalone
    public static void main(String[] args) {
        FileMSClient client = new FileMSClient();
        
        logger.info("--- STARTING CLIENT TEST ---");
        
        // Let's ask the server for all the court cases!
        NetworkMessage request = new NetworkMessage(NetworkMessage.Command.GET_ALL_CASES, null);
        NetworkMessage response = client.sendRequest(request);
        
        // Process the results
        if (response.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
            System.out.println("\nSUCCESS! The server sent back the following data:");
            
            // Cast the payload back to a List of CourtCases
            @SuppressWarnings("unchecked")
            List<CourtCase> cases = (List<CourtCase>) response.getPayload();
            
            for (CourtCase c : cases) {
                System.out.println(" - Case Number: " + c.getCaseNumber() + " | Order: " + c.getCourtOrder());
            }
        } else {
            System.out.println("\nFAILED: Server returned an error - " + response.getPayload());
        }
        
        logger.info("--- CLIENT TEST FINISHED ---");
    }
}