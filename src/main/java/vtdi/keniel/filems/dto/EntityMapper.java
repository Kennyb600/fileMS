package vtdi.keniel.filems.dto;

import java.util.List;
import java.util.stream.Collectors;

import vtdi.keniel.filems.models.CourtCase;
import vtdi.keniel.filems.models.InvolvedParty;
import vtdi.keniel.filems.models.Judge;

/**
 * Utility class to map heavy Hibernate Entities to lightweight DTOs.
 */
public class EntityMapper {

    // --- Single Object Mappers ---

    public static JudgeDTO toJudgeDTO(Judge judge) {
        if (judge == null) return null;
        return new JudgeDTO(judge.getId(), judge.getFirstName(), judge.getLastName());
    }

    public static InvolvedPartyDTO toInvolvedPartyDTO(InvolvedParty party) {
        if (party == null) return null;
        return new InvolvedPartyDTO(party.getId(), party.getFirstName(), party.getLastName(), party.getDateOfBirth());
    }
    
    // Converts a clean DTO back into a heavy Database Entity for saving
    public static InvolvedParty toInvolvedPartyEntity(InvolvedPartyDTO dto) {
        if (dto == null) return null;
        InvolvedParty party = new InvolvedParty();
        party.setId(dto.id());
        party.setFirstName(dto.firstName());
        party.setLastName(dto.lastName());
        party.setDateOfBirth(dto.dateOfBirth());
        return party;
    }

    public static CourtCaseDTO toCourtCaseDTO(CourtCase courtCase) {
        if (courtCase == null) return null;
        return new CourtCaseDTO(
            courtCase.getCaseNumber(),
            toInvolvedPartyDTO(courtCase.getApplicant()),
            toInvolvedPartyDTO(courtCase.getRespondent()),
            toInvolvedPartyDTO(courtCase.getChild()),
            toJudgeDTO(courtCase.getJudge()),
            courtCase.getCourtOrder(),
            courtCase.getOrderDate()
        );
    }

    // --- List Mappers (Uses Java 8+ Streams for efficiency) ---

    public static List<JudgeDTO> toJudgeDTOList(List<Judge> judges) {
        if (judges == null) return null;
        return judges.stream()
            .map(EntityMapper::toJudgeDTO)
            .collect(Collectors.toList());
    }

    public static List<InvolvedPartyDTO> toInvolvedPartyDTOList(List<InvolvedParty> parties) {
        if (parties == null) return null;
        return parties.stream()
            .map(EntityMapper::toInvolvedPartyDTO)
            .collect(Collectors.toList());
    }

    public static List<CourtCaseDTO> toCourtCaseDTOList(List<CourtCase> cases) {
        if (cases == null) return null;
        return cases.stream()
            .map(EntityMapper::toCourtCaseDTO)
            .collect(Collectors.toList());
    }

    // Converts a clean DTO back into a heavy Database Entity for saving
    public static Judge toJudgeEntity(JudgeDTO dto) {
        if (dto == null) return null;
        Judge judge = new Judge();
        judge.setId(dto.id());
        judge.setFirstName(dto.firstName());
        judge.setLastName(dto.lastName());
        return judge;
    }
    
    // Converts a clean DTO back into a heavy Database Entity for saving
    public static CourtCase toCourtCaseEntity(CourtCaseDTO dto) {
        if (dto == null) return null;
        CourtCase caseEntity = new CourtCase();
        
        // Map standard fields
        caseEntity.setCaseNumber(dto.caseNumber());
        caseEntity.setCourtOrder(dto.courtOrder());
        caseEntity.setOrderDate(dto.orderDate());

        // Map the Nested Judge Entity
        if (dto.judge() != null) {
            Judge judge = new Judge();
            judge.setId(dto.judge().id());
            judge.setFirstName(dto.judge().firstName());
            judge.setLastName(dto.judge().lastName());
            caseEntity.setJudge(judge);
        }

        // Map the Nested Applicant
        if (dto.applicant() != null) {
            InvolvedParty applicant = new InvolvedParty();
            applicant.setId(dto.applicant().id());
            applicant.setFirstName(dto.applicant().firstName());
            applicant.setLastName(dto.applicant().lastName());
            applicant.setDateOfBirth(dto.applicant().dateOfBirth());
            caseEntity.setApplicant(applicant);
        }

        // Map the Nested Respondent
        if (dto.respondent() != null) {
            InvolvedParty respondent = new InvolvedParty();
            respondent.setId(dto.respondent().id());
            respondent.setFirstName(dto.respondent().firstName());
            respondent.setLastName(dto.respondent().lastName());
            respondent.setDateOfBirth(dto.respondent().dateOfBirth());
            caseEntity.setRespondent(respondent);
        }

        // Map the Nested Child
        if (dto.child() != null) {
            InvolvedParty child = new InvolvedParty();
            child.setId(dto.child().id());
            child.setFirstName(dto.child().firstName());
            child.setLastName(dto.child().lastName());
            child.setDateOfBirth(dto.child().dateOfBirth());
            caseEntity.setChild(child);
        }

        return caseEntity;
    }
}