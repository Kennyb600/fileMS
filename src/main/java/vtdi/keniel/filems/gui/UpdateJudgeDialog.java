package vtdi.keniel.filems.gui;

import java.awt.*;
import javax.swing.*;
import vtdi.keniel.filems.dto.JudgeDTO;

public class UpdateJudgeDialog extends JDialog {

    private JTextField txtFirstName;
    private JTextField txtLastName;
    private boolean approved = false;
    private int judgeId; 

    public UpdateJudgeDialog(Window parent, JudgeDTO existingJudge) {
        super(parent, "Update Judge Details", Dialog.ModalityType.APPLICATION_MODAL);
        this.judgeId = existingJudge.id(); 
        initComponents(existingJudge);
        setSize(300, 180);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents(JudgeDTO existingJudge) {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("First Name:"));
        txtFirstName = new JTextField(existingJudge.firstName());
        formPanel.add(txtFirstName);

        formPanel.add(new JLabel("Last Name:"));
        txtLastName = new JTextField(existingJudge.lastName());
        formPanel.add(txtLastName);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save Changes");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> {
            if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            approved = true;
            dispose();
        });

        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnSave);
    }

    public boolean isApproved() { return approved; }

    public JudgeDTO getUpdatedJudgeDTO() {
        return new JudgeDTO(judgeId, txtFirstName.getText().trim(), txtLastName.getText().trim());
    }
}