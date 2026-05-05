package vtdi.keniel.filems.gui;

import java.awt.*;
import javax.swing.*;
import vtdi.keniel.filems.dto.JudgeDTO;

/**
 * A modal dialog for capturing new Judge data.
 */
public class InsertJudgeDialog extends JDialog {

    private JTextField txtFirstName;
    private JTextField txtLastName;
    private boolean approved = false; // Tracks if the user clicked "Save" or "Cancel"

    public InsertJudgeDialog(Window parent) {
        super(parent, "Register New Judge", Dialog.ModalityType.APPLICATION_MODAL);
        initComponents();
        setSize(350, 200);
        setLocationRelativeTo(parent); // Centers the modal on the screen
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("First Name:"));
        txtFirstName = new JTextField();
        formPanel.add(txtFirstName);

        formPanel.add(new JLabel("Last Name:"));
        txtLastName = new JTextField();
        formPanel.add(txtLastName);

        add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save Judge");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> {
            if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            approved = true;
            dispose(); // Close dialog
        });

        btnCancel.addActionListener(e -> dispose()); // Close without saving

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public boolean isApproved() {
        return approved;
    }

    /**
     * Packages the form data into our clean DTO.
     * Note: ID is 0 because the database will generate it.
     */
    public JudgeDTO getJudgeDTO() {
        return new JudgeDTO(0, txtFirstName.getText().trim(), txtLastName.getText().trim());
    }
}