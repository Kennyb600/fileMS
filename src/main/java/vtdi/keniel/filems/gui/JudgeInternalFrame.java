package vtdi.keniel.filems.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vtdi.keniel.filems.network.FileMSClient;
import vtdi.keniel.filems.network.NetworkMessage;
import vtdi.keniel.filems.dto.JudgeDTO;

public class JudgeInternalFrame extends JPanel {

    private static final Logger logger = LogManager.getLogger(JudgeInternalFrame.class);
    private JTable judgeTable;
    private DefaultTableModel tableModel;
    private FileMSClient apiClient;
    
    private List<JudgeDTO> currentJudges;

    public JudgeInternalFrame() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        try {
            apiClient = new FileMSClient();
            setupTable();
            setupControlPanel();
            loadJudgesFromDatabase(); 
        } catch (Exception e) {
            logger.error("Error building Judge Panel: " + e.getMessage(), e);
        }
    }

    private void setupTable() {
        String[] columnNames = {"Judge ID", "First Name", "Last Name"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        judgeTable = new JTable(tableModel);
        judgeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(judgeTable), BorderLayout.CENTER);
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnInsert = new JButton("Register Judge");
        JButton btnUpdate = new JButton("Update Judge");
        JButton btnDelete = new JButton("Delete Judge");

        btnInsert.addActionListener(e -> {
            InsertJudgeDialog dialog = new InsertJudgeDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isApproved()) {
                sendJudgeNetworkRequest(NetworkMessage.Command.INSERT_JUDGE, dialog.getJudgeDTO(), "Judge registered successfully!");
            }
        });
        
        btnUpdate.addActionListener(e -> {
            int selectedRow = judgeTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a judge to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JudgeDTO selectedJudge = currentJudges.get(selectedRow);
            UpdateJudgeDialog dialog = new UpdateJudgeDialog(SwingUtilities.getWindowAncestor(this), selectedJudge);
            dialog.setVisible(true);

            if (dialog.isApproved()) {
                sendJudgeNetworkRequest(NetworkMessage.Command.UPDATE_JUDGE, dialog.getUpdatedJudgeDTO(), "Judge updated successfully!");
            }
        });

        btnDelete.addActionListener(e -> {
            int selectedRow = judgeTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a judge to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            JudgeDTO selectedJudge = currentJudges.get(selectedRow);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete Hon. " + selectedJudge.lastName() + "?\n(This will fail if they are assigned to a Court Case).", 
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                sendJudgeNetworkRequest(NetworkMessage.Command.DELETE_JUDGE, selectedJudge.id(), "Judge deleted successfully!");
            }
        });

        controlPanel.add(btnInsert);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnDelete);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadJudgesFromDatabase() {
        SwingWorker<List<JudgeDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<JudgeDTO> doInBackground() throws Exception {
                NetworkMessage request = new NetworkMessage(NetworkMessage.Command.GET_ALL_JUDGES, null);
                NetworkMessage response = apiClient.sendRequest(request);

                if (response.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    @SuppressWarnings("unchecked")
                    List<JudgeDTO> judges = (List<JudgeDTO>) response.getPayload();
                    return judges;
                } else {
                    throw new Exception(response.getPayload().toString());
                }
            }
            
            @Override
            protected void done() {
                try {
                    currentJudges = get();
                    tableModel.setRowCount(0); 
                    
                    if (currentJudges != null) {
                        for (JudgeDTO j : currentJudges) {
                            tableModel.addRow(new Object[]{ j.id(), j.firstName(), j.lastName() });
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(JudgeInternalFrame.this, "Failed to load judges: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void sendJudgeNetworkRequest(NetworkMessage.Command command, Object payload, String successMessage) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                NetworkMessage request = new NetworkMessage(command, payload);
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
                    JOptionPane.showMessageDialog(JudgeInternalFrame.this, successMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadJudgesFromDatabase(); // Refresh the table!
                } catch (Exception e) {
                    logger.error("Exception processing judge request: " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(JudgeInternalFrame.this, "Action Failed: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}