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
import vtdi.keniel.filems.dto.InvolvedPartyDTO;

public class InvolvedPartyInternalFrame extends JPanel {

    private static final Logger logger = LogManager.getLogger(InvolvedPartyInternalFrame.class);
    private JTable partyTable;
    private DefaultTableModel tableModel;
    private FileMSClient apiClient;
    
    private List<InvolvedPartyDTO> currentParties; // Cache the data

    public InvolvedPartyInternalFrame() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        try {
            apiClient = new FileMSClient();
            setupTable();
            setupControlPanel();
            loadPartiesFromDatabase(); 
        } catch (Exception e) {
            logger.error("Error building Involved Party Panel: " + e.getMessage(), e);
        }
    }

    private void setupTable() {
        String[] columnNames = {"Party ID", "First Name", "Last Name", "Date of Birth"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        partyTable = new JTable(tableModel);
        partyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(partyTable), BorderLayout.CENTER);
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnInsert = new JButton("Insert Party");
        JButton btnUpdate = new JButton("Update Party");
        JButton btnDelete = new JButton("Delete Party");

        btnInsert.addActionListener(e -> {
            InsertPartyDialog dialog = new InsertPartyDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isApproved()) {
                sendPartyNetworkRequest(NetworkMessage.Command.INSERT_PARTY, dialog.getInvolvedPartyDTO(), "Party registered successfully!");
            }
        });
        
        btnUpdate.addActionListener(e -> {
            int selectedRow = partyTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a party to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            InvolvedPartyDTO selectedParty = currentParties.get(selectedRow);
            UpdatePartyDialog dialog = new UpdatePartyDialog(SwingUtilities.getWindowAncestor(this), selectedParty);
            dialog.setVisible(true);

            if (dialog.isApproved()) {
                sendPartyNetworkRequest(NetworkMessage.Command.UPDATE_PARTY, dialog.getUpdatedPartyDTO(), "Party updated successfully!");
            }
        });

        btnDelete.addActionListener(e -> {
            int selectedRow = partyTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a party to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            InvolvedPartyDTO selectedParty = currentParties.get(selectedRow);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete " + selectedParty.firstName() + " " + selectedParty.lastName() + "?\n(This may fail if they are linked to an active Court Case).", 
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                // For delete, we just send the ID (Integer) as the payload
                sendPartyNetworkRequest(NetworkMessage.Command.DELETE_PARTY, selectedParty.id(), "Party deleted successfully!");
            }
        });

        controlPanel.add(btnInsert);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnDelete);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadPartiesFromDatabase() {
        SwingWorker<List<InvolvedPartyDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<InvolvedPartyDTO> doInBackground() throws Exception {
                NetworkMessage request = new NetworkMessage(NetworkMessage.Command.GET_ALL_PARTIES, null);
                NetworkMessage response = apiClient.sendRequest(request);

                if (response.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    @SuppressWarnings("unchecked")
                    List<InvolvedPartyDTO> parties = (List<InvolvedPartyDTO>) response.getPayload();
                    return parties;
                } else {
                    throw new Exception(response.getPayload().toString());
                }
            }
            
            @Override
            protected void done() {
                try {
                    currentParties = get();
                    tableModel.setRowCount(0); 
                    
                    if (currentParties != null) {
                        for (InvolvedPartyDTO p : currentParties) {
                            tableModel.addRow(new Object[]{
                                p.id(), p.firstName(), p.lastName(), p.dateOfBirth() != null ? p.dateOfBirth().toString() : "N/A"
                            });
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(InvolvedPartyInternalFrame.this, "Failed to load parties: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * A unified method to handle Insert, Update, and Delete network calls!
     */
    private void sendPartyNetworkRequest(NetworkMessage.Command command, Object payload, String successMessage) {
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
                    JOptionPane.showMessageDialog(InvolvedPartyInternalFrame.this, successMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadPartiesFromDatabase(); // Refresh the table!
                } catch (Exception e) {
                    logger.error("Exception processing party request: " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(InvolvedPartyInternalFrame.this, "Action Failed: " + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}