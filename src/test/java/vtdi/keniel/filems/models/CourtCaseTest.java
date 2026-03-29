package vtdi.keniel.filems.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.LocalDate;

public class CourtCaseTest {
    
    // Fulfills "Log All Events" and "Logging All Exceptions" for Unit Tests
    private static final Logger logger = LogManager.getLogger(CourtCaseTest.class);
    private CourtCase courtCase;

    @BeforeEach
    public void setUp() {
        logger.info("Initializing setup for CourtCase testing.");
        courtCase = new CourtCase();
        courtCase.setCaseNumber("MN-2026-001");
        courtCase.setCourtOrder("Routine Maintenance Check");
        courtCase.setOrderDate(LocalDate.now());
    }

    @Test
    public void testCourtCaseCreation() {
        logger.info("Executing testCourtCaseCreation...");
        
        try {
            assertNotNull(courtCase, "CourtCase object should have been instantiated.");
            assertEquals("MN-2026-001", courtCase.getCaseNumber(), "Case number should match the assigned value.");
            assertEquals("Routine Maintenance Check", courtCase.getCourtOrder(), "Court order details should match.");
            
            logger.info("testCourtCaseCreation passed successfully.");
        } catch (AssertionError | Exception e) {
            logger.fatal("testCourtCaseCreation failed with exception: " + e.getMessage(), e);
            fail("Test failed due to an unexpected error.");
        }
    }
}