package vtdi.keniel.filems.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vtdi.keniel.filems.network.FileMSClient;

// Changed to JPanel
public class InvolvedPartyInternalFrame extends JPanel {

    private static final Logger logger = LogManager.getLogger(InvolvedPartyInternalFrame.class);
    private JTable partyTable;
    private DefaultTableModel tableModel;
    private FileMSClient apiClient;

    public InvolvedPartyInternalFrame() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Clean padding
        
        try {
            apiClient = new FileMSClient();
            setupTable();
            setupControlPanel();
            loadPartiesFromDatabase();
            
        } catch (Exception e) {
            logger.error("Error building Involved Party Form: " + e.getMessage(), e);
        }
    }

    private void setupTable() {
        String[] columnNames = {"Party ID", "Full Name"};
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

        btnInsert.addActionListener(e -> JOptionPane.showMessageDialog(this, "Insert UI ready. Awaiting backend Command implementation."));
        btnUpdate.addActionListener(e -> JOptionPane.showMessageDialog(this, "Update UI ready. Awaiting backend Command implementation."));
        btnDelete.addActionListener(e -> JOptionPane.showMessageDialog(this, "Delete UI ready. Awaiting backend Command implementation."));

        controlPanel.add(btnInsert);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnDelete);
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private void loadPartiesFromDatabase() {
        try {
            // Awaiting GET_ALL_PARTIES backend implementation
        } catch (Exception e) {
            logger.error("Exception loading involved parties: " + e.getMessage(), e);
        }
    }
}