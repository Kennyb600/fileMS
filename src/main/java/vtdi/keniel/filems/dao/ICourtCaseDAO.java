package vtdi.keniel.filems.dao;

import java.util.List;
import vtdi.keniel.filems.models.CourtCase;
import vtdi.keniel.filems.models.Judge;
import vtdi.keniel.filems.models.InvolvedParty;

public interface ICourtCaseDAO {
    boolean insertCase(CourtCase courtCase);
    CourtCase getCaseByNumber(String caseNumber);
    List<CourtCase> getAllCases();
    List<Judge> getAllJudges();
    List<InvolvedParty> getAllParties();
    boolean updateCase(CourtCase courtCase);
    boolean deleteCase(String caseNumber);
}