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
import vtdi.keniel.filems.dto.CourtCaseDTO;
import vtdi.keniel.filems.dto.JudgeDTO;          
import vtdi.keniel.filems.dto.InvolvedPartyDTO; 
public class CourtCaseInternalFrame extends JPanel {

    private static final Logger logger = LogManager.getLogger(CourtCaseInternalFrame.class);
    private JTable caseTable;
    private DefaultTableModel tableModel;
    private FileMSClient apiClient;

    public CourtCaseInternalFrame() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        try {
            apiClient = new FileMSClient();
            setupTable();
            setupControlPanel();
            loadCasesFromDatabase(); // Async load
        } catch (Exception e) {
            logger.error("Error building Court Case Panel: " + e.getMessage(), e);
        }
    }

    private void setupTable() {
        // Expanded columns to show the nested DTO data
        String[] columnNames = {
            "Case Number", "Order Date", "Current Order", 
            "Presiding Judge", "Applicant", "Respondent", "Child"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        caseTable = new JTable(tableModel);
        // Optional: Tweak column widths for better UX
        caseTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        caseTable.getColumnModel().getColumn(2).setPreferredWidth(200); 
        
        add(new JScrollPane(caseTable), BorderLayout.CENTER);
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnInsert = new JButton("New Case File");
        JButton btnUpdate = new JButton("Update Status/Order");
        JButton btnViewDetails = new JButton("View Full Dossier");

        btnInsert.addActionListener(e -> fetchPrerequisitesAndShowDialog());
        btnUpdate.addActionListener(e -> JOptionPane.showMessageDialog(this, "Update Case Form pending."));
        btnViewDetails.addActionListener(e -> JOptionPane.showMessageDialog(this, "Detailed Split-Pane view pending."));

        controlPanel.add(btnInsert);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnViewDetails);
        
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadCasesFromDatabase() {
        logger.info("Requesting Court Case data from server on background thread...");
        
        SwingWorker<List<CourtCaseDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CourtCaseDTO> doInBackground() throws Exception {
                NetworkMessage request = new NetworkMessage(NetworkMessage.Command.GET_ALL_CASES, null);
                NetworkMessage response = apiClient.sendRequest(request);

                if (response.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    @SuppressWarnings("unchecked")
                    List<CourtCaseDTO> cases = (List<CourtCaseDTO>) response.getPayload();
                    return cases;
                } else {
                    throw new Exception(response.getPayload().toString());
                }
            }

            @Override
            protected void done() {
                try {
                    List<CourtCaseDTO> cases = get();
                    tableModel.setRowCount(0); 
                    
                    if (cases != null) {
                        for (CourtCaseDTO c : cases) {
                            
                            // Safe extraction of nested DTO data (Null-Safe)
                            String judgeName = (c.judge() != null) ? "Hon. " + c.judge().lastName() : "Unassigned";
                            String applicantName = (c.applicant() != null) ? c.applicant().firstName() + " " + c.applicant().lastName() : "N/A";
                            String respondentName = (c.respondent() != null) ? c.respondent().firstName() + " " + c.respondent().lastName() : "N/A";
                            String childName = (c.child() != null) ? c.child().firstName() + " " + c.child().lastName() : "N/A";
                            String orderDate = (c.orderDate() != null) ? c.orderDate().toString() : "Pending";

                            tableModel.addRow(new Object[]{
                                c.caseNumber(),
                                orderDate,
                                c.courtOrder(),
                                judgeName,
                                applicantName,
                                respondentName,
                                childName
                            });
                        }
                        logger.info("Successfully loaded " + cases.size() + " court cases.");
                    }
                } catch (Exception e) {
                    logger.error("Exception loading cases: " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(CourtCaseInternalFrame.this,
                            "Failed to load court cases: " + e.getCause().getMessage(), 
                            "Network Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Fetches Judges and Parties from the server, THEN opens the Dialog.
     */
    private void fetchPrerequisitesAndShowDialog() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<JudgeDTO> judges;
            List<InvolvedPartyDTO> parties;
            
            @Override
            protected Void doInBackground() throws Exception {
                // Fetch Judges
                NetworkMessage judgeReq = new NetworkMessage(NetworkMessage.Command.GET_ALL_JUDGES, null);
                NetworkMessage judgeRes = apiClient.sendRequest(judgeReq);
                if (judgeRes.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    judges = (List<JudgeDTO>) judgeRes.getPayload();
                }
                
                // Fetch Parties
                NetworkMessage partyReq = new NetworkMessage(NetworkMessage.Command.GET_ALL_PARTIES, null);
                NetworkMessage partyRes = apiClient.sendRequest(partyReq);
                if (partyRes.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    parties = (List<InvolvedPartyDTO>) partyRes.getPayload();
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    if (judges != null && parties != null) {
                        // Pass the live data to the UI Modal
                        InsertCaseDialog dialog = new InsertCaseDialog(SwingUtilities.getWindowAncestor(CourtCaseInternalFrame.this), judges, parties);
                        dialog.setVisible(true);
                        
                        if (dialog.isApproved()) {
                            insertCaseToServer(dialog.getCourtCaseDTO());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to load prerequisites for dialog.", e);
                    JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Network error loading dialog data.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    /**
     * Sends the completed DTO to the server.
     */
    private void insertCaseToServer(CourtCaseDTO newCase) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                NetworkMessage request = new NetworkMessage(NetworkMessage.Command.INSERT_CASE, newCase);
                NetworkMessage response = apiClient.sendRequest(request);
                
                if (response.getCommand() != NetworkMessage.Command.RESPONSE_OK) {
                    throw new Exception(response.getPayload().toString());
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Case filed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCasesFromDatabase(); // Refresh the table automatically!
                } catch (Exception e) {
                    logger.error("Exception filing case: " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Failed to file case: " + e.getCause().getMessage(), "Network Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    } 
}