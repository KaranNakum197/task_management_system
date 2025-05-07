import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class NewTaskForm extends JFrame implements ActionListener {
    // Class members
    int userId, roleId;
    JTextField tfTaskName, tfDueDate;
    JTextArea taDescription;
    JComboBox<String> cbAssignedTo, cbDepartment, cbStatus;
    JLabel lblAssignedBy;
    JButton btnSave, btncancel;
    Connection conn;
    
    // Map to store user IDs and their department IDs
    private Map<Integer, Integer> userDepartments = new HashMap<>();

    public NewTaskForm(int userId, int roleId) {
        this.userId = userId;
        this.roleId = roleId;

        if (roleId > 2) {
            JOptionPane.showMessageDialog(this, "You are not allowed to assign tasks.");
            dispose();
            return;
        }

        setTitle("Create New Task");
        setSize(400, 500);
        setLayout(new GridLayout(0, 1));

        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/task_management_system", "root", "19K@ran2001");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "DB Connection Error: " + e.getMessage());
            return;
        }

        tfTaskName = new JTextField();
        taDescription = new JTextArea(4, 20);
        tfDueDate = new JTextField("2025-04-30");

        cbAssignedTo = new JComboBox<>();
        cbDepartment = new JComboBox<>();
        cbStatus = new JComboBox<>(new String[]{"Pending", "In Progress", "Completed"});

        // Load user department mappings
        loadUserDepartments();
        
        // Populate dropdowns
        populateDropdown(cbDepartment, "SELECT department_id, department_name FROM departments");
        populateDropdown(cbAssignedTo, "SELECT department_id, username FROM users WHERE role_id IN (3,4)");

        // Assigned By is fixed (logged-in user)
        String assignedByUsername = getUsernameFromUserId(userId);
        lblAssignedBy = new JLabel(" - " + assignedByUsername);

        btnSave = new JButton("Save Task");
        btnSave.addActionListener(this);
        
        btncancel = new JButton("Cancel");
        btncancel.addActionListener(e -> { 
            dispose(); // close current frame
            new DashboardFrame(userId, roleId).setVisible(true);
        });

        // Add components
        add(new JLabel("Task Name:")); add(tfTaskName);
        add(new JLabel("Description:")); add(new JScrollPane(taDescription));
        add(new JLabel("Due Date (YYYY-MM-DD):")); add(tfDueDate);
        add(new JLabel("Assigned By:")); add(lblAssignedBy);
        add(new JLabel("Department:")); add(cbDepartment);
        add(new JLabel("Assigned To:")); add(cbAssignedTo);
        
        add(new JLabel("Status:")); add(cbStatus);
        add(btnSave);
        add(btncancel);

        setVisible(true);
        setLocationRelativeTo(null);
    }
    
    private void loadUserDepartments() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT user_id, department_id FROM users WHERE role_id IN (3,4)");
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                int departmentId = rs.getInt("department_id");
                userDepartments.put(userId, departmentId);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading user departments: " + e.getMessage());
        }
    }
    
    private String getUsernameFromUserId(int userId) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT username FROM users WHERE user_id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching username: " + e.getMessage());
        }
        return "Unknown";
    }

    private void populateDropdown(JComboBox<String> combo, String query) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                combo.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading dropdown: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            String taskName = tfTaskName.getText();
            String description = taDescription.getText();
            String dueDate = tfDueDate.getText();
            
            if (taskName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please Enter Fill Empty Fields", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int assignedBy = this.userId; // From login
            int assignedTo = Integer.parseInt(cbAssignedTo.getSelectedItem().toString().split(" - ")[0]);
            int departmentId = Integer.parseInt(cbDepartment.getSelectedItem().toString().split(" - ")[0]);
            
            // Check if assignedTo user belongs to the selected department
            Integer userDepartmentId = userDepartments.get(assignedTo);
            if (userDepartmentId == null || userDepartmentId != departmentId) {
                JOptionPane.showMessageDialog(this, 
                    "The selected user does not belong to the selected department.", 
                    "Department Mismatch Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String status = cbStatus.getSelectedItem().toString();

            String sql = "INSERT INTO tasks (task_name, description, assigned_to, assigned_by, department_id, status, due_date) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, taskName);
            ps.setString(2, description);
            ps.setInt(3, assignedTo);
            ps.setInt(4, assignedBy);
            ps.setInt(5, departmentId);
            ps.setString(6, status);
            ps.setDate(7, Date.valueOf(dueDate));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Task added successfully!");
                dispose(); // close form
                returnToDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add task.");
                returnToDashboard();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    private void returnToDashboard() {
        dispose();
        new DashboardFrame(userId, roleId);
    }

    public static void main(String[] args) {
        new NewTaskForm(1, 1);
    }
}