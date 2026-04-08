package vtdi.keniel.filems.dao;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vtdi.keniel.filems.models.CourtCase;
import vtdi.keniel.filems.models.InvolvedParty;
import vtdi.keniel.filems.models.Judge;
import java.time.LocalDate;
import java.util.List;
import vtdi.keniel.filems.dao.HibernateCaseDAO;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HibernateCaseDAOTest {
    
    private static final Logger logger = LogManager.getLogger(HibernateCaseDAOTest.class);
    private static HibernateCaseDAO dao;
    private static CourtCase testCase;

    @BeforeAll
    public static void setUp() {
        logger.info("Setting up Hibernate ORM Test Environment.");
        dao = new HibernateCaseDAO(); 
        testCase = new CourtCase();
        
        testCase.setCaseNumber("TEST-HIB-001");
        testCase.setCourtOrder("Hibernate Unit Test Order");
        testCase.setOrderDate(LocalDate.now());

        // REMOVED .setId() CALLS! Let Hibernate handle the IDs dynamically.
        InvolvedParty p1 = new InvolvedParty(); p1.setName("Test App");
        InvolvedParty p2 = new InvolvedParty(); p2.setName("Test Resp");
        InvolvedParty p3 = new InvolvedParty(); p3.setName("Test Child");
        Judge j1 = new Judge(); j1.setName("Test Judge");

        testCase.setApplicant(p1);
        testCase.setRespondent(p2);
        testCase.setChild(p3);
        testCase.setJudge(j1);
    }

    @Test
    @Order(1)
    public void testInsertCase() {
        logger.info("Executing Hibernate testInsertCase...");
        boolean result = dao.insertCase(testCase);
        assertTrue(result, "The case should be inserted successfully via Hibernate.");
    }

    @Test
    @Order(2)
    public void testGetCaseByNumber() {
        logger.info("Executing Hibernate testGetCaseByNumber...");
        CourtCase retrieved = dao.getCaseByNumber("TEST-HIB-001");
        assertNotNull(retrieved, "The case should be retrieved via Hibernate.");
        assertEquals("Hibernate Unit Test Order", retrieved.getCourtOrder(), "Court order details should match.");
    }

    @Test
    @Order(3)
    public void testUpdateCase() {
        logger.info("Executing Hibernate testUpdateCase...");
        testCase.setCourtOrder("Updated Hibernate Order Details");
        boolean result = dao.updateCase(testCase);
        assertTrue(result, "The case should be updated successfully via Hibernate.");
    }

    @Test
    @Order(4)
    public void testGetAllCases() {
        logger.info("Executing Hibernate testGetAllCases...");
        List<CourtCase> allCases = dao.getAllCases();
        assertFalse(allCases.isEmpty(), "The retrieved case list should not be empty.");
    }

    @Test
    @Order(5)
    public void testDeleteCase() {
        logger.info("Executing Hibernate testDeleteCase...");
        boolean result = dao.deleteCase("TEST-HIB-001");
        assertTrue(result, "The case should be deleted successfully via Hibernate.");
    }
}