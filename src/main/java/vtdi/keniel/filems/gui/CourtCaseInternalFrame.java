package vtdi.keniel.filems.gui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vtdi.keniel.filems.network.FileMSClient;
import vtdi.keniel.filems.network.NetworkMessage;
import vtdi.keniel.filems.models.CourtCase;

// Notice this is now a JPanel, NOT a JInternalFrame
public class CourtCaseInternalFrame extends JPanel {

    private static final Logger logger = LogManager.getLogger(CourtCaseInternalFrame.class);

    private JTable caseTable;
    private DefaultTableModel tableModel;
    private JTextArea detailViewArea; // Added for the right-hand panel
    private FileMSClient apiClient;
    private List<CourtCase> currentCaseList; 

    public CourtCaseInternalFrame() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add clean padding
        
        try {
            apiClient = new FileMSClient();

            // Set up the Split Screen (Master-Detail View)
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setLeftComponent(createTablePanel());
            splitPane.setRightComponent(createDetailPanel());
            splitPane.setDividerLocation(550); // Give the table 550px of space
            
            add(splitPane, BorderLayout.CENTER);
            add(createControlPanel(), BorderLayout.SOUTH);

            // Auto-load on startup
            loadCasesFromDatabase();

        } catch (Exception e) {
            logger.error("Error building Court Case Panel: " + e.getMessage(), e);
        }
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columnNames = {"Case Number", "Court Order", "Order Date"};
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        caseTable = new JTable(tableModel);
        
        // This listener replaces the popup! It detects a click and updates the right panel.
        caseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailView();
            }
        });

        panel.add(new JScrollPane(caseTable), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        detailViewArea = new JTextArea("Select a case from the table to view details...");
        detailViewArea.setEditable(false); 
        detailViewArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        detailViewArea.setMargin(new Insets(15, 15, 15, 15));
        
        // Wrap the text area in a scroll pane just in case the order text is very long
        panel.add(new JScrollPane(detailViewArea), BorderLayout.CENTER);
        return panel;
    }
    
    private void updateDetailView() {
        int selectedRow = caseTable.getSelectedRow();
        if (selectedRow != -1 && currentCaseList != null && selectedRow < currentCaseList.size()) {
            CourtCase selectedCase = currentCaseList.get(selectedRow);
            detailViewArea.setText(selectedCase.getFormattedConsoleView());
        } else {
            detailViewArea.setText("Select a case from the table to view details...");
        }
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Align buttons left

        JButton btnInsert = new JButton("Insert Case");
        JButton btnUpdate = new JButton("Update Case");
        JButton btnDelete = new JButton("Delete Case");

        btnDelete.setToolTipText("Permanently removes the selected case. This action cannot be undone.");

        btnInsert.addActionListener(e -> openInsertDialog());
        btnUpdate.addActionListener(e -> openUpdateDialog());
        btnDelete.addActionListener(e -> handleDeleteCase());

        controlPanel.add(btnInsert);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnDelete);
        
        return controlPanel;
    }

    private void openInsertDialog() {
        JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "Insert New Case", true);
        dialog.setSize(450, 400); 
        dialog.setLayout(new java.awt.GridLayout(8, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        JTextField txtCaseNum = new JTextField();
        JTextField txtOrder = new JTextField();
        JTextField txtDate = new JTextField(java.time.LocalDate.now().toString());
        JTextField txtAppName = new JTextField();
        JTextField txtRespName = new JTextField();
        JTextField txtChildName = new JTextField();
        JTextField txtJudgeName = new JTextField();

        dialog.add(new JLabel(" Case Number:")); dialog.add(txtCaseNum);
        dialog.add(new JLabel(" Court Order:")); dialog.add(txtOrder);
        dialog.add(new JLabel(" Order Date (YYYY-MM-DD):")); dialog.add(txtDate);
        dialog.add(new JLabel(" Applicant Name:")); dialog.add(txtAppName);
        dialog.add(new JLabel(" Respondent Name:")); dialog.add(txtRespName);
        dialog.add(new JLabel(" Child Name:")); dialog.add(txtChildName);
        dialog.add(new JLabel(" Judge Name:")); dialog.add(txtJudgeName);

        JButton btnSubmit = new JButton("Save Case");
        dialog.getRootPane().setDefaultButton(btnSubmit);
        
        btnSubmit.addActionListener(e -> {
            try {
                CourtCase newCase = new CourtCase();
                newCase.setCaseNumber(txtCaseNum.getText());
                newCase.setCourtOrder(txtOrder.getText());
                newCase.setOrderDate(java.time.LocalDate.parse(txtDate.getText()));

                vtdi.keniel.filems.models.InvolvedParty app = new vtdi.keniel.filems.models.InvolvedParty(); 
                app.setName(txtAppName.getText()); 
                vtdi.keniel.filems.models.InvolvedParty resp = new vtdi.keniel.filems.models.InvolvedParty(); 
                resp.setName(txtRespName.getText()); 
                vtdi.keniel.filems.models.InvolvedParty child = new vtdi.keniel.filems.models.InvolvedParty(); 
                child.setName(txtChildName.getText()); 
                vtdi.keniel.filems.models.Judge judge = new vtdi.keniel.filems.models.Judge(); 
                judge.setName(txtJudgeName.getText()); 

                newCase.setApplicant(app);
                newCase.setRespondent(resp);
                newCase.setChild(child);
                newCase.setJudge(judge);

                NetworkMessage req = new NetworkMessage(NetworkMessage.Command.INSERT_CASE, newCase);
                NetworkMessage res = apiClient.sendRequest(req);

                if (res.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    JOptionPane.showMessageDialog(dialog, "Case and Parties Inserted Successfully!");
                    dialog.dispose(); 
                    loadCasesFromDatabase(); 
                } else {
                    JOptionPane.showMessageDialog(dialog, "Server Error: " + res.getPayload(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                logger.error("Error parsing input data: " + ex.getMessage(), ex);
                JOptionPane.showMessageDialog(dialog, "Invalid input format. Check your date.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel()); 
        dialog.add(btnSubmit);
        dialog.setVisible(true);
    }

    private void openUpdateDialog() {
        try {
            int selectedRow = caseTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a case from the table to update.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String currentCaseNum = (String) tableModel.getValueAt(selectedRow, 0);
            String currentOrder = (String) tableModel.getValueAt(selectedRow, 1);
            String currentDate = (String) tableModel.getValueAt(selectedRow, 2);

            JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "Update Case: " + currentCaseNum, true);
            dialog.setSize(450, 400);
            dialog.setLayout(new java.awt.GridLayout(8, 2, 10, 10));
            dialog.setLocationRelativeTo(this);

            JTextField txtCaseNum = new JTextField(currentCaseNum);
            txtCaseNum.setEditable(false); 
            
            JTextField txtOrder = new JTextField(currentOrder);
            JTextField txtDate = new JTextField(currentDate.equals("N/A") ? java.time.LocalDate.now().toString() : currentDate);
            JTextField txtAppName = new JTextField("Update Applicant");
            JTextField txtRespName = new JTextField("Update Respondent");
            JTextField txtChildName = new JTextField("Update Child");
            JTextField txtJudgeName = new JTextField("Update Judge");

            dialog.add(new JLabel(" Case Number (Locked):")); dialog.add(txtCaseNum);
            dialog.add(new JLabel(" Court Order:")); dialog.add(txtOrder);
            dialog.add(new JLabel(" Order Date (YYYY-MM-DD):")); dialog.add(txtDate);
            dialog.add(new JLabel(" Applicant Name:")); dialog.add(txtAppName);
            dialog.add(new JLabel(" Respondent Name:")); dialog.add(txtRespName);
            dialog.add(new JLabel(" Child Name:")); dialog.add(txtChildName);
            dialog.add(new JLabel(" Judge Name:")); dialog.add(txtJudgeName);

            JButton btnSubmit = new JButton("Update Case");
            dialog.getRootPane().setDefaultButton(btnSubmit);

            btnSubmit.addActionListener(e -> {
                try {
                    CourtCase updatedCase = new CourtCase();
                    updatedCase.setCaseNumber(txtCaseNum.getText()); 
                    updatedCase.setCourtOrder(txtOrder.getText());
                    updatedCase.setOrderDate(java.time.LocalDate.parse(txtDate.getText()));

                    vtdi.keniel.filems.models.InvolvedParty app = new vtdi.keniel.filems.models.InvolvedParty(); 
                    app.setName(txtAppName.getText()); 
                    vtdi.keniel.filems.models.InvolvedParty resp = new vtdi.keniel.filems.models.InvolvedParty(); 
                    resp.setName(txtRespName.getText()); 
                    vtdi.keniel.filems.models.InvolvedParty child = new vtdi.keniel.filems.models.InvolvedParty(); 
                    child.setName(txtChildName.getText()); 
                    vtdi.keniel.filems.models.Judge judge = new vtdi.keniel.filems.models.Judge(); 
                    judge.setName(txtJudgeName.getText()); 

                    updatedCase.setApplicant(app);
                    updatedCase.setRespondent(resp);
                    updatedCase.setChild(child);
                    updatedCase.setJudge(judge);

                    NetworkMessage req = new NetworkMessage(NetworkMessage.Command.UPDATE_CASE, updatedCase);
                    NetworkMessage res = apiClient.sendRequest(req);

                    if (res.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                        JOptionPane.showMessageDialog(dialog, "Case Updated Successfully!");
                        dialog.dispose(); 
                        loadCasesFromDatabase(); 
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Server Error: " + res.getPayload(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    logger.error("Error parsing update data: " + ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(dialog, "Invalid input format. Check your date.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            dialog.add(new JLabel()); 
            dialog.add(btnSubmit);
            dialog.setVisible(true);

        } catch (Exception e) {
            logger.error("GUI Error during update dialog creation.", e);
        }
    }

    private void handleDeleteCase() {
        try {
            int selectedRow = caseTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a case to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String caseNumber = (String) tableModel.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to permanently delete Case Number " + caseNumber + "?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                NetworkMessage request = new NetworkMessage(NetworkMessage.Command.DELETE_CASE, caseNumber);
                NetworkMessage response = apiClient.sendRequest(request);

                if (response.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    JOptionPane.showMessageDialog(this, "Case deleted successfully.");
                    loadCasesFromDatabase(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete: " + response.getPayload(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            logger.fatal("GUI Error during delete case operation.", e);
        }
    }

    private void loadCasesFromDatabase() {
        try {
            NetworkMessage request = new NetworkMessage(NetworkMessage.Command.GET_ALL_CASES, null);
            NetworkMessage response = apiClient.sendRequest(request);

            if (response.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                @SuppressWarnings("unchecked")
                List<CourtCase> cases = (List<CourtCase>) response.getPayload();
                currentCaseList = cases; 

                tableModel.setRowCount(0);

                for (CourtCase c : cases) {
                    Object[] rowData = {
                        c.getCaseNumber(),
                        c.getCourtOrder(),
                        c.getOrderDate() != null ? c.getOrderDate().toString() : "N/A"
                    };
                    tableModel.addRow(rowData);
                }
                
                // Clear the detail view when data reloads
                detailViewArea.setText("Select a case from the table to view details...");
                
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load cases:\n" + response.getPayload(), "Server Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            logger.error("Exception occurred while loading cases into GUI: " + e.getMessage(), e);
        }
    }
}