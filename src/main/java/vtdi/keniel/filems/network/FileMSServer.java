package vtdi.keniel.filems.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import vtdi.keniel.filems.config.AppConfig;
import vtdi.keniel.filems.dao.ICourtCaseDAO;

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
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("FileMS TCP/IP Server Engine started. Listening on port " + PORT);

            // 1. Initialize Spring IoC Container
            logger.info("Initializing Spring IoC Container...");
            ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
            
            // 2. Ask Spring for our DAO plug-in
            ICourtCaseDAO caseDAO = context.getBean(ICourtCaseDAO.class);

            while (isRunning) {
                logger.info("Waiting for a new client connection...");
                Socket clientSocket = serverSocket.accept(); 
                logger.info("New client connected from: " + clientSocket.getInetAddress().getHostAddress());

                // 3. Pass the injected DAO to the ClientHandler
                ClientHandler handler = new ClientHandler(clientSocket, caseDAO);
                Thread clientThread = new Thread(handler);
                clientThread.start();
            }
        } catch (Exception e) { // Caught broadly to satisfy manage and log all exceptions [cite: 92]
            logger.fatal("Server Engine encountered a fatal error: " + e.getMessage(), e);
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