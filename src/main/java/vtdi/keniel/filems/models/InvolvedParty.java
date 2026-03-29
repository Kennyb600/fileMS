package vtdi.keniel.filems.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Persons")
@AttributeOverride(name = "id", column = @Column(name = "person_id"))
public class InvolvedParty extends Person {
    
    @Column(name = "dob")
    private LocalDate dateOfBirth; 

    public InvolvedParty() {
        super();
    }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    @Override
    public String getFullName() {
        return firstName + " " + lastName;
    }
}