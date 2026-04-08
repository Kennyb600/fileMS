package vtdi.keniel.filems.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainAppFrame extends JFrame {

    private static final Logger logger = LogManager.getLogger(MainAppFrame.class);
    private JDesktopPane desktopPane;

    public MainAppFrame() {
        try {
            logger.info("Initializing MainAppFrame (Parent Window)...");
            
            setTitle("Maintenance Dept - File Management System");
            setSize(1024, 768);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null); // Center on screen

            desktopPane = new JDesktopPane();
            add(desktopPane, BorderLayout.CENTER);

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
        fileMenu.setMnemonic(KeyEvent.VK_F); // Alt+F opens this menu
        fileMenu.setToolTipText("System file operations");

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK)); // Ctrl+Q
        exitItem.setToolTipText("Safely exit the application");
        exitItem.addActionListener(e -> {
            logger.info("User requested exit via Menu.");
            System.exit(0);
        });
        
        fileMenu.add(exitItem);

        // --- MANAGE MENU ---
        JMenu manageMenu = new JMenu("Manage");
        manageMenu.setMnemonic(KeyEvent.VK_M);

        JMenuItem caseFormItem = new JMenuItem("Court Cases");
        caseFormItem.setMnemonic(KeyEvent.VK_C);
        caseFormItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK)); // Alt+C
        caseFormItem.setToolTipText("Open the Court Case management form");
        caseFormItem.addActionListener(e -> openCourtCaseForm());

        // 2. NEW: Judge Menu Item
        JMenuItem judgeFormItem = new JMenuItem("Judges");
        judgeFormItem.setMnemonic(KeyEvent.VK_J);
        judgeFormItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, ActionEvent.ALT_MASK)); // Alt+J
        judgeFormItem.setToolTipText("Open the Judge management form");
        judgeFormItem.addActionListener(e -> openJudgeForm());

        // 3. NEW: Involved Party Menu Item
        JMenuItem partyFormItem = new JMenuItem("Involved Parties");
        partyFormItem.setMnemonic(KeyEvent.VK_P);
        partyFormItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK)); // Alt+P
        partyFormItem.setToolTipText("Open the Involved Party management form");
        partyFormItem.addActionListener(e -> openInvolvedPartyForm());
        
        manageMenu.add(caseFormItem);
        manageMenu.add(judgeFormItem);
        manageMenu.add(partyFormItem);

        // Add menus to bar
        menuBar.add(fileMenu);
        menuBar.add(manageMenu);
        setJMenuBar(menuBar);
    }

    private void openCourtCaseForm() {
        logger.info("Opening Court Case Internal Form.");
        CourtCaseInternalFrame caseFrame = new CourtCaseInternalFrame();
        desktopPane.add(caseFrame);
        caseFrame.setVisible(true);
    }

    //Open Judge Form
    private void openJudgeForm() {
        logger.info("Opening Judge Internal Form.");
        JudgeInternalFrame judgeFrame = new JudgeInternalFrame();
        desktopPane.add(judgeFrame);
        judgeFrame.setVisible(true);
    }

    //Open Involved Party Form
    private void openInvolvedPartyForm() {
        logger.info("Opening Involved Party Internal Form.");
        InvolvedPartyInternalFrame partyFrame = new InvolvedPartyInternalFrame();
        desktopPane.add(partyFrame);
        partyFrame.setVisible(true);
    }
    
    public static void main(String[] args) {
        // Swing GUIs should be created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new MainAppFrame().setVisible(true);
        });
    }
}