package vtdi.keniel.filems.dao;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import vtdi.keniel.filems.models.CourtCase;
import vtdi.keniel.filems.models.Judge;
import vtdi.keniel.filems.models.InvolvedParty;
import vtdi.keniel.filems.utils.HibernateUtil;

public class HibernateCaseDAO implements ICourtCaseDAO {

    private static final Logger logger = LogManager.getLogger(HibernateCaseDAO.class);

    public boolean insertCase(CourtCase courtCase) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.persist(courtCase); 
                transaction.commit();
                logger.info("Hibernate successfully inserted new court case.");
                return true;
            } catch (Exception e) {
                transaction.rollback();
                logger.error("Hibernate failed to insert case: " + e.getMessage(), e);
                return false;
            }
        }
    }

    public CourtCase getCaseByNumber(String caseNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM CourtCase c WHERE c.caseNumber = :caseNum";
            Query<CourtCase> query = session.createQuery(hql, CourtCase.class);
            query.setParameter("caseNum", caseNumber);
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Hibernate error retrieving case " + caseNumber + ": " + e.getMessage(), e);
            return null;
        }
    }

    public List<CourtCase> getAllCases() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM CourtCase", CourtCase.class).list();
        } catch (Exception e) {
            logger.error("Hibernate error retrieving all cases: " + e.getMessage(), e);
            return null;
        }
    }

    public boolean updateCase(CourtCase courtCase) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.merge(courtCase);
                transaction.commit();
                logger.info("Hibernate successfully updated court case.");
                return true;
            } catch (Exception e) {
                transaction.rollback();
                logger.error("Hibernate failed to update case: " + e.getMessage(), e);
                return false;
            }
        }
    }

    public boolean deleteCase(String caseNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                CourtCase courtCase = session.createQuery("FROM CourtCase c WHERE c.caseNumber = :caseNum", CourtCase.class)
                                             .setParameter("caseNum", caseNumber)
                                             .uniqueResult();
                if (courtCase != null) {
                    session.remove(courtCase); 
                    transaction.commit();
                    logger.info("Hibernate successfully deleted court case.");
                    return true;
                }
                return false;
            } catch (Exception e) {
                transaction.rollback();
                logger.error("Hibernate error deleting court case: " + e.getMessage(), e);
                return false;
            }
        }
    }
    
    @Override
    public List<Judge> getAllJudges() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Judge", Judge.class).list();
        } catch (Exception e) {
            logger.error("Error fetching judges: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<InvolvedParty> getAllParties() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM InvolvedParty", InvolvedParty.class).list();
        } catch (Exception e) {
            logger.error("Error fetching parties: " + e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public void saveCase(vtdi.keniel.filems.models.CourtCase courtCase) {
        try (org.hibernate.Session session = vtdi.keniel.filems.utils.HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(courtCase); // Persist is the optimal Hibernate command for new records
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void saveParty(vtdi.keniel.filems.models.InvolvedParty party) {
        try (org.hibernate.Session session = vtdi.keniel.filems.utils.HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(party);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void saveJudge(Judge judge) {
        // Updated to use your project's specific HibernateUtil class
        try (org.hibernate.Session session = vtdi.keniel.filems.utils.HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(judge); 
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}