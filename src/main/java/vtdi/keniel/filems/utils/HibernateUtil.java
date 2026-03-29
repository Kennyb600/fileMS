package vtdi.keniel.filems.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HibernateUtil {
    
    // Manage and Log All Exceptions requirement
    private static final Logger logger = LogManager.getLogger(HibernateUtil.class);
    
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            logger.info("Attempting to build Hibernate SessionFactory.");
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Log the exception if Hibernate fails to start
            logger.fatal("Initial SessionFactory creation failed: " + ex.getMessage(), ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        // Close caches and connection pools when closing the app
        if (sessionFactory != null) {
            sessionFactory.close();
            logger.info("Hibernate SessionFactory closed.");
        }
    }
}
