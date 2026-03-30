package vtdi.keniel.filems.models;

import jakarta.persistence.*;
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MappedSuperclass
public abstract class Person implements Serializable {
    
    // 1. Serialization requirement for Network transmission
    private static final long serialVersionUID = 1L;
    
    // 2. Strict Rule: Log4j Logger for event logging
    private static final Logger logger = LogManager.getLogger(Person.class);

    // We will override this column name in the subclasses
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;

    @Column(name = "first_name", nullable = false)
    protected String firstName;

    @Column(name = "last_name", nullable = false)
    protected String lastName;

    public Person() {
        logger.info("Instantiated a new empty Person object.");
    }

    public Person(int id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        logger.info("Instantiated a new Person object: " + firstName + " " + lastName);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public abstract String getFullName(); 
}