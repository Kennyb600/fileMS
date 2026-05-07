package vtdi.keniel.filems.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.formdev.flatlaf.FlatDarkLaf;

public class MainAppFrame extends JFrame {

    private static final Logger logger = LogManager.getLogger(MainAppFrame.class);
    private JTabbedPane tabbedPane; // Escaping the pop-up trap!

    public MainAppFrame() {
        try {
            logger.info("Initializing Modern MainAppFrame Dashboard...");
            
            setTitle("Maintenance Dept - File Management System");
            setSize(1100, 700);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null); // Center on screen

            // Initialize the Tabbed Dashboard
            tabbedPane = new JTabbedPane();
            
            // Dock all the views natively into tabs
            tabbedPane.addTab("Court Cases", new CourtCaseInternalFrame());
            tabbedPane.addTab("Judges", new JudgeInternalFrame());
            tabbedPane.addTab("Involved Parties", new InvolvedPartyInternalFrame());

            add(tabbedPane, BorderLayout.CENTER);

            setupMenuBar();

            logger.info("MainAppFrame initialized successfully.");
        } catch (Exception e) {
            logger.fatal("Failed to initialize GUI: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Critical UI Error. See logs.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // --- FILE MENU ---
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F); 
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK)); 
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // --- NAVIGATE MENU ---
        JMenu navMenu = new JMenu("Navigate");
        navMenu.setMnemonic(KeyEvent.VK_N);

        // Instead of opening popups, the menu now just switches the active tab!
        JMenuItem caseItem = new JMenuItem("Go to Court Cases");
        caseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
        caseItem.addActionListener(e -> tabbedPane.setSelectedIndex(0));

        JMenuItem judgeItem = new JMenuItem("Go to Judges");
        judgeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
        judgeItem.addActionListener(e -> tabbedPane.setSelectedIndex(1));

        JMenuItem partyItem = new JMenuItem("Go to Involved Parties");
        partyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
        partyItem.addActionListener(e -> tabbedPane.setSelectedIndex(2));
        
        navMenu.add(caseItem);
        navMenu.add(judgeItem);
        navMenu.add(partyItem);

        menuBar.add(fileMenu);
        menuBar.add(navMenu);
        setJMenuBar(menuBar);
    }
   
   
}