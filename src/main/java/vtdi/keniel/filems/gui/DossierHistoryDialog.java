package vtdi.keniel.filems.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vtdi.keniel.filems.utils.DatabaseConnection;

public class DossierHistoryDialog extends JDialog {

    private static final Logger logger = LogManager.getLogger(DossierHistoryDialog.class);
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private String caseNumber;

    public DossierHistoryDialog(java.awt.Window parent, String caseNumber) {
        super(parent, "Immutable Dossier History: " + caseNumber, Dialog.ModalityType.APPLICATION_MODAL);
        this.caseNumber = caseNumber;
        
        setSize(800, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        setupTable();
        fetchImmutableHistory();
    }

    private void setupTable() {
        String[] columns = {"Timestamp", "Action Type", "Database User", "Previous Order", "New Order"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Immutable log cannot be edited by anyone
            }
        };
        historyTable = new JTable(tableModel);
        
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(200);

        add(new JScrollPane(historyTable), BorderLayout.CENTER);
        
        JButton btnClose = new JButton("Close Secure Viewer");
        btnClose.addActionListener(e -> dispose());
        add(btnClose, BorderLayout.SOUTH);
    }

    private void fetchImmutableHistory() {
        String sql = "SELECT change_timestamp, action_type, changed_by_db_user, previous_order, new_order " +
                     "FROM Dossier_History WHERE case_number = ? ORDER BY change_timestamp DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, this.caseNumber);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getTimestamp("change_timestamp"),
                    rs.getString("action_type"),
                    rs.getString("changed_by_db_user"),
                    rs.getString("previous_order") != null ? rs.getString("previous_order") : "N/A",
                    rs.getString("new_order")
                });
            }
            logger.info("Admin accessed secure Dossier History for case: " + this.caseNumber);

        } catch (Exception e) {
            logger.error("Failed to load Dossier History for case: " + this.caseNumber, e);
            JOptionPane.showMessageDialog(this, "Could not load security audit log. Has the schema.sql trigger been run?", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}