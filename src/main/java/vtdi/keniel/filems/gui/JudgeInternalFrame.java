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
import vtdi.keniel.filems.dto.JudgeDTO; 

public class JudgeInternalFrame extends JPanel { 

    private static final Logger logger = LogManager.getLogger(JudgeInternalFrame.class);
    private JTable judgeTable;
    private DefaultTableModel tableModel;
    private FileMSClient apiClient;

    public JudgeInternalFrame() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); 
        
        try {
            apiClient = new FileMSClient();

            setupTable();
            setupControlPanel();
            
            // Auto-load data asynchronously immediately upon tab creation
            loadJudgesFromDatabase();
            
        } catch (Exception e) {
            logger.error("Error building Judge Panel: " + e.getMessage(), e);
        }
    }

    private void setupTable() {
        String[] columnNames = {"Judge ID", "Full Name"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        judgeTable = new JTable(tableModel);
        
        add(new JScrollPane(judgeTable), BorderLayout.CENTER);
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnInsert = new JButton("Insert Judge");
        JButton btnUpdate = new JButton("Update Judge");
        JButton btnDelete = new JButton("Delete Judge");

        btnInsert.addActionListener(e -> {
            // Open the modal
            InsertJudgeDialog dialog = new InsertJudgeDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);

            // If the user clicked "Save" instead of "Cancel"
            if (dialog.isApproved()) {
                JudgeDTO newJudge = dialog.getJudgeDTO();
                insertJudgeToServer(newJudge); // Fire off the network request
            }
        });
        
        btnUpdate.addActionListener(e -> JOptionPane.showMessageDialog(this, "Update UI ready. Awaiting backend implementation."));
        btnDelete.addActionListener(e -> JOptionPane.showMessageDialog(this, "Delete UI ready. Awaiting backend implementation."));

        controlPanel.add(btnInsert);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnDelete);
        
        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Now uses a SwingWorker to fetch data on a background thread.
     * This prevents the Single Page Application UI from freezing while waiting for the network.
     */
    private void loadJudgesFromDatabase() {
        logger.info("Requesting Judge data from server on background thread...");
        
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
                    List<JudgeDTO> judges = get(); 
                    
                    tableModel.setRowCount(0); 
                    
                    if (judges != null) {
                        for (JudgeDTO j : judges) {
                            String fullName = "Hon. " + j.firstName() + " " + j.lastName();
                            
                            tableModel.addRow(new Object[]{
                                j.id(), 
                                fullName
                            });
                        }
                        logger.info("Successfully loaded " + judges.size() + " judges to the UI.");
                    }
                } catch (Exception e) {
                    logger.error("Exception loading judges: " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(JudgeInternalFrame.this, 
                            "Failed to load judges: " + e.getCause().getMessage(), 
                            "Network Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        // Fire off the background thread!
        worker.execute();
    } // <-- Properly closed method!

    /**
     * Sends the new JudgeDTO to the server asynchronously.
     */
    private void insertJudgeToServer(JudgeDTO newJudge) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                NetworkMessage request = new NetworkMessage(NetworkMessage.Command.INSERT_JUDGE, newJudge);
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
                    JOptionPane.showMessageDialog(JudgeInternalFrame.this, "Judge registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    // The magic touch: refresh the table immediately!
                    loadJudgesFromDatabase();
                    
                } catch (Exception e) {
                    logger.error("Exception inserting judge: " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(JudgeInternalFrame.this, 
                            "Failed to register Judge: " + e.getCause().getMessage(), 
                            "Network Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    } // <-- Properly closed method!
}