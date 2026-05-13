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

    @Override
    public boolean insertCase(CourtCase courtCase) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.merge(courtCase); 
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

    @Override
    public boolean updateParty(InvolvedParty party) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(party); 
            transaction.commit();
            return true;
        } catch (Exception e) {
            logger.error("Hibernate failed to update party: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean deleteParty(int partyId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            InvolvedParty party = session.get(InvolvedParty.class, partyId);
            if (party != null) {
                session.remove(party);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Hibernate failed to delete party: " + e.getMessage(), e);
            return false;
        }
    }
    
    @Override
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

    @Override
    public List<CourtCase> getAllCases() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM CourtCase", CourtCase.class).list();
        } catch (Exception e) {
            logger.error("Hibernate error retrieving all cases: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
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

    @Override
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
    public void saveCase(CourtCase courtCase) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.merge(courtCase);
            session.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Error saving case: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void saveParty(InvolvedParty party) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(party);
            session.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Error saving party: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void saveJudge(Judge judge) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(judge); 
            session.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Error saving judge: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean updateJudge(Judge judge) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(judge); 
            transaction.commit();
            return true;
        } catch (Exception e) {
            logger.error("Hibernate failed to update judge: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean deleteJudge(int judgeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Judge judge = session.get(Judge.class, judgeId);
            if (judge != null) {
                session.remove(judge);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Hibernate failed to delete judge: " + e.getMessage(), e);
            return false;
        }
    }
}