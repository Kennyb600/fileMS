package vtdi.keniel.filems;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import vtdi.keniel.filems.dao.CourtCaseDAOTest;
import vtdi.keniel.filems.dao.HibernateCaseDAOTest;
import vtdi.keniel.filems.models.CourtCaseTest;

@Suite
@SelectClasses({
    CourtCaseTest.class,
    CourtCaseDAOTest.class,
    HibernateCaseDAOTest.class
})
public class MasterTestSuite {
    // This class remains empty. It acts purely as a holder for the Suite annotations.
}