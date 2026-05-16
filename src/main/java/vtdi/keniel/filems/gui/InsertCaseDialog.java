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

public class InsertCaseDialog extends JDialog {

    private static final Logger logger = LogManager.getLogger(InsertCaseDialog.class);
    
    private CourtCaseDTO createdCase;
    private boolean approved = false;

    private JTextField txtCaseNumber;
    private JComboBox<JudgeItem> cmbJudge;
    private JComboBox<PartyItem> cmbApplicant;
    private JComboBox<PartyItem> cmbRespondent;
    private JTextArea txtCourtOrder;
    
    private List<JudgeDTO> availableJudges;
    private List<InvolvedPartyDTO> availableParties;

    public InsertCaseDialog(Window parent, List<JudgeDTO> judges, List<InvolvedPartyDTO> parties) {
        super(parent, "File New Court Case", ModalityType.APPLICATION_MODAL);
        this.availableJudges = judges;
        this.availableParties = parties;
        
        setSize(550, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 20));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        // 1. Case Number Generation
        formPanel.add(new JLabel("Generated Case Number:"));
        txtCaseNumber = new JTextField("ST-" + System.currentTimeMillis()); 
        txtCaseNumber.setEditable(false); 
        formPanel.add(txtCaseNumber);

        // 2. Judge Dropdown
        formPanel.add(new JLabel("Presiding Judge:"));
        JPanel pnlJudge = new JPanel(new BorderLayout(5, 0));
        cmbJudge = new JComboBox<>();
        cmbJudge.addItem(new JudgeItem(null)); 
        for (JudgeDTO j : availableJudges) cmbJudge.addItem(new JudgeItem(j));
        
        AutoCompleteDecorator.decorate(cmbJudge); 
        
        pnlJudge.add(cmbJudge, BorderLayout.CENTER);
        JButton btnAddJudge = new JButton("+");
        btnAddJudge.addActionListener(e -> quickAddJudge());
        pnlJudge.add(btnAddJudge, BorderLayout.EAST);
        formPanel.add(pnlJudge);

        // 3. Applicant Dropdown
        formPanel.add(new JLabel("Applicant Name:"));
        JPanel pnlApp = new JPanel(new BorderLayout(5, 0));
        cmbApplicant = new JComboBox<>();
        cmbApplicant.addItem(new PartyItem(null)); 
        for (InvolvedPartyDTO p : availableParties) cmbApplicant.addItem(new PartyItem(p));
        
        AutoCompleteDecorator.decorate(cmbApplicant); 
        
        pnlApp.add(cmbApplicant, BorderLayout.CENTER);
        JButton btnAddApp = new JButton("+");
        btnAddApp.addActionListener(e -> quickAddParty(cmbApplicant));
        pnlApp.add(btnAddApp, BorderLayout.EAST);
        formPanel.add(pnlApp);

        // 4. Respondent Dropdown
        formPanel.add(new JLabel("Respondent Name:"));
        JPanel pnlResp = new JPanel(new BorderLayout(5, 0));
        cmbRespondent = new JComboBox<>();
        cmbRespondent.addItem(new PartyItem(null)); 
        for (InvolvedPartyDTO p : availableParties) cmbRespondent.addItem(new PartyItem(p));
        
        AutoCompleteDecorator.decorate(cmbRespondent); 
        
        pnlResp.add(cmbRespondent, BorderLayout.CENTER);
        JButton btnAddResp = new JButton("+");
        btnAddResp.addActionListener(e -> quickAddParty(cmbRespondent));
        pnlResp.add(btnAddResp, BorderLayout.EAST);
        formPanel.add(pnlResp);
        
        add(formPanel, BorderLayout.NORTH);

        // 5. Initial Status Area
        JPanel orderPanel = new JPanel(new BorderLayout(0, 5));
        orderPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));
        orderPanel.add(new JLabel("Initial Court Order / Status Summary:"), BorderLayout.NORTH);
        
        txtCourtOrder = new JTextArea("Case Filed. Awaiting Initial Hearing.");
        txtCourtOrder.setLineWrap(true);
        txtCourtOrder.setWrapStyleWord(true);
        orderPanel.add(new JScrollPane(txtCourtOrder), BorderLayout.CENTER);
        
        add(orderPanel, BorderLayout.CENTER);

        // 6. Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        
        JButton btnSave = new JButton("File Case");
        btnSave.addActionListener(e -> saveChanges());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // --- QUICK ADD WORKFLOWS ---
    private void quickAddJudge() {
        String lastName = JOptionPane.showInputDialog(this, "Enter New Judge's Last Name (e.g., Smith):");
        if (lastName != null && !lastName.trim().isEmpty()) {
            JudgeDTO newJudge = new JudgeDTO(999, "New", lastName); 
            JudgeItem newItem = new JudgeItem(newJudge);
            cmbJudge.addItem(newItem);
            cmbJudge.setSelectedItem(newItem);
            JOptionPane.showMessageDialog(this, "Judge Added Successfully!");
        }
    }

    private void quickAddParty(JComboBox<PartyItem> targetDropdown) {
        String fullName = JOptionPane.showInputDialog(this, "Enter Citizen's Full Name:");
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.split(" ", 2);
            String first = parts[0];
            String last = parts.length > 1 ? parts[1] : "";
            
            // FIXED: Using LocalDate for the required 4th parameter
            InvolvedPartyDTO newParty = new InvolvedPartyDTO(999, first, last, LocalDate.now());
            PartyItem newItem = new PartyItem(newParty);
            targetDropdown.addItem(newItem);
            targetDropdown.setSelectedItem(newItem);
            JOptionPane.showMessageDialog(this, "Citizen Added Successfully!");
        }
    }

    private void saveChanges() {
        JudgeDTO selectedJudge = ((JudgeItem) cmbJudge.getSelectedItem()).getJudge();
        InvolvedPartyDTO selectedApplicant = ((PartyItem) cmbApplicant.getSelectedItem()).getParty();
        InvolvedPartyDTO selectedRespondent = ((PartyItem) cmbRespondent.getSelectedItem()).getParty();

        createdCase = new CourtCaseDTO(
            txtCaseNumber.getText(),
            selectedApplicant, 
            selectedRespondent,
            null, 
            selectedJudge,
            txtCourtOrder.getText(),
            LocalDate.now() // FIXED: Using modern LocalDate instead of java.util.Date
        );
        
        approved = true;
        dispose();
    }

    public boolean isApproved() { return approved; }
    public CourtCaseDTO getCreatedCase() { return createdCase; }

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