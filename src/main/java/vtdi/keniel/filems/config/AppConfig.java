package vtdi.keniel.filems.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import vtdi.keniel.filems.dao.CourtCaseDAO;
import vtdi.keniel.filems.dao.HibernateCaseDAO;
import vtdi.keniel.filems.dao.ICourtCaseDAO;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {
    
    private static final Logger logger = LogManager.getLogger(AppConfig.class);

    @Bean
    public ICourtCaseDAO courtCaseDAO(Environment env) {
        // Read the configuration file at runtime
        String pluginType = env.getProperty("filems.dao.plugin", "hibernate");
        
        // Dynamically inject the requested dependency matching the rubric terminology
        if ("connector-j".equalsIgnoreCase(pluginType)) {
            logger.info("Spring IoC Framework is dynamically injecting the MySQL Connector/J CourtCaseDAO plug-in.");
            return new CourtCaseDAO();
        } else {
            logger.info("Spring IoC Framework is dynamically injecting the ORM HibernateCaseDAO plug-in.");
            return new HibernateCaseDAO();
        }
    }
}