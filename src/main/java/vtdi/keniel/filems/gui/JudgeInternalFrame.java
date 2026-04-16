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
import vtdi.keniel.filems.models.Judge;

public class JudgeInternalFrame extends JPanel { // Extends JPanel for Tabbed Dashboard

    private static final Logger logger = LogManager.getLogger(JudgeInternalFrame.class);
    private JTable judgeTable;
    private DefaultTableModel tableModel;
    private FileMSClient apiClient;

    public JudgeInternalFrame() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Clean padding
        
        try {
            apiClient = new FileMSClient();

            setupTable();
            setupControlPanel();
            
            // Auto-load data immediately
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
        
        // Add scroll pane to center
        add(new JScrollPane(judgeTable), BorderLayout.CENTER);
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnInsert = new JButton("Insert Judge");
        JButton btnUpdate = new JButton("Update Judge");
        JButton btnDelete = new JButton("Delete Judge");

        // Placeholders for future CRUD expansion
        btnInsert.addActionListener(e -> JOptionPane.showMessageDialog(this, "Insert UI ready. Awaiting backend implementation."));
        btnUpdate.addActionListener(e -> JOptionPane.showMessageDialog(this, "Update UI ready. Awaiting backend implementation."));
        btnDelete.addActionListener(e -> JOptionPane.showMessageDialog(this, "Delete UI ready. Awaiting backend implementation."));

        controlPanel.add(btnInsert);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnDelete);
        
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadJudgesFromDatabase() {
        logger.info("Requesting Judge data from server...");
        try {
            NetworkMessage request = new NetworkMessage(NetworkMessage.Command.GET_ALL_JUDGES, null);
            NetworkMessage response = apiClient.sendRequest(request);

            if (response.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                @SuppressWarnings("unchecked")
                List<Judge> judges = (List<Judge>) response.getPayload();
                
                tableModel.setRowCount(0); // Clear existing rows
                
                for (Judge j : judges) {
                    tableModel.addRow(new Object[]{
                        j.getId(), 
                        j.getFullName()
                    });
                }
                logger.info("Successfully loaded " + judges.size() + " judges.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load judges: " + response.getPayload(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            logger.error("Exception loading judges: " + e.getMessage(), e);
        }
    }
}