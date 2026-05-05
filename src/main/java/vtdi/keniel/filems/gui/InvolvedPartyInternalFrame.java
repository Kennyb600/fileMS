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
import vtdi.keniel.filems.dto.InvolvedPartyDTO;

public class InvolvedPartyInternalFrame extends JPanel {

    private static final Logger logger = LogManager.getLogger(InvolvedPartyInternalFrame.class);
    private JTable partyTable;
    private DefaultTableModel tableModel;
    private FileMSClient apiClient;

    public InvolvedPartyInternalFrame() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        try {
            apiClient = new FileMSClient();
            setupTable();
            setupControlPanel();
            loadPartiesFromDatabase(); // Async load
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
                insertPartyToServer(dialog.getInvolvedPartyDTO());
            }
        });
        
        btnUpdate.addActionListener(e -> JOptionPane.showMessageDialog(this, "Update UI ready. Awaiting implementation."));
        btnDelete.addActionListener(e -> JOptionPane.showMessageDialog(this, "Delete UI ready. Awaiting implementation."));

        controlPanel.add(btnInsert);
        controlPanel.add(btnUpdate);
        controlPanel.add(btnDelete);
        
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadPartiesFromDatabase() {
        logger.info("Requesting Party data from server on background thread...");
        
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
                    List<InvolvedPartyDTO> parties = get();
                    tableModel.setRowCount(0); 
                    
                    if (parties != null) {
                        for (InvolvedPartyDTO p : parties) {
                            tableModel.addRow(new Object[]{
                                p.id(), 
                                p.firstName(),
                                p.lastName(),
                                p.dateOfBirth() != null ? p.dateOfBirth().toString() : "N/A"
                            });
                        }
                        logger.info("Successfully loaded " + parties.size() + " parties.");
                    }
                } catch (Exception e) {
                    logger.error("Exception loading parties: " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(InvolvedPartyInternalFrame.this, 
                            "Failed to load parties: " + e.getCause().getMessage(), 
                            "Network Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Sends the new InvolvedPartyDTO to the server asynchronously.
     */
    private void insertPartyToServer(InvolvedPartyDTO newParty) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                NetworkMessage request = new NetworkMessage(NetworkMessage.Command.INSERT_PARTY, newParty);
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
                    JOptionPane.showMessageDialog(InvolvedPartyInternalFrame.this, "Party registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadPartiesFromDatabase(); // Refresh the table automatically!
                } catch (Exception e) {
                    logger.error("Exception inserting party: " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(InvolvedPartyInternalFrame.this, 
                            "Failed to register Party: " + e.getCause().getMessage(), 
                            "Network Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}