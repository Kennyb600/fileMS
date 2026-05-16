package vtdi.keniel.filems.gui;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import vtdi.keniel.filems.dto.CourtCaseDTO;
import vtdi.keniel.filems.dto.InvolvedPartyDTO;
import vtdi.keniel.filems.dto.JudgeDTO;

public class UpdateCaseDialog extends JDialog {

    private static final Logger logger = LogManager.getLogger(UpdateCaseDialog.class);
    
    private CourtCaseDTO originalCase;
    private CourtCaseDTO updatedCase;
    private boolean approved = false;
    private String userRole;

    private JTextField txtCaseNumber;
    private JComboBox<JudgeItem> cmbJudge;
    private JComboBox<PartyItem> cmbApplicant;
    private JComboBox<PartyItem> cmbRespondent;
    private JTextArea txtCourtOrder;
    
    private List<JudgeDTO> availableJudges;
    private List<InvolvedPartyDTO> availableParties;

    public UpdateCaseDialog(Window parent, CourtCaseDTO courtCase, String userRole, List<JudgeDTO> judges, List<InvolvedPartyDTO> parties) {
        super(parent, "Update Case Status: " + courtCase.caseNumber(), ModalityType.APPLICATION_MODAL);
        this.originalCase = courtCase;
        this.userRole = userRole;
        this.availableJudges = judges;
        this.availableParties = parties;
        
        setSize(550, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        buildUI();
        enforceSecurityClearance();
    }

    private void buildUI() {
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 20));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        formPanel.add(new JLabel("Case Number:"));
        txtCaseNumber = new JTextField(originalCase.caseNumber());
        txtCaseNumber.setEditable(false); 
        formPanel.add(txtCaseNumber);

        // Judge Dropdown
        formPanel.add(new JLabel("Presiding Judge:"));
        JPanel pnlJudge = new JPanel(new BorderLayout(5, 0));
        cmbJudge = new JComboBox<>();
        cmbJudge.addItem(new JudgeItem(null));
        for (JudgeDTO j : availableJudges) {
            JudgeItem item = new JudgeItem(j);
            cmbJudge.addItem(item);
            if (originalCase.judge() != null && originalCase.judge().id() == j.id()) cmbJudge.setSelectedItem(item);
        }
        
        AutoCompleteDecorator.decorate(cmbJudge); 
        
        pnlJudge.add(cmbJudge, BorderLayout.CENTER);
        JButton btnAddJudge = new JButton("+");
        btnAddJudge.addActionListener(e -> quickAddJudge());
        pnlJudge.add(btnAddJudge, BorderLayout.EAST);
        formPanel.add(pnlJudge);

        // Applicant Dropdown
        formPanel.add(new JLabel("Applicant Name:"));
        JPanel pnlApp = new JPanel(new BorderLayout(5, 0));
        cmbApplicant = new JComboBox<>();
        cmbApplicant.addItem(new PartyItem(null));
        for (InvolvedPartyDTO p : availableParties) {
            PartyItem item = new PartyItem(p);
            cmbApplicant.addItem(item);
            if (originalCase.applicant() != null && originalCase.applicant().id() == p.id()) cmbApplicant.setSelectedItem(item);
        }
        
        AutoCompleteDecorator.decorate(cmbApplicant); 
        
        pnlApp.add(cmbApplicant, BorderLayout.CENTER);
        JButton btnAddApp = new JButton("+");
        btnAddApp.addActionListener(e -> quickAddParty(cmbApplicant));
        pnlApp.add(btnAddApp, BorderLayout.EAST);
        formPanel.add(pnlApp);

        // Respondent Dropdown
        formPanel.add(new JLabel("Respondent Name:"));
        JPanel pnlResp = new JPanel(new BorderLayout(5, 0));
        cmbRespondent = new JComboBox<>();
        cmbRespondent.addItem(new PartyItem(null));
        for (InvolvedPartyDTO p : availableParties) {
            PartyItem item = new PartyItem(p);
            cmbRespondent.addItem(item);
            if (originalCase.respondent() != null && originalCase.respondent().id() == p.id()) cmbRespondent.setSelectedItem(item);
        }
        
        AutoCompleteDecorator.decorate(cmbRespondent); 
        
        pnlResp.add(cmbRespondent, BorderLayout.CENTER);
        JButton btnAddResp = new JButton("+");
        btnAddResp.addActionListener(e -> quickAddParty(cmbRespondent));
        pnlResp.add(btnAddResp, BorderLayout.EAST);
        formPanel.add(pnlResp);
        
        add(formPanel, BorderLayout.NORTH);

        // Court Order Area
        JPanel orderPanel = new JPanel(new BorderLayout(0, 5));
        orderPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));
        orderPanel.add(new JLabel("Court Order / Status Summary:"), BorderLayout.NORTH);
        
        txtCourtOrder = new JTextArea(originalCase.courtOrder());
        txtCourtOrder.setLineWrap(true);
        txtCourtOrder.setWrapStyleWord(true);
        orderPanel.add(new JScrollPane(txtCourtOrder), BorderLayout.CENTER);
        
        add(orderPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRequestEdit = new JButton("Request Admin Edit");
        btnRequestEdit.setForeground(new Color(192, 57, 43)); 
        btnRequestEdit.addActionListener(e -> submitChangeRequest());
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        
        JButton btnSave = new JButton("Save Changes");
        btnSave.addActionListener(e -> saveChanges());

        if (!"ADMIN".equals(userRole)) buttonPanel.add(btnRequestEdit);
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void enforceSecurityClearance() {
        if (!"ADMIN".equals(userRole)) {
            cmbJudge.setEnabled(false);
            cmbApplicant.setEnabled(false);
            cmbRespondent.setEnabled(false);
            
            for (Component comp : ((JPanel)getContentPane().getComponent(0)).getComponents()) {
                if (comp instanceof JPanel) {
                    for (Component inner : ((JPanel)comp).getComponents()) {
                        if (inner instanceof JButton) inner.setEnabled(false);
                    }
                }
            }
        }
    }
    
    private void quickAddJudge() {
        String lastName = JOptionPane.showInputDialog(this, "Enter New Judge's Last Name:");
        if (lastName != null && !lastName.trim().isEmpty()) {
            JudgeDTO newJudge = new JudgeDTO(999, "New", lastName); 
            JudgeItem newItem = new JudgeItem(newJudge);
            cmbJudge.addItem(newItem);
            cmbJudge.setSelectedItem(newItem);
        }
    }

    private void quickAddParty(JComboBox<PartyItem> targetDropdown) {
        String fullName = JOptionPane.showInputDialog(this, "Enter Citizen's Full Name:");
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.split(" ", 2);
            // FIXED: Using LocalDate for the required 4th parameter
            InvolvedPartyDTO newParty = new InvolvedPartyDTO(999, parts[0], parts.length > 1 ? parts[1] : "", LocalDate.now());
            PartyItem newItem = new PartyItem(newParty);
            targetDropdown.addItem(newItem);
            targetDropdown.setSelectedItem(newItem);
        }
    }
    
    private void submitChangeRequest() {
        String reason = JOptionPane.showInputDialog(this, "Reason for Admin Modification:", "Request Modification", JOptionPane.QUESTION_MESSAGE);
        if (reason != null && !reason.trim().isEmpty()) {
            logger.warn("CHANGE REQUEST - Case {}. Reason: {}", originalCase.caseNumber(), reason);
            JOptionPane.showMessageDialog(this, "Request securely logged.");
            dispose();
        }
    }

    private void saveChanges() {
        JudgeDTO selectedJudge = ((JudgeItem) cmbJudge.getSelectedItem()).getJudge();
        InvolvedPartyDTO selectedApplicant = ((PartyItem) cmbApplicant.getSelectedItem()).getParty();
        InvolvedPartyDTO selectedRespondent = ((PartyItem) cmbRespondent.getSelectedItem()).getParty();

        updatedCase = new CourtCaseDTO(
            originalCase.caseNumber(),
            selectedApplicant, 
            selectedRespondent,
            originalCase.child(),
            selectedJudge,
            txtCourtOrder.getText(),
            originalCase.orderDate()
        );
        
        approved = true;
        dispose();
    }

    public boolean isApproved() { return approved; }
    public CourtCaseDTO getUpdatedCase() { return updatedCase; }

    private class JudgeItem {
        private JudgeDTO judge;
        public JudgeItem(JudgeDTO judge) { this.judge = judge; }
        public JudgeDTO getJudge() { return judge; }
        @Override public String toString() { return judge == null ? "-- Unassigned --" : "Hon. " + judge.lastName(); } 
    }

    private class PartyItem {
        private InvolvedPartyDTO party;
        public PartyItem(InvolvedPartyDTO party) { this.party = party; }
        public InvolvedPartyDTO getParty() { return party; }
        @Override public String toString() { return party == null ? "-- Unassigned --" : party.firstName() + " " + party.lastName(); } 
    }
}