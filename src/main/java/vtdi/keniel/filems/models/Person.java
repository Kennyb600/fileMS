package vtdi.keniel.filems.models;

import jakarta.persistence.*;
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MappedSuperclass
public abstract class Person implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(Person.class);

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

    // --- THE SMART SETTER ---
    // Takes the single text box string from the GUI and splits it for the database
    public void setName(String fullName) {
        if (fullName != null && fullName.contains(" ")) {
            String[] parts = fullName.split(" ", 2); // Splits into 2 pieces at the first space
            this.firstName = parts[0];
            this.lastName = parts[1];
        } else {
            // If they only typed one word (e.g., just "John")
            this.firstName = fullName;
            this.lastName = ""; 
        }
    }

    // The GUI or other classes might still want to ask for "getName()"
    public String getName() {
        return this.firstName + " " + this.lastName;
    }
    // ------------------------
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public abstract String getFullName(); 
}