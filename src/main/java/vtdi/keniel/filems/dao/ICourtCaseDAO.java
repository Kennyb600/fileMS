package vtdi.keniel.filems.dao;

import java.util.List;
import vtdi.keniel.filems.models.CourtCase;

public interface ICourtCaseDAO {
    boolean insertCase(CourtCase courtCase);
    CourtCase getCaseByNumber(String caseNumber);
    List<CourtCase> getAllCases();
    boolean updateCase(CourtCase courtCase);
    boolean deleteCase(String caseNumber);
}