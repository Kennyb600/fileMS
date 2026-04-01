package vtdi.keniel.filems.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vtdi.keniel.filems.network.FileMSClient;
import vtdi.keniel.filems.network.NetworkMessage;
import vtdi.keniel.filems.models.CourtCase;

public class CourtCaseInternalFrame extends JInternalFrame {

    private static final Logger logger = LogManager.getLogger(CourtCaseInternalFrame.class);

    private JTable caseTable;
    private DefaultTableModel tableModel;
    private FileMSClient apiClient;

    public CourtCaseInternalFrame() {
        super("Manage Court Cases", true, true, true, true);
        try {
            logger.info("Initializing CourtCaseInternalFrame...");
            setSize(800, 500);
            setLayout(new BorderLayout());

            apiClient = new FileMSClient();

            setupTable();
            setupControlPanel();

        } catch (Exception e) {
            logger.error("Error building Court Case Form: " + e.getMessage(), e);
        }
    }
    
    private void setupTable() {
        // CHANGED: Removed Case ID column
        String[] columnNames = {"Case Number", "Court Order", "Order Date"};
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        caseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(caseTable);
        add(scrollPane, BorderLayout.CENTER);
        logger.info("JTable and TableModel configured.");
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout());

        JButton btnSelect = new JButton("Load Cases");
        JButton btnInsert = new JButton("Insert Case");
        JButton btnUpdate = new JButton("Update Case");
        JButton btnDelete = new JButton("Delete Case");

        btnSelect.addActionListener(e -> loadCasesFromDatabase());
        btnInsert.addActionListener(e -> openInsertDialog());
        btnUpdate.addActionListener(e -> openUpdateDialog());
        btnDelete.addActionListener(e -> handleDeleteCase());

        controlPanel.add(btnSelect);
        controlPanel.add(btnInsert);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnDelete);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void openInsertDialog() {
        logger.info("Opening Insert Case Dialog Form...");
        
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

            // CHANGED: Shifted indexes because Case ID is gone
            String currentCaseNum = (String) tableModel.getValueAt(selectedRow, 0);
            String currentOrder = (String) tableModel.getValueAt(selectedRow, 1);
            String currentDate = (String) tableModel.getValueAt(selectedRow, 2);

            JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "Update Case: " + currentCaseNum, true);
            dialog.setSize(450, 400);
            dialog.setLayout(new java.awt.GridLayout(8, 2, 10, 10));
            dialog.setLocationRelativeTo(this);

            JTextField txtCaseNum = new JTextField(currentCaseNum);
            txtCaseNum.setEditable(false); // CRITICAL: You cannot change a Primary Key once created!
            txtCaseNum.setToolTipText("Case Number is the Primary Key and cannot be edited.");
            
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
            btnSubmit.addActionListener(e -> {
                try {
                    CourtCase updatedCase = new CourtCase();
                    updatedCase.setCaseNumber(txtCaseNum.getText()); // This is now the ID Hibernate looks for
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
            JOptionPane.showMessageDialog(this, "An unexpected error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteCase() {
        try {
            int selectedRow = caseTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a case to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // CHANGED: Case Number is now at index 0
            String caseNumber = (String) tableModel.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to permanently delete Case Number " + caseNumber + "?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                logger.info("Attempting to delete Case Number: " + caseNumber);
                
                NetworkMessage request = new NetworkMessage(NetworkMessage.Command.DELETE_CASE, caseNumber);
                NetworkMessage response = apiClient.sendRequest(request);

                if (response.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    JOptionPane.showMessageDialog(this, "Case deleted successfully.");
                    logger.info("Deleted Case Number " + caseNumber + " via GUI.");
                    loadCasesFromDatabase(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete: " + response.getPayload(), "Error", JOptionPane.ERROR_MESSAGE);
                    logger.error("Server rejected delete request: " + response.getPayload());
                }
            }
        } catch (Exception e) {
            logger.fatal("GUI Error during delete case operation.", e);
            JOptionPane.showMessageDialog(this, "An unexpected error occurred while deleting. Check logs.", "Critical Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCasesFromDatabase() {
        logger.info("User clicked 'Load Cases'. Requesting data from server...");
        try {
            NetworkMessage request = new NetworkMessage(NetworkMessage.Command.GET_ALL_CASES, null);
            NetworkMessage response = apiClient.sendRequest(request);

            if (response.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                @SuppressWarnings("unchecked")
                List<CourtCase> cases = (List<CourtCase>) response.getPayload();

                tableModel.setRowCount(0);

                for (CourtCase c : cases) {
                    // CHANGED: Removed c.getCaseId()
                    Object[] rowData = {
                        c.getCaseNumber(),
                        c.getCourtOrder(),
                        c.getOrderDate() != null ? c.getOrderDate().toString() : "N/A"
                    };
                    tableModel.addRow(rowData);
                }
                
                logger.info("Successfully loaded " + cases.size() + " cases into the JTable.");
                
            } else {
                logger.warn("Server returned an error: " + response.getPayload());
                JOptionPane.showMessageDialog(this, "Failed to load cases:\n" + response.getPayload(), "Server Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            logger.error("Exception occurred while loading cases into GUI: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "A critical error occurred. Please check the logs.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}