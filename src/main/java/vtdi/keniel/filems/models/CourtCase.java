package vtdi.keniel.filems.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Entity
@Table(name = "CourtCases")
public class CourtCase implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(CourtCase.class);

    // caseId has been removed. caseNumber is now the Primary Key!
    @Id
    @Column(name = "case_number", nullable = false)
    private String caseNumber;
    
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "applicant_id")
    private InvolvedParty applicant;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "respondent_id")
    private InvolvedParty respondent;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "child_id")
    private InvolvedParty child;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "judge_id")
    private Judge judge;
    
    @Column(name = "court_order", columnDefinition = "TEXT")
    private String courtOrder;

    @Column(name = "order_date")
    private LocalDate orderDate;

    public CourtCase() {
        logger.info("CourtCase entity instantiated.");
    }

    // Getters and Setters (Notice caseId is gone)
    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public InvolvedParty getApplicant() { return applicant; }
    public void setApplicant(InvolvedParty applicant) { this.applicant = applicant; }

    public InvolvedParty getRespondent() { return respondent; }
    public void setRespondent(InvolvedParty respondent) { this.respondent = respondent; }

    public InvolvedParty getChild() { return child; }
    public void setChild(InvolvedParty child) { this.child = child; }

    public Judge getJudge() { return judge; }
    public void setJudge(Judge judge) { this.judge = judge; }

    public String getCourtOrder() { return courtOrder; }
    public void setCourtOrder(String courtOrder) { this.courtOrder = courtOrder; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
}