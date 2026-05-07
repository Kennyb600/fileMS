package vtdi.keniel.filems.gui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vtdi.keniel.filems.dto.CourtCaseDTO;
import vtdi.keniel.filems.dto.InvolvedPartyDTO;
import vtdi.keniel.filems.dto.JudgeDTO;
import vtdi.keniel.filems.network.FileMSClient;
import vtdi.keniel.filems.network.NetworkMessage;

// JFreeChart Imports
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

    public CourtCaseInternalFrame() {
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
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            
            private void filter() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
        
        add(searchPanel, BorderLayout.NORTH);
    }

    private void setupControlPanel() {
        JPanel buttonGrid = new JPanel(new GridLayout(1, 6, 10, 0));
        buttonGrid.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton btnFileCase = new JButton("File New Case");
        JButton btnUpdateStatus = new JButton("Update Status");
        JButton btnViewDossier = new JButton("View Dossier");
        JButton btnDeleteCase = new JButton("Delete Case"); 
        JButton btnExport = new JButton("Export CSV");
        JButton btnGraph = new JButton("View Statistics");

        // --- BUTTON LISTENERS ---
        btnGraph.addActionListener(e -> showCaseloadGraph());
        btnExport.addActionListener(e -> exportTableToCSV());
        btnFileCase.addActionListener(e -> openInsertDialogWithData());
        
        btnUpdateStatus.addActionListener(e -> {
            int viewRow = caseTable.getSelectedRow();
            if (viewRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a case first.");
                return;
            }
            int modelRow = caseTable.convertRowIndexToModel(viewRow);
            CourtCaseDTO selectedCase = currentCases.get(modelRow);
            
            // THIS WAS THE MISSING PIECE: Open the Dialog so you can actually edit it!
            UpdateCaseDialog dialog = new UpdateCaseDialog(SwingUtilities.getWindowAncestor(this), selectedCase);
            dialog.setVisible(true);
            
            // Only send it to the server if the user clicked "Save"
            if (dialog.isApproved()) {
                updateCaseToServer(dialog.getUpdatedCase());
            }
        });
        
        btnViewDossier.addActionListener(e -> {
            int viewRow = caseTable.getSelectedRow();
            if (viewRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a case first.");
                return;
            }
            int modelRow = caseTable.convertRowIndexToModel(viewRow);
            new ViewDossierDialog(SwingUtilities.getWindowAncestor(this), currentCases.get(modelRow)).setVisible(true);
        });

        btnDeleteCase.addActionListener(e -> {
            int viewRow = caseTable.getSelectedRow();
            if (viewRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a case first.");
                return;
            }
            int modelRow = caseTable.convertRowIndexToModel(viewRow);
            CourtCaseDTO selectedCase = currentCases.get(modelRow);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete Case " + selectedCase.caseNumber() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) deleteCaseToServer(selectedCase.caseNumber());
        });

        buttonGrid.add(btnFileCase);
        buttonGrid.add(btnUpdateStatus);
        buttonGrid.add(btnViewDossier);
        buttonGrid.add(btnDeleteCase); 
        buttonGrid.add(btnExport);
        buttonGrid.add(btnGraph);

        add(buttonGrid, BorderLayout.SOUTH);
    }

    private void showCaseloadGraph() {
        if (currentCases == null || currentCases.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data available for graph.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

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
                    loadCasesFromDatabase(); 
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Failed to update Case: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void deleteCaseToServer(String caseNumber) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                NetworkMessage request = new NetworkMessage(NetworkMessage.Command.DELETE_CASE, caseNumber);
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
                    JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Court Case permanently deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    loadCasesFromDatabase(); 
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CourtCaseInternalFrame.this, "Failed to delete Case: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void exportTableToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report As...");
        fileChooser.setSelectedFile(new java.io.File("Court_Cases_Report.csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            try (java.io.FileWriter fw = new java.io.FileWriter(fileToSave);
                 java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {

                for (int i = 0; i < caseTable.getColumnCount(); i++) {
                    bw.write(caseTable.getColumnName(i) + ",");
                }
                bw.newLine();

                for (int row = 0; row < caseTable.getRowCount(); row++) {
                    for (int col = 0; col < caseTable.getColumnCount(); col++) {
                        Object value = caseTable.getValueAt(row, col);
                        String cellData = value != null ? value.toString().replace(",", ";") : "";
                        bw.write(cellData + ",");
                    }
                    bw.newLine();
                }

                JOptionPane.showMessageDialog(this, "Report successfully exported to:\n" + fileToSave.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                logger.error("Error exporting report: " + ex.getMessage(), ex);
                JOptionPane.showMessageDialog(this, "Failed to export report: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}