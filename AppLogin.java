import java.awt.*;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleContext;

public class AppLogin extends JFrame {
    private static final Font defaultFont = new Font("PT Sans", Font.PLAIN, 16);
    private static final Color borderColor = Color.decode("#cccccc");
    private static final Color placeholderColor = Color.GRAY;
    private static final Color textColor = Color.BLACK;
    
    public AppLogin() {
        JFrame frame = new JFrame("KARM - Task Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel backgroundPanel = new JPanel() {
            private Image backgroundImage;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage == null) {
                    try {
                        backgroundImage = ImageIO.read(new File("E:/JP/src/AppBg.png"));
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("PT Sans", Font.BOLD, 24));
        titleLabel.setForeground(Color.decode("#333333"));

        JPanel formContainer = createTranslucentPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        JTextField usernameField = createPlaceholderTextField("Username");
        JPasswordField passwordField = createPlaceholderPasswordField("Password");

        String[] designations = {"Select Designation", "Manager", "Project Lead", "Employee", "Admin"};
        JComboBox<String> designationDropdown = createDesignationDropdown(designations);  

        JButton loginButton = createButton("Login");

        gbc.gridy = 0; formContainer.add(usernameField, gbc);
        gbc.gridy = 1; formContainer.add(passwordField, gbc);
        gbc.gridy = 2; formContainer.add(designationDropdown, gbc);
        gbc.gridy = 3; formContainer.add(loginButton, gbc);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        gbc.gridy = 0; gbc.gridwidth = 2; mainPanel.add(titleLabel, gbc);
        gbc.gridy = 1; gbc.gridwidth = 1;
        mainPanel.add(formContainer, gbc);

        backgroundPanel.add(mainPanel);
        frame.add(backgroundPanel);
        frame.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            backgroundPanel.requestFocusInWindow();
        });

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = String.valueOf(passwordField.getPassword()).trim();
            String role = (String) designationDropdown.getSelectedItem();
            //String role = designationdropdown.getSelectedItem().toString().toLowerCase();

            // Map role to ID
            int roleId = switch (role) {
                case "Admin" -> 1;
                case "Manager" -> 2;
                case "Project Lead" -> 3;
                case "Employee" -> 4;
                default -> 0;
            };
            
            if (username.isEmpty() || username.equals("Username")) {
                
                JOptionPane.showMessageDialog(frame, "Please enter a valid username","Validation Error",JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (role == null || role.equals("Select Designation")) {
                JOptionPane.showMessageDialog(frame, "Please select your designation.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            
            try {
                Connection con = DBConnection.getConnection();
                String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role_id = ?";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, username);
                pst.setString(2, password);
                pst.setInt(3, roleId);

                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    roleId = rs.getInt("role_id");

                    JOptionPane.showMessageDialog(frame, username + " Loged in", "Login Successfull", JOptionPane.INFORMATION_MESSAGE);

                 
                    
                    new DashboardFrame(userId, roleId).setVisible(true);
                    frame.dispose();
                  
                }

                else {
                    JOptionPane.showMessageDialog(this, "Invalid username/password/role.");
                }  
                con.close();
            } 
            catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        });
    }
    
    public static void main(String[] args) {
        new AppLogin(); // Show login screen
    }

	private static JPanel createTranslucentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 255, 200));
        panel.setPreferredSize(new Dimension(300, 320));
        return panel;
    }

    private static JTextField createPlaceholderTextField(String placeholder) {
        JTextField field = new JTextField(15);
        field.setFont(defaultFont);
        field.setForeground(placeholderColor);
        field.setText(placeholder);
        field.setBorder(new LineBorder(borderColor, 2));
        addPlaceholderBehavior(field, placeholder);
        return field;
    }

    private static JPasswordField createPlaceholderPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(15);
        field.setFont(defaultFont);
        field.setEchoChar((char) 0);
        field.setForeground(placeholderColor);
        field.setText(placeholder);
        field.setBorder(new LineBorder(borderColor, 2));
        addPlaceholderBehavior(field, placeholder);
        return field;
    }

    private static JComboBox<String> createDesignationDropdown(String[] designations) {
        JComboBox<String> dropdown = new JComboBox<>(designations);
        styleComponent(dropdown);  // Ensure the same style as text fields
        dropdown.setPreferredSize(new Dimension(218, 30));  // Explicitly set the size to match other fields
        return dropdown;
    }

    private static void addPlaceholderBehavior(JTextComponent field, String placeholder) {
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(textColor);
                    if (field instanceof JPasswordField) {
                        ((JPasswordField) field).setEchoChar('•');
                    }
                }
            }

            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(placeholderColor);
                    field.setText(placeholder);
                    if (field instanceof JPasswordField) {
                        ((JPasswordField) field).setEchoChar((char) 0);
                    }
                }
            }
        });
    }

    private static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.decode("#007bff"));
        button.setForeground(Color.WHITE);
        button.setFont(defaultFont);
        button.setPreferredSize(new Dimension(120, 30));
        return button;
    }

    private static void styleComponent(JComponent component) {
        component.setFont(defaultFont);
        component.setForeground(textColor);
        component.setPreferredSize(new Dimension(240, 30));
        component.setBorder(new LineBorder(borderColor, 2));
    }   
    
}

