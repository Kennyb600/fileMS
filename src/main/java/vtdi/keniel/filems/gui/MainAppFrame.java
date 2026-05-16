package vtdi.keniel.filems.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainAppFrame extends JFrame {

    private static final Logger logger = LogManager.getLogger(MainAppFrame.class);
    private JTabbedPane tabbedPane; 

    public MainAppFrame(String userRole) {
        try {
            logger.info("Initializing MainAppFrame Dashboard with Role: " + userRole);
            
            setTitle("Maintenance Dept - File Management System");
            setSize(1150, 750); 
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null); 

            tabbedPane = new JTabbedPane();
            tabbedPane.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
            
            tabbedPane.addTab("Dashboard", new DashboardPanel(userRole));
            tabbedPane.addTab("Court Cases", new CourtCaseInternalFrame(userRole));
            tabbedPane.addTab("Judges", new JudgeInternalFrame());
            tabbedPane.addTab("Involved Parties", new InvolvedPartyInternalFrame());

            tabbedPane.setToolTipTextAt(0, "High-level registry statistics and security clearance.");
            tabbedPane.setToolTipTextAt(1, "Manage Parish Court files and workflows.");
            tabbedPane.setToolTipTextAt(2, "Manage the roster of presiding Judges.");
            tabbedPane.setToolTipTextAt(3, "Manage Applicants, Respondents, and Children profiles.");

            add(tabbedPane, BorderLayout.CENTER);
            setupMenuBar();

        } catch (Exception e) {
            logger.fatal("Failed to initialize GUI: " + e.getMessage(), e);
        }
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // --- FILE MENU ---
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F); 
        
        JMenuItem exitItem = new JMenuItem("Exit System");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK)); 
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // --- NAVIGATE MENU ---
        JMenu navMenu = new JMenu("View");
        navMenu.setMnemonic(KeyEvent.VK_V);

        JMenuItem dashItem = new JMenuItem("Dashboard");
        dashItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK));
        dashItem.addActionListener(e -> tabbedPane.setSelectedIndex(0));
        
        JMenuItem caseItem = new JMenuItem("Court Cases");
        caseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_DOWN_MASK));
        caseItem.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        
        JMenuItem judgeItem = new JMenuItem("Judges");
        judgeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.ALT_DOWN_MASK));
        judgeItem.addActionListener(e -> tabbedPane.setSelectedIndex(2));

        JMenuItem partyItem = new JMenuItem("Involved Parties");
        partyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.ALT_DOWN_MASK));
        partyItem.addActionListener(e -> tabbedPane.setSelectedIndex(3));

        navMenu.add(dashItem);
        navMenu.add(caseItem);
        navMenu.add(judgeItem);
        navMenu.add(partyItem);

        menuBar.add(fileMenu);
        menuBar.add(navMenu);

        // =========================================================
        // TOP RIGHT QUICK-ACCESS LOGOUT BUTTON
        // =========================================================
        // This glue acts as an invisible spring, pushing everything added after it to the far right!
        menuBar.add(Box.createHorizontalGlue()); 

        JButton btnQuickLogout = new JButton("  Log Out  ");
        btnQuickLogout.setFocusPainted(false); // Keeps the modern flat look without ugly borders
        btnQuickLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnQuickLogout.addActionListener(e -> {
            logger.info("User logged out securely via quick-access button.");
            this.dispose(); // Destroy the current secured window
            new LoginFrame().setVisible(true); // Return to the login gateway
        });

        menuBar.add(btnQuickLogout);
        
        setJMenuBar(menuBar);
    }
}