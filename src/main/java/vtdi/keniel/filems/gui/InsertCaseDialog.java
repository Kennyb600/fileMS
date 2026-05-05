package vtdi.keniel.filems.gui;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import vtdi.keniel.filems.dto.CourtCaseDTO;
import vtdi.keniel.filems.dto.JudgeDTO;
import vtdi.keniel.filems.dto.InvolvedPartyDTO;

public class InsertCaseDialog extends JDialog {

    private JTextField txtCaseNumber;
    private JTextField txtOrderDate;
    private JTextArea txtCourtOrder;
    
    private JComboBox<JudgeDTO> comboJudge;
    private JComboBox<InvolvedPartyDTO> comboApplicant;
    private JComboBox<InvolvedPartyDTO> comboRespondent;
    private JComboBox<InvolvedPartyDTO> comboChild;
    
    private boolean approved = false;

    public InsertCaseDialog(Window parent, List<JudgeDTO> judges, List<InvolvedPartyDTO> parties) {
        super(parent, "File New Court Case", Dialog.ModalityType.APPLICATION_MODAL);
        initComponents(judges, parties);
        setSize(450, 450);
        setLocationRelativeTo(parent);
    }

    private void initComponents(List<JudgeDTO> judges, List<InvolvedPartyDTO> parties) {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Text Fields
        formPanel.add(new JLabel("Case Number:"));
        txtCaseNumber = new JTextField();
        formPanel.add(txtCaseNumber);

        formPanel.add(new JLabel("Order Date (YYYY-MM-DD):"));
        txtOrderDate = new JTextField(LocalDate.now().toString());
        formPanel.add(txtOrderDate);

        // Dropdowns (Populated with live DB data!)
        formPanel.add(new JLabel("Presiding Judge:"));
        comboJudge = new JComboBox<>(judges.toArray(new JudgeDTO[0]));
        comboJudge.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof JudgeDTO) setText("Hon. " + ((JudgeDTO) value).firstName() + " " + ((JudgeDTO) value).lastName());
                return this;
            }
        });
        formPanel.add(comboJudge);

        // Shared Renderer for Involved Parties
        DefaultListCellRenderer partyRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof InvolvedPartyDTO) setText(((InvolvedPartyDTO) value).firstName() + " " + ((InvolvedPartyDTO) value).lastName());
                return this;
            }
        };

        formPanel.add(new JLabel("Applicant:"));
        comboApplicant = new JComboBox<>(parties.toArray(new InvolvedPartyDTO[0]));
        comboApplicant.setRenderer(partyRenderer);
        formPanel.add(comboApplicant);

        formPanel.add(new JLabel("Respondent:"));
        comboRespondent = new JComboBox<>(parties.toArray(new InvolvedPartyDTO[0]));
        comboRespondent.setRenderer(partyRenderer);
        formPanel.add(comboRespondent);

        formPanel.add(new JLabel("Child:"));
        comboChild = new JComboBox<>(parties.toArray(new InvolvedPartyDTO[0]));
        comboChild.setRenderer(partyRenderer);
        formPanel.add(comboChild);

        add(formPanel, BorderLayout.NORTH);

        // Court Order Text Area
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        orderPanel.add(new JLabel("Initial Court Order:"), BorderLayout.NORTH);
        txtCourtOrder = new JTextArea(3, 20);
        orderPanel.add(new JScrollPane(txtCourtOrder), BorderLayout.CENTER);
        add(orderPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("File Case");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> {
            if (txtCaseNumber.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Case Number is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            approved = true;
            dispose();
        });

        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public boolean isApproved() { return approved; }

    /** Packages the UI selections into our safe DTO */
    public CourtCaseDTO getCourtCaseDTO() {
        return new CourtCaseDTO(
            txtCaseNumber.getText().trim(),
            (InvolvedPartyDTO) comboApplicant.getSelectedItem(),
            (InvolvedPartyDTO) comboRespondent.getSelectedItem(),
            (InvolvedPartyDTO) comboChild.getSelectedItem(),
            (JudgeDTO) comboJudge.getSelectedItem(),
            txtCourtOrder.getText().trim(),
            LocalDate.parse(txtOrderDate.getText().trim())
        );
    }
}