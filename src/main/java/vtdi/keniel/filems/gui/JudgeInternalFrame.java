package vtdi.keniel.filems.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vtdi.keniel.filems.network.FileMSClient;

// Changed to JPanel
public class JudgeInternalFrame extends JPanel {

    private static final Logger logger = LogManager.getLogger(JudgeInternalFrame.class);
    private JTable judgeTable;
    private DefaultTableModel tableModel;
    private FileMSClient apiClient;

    public JudgeInternalFrame() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Clean padding
        
        try {
            apiClient = new FileMSClient();
            setupTable();
            setupControlPanel();
            loadJudgesFromDatabase();
            
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
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnInsert = new JButton("Insert Judge");
        JButton btnUpdate = new JButton("Update Judge");
        JButton btnDelete = new JButton("Delete Judge");

        btnInsert.addActionListener(e -> JOptionPane.showMessageDialog(this, "Insert UI ready. Awaiting backend Command implementation."));
        btnUpdate.addActionListener(e -> JOptionPane.showMessageDialog(this, "Update UI ready. Awaiting backend Command implementation."));
        btnDelete.addActionListener(e -> JOptionPane.showMessageDialog(this, "Delete UI ready. Awaiting backend Command implementation."));

        controlPanel.add(btnInsert);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnDelete);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadJudgesFromDatabase() {
        try {
            // Awaiting GET_ALL_JUDGES backend implementation
        } catch (Exception e) {
            logger.error("Exception loading judges: " + e.getMessage(), e);
        }
    }
}