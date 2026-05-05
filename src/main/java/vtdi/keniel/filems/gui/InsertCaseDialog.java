package vtdi.keniel.filems.gui;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;

import vtdi.keniel.filems.dto.CourtCaseDTO;
import vtdi.keniel.filems.dto.InvolvedPartyDTO;
import vtdi.keniel.filems.dto.JudgeDTO;

public class InsertCaseDialog extends JDialog {

    private JTextField txtCaseNumber;
    private JComboBox<JudgeDTO> cbJudge;
    private JComboBox<InvolvedPartyDTO> cbApplicant;
    private JComboBox<InvolvedPartyDTO> cbRespondent;
    private JComboBox<InvolvedPartyDTO> cbChild;
    private JTextField txtCourtOrder;
    private JTextField txtOrderDate;
    
    private boolean approved = false;
    private CourtCaseDTO createdCase = null;

    public InsertCaseDialog(Window parent, List<JudgeDTO> judges, List<InvolvedPartyDTO> parties) {
        super(parent, "File New Court Case", Dialog.ModalityType.APPLICATION_MODAL);
        initComponents(judges, parties);
        setSize(450, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents(List<JudgeDTO> judges, List<InvolvedPartyDTO> parties) {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Case Number ---
        formPanel.add(new JLabel("Case Number:"));
        txtCaseNumber = new JTextField();
        formPanel.add(txtCaseNumber);

        // --- Dropdowns setup ---
        cbJudge = new JComboBox<>(judges.toArray(new JudgeDTO[0]));
        cbJudge.setRenderer(createJudgeRenderer());
        
        // Add a "null" option at index 0 for optional fields like Child
        parties.add(0, null); 
        InvolvedPartyDTO[] partyArray = parties.toArray(new InvolvedPartyDTO[0]);
        
        cbApplicant = new JComboBox<>(partyArray);
        cbApplicant.setRenderer(createPartyRenderer());
        
        cbRespondent = new JComboBox<>(partyArray);
        cbRespondent.setRenderer(createPartyRenderer());
        
        cbChild = new JComboBox<>(partyArray);
        cbChild.setRenderer(createPartyRenderer());

        formPanel.add(new JLabel("Assign Judge:"));
        formPanel.add(cbJudge);

        formPanel.add(new JLabel("Applicant:"));
        formPanel.add(cbApplicant);

        formPanel.add(new JLabel("Respondent:"));
        formPanel.add(cbRespondent);

        formPanel.add(new JLabel("Child (Optional):"));
        formPanel.add(cbChild);

        // --- Order & Date ---
        formPanel.add(new JLabel("Initial Court Order:"));
        txtCourtOrder = new JTextField();
        formPanel.add(txtCourtOrder);

        formPanel.add(new JLabel("Order Date (YYYY-MM-DD):"));
        txtOrderDate = new JTextField();
        formPanel.add(txtOrderDate);

        add(formPanel, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("File Case");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> processSave());
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
        
        getRootPane().setDefaultButton(btnSave);
    }

    private void processSave() {
        String caseNum = txtCaseNumber.getText().trim();
        String order = txtCourtOrder.getText().trim();
        String dateStr = txtOrderDate.getText().trim();
        
        JudgeDTO judge = (JudgeDTO) cbJudge.getSelectedItem();
        InvolvedPartyDTO applicant = (InvolvedPartyDTO) cbApplicant.getSelectedItem();
        InvolvedPartyDTO respondent = (InvolvedPartyDTO) cbRespondent.getSelectedItem();
        InvolvedPartyDTO child = (InvolvedPartyDTO) cbChild.getSelectedItem();

        if (caseNum.isEmpty() || order.isEmpty() || dateStr.isEmpty() || judge == null || applicant == null || respondent == null) {
            JOptionPane.showMessageDialog(this, "Case Number, Judge, Applicant, Respondent, and Order details are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate orderDate = LocalDate.parse(dateStr);
            createdCase = new CourtCaseDTO(caseNum, applicant, respondent, child, judge, order, orderDate);
            approved = true;
            dispose();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date Format. Please use YYYY-MM-DD.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isApproved() { return approved; }
    public CourtCaseDTO getCreatedCase() { return createdCase; }

    // --- Custom Renderers to make dropdowns look pretty ---
    
    // FIX: Returned DefaultListCellRenderer directly
    private DefaultListCellRenderer createJudgeRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof JudgeDTO judge) {
                    setText("Hon. " + judge.firstName() + " " + judge.lastName());
                }
                return this;
            }
        };
    }

    // FIX: Returned DefaultListCellRenderer directly
    private DefaultListCellRenderer createPartyRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof InvolvedPartyDTO party) {
                    setText(party.firstName() + " " + party.lastName() + " (ID: " + party.id() + ")");
                } else {
                    setText("--- None / Unassigned ---");
                }
                return this;
            }
        };
    }
}