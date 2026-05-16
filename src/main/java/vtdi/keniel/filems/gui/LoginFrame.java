package vtdi.keniel.filems.gui;

import com.formdev.flatlaf.FlatLightLaf; // Correct capitalization here
import java.awt.*;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginFrame extends JFrame {
    
    private static final Logger logger = LogManager.getLogger(LoginFrame.class);
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginFrame() {
        setTitle("FileMS - Secure Access Gateway");
        setSize(450, 350); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 25));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel lblUser = new JLabel("Registry Username:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lblUser);
        
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(txtUsername);

        JLabel lblPass = new JLabel("Security Password:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lblPass);
        
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(txtPassword);

        JButton btnLogin = new JButton("submit");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        getRootPane().setDefaultButton(btnLogin); 
        btnLogin.addActionListener(e -> authenticate());
        
        panel.add(new JLabel("")); // Spacer
        panel.add(btnLogin);

        add(panel, BorderLayout.CENTER);

        JLabel lblInfo = new JLabel("Demo Logins: admin/admin | super/super | clerk/clerk", SwingConstants.CENTER);
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        add(lblInfo, BorderLayout.SOUTH);
    }

    private void authenticate() {
    String user = txtUsername.getText();
    String pass = new String(txtPassword.getPassword());
    String role = null;

    // Connect to the database to verify the user
    String sql = "SELECT security_role FROM Registry_Users WHERE username = ? AND password = ?";
    
    try (java.sql.Connection conn = vtdi.keniel.filems.utils.DatabaseConnection.getConnection();
         java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, user);
        stmt.setString(2, pass); 
        
        try (java.sql.ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                role = rs.getString("security_role"); // Fetch the role from the DB!
            }
        }
    } catch (Exception e) {
        logger.error("Database connection failed during login", e);
        JOptionPane.showMessageDialog(this, "Database Connection Error.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    if (role != null) {
        logger.info("Database authentication successful. Granted role: " + role);
        new MainAppFrame(role).setVisible(true);
        this.dispose(); 
    } else {
        logger.warn("Failed unauthorized login attempt for username: " + user);
        JOptionPane.showMessageDialog(this, "Invalid credentials. Access Denied.", "Security Alert", JOptionPane.ERROR_MESSAGE);
        txtPassword.setText(""); 
    }
}

    public static void main(String[] args) {
        // ACTIVATE MODERN LIGHT GUI THEME BEFORE STARTING APP
        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); // Correct capitalization here
        } catch (Exception ex) {
            logger.error("Failed to initialize modern FlatLaf theme.", ex);
        }
        
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}