package vtdi.keniel.filems.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vtdi.keniel.filems.dao.HibernateCaseDAO;
import vtdi.keniel.filems.dao.ICourtCaseDAO;

@Configuration
public class AppConfig {
    
    // Strict Rule: Log all events [cite: 91]
    private static final Logger logger = LogManager.getLogger(AppConfig.class);

    @Bean
    public ICourtCaseDAO courtCaseDAO() {
        // This is where we load the plug-in at runtime! 
        // If you ever want to switch to traditional JDBC, just change this to "return new CourtCaseDAO();"
        logger.info("Spring IoC Framework is injecting the HibernateCaseDAO plug-in.");
        return new HibernateCaseDAO();
    }
}
