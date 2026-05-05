package vtdi.keniel.filems.dto;

import java.io.Serializable;
import java.time.LocalDate;

public record CourtCaseDTO(
    String caseNumber,
    InvolvedPartyDTO applicant,
    InvolvedPartyDTO respondent,
    InvolvedPartyDTO child,
    JudgeDTO judge,
    String courtOrder,
    LocalDate orderDate
) implements Serializable {}