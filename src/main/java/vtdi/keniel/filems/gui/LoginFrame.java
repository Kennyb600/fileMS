package vtdi.keniel.filems.gui;

import java.awt.*;
import javax.swing.*;

import com.formdev.flatlaf.FlatDarkLaf; 

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginFrame() {
        setTitle("FileMS - Secure Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centers the window on the screen
        setResizable(false);
        
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- Top Header Panel ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        JLabel lblTitle = new JLabel("Maintenance Dept FileMS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center Form Panel ---
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        formPanel.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        formPanel.add(txtUsername);

        formPanel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        formPanel.add(txtPassword);

        add(formPanel, BorderLayout.CENTER);

        // --- Bottom Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnLogin = new JButton("Login");
        JButton btnCancel = new JButton("Cancel");

        // Make the Login button slightly wider
        btnLogin.setPreferredSize(new Dimension(100, 35));
        btnCancel.setPreferredSize(new Dimension(100, 35));

        // --- Login Logic ---
        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());

            // TODO: Replace this with a NetworkMessage to check your MySQL Database!
            if ("admin".equals(username) && "admin123".equals(password)) {
                // Success! Close the login screen and open the main dashboard
                this.dispose(); 
                SwingUtilities.invokeLater(() -> {
                    new MainAppFrame().setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText(""); // Clear the password field on failure
            }
        });

        btnCancel.addActionListener(e -> System.exit(0));

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        // Pressing "Enter" on the keyboard will automatically click the Login button
        getRootPane().setDefaultButton(btnLogin);
    }

    // --- We moved the Main Launcher here! ---
    public static void main(String[] args) {
        // Apply your modern theme!
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf()); 
            // If you didn't use FlatLaf, replace the line above with:
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("Failed to initialize theme.");
        }

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
