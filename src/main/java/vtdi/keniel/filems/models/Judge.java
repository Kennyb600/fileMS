package vtdi.keniel.filems.models;

import jakarta.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Entity
@Table(name = "Judges")
@AttributeOverride(name = "id", column = @Column(name = "judge_id"))
public class Judge extends Person {
    
    private static final long serialVersionUID = 1L;
    
    // Strict Rule: Log4j Logger for this specific class
    private static final Logger logger = LogManager.getLogger(Judge.class);
    
    public Judge() {
        super();
        logger.info("Judge entity instantiated.");
    }

    @Override
    public String getFullName() {
        return "Hon. " + firstName + " " + lastName;
    }
}