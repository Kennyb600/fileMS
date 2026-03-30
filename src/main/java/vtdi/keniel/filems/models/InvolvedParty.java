package vtdi.keniel.filems.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Entity
@Table(name = "Persons")
@AttributeOverride(name = "id", column = @Column(name = "person_id"))
public class InvolvedParty extends Person {
    
    private static final long serialVersionUID = 1L;
    
    // FIXED: Changed Judge.class to InvolvedParty.class
    private static final Logger logger = LogManager.getLogger(InvolvedParty.class);
    
    @Column(name = "dob")
    private LocalDate dateOfBirth; 

    public InvolvedParty() {
        super();
        // FIXED: Changed log message
        logger.info("InvolvedParty entity instantiated."); 
    }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    @Override
    public String getFullName() {
        return firstName + " " + lastName;
    }
}