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
import vtdi.keniel.filems.models.InvolvedParty;

public class InvolvedPartyInternalFrame extends JPanel { // Extends JPanel for Tabbed Dashboard

    private static final Logger logger = LogManager.getLogger(InvolvedPartyInternalFrame.class);
    private JTable partyTable;
    private DefaultTableModel tableModel;
    private FileMSClient apiClient;

    public InvolvedPartyInternalFrame() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Clean padding
        
        try {
            apiClient = new FileMSClient();

            setupTable();
            setupControlPanel();
            
            // Auto-load data immediately
            loadPartiesFromDatabase();
            
        } catch (Exception e) {
            logger.error("Error building Involved Party Panel: " + e.getMessage(), e);
        }
    }

    private void setupTable() {
        String[] columnNames = {"Party ID", "Full Name", "Date of Birth"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        partyTable = new JTable(tableModel);
        
        add(new JScrollPane(partyTable), BorderLayout.CENTER);
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnInsert = new JButton("Insert Party");
        JButton btnUpdate = new JButton("Update Party");
        JButton btnDelete = new JButton("Delete Party");

        // Placeholders for future CRUD expansion
        btnInsert.addActionListener(e -> JOptionPane.showMessageDialog(this, "Insert UI ready. Awaiting backend implementation."));
        btnUpdate.addActionListener(e -> JOptionPane.showMessageDialog(this, "Update UI ready. Awaiting backend implementation."));
        btnDelete.addActionListener(e -> JOptionPane.showMessageDialog(this, "Delete UI ready. Awaiting backend implementation."));

        controlPanel.add(btnInsert);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnDelete);
        
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private void loadPartiesFromDatabase() {
        logger.info("Requesting Involved Party data from server...");
        try {
            NetworkMessage request = new NetworkMessage(NetworkMessage.Command.GET_ALL_PARTIES, null);
            NetworkMessage response = apiClient.sendRequest(request);

            if (response.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                @SuppressWarnings("unchecked")
                List<InvolvedParty> parties = (List<InvolvedParty>) response.getPayload();
                
                tableModel.setRowCount(0); // Clear existing rows
                
                for (InvolvedParty p : parties) {
                    tableModel.addRow(new Object[]{
                        p.getId(), 
                        p.getFullName(),
                        p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : "N/A"
                    });
                }
                logger.info("Successfully loaded " + parties.size() + " involved parties.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load parties: " + response.getPayload(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            logger.error("Exception loading involved parties: " + e.getMessage(), e);
        }
    }
}