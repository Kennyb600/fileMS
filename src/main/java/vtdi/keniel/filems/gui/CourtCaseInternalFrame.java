package vtdi.keniel.filems.gui;

import java.awt.BorderLayout;
import java.awt.Cursor; 
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vtdi.keniel.filems.dto.CourtCaseDTO;
import vtdi.keniel.filems.dto.InvolvedPartyDTO;
import vtdi.keniel.filems.dto.JudgeDTO;
import vtdi.keniel.filems.network.FileMSClient;
import vtdi.keniel.filems.network.NetworkMessage;

public class CourtCaseInternalFrame extends JPanel {

    private static final Logger logger = LogManager.getLogger(CourtCaseInternalFrame.class);
    private JTable caseTable;
    private DefaultTableModel tableModel;
    private FileMSClient apiClient;
    
    // We cache the cases so we can instantly pull up the dossier without another network call!
    private List<CourtCaseDTO> currentCases; 

    public CourtCaseInternalFrame() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        try {
            apiClient = new FileMSClient();
            setupTable();
            setupControlPanel();
            loadCasesFromDatabase(); 
        } catch (Exception e) {
            logger.error("Error building Court Case Panel: " + e.getMessage(), e);
        }
    }

    private void setupTable() {
        String[] columnNames = {"Case Number", "Applicant", "Respondent", "Judge", "Order Date", "Order Summary"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        caseTable = new JTable(tableModel);
        // Set selection mode to strictly one row at a time to prevent bugs when viewing dossiers
        caseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(caseTable), BorderLayout.CENTER);
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnFileCase = new JButton("File New Case");
        JButton btnUpdateStatus = new JButton("Update Status/Order");
        JButton btnViewDossier = new JButton("View Full Dossier");

        btnFileCase.addActionListener(e -> openInsertDialogWithData());
        
        btnUpdateStatus.addActionListener(e -> {
            int selectedRow = caseTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a case from the table to update.", "No Case Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            CourtCaseDTO selectedCase = currentCases.get(selectedRow);
            
            UpdateCaseDialog dialog = new UpdateCaseDialog(SwingUtilities.getWindowAncestor(this), selectedCase);
            dialog.setVisible(true);

            if (dialog.isApproved()) {
                updateCaseToServer(dialog.getUpdatedCase());
            }
        });
        
        // --- NEW DOSSIER LOGIC ---
        btnViewDossier.addActionListener(e -> {
            int selectedRow = caseTable.getSelectedRow();
            
            // Check if they actually clicked a row
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a case from the table to view its dossier.", "No Case Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Grab the specific DTO from our cached list based on the row they clicked
            CourtCaseDTO selectedCase = currentCases.get(selectedRow);
            
            // Open the new Dossier Dialog!
            ViewDossierDialog dialog = new ViewDossierDialog(SwingUtilities.getWindowAncestor(this), selectedCase);
            dialog.setVisible(true);
        });

        controlPanel.add(btnFileCase);
        controlPanel.add(btnUpdateStatus);
        controlPanel.add(btnViewDossier);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadCasesFromDatabase() {
        SwingWorker<List<CourtCaseDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CourtCaseDTO> doInBackground() throws Exception {
                NetworkMessage request = new NetworkMessage(NetworkMessage.Command.GET_ALL_CASES, null);
                NetworkMessage response = apiClient.sendRequest(request);

                if (response.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    @SuppressWarnings("unchecked")
                    List<CourtCaseDTO> cases = (List<CourtCaseDTO>) response.getPayload();
                    return cases;
                }
                throw new Exception(response.getPayload().toString());
            }

            @Override
            protected void done() {
                try {
                    // Update our cache!
                    currentCases = get();
                    tableModel.setRowCount(0);

                    if (currentCases != null) {
                        for (CourtCaseDTO c : currentCases) {
                            String applicantName = c.applicant() != null ? c.applicant().firstName() + " " + c.applicant().lastName() : "N/A";
                            String respondentName = c.respondent() != null ? c.respondent().firstName() + " " + c.respondent().lastName() : "N/A";
                            String judgeName = c.judge() != null ? c.judge().lastName() : "N/A";

                            tableModel.addRow(new Object[]{
                                c.caseNumber(), applicantName, respondentName, judgeName, c.orderDate(), c.courtOrder()
                            });
                        }
                    }
                } catch (Exception e) {
                    logger.error("Exception loading cases: " + e.getMessage(), e);
                }
            }
        };
        worker.execute();
    }

    private void openInsertDialogWithData() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                NetworkMessage judgeReq = new NetworkMessage(NetworkMessage.Command.GET_ALL_JUDGES, null);
                NetworkMessage judgeRes = apiClient.sendRequest(judgeReq);
                
                NetworkMessage partyReq = new NetworkMessage(NetworkMessage.Command.GET_ALL_PARTIES, null);
                NetworkMessage partyRes = apiClient.sendRequest(partyReq);

                if (judgeRes.getCommand() == NetworkMessage.Command.RESPONSE_OK && partyRes.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    return new Object[]{ judgeRes.getPayload(), partyRes.getPayload() };
                }
                throw new Exception("Failed to fetch prerequisites from server.");
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    Object[] results = get();
                    @SuppressWarnings("unchecked")
                    List<JudgeDTO> judges = (List<JudgeDTO>) results[0];
                    @SuppressWarnings("unchecked")
                    List<InvolvedPartyDTO> parties = (List<InvolvedPartyDTO>) results[1];

                    InsertCaseDialog dialog = new InsertCaseDialog(SwingUtilities.getWindowAncestor(CourtCaseInternalFrame.this), judges, parties);
                    dialog.setVisible(true);

                    if (dialog.isApproved()) {
                        insertCaseToServer(dialog.getCreatedCase());
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Cannot open form: Could not fetch Judges/Parties from server.", "Network Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

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
                    JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Court Case filed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCasesFromDatabase(); 
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Failed to file Case: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void updateCaseToServer(CourtCaseDTO updatedCase) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                NetworkMessage request = new NetworkMessage(NetworkMessage.Command.UPDATE_CASE, updatedCase);
                NetworkMessage response = apiClient.sendRequest(request);

                if (response.getCommand() != NetworkMessage.Command.RESPONSE_OK) {
                    throw new Exception(response.getPayload().toString());
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Case Status updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCasesFromDatabase(); // Refresh the table so the new order shows!
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Failed to update Case: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}