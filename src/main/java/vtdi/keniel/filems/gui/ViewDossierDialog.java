package vtdi.keniel.filems.gui;

import java.awt.*;
import javax.swing.*;
import vtdi.keniel.filems.dto.CourtCaseDTO;
import vtdi.keniel.filems.dto.InvolvedPartyDTO;

public class ViewDossierDialog extends JDialog {

    public ViewDossierDialog(Window parent, CourtCaseDTO caseData) {
        super(parent, "Full Dossier: Case " + caseData.caseNumber(), Dialog.ModalityType.APPLICATION_MODAL);
        initComponents(caseData);
        setSize(400, 550);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents(CourtCaseDTO caseData) {
        setLayout(new BorderLayout());

        // A vertical box layout to stack our sections neatly
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. General Case Details
        JPanel pnlGeneral = createSectionPanel("Case Details");
        pnlGeneral.add(new JLabel("<html><b>Case Number:</b> " + caseData.caseNumber() + "</html>"));
        pnlGeneral.add(new JLabel("<html><b>Order Date:</b> " + caseData.orderDate() + "</html>"));
        pnlGeneral.add(new JLabel("<html><b>Current Order:</b> " + caseData.courtOrder() + "</html>"));
        mainPanel.add(pnlGeneral);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing

        // 2. Presiding Judge
        JPanel pnlJudge = createSectionPanel("Presiding Judge");
        if (caseData.judge() != null) {
            pnlJudge.add(new JLabel("Hon. " + caseData.judge().firstName() + " " + caseData.judge().lastName()));
        } else {
            pnlJudge.add(new JLabel("--- No Judge Assigned ---"));
        }
        mainPanel.add(pnlJudge);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 3. Applicant
        JPanel pnlApplicant = createSectionPanel("Applicant");
        pnlApplicant.add(createPartyLabel(caseData.applicant()));
        mainPanel.add(pnlApplicant);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 4. Respondent
        JPanel pnlRespondent = createSectionPanel("Respondent");
        pnlRespondent.add(createPartyLabel(caseData.respondent()));
        mainPanel.add(pnlRespondent);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 5. Subject Child
        JPanel pnlChild = createSectionPanel("Subject Child");
        pnlChild.add(createPartyLabel(caseData.child()));
        mainPanel.add(pnlChild);

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        // Close Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Close Dossier");
        btnClose.addActionListener(e -> dispose());
        buttonPanel.add(btnClose);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Let the user hit Enter/Escape to quickly close the dossier
        getRootPane().setDefaultButton(btnClose);
    }

    // Helper method to draw clean bordered sections
    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel;
    }

    // Helper method to format party data (Name, ID, and DOB)
    private JLabel createPartyLabel(InvolvedPartyDTO party) {
        if (party == null) {
            return new JLabel("--- None / Unassigned ---");
        }
        return new JLabel("<html>" + party.firstName() + " " + party.lastName() + 
                          " <br><i>ID: " + party.id() + " | DOB: " + party.dateOfBirth() + "</i></html>");
    }
}