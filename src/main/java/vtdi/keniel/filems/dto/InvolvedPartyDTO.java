package vtdi.keniel.filems.dto;

import java.io.Serializable;
import java.time.LocalDate;

public record InvolvedPartyDTO(
    int id, 
    String firstName, 
    String lastName, 
    LocalDate dateOfBirth
) implements Serializable {}
