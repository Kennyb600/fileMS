package vtdi.keniel.filems.gui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vtdi.keniel.filems.services.LegalDocumentGenerator;
import vtdi.keniel.filems.dto.CourtCaseDTO;
import vtdi.keniel.filems.dto.InvolvedPartyDTO;
import vtdi.keniel.filems.dto.JudgeDTO;
import vtdi.keniel.filems.network.FileMSClient;
import vtdi.keniel.filems.network.NetworkMessage;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class CourtCaseInternalFrame extends JPanel {

    private static final Logger logger = LogManager.getLogger(CourtCaseInternalFrame.class);
    private JTable caseTable;
    private DefaultTableModel tableModel;
    private FileMSClient apiClient;
    private TableRowSorter<DefaultTableModel> rowSorter; 
    private List<CourtCaseDTO> currentCases; 
    
    private String userRole;

    public CourtCaseInternalFrame(String userRole) {
        this.userRole = userRole;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        try {
            apiClient = new FileMSClient();
            setupTable();
            setupSearchBar(); 
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
        caseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rowSorter = new TableRowSorter<>(tableModel);
        caseTable.setRowSorter(rowSorter);
        add(new JScrollPane(caseTable), BorderLayout.CENTER);
    }
    
    private void setupSearchBar() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search Cases: "));
        JTextField txtSearch = new JTextField(30);
        searchPanel.add(txtSearch);
        
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            
            private void filter() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0) rowSorter.setRowFilter(null);
                else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });
        add(searchPanel, BorderLayout.NORTH);
    }

    private void setupControlPanel() {
        JPanel buttonGrid = new JPanel(new GridLayout(1, 8, 10, 0));
        buttonGrid.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton btnFileCase = new JButton("File New Case");
        JButton btnUpdateStatus = new JButton("Update Status");
        JButton btnViewDossier = new JButton("View Dossier");
        JButton btnAuditLog = new JButton("Audit Log"); 
        JButton btnGenerateWarrant = new JButton("Generate Warrant"); 
        JButton btnDeleteCase = new JButton("Delete Case"); 
        JButton btnExport = new JButton("Export CSV");
        JButton btnGraph = new JButton("View Statistics");

        // RBAC ENFORCEMENT
        if ("USER".equalsIgnoreCase(userRole)) {
            btnDeleteCase.setEnabled(false);
            btnDeleteCase.setToolTipText("Permission Denied: Admins Only");
            btnAuditLog.setEnabled(false);
            btnAuditLog.setToolTipText("Permission Denied: Supervisors Only");
        } else if ("SUPERVISOR".equalsIgnoreCase(userRole)) {
            btnDeleteCase.setEnabled(false);
            btnDeleteCase.setToolTipText("Permission Denied: Admins Only");
        }

        btnGraph.addActionListener(e -> showCaseloadGraph());
        btnExport.addActionListener(e -> exportTableToCSV());
        btnFileCase.addActionListener(e -> openInsertDialogWithData());
        
        btnGenerateWarrant.addActionListener(e -> {
            int viewRow = caseTable.getSelectedRow();
            if (viewRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a case first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CourtCaseDTO selectedCase = currentCases.get(caseTable.convertRowIndexToModel(viewRow));
            LegalDocumentGenerator docService = new LegalDocumentGenerator();
            docService.generateBenchWarrant(selectedCase);
            JOptionPane.showMessageDialog(this, "Warrant Generated Successfully.", "Workflow Complete", JOptionPane.INFORMATION_MESSAGE);
        });
        
        btnAuditLog.addActionListener(e -> {
            int viewRow = caseTable.getSelectedRow();
            if (viewRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a case first to view its audit history.");
                return;
            }
            CourtCaseDTO selectedCase = currentCases.get(caseTable.convertRowIndexToModel(viewRow));
            new DossierHistoryDialog(SwingUtilities.getWindowAncestor(this), selectedCase.caseNumber()).setVisible(true);
        });
        
        btnViewDossier.addActionListener(e -> {
            int viewRow = caseTable.getSelectedRow();
            if (viewRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a case first.");
                return;
            }
            new ViewDossierDialog(SwingUtilities.getWindowAncestor(this), currentCases.get(caseTable.convertRowIndexToModel(viewRow))).setVisible(true);
        });

        btnDeleteCase.addActionListener(e -> {
            int viewRow = caseTable.getSelectedRow();
            if (viewRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a case first.");
                return;
            }
            CourtCaseDTO selectedCase = currentCases.get(caseTable.convertRowIndexToModel(viewRow));
            int confirm = JOptionPane.showConfirmDialog(this, "Delete Case " + selectedCase.caseNumber() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) deleteCaseToServer(selectedCase.caseNumber());
        });

        // ==========================================
        // NEW: UPDATED UPDATE STATUS WORKFLOW
        // ==========================================
        btnUpdateStatus.addActionListener(e -> {
            int viewRow = caseTable.getSelectedRow();
            if (viewRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a case first.");
                return;
            }
            CourtCaseDTO selectedCase = currentCases.get(caseTable.convertRowIndexToModel(viewRow));
            openUpdateDialogWithData(selectedCase); // Call new method to fetch names
        });

        buttonGrid.add(btnFileCase);
        buttonGrid.add(btnUpdateStatus);
        buttonGrid.add(btnViewDossier);
        buttonGrid.add(btnAuditLog); 
        buttonGrid.add(btnGenerateWarrant); 
        buttonGrid.add(btnDeleteCase); 
        buttonGrid.add(btnExport);
        buttonGrid.add(btnGraph);

        add(buttonGrid, BorderLayout.SOUTH);
    }

    private void showCaseloadGraph() {
        if (currentCases == null || currentCases.isEmpty()) return;
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        for (CourtCaseDTO c : currentCases) {
            String judgeName = (c.judge() != null) ? "Hon. " + c.judge().lastName() : "Unassigned";
            counts.put(judgeName, counts.getOrDefault(judgeName, 0) + 1);
        }
        counts.forEach(dataset::setValue);
        JFreeChart chart = ChartFactory.createPieChart("Current Caseload by Judge", dataset, true, true, false);
        JDialog chartDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Caseload Statistics", true);
        chartDialog.setLayout(new BorderLayout());
        chartDialog.add(new ChartPanel(chart), BorderLayout.CENTER);
        chartDialog.setSize(600, 500);
        chartDialog.setLocationRelativeTo(this);
        chartDialog.setVisible(true);
    }

    private void loadCasesFromDatabase() {
        SwingWorker<List<CourtCaseDTO>, Void> worker = new SwingWorker<>() {
            @Override protected List<CourtCaseDTO> doInBackground() throws Exception {
                NetworkMessage response = apiClient.sendRequest(new NetworkMessage(NetworkMessage.Command.GET_ALL_CASES, null));
                if (response.getCommand() == NetworkMessage.Command.RESPONSE_OK) return (List<CourtCaseDTO>) response.getPayload();
                throw new Exception(response.getPayload().toString());
            }
            @Override protected void done() {
                try {
                    currentCases = get();
                    tableModel.setRowCount(0);
                    if (currentCases != null) {
                        for (CourtCaseDTO c : currentCases) {
                            String applicantName = c.applicant() != null && c.applicant().firstName() != null ? c.applicant().firstName() + " " + c.applicant().lastName() : "N/A";
                            String respondentName = c.respondent() != null && c.respondent().firstName() != null ? c.respondent().firstName() + " " + c.respondent().lastName() : "N/A";
                            String judgeName = c.judge() != null && c.judge().lastName() != null ? c.judge().lastName() : "N/A";
                            tableModel.addRow(new Object[]{ c.caseNumber(), applicantName, respondentName, judgeName, c.orderDate(), c.courtOrder() });
                        }
                    }
                } catch (Exception e) { logger.error("Exception loading cases: " + e.getMessage(), e); }
            }
        };
        worker.execute();
    }

    private void openInsertDialogWithData() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override protected Object[] doInBackground() throws Exception {
                NetworkMessage judgeRes = apiClient.sendRequest(new NetworkMessage(NetworkMessage.Command.GET_ALL_JUDGES, null));
                NetworkMessage partyRes = apiClient.sendRequest(new NetworkMessage(NetworkMessage.Command.GET_ALL_PARTIES, null));
                if (judgeRes.getCommand() == NetworkMessage.Command.RESPONSE_OK && partyRes.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    return new Object[]{ judgeRes.getPayload(), partyRes.getPayload() };
                }
                throw new Exception("Failed to fetch prerequisites from server.");
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    Object[] results = get();
                    InsertCaseDialog dialog = new InsertCaseDialog(SwingUtilities.getWindowAncestor(CourtCaseInternalFrame.this), (List<JudgeDTO>) results[0], (List<InvolvedPartyDTO>) results[1]);
                    dialog.setVisible(true);
                    if (dialog.isApproved()) insertCaseToServer(dialog.getCreatedCase());
                } catch (Exception e) { JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Cannot open form: Network Error", "Network Error", JOptionPane.ERROR_MESSAGE); }
            }
        };
        worker.execute();
    }

    // ==========================================================
    // NEW METHOD: Fetches data for the Update Dialog
    // ==========================================================
    private void openUpdateDialogWithData(CourtCaseDTO selectedCase) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override protected Object[] doInBackground() throws Exception {
                NetworkMessage judgeRes = apiClient.sendRequest(new NetworkMessage(NetworkMessage.Command.GET_ALL_JUDGES, null));
                NetworkMessage partyRes = apiClient.sendRequest(new NetworkMessage(NetworkMessage.Command.GET_ALL_PARTIES, null));
                if (judgeRes.getCommand() == NetworkMessage.Command.RESPONSE_OK && partyRes.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    return new Object[]{ judgeRes.getPayload(), partyRes.getPayload() };
                }
                throw new Exception("Failed to fetch prerequisites from server.");
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    Object[] results = get();
                    UpdateCaseDialog dialog = new UpdateCaseDialog(
                        SwingUtilities.getWindowAncestor(CourtCaseInternalFrame.this), 
                        selectedCase, 
                        userRole,
                        (List<JudgeDTO>) results[0], 
                        (List<InvolvedPartyDTO>) results[1]
                    );
                    dialog.setVisible(true);
                    if (dialog.isApproved()) updateCaseToServer(dialog.getUpdatedCase());
                } catch (Exception e) { JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Cannot open form: Network Error", "Network Error", JOptionPane.ERROR_MESSAGE); }
            }
        };
        worker.execute();
    }

    private void insertCaseToServer(CourtCaseDTO newCase) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                NetworkMessage response = apiClient.sendRequest(new NetworkMessage(NetworkMessage.Command.INSERT_CASE, newCase));
                if (response.getCommand() != NetworkMessage.Command.RESPONSE_OK) throw new Exception(response.getPayload().toString());
                return null;
            }
            @Override protected void done() {
                try { get(); JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Case filed successfully!"); loadCasesFromDatabase(); } 
                catch (Exception e) { JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Failed to file Case.", "Error", JOptionPane.ERROR_MESSAGE); }
            }
        };
        worker.execute();
    }
    
    private void updateCaseToServer(CourtCaseDTO updatedCase) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                NetworkMessage response = apiClient.sendRequest(new NetworkMessage(NetworkMessage.Command.UPDATE_CASE, updatedCase));
                if (response.getCommand() != NetworkMessage.Command.RESPONSE_OK) throw new Exception(response.getPayload().toString());
                return null;
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try { get(); JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Case updated successfully!"); loadCasesFromDatabase(); } 
                catch (Exception e) { JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Failed to update Case.", "Error", JOptionPane.ERROR_MESSAGE); }
            }
        };
        worker.execute();
    }
    
    private void deleteCaseToServer(String caseNumber) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                NetworkMessage response = apiClient.sendRequest(new NetworkMessage(NetworkMessage.Command.DELETE_CASE, caseNumber));
                if (response.getCommand() != NetworkMessage.Command.RESPONSE_OK) throw new Exception(response.getPayload().toString());
                return null;
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try { get(); JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Case permanently deleted."); loadCasesFromDatabase(); } 
                catch (Exception e) { JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Failed to delete Case.", "Error", JOptionPane.ERROR_MESSAGE); }
            }
        };
        worker.execute();
    }
    
    private void exportTableToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("Court_Cases_Report.csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter fw = new java.io.FileWriter(fileChooser.getSelectedFile());
                 java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {
                for (int i = 0; i < caseTable.getColumnCount(); i++) bw.write(caseTable.getColumnName(i) + ",");
                bw.newLine();
                for (int row = 0; row < caseTable.getRowCount(); row++) {
                    for (int col = 0; col < caseTable.getColumnCount(); col++) {
                        Object value = caseTable.getValueAt(row, col);
                        bw.write((value != null ? value.toString().replace(",", ";") : "") + ",");
                    }
                    bw.newLine();
                }
                JOptionPane.showMessageDialog(this, "Report exported!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Export Error", "Export Error", JOptionPane.ERROR_MESSAGE); }
        }
    }
}