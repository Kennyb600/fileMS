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

// This annotation forces the tests to run in sequential order (1 to 5)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourtCaseDAOTest {
    
    private static final Logger logger = LogManager.getLogger(CourtCaseDAOTest.class);
    private static CourtCaseDAO dao;
    private static CourtCase testCase;

    @BeforeAll
    public static void setUp() {
        logger.info("Setting up Traditional DAO Test Environment.");
        dao = new CourtCaseDAO();
        testCase = new CourtCase();
        
        testCase.setCaseNumber("TEST-DAO-001");
        testCase.setCourtOrder("Initial Unit Test Order");
        testCase.setOrderDate(LocalDate.now());

        // Linking to the dummy data we just inserted via MySQL Workbench
        InvolvedParty p1 = new InvolvedParty(); p1.setId(1);
        InvolvedParty p2 = new InvolvedParty(); p2.setId(2);
        InvolvedParty p3 = new InvolvedParty(); p3.setId(3);
        Judge j1 = new Judge(); j1.setId(1);

        testCase.setApplicant(p1);
        testCase.setRespondent(p2);
        testCase.setChild(p3);
        testCase.setJudge(j1);
    }

    @Test
    @Order(1)
    public void testInsertCase() {
        logger.info("Executing testInsertCase...");
        boolean result = dao.insertCase(testCase);
        assertTrue(result, "The case should be inserted successfully into the database.");
    }

    @Test
    @Order(2)
    public void testGetCaseByNumber() {
        logger.info("Executing testGetCaseByNumber...");
        CourtCase retrieved = dao.getCaseByNumber("TEST-DAO-001");
        assertNotNull(retrieved, "The case should be retrieved from the database.");
        assertEquals("Initial Unit Test Order", retrieved.getCourtOrder(), "Court order details should match.");
    }

    @Test
    @Order(3)
    public void testUpdateCase() {
        logger.info("Executing testUpdateCase...");
        testCase.setCourtOrder("Updated Order Details");
        boolean result = dao.updateCase(testCase);
        assertTrue(result, "The case should be updated successfully.");

        // Verify the update actually hit the database
        CourtCase updated = dao.getCaseByNumber("TEST-DAO-001");
        assertEquals("Updated Order Details", updated.getCourtOrder(), "Database should reflect the updated details.");
    }

    @Test
    @Order(4)
    public void testGetAllCases() {
        logger.info("Executing testGetAllCases...");
        List<CourtCase> allCases = dao.getAllCases();
        assertFalse(allCases.isEmpty(), "The retrieved case list should not be empty.");
    }

    @Test
    @Order(5)
    public void testDeleteCase() {
        logger.info("Executing testDeleteCase...");
        boolean result = dao.deleteCase("TEST-DAO-001");
        assertTrue(result, "The case should be deleted successfully.");

        // Verify it no longer exists
        CourtCase deleted = dao.getCaseByNumber("TEST-DAO-001");
        assertNull(deleted, "The case should no longer exist in the database.");
    }
}