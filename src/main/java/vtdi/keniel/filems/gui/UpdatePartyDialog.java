package vtdi.keniel.filems.gui;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import vtdi.keniel.filems.dto.InvolvedPartyDTO;

public class UpdatePartyDialog extends JDialog {

    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtDob;
    private boolean approved = false;
    private int partyId; // Store the original ID!

    public UpdatePartyDialog(Window parent, InvolvedPartyDTO existingParty) {
        super(parent, "Update Involved Party", Dialog.ModalityType.APPLICATION_MODAL);
        this.partyId = existingParty.id(); 
        initComponents(existingParty);
        setSize(350, 250);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents(InvolvedPartyDTO existingParty) {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("First Name:"));
        txtFirstName = new JTextField(existingParty.firstName());
        formPanel.add(txtFirstName);

        formPanel.add(new JLabel("Last Name:"));
        txtLastName = new JTextField(existingParty.lastName());
        formPanel.add(txtLastName);

        formPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):"));
        txtDob = new JTextField(existingParty.dateOfBirth() != null ? existingParty.dateOfBirth().toString() : "");
        formPanel.add(txtDob);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save Changes");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> {
            if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty() || txtDob.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                LocalDate.parse(txtDob.getText().trim());
                approved = true;
                dispose();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Date Format. Please use YYYY-MM-DD.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnSave);
    }

    public boolean isApproved() { return approved; }

    public InvolvedPartyDTO getUpdatedPartyDTO() {
        return new InvolvedPartyDTO(
            partyId, // Keep the original ID!
            txtFirstName.getText().trim(), 
            txtLastName.getText().trim(),
            LocalDate.parse(txtDob.getText().trim())
        );
    }
}