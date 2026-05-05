package vtdi.keniel.filems.gui;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import vtdi.keniel.filems.dto.CourtCaseDTO;

public class UpdateCaseDialog extends JDialog {

    private CourtCaseDTO originalCase;
    private CourtCaseDTO updatedCase = null;
    private boolean approved = false;

    private JTextField txtOrderDate;
    private JTextArea txtCourtOrder;

    public UpdateCaseDialog(Window parent, CourtCaseDTO caseData) {
        super(parent, "Update Case Status: " + caseData.caseNumber(), Dialog.ModalityType.APPLICATION_MODAL);
        this.originalCase = caseData;
        initComponents();
        setSize(450, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- Read-Only Identity Details ---
        JPanel pnlIdentity = new JPanel(new GridLayout(4, 1, 5, 5));
        pnlIdentity.setBorder(BorderFactory.createTitledBorder("Case Details (Read-Only)"));
        pnlIdentity.add(new JLabel("Case Number: " + originalCase.caseNumber()));
        
        String judgeName = originalCase.judge() != null ? "Hon. " + originalCase.judge().lastName() : "Unassigned";
        pnlIdentity.add(new JLabel("Judge: " + judgeName));
        
        String appName = originalCase.applicant() != null ? originalCase.applicant().firstName() + " " + originalCase.applicant().lastName() : "N/A";
        pnlIdentity.add(new JLabel("Applicant: " + appName));
        
        String resName = originalCase.respondent() != null ? originalCase.respondent().firstName() + " " + originalCase.respondent().lastName() : "N/A";
        pnlIdentity.add(new JLabel("Respondent: " + resName));
        
        formPanel.add(pnlIdentity);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- Editable Update Fields ---
        JPanel pnlUpdate = new JPanel(new BorderLayout(5, 5));
        pnlUpdate.setBorder(BorderFactory.createTitledBorder("Update Status / Order"));

        JPanel pnlDate = new JPanel(new BorderLayout(5, 5));
        pnlDate.add(new JLabel("New Order Date (YYYY-MM-DD):"), BorderLayout.NORTH);
        txtOrderDate = new JTextField(LocalDate.now().toString()); // Default to today!
        pnlDate.add(txtOrderDate, BorderLayout.CENTER);
        pnlUpdate.add(pnlDate, BorderLayout.NORTH);

        JPanel pnlOrder = new JPanel(new BorderLayout(5, 5));
        pnlOrder.add(new JLabel("New Court Order / Status Notes:"), BorderLayout.NORTH);
        txtCourtOrder = new JTextArea(originalCase.courtOrder());
        txtCourtOrder.setLineWrap(true);
        txtCourtOrder.setWrapStyleWord(true);
        pnlOrder.add(new JScrollPane(txtCourtOrder), BorderLayout.CENTER);
        pnlUpdate.add(pnlOrder, BorderLayout.CENTER);

        formPanel.add(pnlUpdate);
        add(formPanel, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save Update");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> processUpdate());
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void processUpdate() {
        String newOrder = txtCourtOrder.getText().trim();
        String dateStr = txtOrderDate.getText().trim();

        if (newOrder.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Order Date and Status details are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate newDate = LocalDate.parse(dateStr);
            
            // Re-package the DTO: Keep the old entities, but use the new text and date!
            updatedCase = new CourtCaseDTO(
                originalCase.caseNumber(), 
                originalCase.applicant(), 
                originalCase.respondent(), 
                originalCase.child(), 
                originalCase.judge(), 
                newOrder, 
                newDate
            );
            
            approved = true;
            dispose();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date Format. Please use YYYY-MM-DD.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isApproved() { return approved; }
    public CourtCaseDTO getUpdatedCase() { return updatedCase; }
}