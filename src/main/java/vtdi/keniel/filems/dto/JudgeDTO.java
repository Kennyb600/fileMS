package vtdi.keniel.filems.dto;

import java.io.Serializable;

public record JudgeDTO(
    int id, 
    String firstName, 
    String lastName
) implements Serializable {}