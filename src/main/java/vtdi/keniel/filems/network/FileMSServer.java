package vtdi.keniel.filems.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import vtdi.keniel.filems.config.AppConfig;
import vtdi.keniel.filems.dao.ICourtCaseDAO;

/**
 * The main TCP/IP Server Engine.
 * Listens for incoming client connections and hands them off to an ExecutorService Thread Pool.
 */
public class FileMSServer {

    // Strict Rule: Log4j Logger [cite: 258]
    private static final Logger logger = LogManager.getLogger(FileMSServer.class);
    
    private static final int PORT = 8888;
    private static final int MAX_THREADS = 10; // Enterprise constraint: prevents server crashing from overload
    private boolean isRunning = true;
    
    private ExecutorService threadPool;

    public void startServer() {
        // Initialize the Thread Pool (The Module 3 Flex) [cite: 272]
        threadPool = Executors.newFixedThreadPool(MAX_THREADS);
        logger.info("Thread Pool initialized with " + MAX_THREADS + " maximum concurrent connections.");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("FileMS TCP/IP Server Engine started. Listening on port " + PORT);

            // 1. Initialize Spring IoC Container
            logger.info("Initializing Spring IoC Container...");
            ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
            
            // 2. Ask Spring for our dynamic DAO plug-in
            ICourtCaseDAO caseDAO = context.getBean(ICourtCaseDAO.class);

            while (isRunning) {
                logger.info("Waiting for a new client connection...");
                Socket clientSocket = serverSocket.accept(); 
                logger.info("New client connected from: " + clientSocket.getInetAddress().getHostAddress());

                // 3. Pass the injected DAO to the ClientHandler
                ClientHandler handler = new ClientHandler(clientSocket, caseDAO);
                
                // 4. Submit the handler to the Thread Pool instead of manually spawning rogue Threads
                threadPool.submit(handler);
            }
        } catch (Exception e) { 
            // Caught broadly to satisfy manage and log all exceptions [cite: 274]
            logger.fatal("Server Engine encountered a fatal error: " + e.getMessage(), e);
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        this.isRunning = false;
        logger.info("Server shutdown initiated.");
        
        // Gracefully shut down the thread pool so no data is corrupted
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException ex) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
                logger.error("Thread pool shutdown interrupted.", ex);
            }
        }
        logger.info("Thread pool successfully shutdown.");
    }
    
    // Quick test method to run the server standalone
    public static void main(String[] args) {
        FileMSServer server = new FileMSServer();
        server.startServer();
    }
}