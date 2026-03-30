package vtdi.keniel.filems.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Import our network and model classes
import vtdi.keniel.filems.network.FileMSClient;
import vtdi.keniel.filems.network.NetworkMessage;
import vtdi.keniel.filems.models.CourtCase;

public class CourtCaseInternalFrame extends JInternalFrame {

    // Strict Requirement: Manage and Log All Exceptions
    private static final Logger logger = LogManager.getLogger(CourtCaseInternalFrame.class);

    private JTable caseTable;
    private DefaultTableModel tableModel;
    
    // The client we will use to talk to the server
    private FileMSClient apiClient;

    public CourtCaseInternalFrame() {
        // Title, Resizable, Closable, Maximizable, Iconifiable
        super("Manage Court Cases", true, true, true, true);
        
        try {
            logger.info("Initializing CourtCaseInternalFrame...");
            setSize(800, 500);
            setLayout(new BorderLayout());

            // Initialize the API Client
            apiClient = new FileMSClient();

            setupTable();
            setupControlPanel();

        } catch (Exception e) {
            logger.error("Error building Court Case Form: " + e.getMessage(), e);
        }
    }
    
    /**
     * Opens a dialog form to collect new case data, then sends it to the server to save.
     */
    private void openInsertDialog() {
        logger.info("Opening Insert Case Dialog Form...");
        
        // Create a popup window
        JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "Insert New Case", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new java.awt.GridLayout(8, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        // Create the text fields
        JTextField txtCaseNum = new JTextField();
        JTextField txtOrder = new JTextField();
        JTextField txtDate = new JTextField(java.time.LocalDate.now().toString());
        JTextField txtAppId = new JTextField("1");
        JTextField txtRespId = new JTextField("2");
        JTextField txtChildId = new JTextField("3");
        JTextField txtJudgeId = new JTextField("1");

        // Add labels and fields to the window
        dialog.add(new JLabel(" Case Number:")); dialog.add(txtCaseNum);
        dialog.add(new JLabel(" Court Order:")); dialog.add(txtOrder);
        dialog.add(new JLabel(" Order Date (YYYY-MM-DD):")); dialog.add(txtDate);
        dialog.add(new JLabel(" Applicant ID:")); dialog.add(txtAppId);
        dialog.add(new JLabel(" Respondent ID:")); dialog.add(txtRespId);
        dialog.add(new JLabel(" Child ID:")); dialog.add(txtChildId);
        dialog.add(new JLabel(" Judge ID:")); dialog.add(txtJudgeId);

        // Create the submit button
        JButton btnSubmit = new JButton("Save Case");
        btnSubmit.addActionListener(e -> {
            try {
                // 1. Build the CourtCase object from the text fields
                CourtCase newCase = new CourtCase();
                newCase.setCaseNumber(txtCaseNum.getText());
                newCase.setCourtOrder(txtOrder.getText());
                newCase.setOrderDate(java.time.LocalDate.parse(txtDate.getText()));

                // Set the relationships using the provided IDs
                vtdi.keniel.filems.models.InvolvedParty app = new vtdi.keniel.filems.models.InvolvedParty(); 
                app.setId(Integer.parseInt(txtAppId.getText()));
                vtdi.keniel.filems.models.InvolvedParty resp = new vtdi.keniel.filems.models.InvolvedParty(); 
                resp.setId(Integer.parseInt(txtRespId.getText()));
                vtdi.keniel.filems.models.InvolvedParty child = new vtdi.keniel.filems.models.InvolvedParty(); 
                child.setId(Integer.parseInt(txtChildId.getText()));
                vtdi.keniel.filems.models.Judge judge = new vtdi.keniel.filems.models.Judge(); 
                judge.setId(Integer.parseInt(txtJudgeId.getText()));

                newCase.setApplicant(app);
                newCase.setRespondent(resp);
                newCase.setChild(child);
                newCase.setJudge(judge);

                // 2. Send it over the network
                NetworkMessage req = new NetworkMessage(NetworkMessage.Command.INSERT_CASE, newCase);
                NetworkMessage res = apiClient.sendRequest(req);

                // 3. Check the result
                if (res.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    JOptionPane.showMessageDialog(dialog, "Case Inserted Successfully!");
                    dialog.dispose(); // Close the popup
                    loadCasesFromDatabase(); // Automatically refresh the JTable!
                } else {
                    JOptionPane.showMessageDialog(dialog, "Server Error: " + res.getPayload(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                logger.error("Error parsing input data: " + ex.getMessage(), ex);
                JOptionPane.showMessageDialog(dialog, "Invalid input format. Check your date or IDs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel()); // Blank spacer
        dialog.add(btnSubmit);

        dialog.setVisible(true);
    }

    private void setupTable() {
        // Strict Requirement: Create JTable with Table Models
        String[] columnNames = {"Case ID", "Case Number", "Court Order", "Order Date"};
        
        // Make cells non-editable by overriding isCellEditable
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        caseTable = new JTable(tableModel);
        
        // Add table to a scroll pane so we can scroll if there are many records
        JScrollPane scrollPane = new JScrollPane(caseTable);
        add(scrollPane, BorderLayout.CENTER);
        logger.info("JTable and TableModel configured.");
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout());

        JButton btnSelect = new JButton("Load Cases");
        btnSelect.setToolTipText("Fetch all cases from the database");
        
        JButton btnInsert = new JButton("Insert Case");
        JButton btnUpdate = new JButton("Update Case");
        JButton btnDelete = new JButton("Delete Case");

        // Wire up the Select button to actually fetch data
        btnSelect.addActionListener(e -> loadCasesFromDatabase());
        
        // We will implement these later
        btnInsert.addActionListener(e -> openInsertDialog());
        btnUpdate.addActionListener(e -> logger.info("Update button clicked."));
        btnDelete.addActionListener(e -> logger.info("Delete button clicked."));

        controlPanel.add(btnSelect);
        controlPanel.add(btnInsert);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnDelete);

        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Contacts the server to get all court cases, then loads them into the JTable.
     */
    private void loadCasesFromDatabase() {
        logger.info("User clicked 'Load Cases'. Requesting data from server...");
        
        try {
            // 1. Create the request message
            NetworkMessage request = new NetworkMessage(NetworkMessage.Command.GET_ALL_CASES, null);
            
            // 2. Send the request via the client
            NetworkMessage response = apiClient.sendRequest(request);

            // 3. Process the server's response
            if (response.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                
                // Extract the payload safely
                @SuppressWarnings("unchecked")
                List<CourtCase> cases = (List<CourtCase>) response.getPayload();

                // Clear any existing rows in the table first
                tableModel.setRowCount(0);

                // Add each case as a new row in the TableModel
                for (CourtCase c : cases) {
                    Object[] rowData = {
                        c.getCaseId(),
                        c.getCaseNumber(),
                        c.getCourtOrder(),
                        c.getOrderDate() != null ? c.getOrderDate().toString() : "N/A"
                    };
                    tableModel.addRow(rowData);
                }
                
                logger.info("Successfully loaded " + cases.size() + " cases into the JTable.");
                JOptionPane.showMessageDialog(this, "Successfully loaded " + cases.size() + " cases.", "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } else {
                // Server returned an error
                logger.warn("Server returned an error: " + response.getPayload());
                JOptionPane.showMessageDialog(this, "Failed to load cases:\n" + response.getPayload(), "Server Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            // Strict Requirement: Manage and Log All Exceptions
            logger.error("Exception occurred while loading cases into GUI: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "A critical error occurred. Please check the logs.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}