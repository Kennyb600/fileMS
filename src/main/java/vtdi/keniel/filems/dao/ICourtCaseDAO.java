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
    boolean updateParty(InvolvedParty party);
    boolean deleteParty(int partyId);
    boolean updateJudge(Judge judge);
    boolean deleteJudge(int judgeId);
    
    void saveJudge(Judge judge);
    void saveCase(CourtCase courtCase);
    void saveParty(InvolvedParty party); 
}   