package vtdi.keniel.filems.models;

import jakarta.persistence.*;

@Entity
@Table(name = "Judges")
@AttributeOverride(name = "id", column = @Column(name = "judge_id"))
public class Judge extends Person {
    
    public Judge() {
        super();
    }

    @Override
    public String getFullName() {
        return "Hon. " + firstName + " " + lastName;
    }
}