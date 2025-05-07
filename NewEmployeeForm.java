import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class NewEmployeeForm extends JFrame implements ActionListener {
    // Class members
    private int userId;
    private int roleId;
    private Color accentColor = new Color(59, 89, 152);
    private Color backgroundColor = new Color(245, 245, 250);
    
    private JTextField tfUsername, tfEmail, tfPassword;
    private JComboBox<String> cbDepartment, cbRole;
    private JButton btnSave, btnCancel;
    private Connection conn;
    
    // Maps to store role and department data
    private Map<Integer, String> roles = new HashMap<>();
    private Map<Integer, String> departments = new HashMap<>();

    public NewEmployeeForm(int userId, int roleId) {
        this.userId = userId;
        this.roleId = roleId;
        
        // Only admin (role_id = 1) can add new employees
        if (roleId != 1) {
            JOptionPane.showMessageDialog(this, "You are not authorized to add new employees.", 
                "Access Denied", JOptionPane.ERROR_MESSAGE);
            dispose();
            new DashboardFrame(userId, roleId).setVisible(true);
            return;
        }

        setTitle("Add New Employee");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Initialize database connection
        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/task_management_system", "root", "19K@ran2001");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database Connection Error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        // Create main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);
        
        // Header
        JLabel headerLabel = new JLabel("New Employee Registration");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(accentColor);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        
        // Create form fields
        tfUsername = createTextField(20);
        tfPassword = createPasswordField(20);
        tfEmail = createTextField(20);
                
        cbDepartment = new JComboBox<>();
        cbRole = new JComboBox<>();
        
        // Load roles and departments
        loadRoles();
        loadDepartments();
        
        // Layout the form
        // First column - labels
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createLabel("Username:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Password:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createLabel("Email:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(createLabel("Department:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(createLabel("Role:"), gbc);
        
        // Second column - fields
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(tfUsername, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(tfPassword, gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(tfEmail, gbc);
        
        gbc.gridx = 1; gbc.gridy = 5;
        formPanel.add(cbDepartment, gbc);
        
        gbc.gridx = 1; gbc.gridy = 6;
        formPanel.add(cbRole, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundColor);
        
        btnSave = createButton("Save Employee", e -> saveEmployee());
        btnCancel = createButton("Cancel", e -> returnToDashboard());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        // Add components to main panel
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set content pane
        setContentPane(mainPanel);
        setVisible(true);
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return label;
    }
    
    private JTextField createTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return textField;
    }
    
    private JPasswordField createPasswordField(int columns) {
        JPasswordField passwordField = new JPasswordField(columns);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return passwordField;
    }
    
    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(accentColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 36));
        button.addActionListener(listener);
        return button;
    }
    
    private void loadRoles() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT role_id, role_name FROM roles where role_id in (3,4)");
            
            while (rs.next()) {
                int roleId = rs.getInt("role_id");
                String roleName = rs.getString("role_name");
                roles.put(roleId, roleName);
                cbRole.addItem(roleId + " - " + roleName);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading roles: " + e.getMessage());
        }
    }
    
    private void loadDepartments() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT department_id, department_name FROM departments");
            
            while (rs.next()) {
                int deptId = rs.getInt("department_id");
                String deptName = rs.getString("department_name");
                departments.put(deptId, deptName);
                cbDepartment.addItem(deptId + " - " + deptName);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading departments: " + e.getMessage());
        }
    }
    
    private void saveEmployee() {
        // Validate input fields
        if (tfUsername.getText().isEmpty() || tfPassword.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Extract values
            String username = tfUsername.getText().trim();
            String password = tfPassword.getText().trim();
            String email = tfEmail.getText().trim();
            
            int departmentId = Integer.parseInt(cbDepartment.getSelectedItem().toString().split(" - ")[0]);
            int roleId = Integer.parseInt(cbRole.getSelectedItem().toString().split(" - ")[0]);
            
            // Check if username already exists
            PreparedStatement checkPs = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?");
            checkPs.setString(1, username);
            ResultSet rs = checkPs.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Username already exists. Please choose a different username.",
                    "Username Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Insert new employee into database
            String sql = "INSERT INTO users (username, password, email, department_id, role_id) " +
                         "VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);  // In production, password should be hashed
            ps.setString(3, email);
            ps.setInt(4, departmentId);
            ps.setInt(5, roleId);
            
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Employee registered successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                returnToDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register employee.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void returnToDashboard() {
        dispose();
        new DashboardFrame(userId, roleId).setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSave) {
            saveEmployee();
        } else if (e.getSource() == btnCancel) {
            returnToDashboard();
        }
    }
    
    public static void main(String[] args) {
        // For testing, create with admin role (1)
        new NewEmployeeForm(1, 1);
    }
}