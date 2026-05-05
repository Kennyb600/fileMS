package vtdi.keniel.filems.gui;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import vtdi.keniel.filems.dto.InvolvedPartyDTO;

public class InsertPartyDialog extends JDialog {

    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtDob;
    private boolean approved = false;

    public InsertPartyDialog(Window parent) {
        super(parent, "Register New Involved Party", Dialog.ModalityType.APPLICATION_MODAL);
        initComponents();
        setSize(350, 250);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("First Name:"));
        txtFirstName = new JTextField();
        formPanel.add(txtFirstName);

        formPanel.add(new JLabel("Last Name:"));
        txtLastName = new JTextField();
        formPanel.add(txtLastName);

        formPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):"));
        txtDob = new JTextField();
        formPanel.add(txtDob);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save Party");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> {
            if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty() || txtDob.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                // Validate the date format before approving
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
    }

    public boolean isApproved() { return approved; }

    public InvolvedPartyDTO getInvolvedPartyDTO() {
        return new InvolvedPartyDTO(
            0, // Database will generate the actual ID
            txtFirstName.getText().trim(), 
            txtLastName.getText().trim(),
            LocalDate.parse(txtDob.getText().trim())
        );
    }
}