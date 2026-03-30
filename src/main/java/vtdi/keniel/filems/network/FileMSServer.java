package vtdi.keniel.filems.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main TCP/IP Server Engine.
 * Listens for incoming client connections and hands them off to a new thread.
 */
public class FileMSServer {

    // Strict Rule: Log4j Logger [cite: 72, 76]
    private static final Logger logger = LogManager.getLogger(FileMSServer.class);
    
    private static final int PORT = 8888;
    private boolean isRunning = true;

    public void startServer() {
        // try-with-resources ensures the ServerSocket is closed if the app crashes
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("FileMS TCP/IP Server Engine started. Listening on port " + PORT);

            while (isRunning) {
                logger.info("Waiting for a new client connection...");
                
                // This line blocks (pauses) until a client actually connects
                Socket clientSocket = serverSocket.accept(); 
                logger.info("New client connected from: " + clientSocket.getInetAddress().getHostAddress());

                // Fulfills the "Threading" requirement to manage multiple client requests 
                // We pass the socket to a new handler and start it on its own thread
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(handler);
                clientThread.start();
            }

        } catch (IOException e) {
            // Strict Rule: Manage and Log All Exceptions [cite: 73, 77]
            logger.fatal("Server Engine encountered a fatal I/O error: " + e.getMessage(), e);
        }
    }

    public void stopServer() {
        this.isRunning = false;
        logger.info("Server shutdown initiated.");
    }
    
    // Quick test method to run the server standalone
    public static void main(String[] args) {
        FileMSServer server = new FileMSServer();
        server.startServer();
    }
}