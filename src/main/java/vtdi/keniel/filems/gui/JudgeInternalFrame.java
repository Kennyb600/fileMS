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

public class JudgeInternalFrame extends JInternalFrame {

    private static final Logger logger = LogManager.getLogger(JudgeInternalFrame.class);
    private JTable judgeTable;
    private DefaultTableModel tableModel;
    private FileMSClient apiClient;

    public JudgeInternalFrame() {
        super("Manage Judges", true, true, true, true);
        try {
            logger.info("Initializing JudgeInternalFrame...");
            setSize(600, 400);
            setLayout(new BorderLayout());
            apiClient = new FileMSClient();

            setupTable();
            setupControlPanel();
        } catch (Exception e) {
            logger.error("Error building Judge Form: " + e.getMessage(), e);
        }
    }

    private void setupTable() {
        String[] columnNames = {"Judge ID", "Judge Name"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        judgeTable = new JTable(tableModel);
        add(new JScrollPane(judgeTable), BorderLayout.CENTER);
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton btnSelect = new JButton("Load Judges");
        JButton btnInsert = new JButton("Insert Judge");
        JButton btnUpdate = new JButton("Update Judge");
        JButton btnDelete = new JButton("Delete Judge");

        btnSelect.addActionListener(e -> loadJudgesFromDatabase());
        btnInsert.addActionListener(e -> JOptionPane.showMessageDialog(this, "Insert UI ready. Awaiting backend Command implementation."));
        btnUpdate.addActionListener(e -> JOptionPane.showMessageDialog(this, "Update UI ready. Awaiting backend Command implementation."));
        btnDelete.addActionListener(e -> JOptionPane.showMessageDialog(this, "Delete UI ready. Awaiting backend Command implementation."));

        controlPanel.add(btnSelect);
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
                // Get the list of judges from the server payload
                List<Judge> judges = (List<Judge>) response.getPayload();
                
                // Clear the table and add the new rows
                tableModel.setRowCount(0); 
                for (Judge j : judges) {
                    tableModel.addRow(new Object[]{j.getId(), j.getName()});
                }
                logger.info("Successfully loaded judges into table.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load Judges: " + response.getPayload());
            }
        } catch (Exception e) {
            logger.error("Exception loading judges: " + e.getMessage(), e);
        }
    }
}