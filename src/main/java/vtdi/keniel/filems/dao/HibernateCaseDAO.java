package vtdi.keniel.filems.dao;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import vtdi.keniel.filems.models.CourtCase;
import vtdi.keniel.filems.utils.HibernateUtil;

public class HibernateCaseDAO implements ICourtCaseDAO {

    // Fulfills "Manage and Log All Exceptions" [cite: 31]
    private static final Logger logger = LogManager.getLogger(HibernateCaseDAO.class);

    // --------------------------------------------------------
    // CREATE (Insert) [cite: 29]
    // --------------------------------------------------------
    public boolean insertCase(CourtCase courtCase) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Hibernate takes the Java object and turns it into an SQL INSERT automatically
            session.persist(courtCase); 
            
            transaction.commit();
            logger.info("Hibernate successfully inserted new court case.");
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback(); // Undo changes if something goes wrong
            }
            logger.error("Hibernate failed to insert case: " + e.getMessage(), e);
            return false;
        }
    }

    // --------------------------------------------------------
    // READ (Select with condition) [cite: 29]
    // --------------------------------------------------------
    public CourtCase getCaseByNumber(String caseNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Using HQL (Hibernate Query Language) - Notice we query the Java Class 'CourtCase', not the DB table
            String hql = "FROM CourtCase c WHERE c.caseNumber = :caseNum";
            Query<CourtCase> query = session.createQuery(hql, CourtCase.class);
            query.setParameter("caseNum", caseNumber);
            
            CourtCase courtCase = query.uniqueResult();
            
            if (courtCase != null) {
                logger.info("Hibernate successfully retrieved case: " + caseNumber);
            } else {
                logger.warn("Hibernate found no case with number: " + caseNumber);
            }
            return courtCase;
        } catch (Exception e) {
            logger.error("Hibernate error retrieving case " + caseNumber + ": " + e.getMessage(), e);
            return null;
        }
    }

    // --------------------------------------------------------
    // READ ALL (Multiple select) [cite: 29]
    // --------------------------------------------------------
    public List<CourtCase> getAllCases() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Fulfills "Managing Result Sets" automatically by returning a List of mapped objects 
            List<CourtCase> cases = session.createQuery("FROM CourtCase", CourtCase.class).list();
            logger.info("Hibernate successfully retrieved " + cases.size() + " cases.");
            return cases;
        } catch (Exception e) {
            logger.error("Hibernate error retrieving all cases: " + e.getMessage(), e);
            return null;
        }
    }

    // --------------------------------------------------------
    // UPDATE [cite: 29]
    // --------------------------------------------------------
    public boolean updateCase(CourtCase courtCase) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Merge checks if the object exists and updates it
            session.merge(courtCase);
            
            transaction.commit();
            logger.info("Hibernate successfully updated court case: " + courtCase.getCaseNumber());
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Hibernate failed to update case " + courtCase.getCaseNumber() + ": " + e.getMessage(), e);
            return false;
        }
    }

    // --------------------------------------------------------
    // DELETE [cite: 29]
    // --------------------------------------------------------
    public boolean deleteCase(String caseNumber) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // First, find the case we want to delete
            CourtCase courtCase = getCaseByNumber(caseNumber);
            if (courtCase != null) {
                session.remove(courtCase); // Delete it
                transaction.commit();
                logger.info("Hibernate successfully deleted court case: " + caseNumber);
                return true;
            } else {
                logger.warn("Hibernate attempted to delete non-existent case: " + caseNumber);
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Hibernate error deleting court case " + caseNumber + ": " + e.getMessage(), e);
            return false;
        }
    }
}