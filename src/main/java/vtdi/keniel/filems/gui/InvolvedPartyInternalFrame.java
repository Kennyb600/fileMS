package vtdi.keniel.filems.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vtdi.keniel.filems.network.FileMSClient;

public class InvolvedPartyInternalFrame extends JInternalFrame {

    private static final Logger logger = LogManager.getLogger(InvolvedPartyInternalFrame.class);
    private JTable partyTable;
    private DefaultTableModel tableModel;
    private FileMSClient apiClient;

    public InvolvedPartyInternalFrame() {
        super("Manage Involved Parties", true, true, true, true);
        try {
            logger.info("Initializing InvolvedPartyInternalFrame...");
            setSize(600, 400);
            setLayout(new BorderLayout());
            apiClient = new FileMSClient();

            setupTable();
            setupControlPanel();
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
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton btnSelect = new JButton("Load Parties");
        JButton btnInsert = new JButton("Insert Party");
        JButton btnUpdate = new JButton("Update Party");
        JButton btnDelete = new JButton("Delete Party");

        btnSelect.addActionListener(e -> JOptionPane.showMessageDialog(this, "To load, implement GET_ALL_PARTIES in NetworkMessage and ClientHandler."));
        btnInsert.addActionListener(e -> JOptionPane.showMessageDialog(this, "Insert UI ready. Awaiting backend Command implementation."));
        btnUpdate.addActionListener(e -> JOptionPane.showMessageDialog(this, "Update UI ready. Awaiting backend Command implementation."));
        btnDelete.addActionListener(e -> JOptionPane.showMessageDialog(this, "Delete UI ready. Awaiting backend Command implementation."));

        controlPanel.add(btnSelect);
        controlPanel.add(btnInsert);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnDelete);
        add(controlPanel, BorderLayout.SOUTH);
    }
}