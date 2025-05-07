import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class EditTaskForm extends JFrame {
    private int userId;
    private int roleId;
    private Color accentColor = new Color(59, 89, 152);
    private Color backgroundColor = new Color(245, 245, 250);

    private JComboBox<String> taskSelector;
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> assigneeComboBox;
    private JComboBox<String> statusComboBox;
    private JSpinner dueDateSpinner;
    private JSpinner prioritySpinner;

    private Map<String, Integer> taskTitleToId = new HashMap<>();
    private Map<String, Integer> userNameToId = new HashMap<>();
    private String[] statusOptions = {"Pending", "In Progress", "Completed", "Overdue"};
    
    // Flag to check if user can only change status
    private boolean canOnlyChangeStatus;

    public EditTaskForm(int userId, int roleId) {
        this.userId = userId;
        this.roleId = roleId;
        // Set flag for roles 3 and 4
        this.canOnlyChangeStatus = (roleId == 3 || roleId == 4);

        setTitle("Edit Task");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(backgroundColor);

        JLabel headerLabel = new JLabel("Edit Task");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(accentColor);

        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPanel.setBackground(backgroundColor);
        selectorPanel.add(new JLabel("Select Task:"));
        taskSelector = new JComboBox<>();
        taskSelector.setPreferredSize(new Dimension(300, 25));
        taskSelector.addActionListener(e -> loadTaskDetails());
        selectorPanel.add(taskSelector);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; titleField = new JTextField(20); formPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridheight = 2;
        descriptionArea = new JTextArea(5, 20); descriptionArea.setLineWrap(true);
        formPanel.add(new JScrollPane(descriptionArea), gbc); gbc.gridheight = 1;

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Assignee:"), gbc);
        gbc.gridx = 1; assigneeComboBox = new JComboBox<>(); formPanel.add(assigneeComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; statusComboBox = new JComboBox<>(statusOptions); formPanel.add(statusComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Due Date:"), gbc);
        gbc.gridx = 1;
        dueDateSpinner = new JSpinner(new SpinnerDateModel());
        dueDateSpinner.setEditor(new JSpinner.DateEditor(dueDateSpinner, "yyyy-MM-dd"));
        formPanel.add(dueDateSpinner, gbc);

//      gbc.gridx = 0; gbc.gridy = 6;
//      formPanel.add(new JLabel("Priority (1-5):"), gbc);
//      gbc.gridx = 1; prioritySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
//      formPanel.add(prioritySpinner, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundColor);
        JButton saveButton = createButton("Save Changes", e -> saveChanges());
        JButton cancelButton = createButton("Cancel", e -> returnToDashboard());
        buttonPanel.add(saveButton); buttonPanel.add(cancelButton);

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setBackground(backgroundColor);
        headerWrapper.add(headerLabel, BorderLayout.NORTH);
        headerWrapper.add(selectorPanel, BorderLayout.CENTER);

        mainPanel.add(headerWrapper, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        
        // Apply role-based restrictions to form fields
        applyRoleBasedRestrictions();
        
        setVisible(true);

        loadUsers(); loadTasks();
    }
    
    private void applyRoleBasedRestrictions() {
        if (canOnlyChangeStatus) {
            // Set all fields except status as read-only
            titleField.setEditable(false);
            descriptionArea.setEditable(false);
            assigneeComboBox.setEnabled(false);
            dueDateSpinner.setEnabled(false);
            
            // Only status can be changed
            statusComboBox.setEnabled(true);
            
            // Update the form title to indicate limited access
            setTitle("Update Task Status");
        }
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(accentColor);
        button.setForeground(Color.black);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 36));
        button.addActionListener(listener);
        return button;
    }

    private Connection connect() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/task_management_system";
        String user = "root";
        String password = "19K@ran2001";
        return DriverManager.getConnection(url, user, password);
    }

    private void loadUsers() {
        try (Connection con = connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id, username FROM users")) {
            assigneeComboBox.removeAllItems();
            userNameToId.clear();
            while (rs.next()) {
                int id = rs.getInt("user_id");
                String name = rs.getString("username");
                userNameToId.put(name, id);
                assigneeComboBox.addItem(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {
        try (Connection con = connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT task_id, task_name FROM tasks ")) {
            taskSelector.removeAllItems();
            taskTitleToId.clear();
            while (rs.next()) {
                int id = rs.getInt("task_id");
                String title = rs.getString("task_name");
                taskTitleToId.put(title, id);
                taskSelector.addItem(title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTaskDetails() {
        String selectedTitle = (String) taskSelector.getSelectedItem();
        if (selectedTitle == null) return;
        int taskId = taskTitleToId.get(selectedTitle);
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM tasks WHERE task_id = ?")) {
            ps.setInt(1, taskId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                titleField.setText(rs.getString("task_name"));
                descriptionArea.setText(rs.getString("description"));
                java.sql.Date dueDate = rs.getDate("due_date");
                dueDateSpinner.setValue(dueDate != null ? dueDate : new Date());
                //prioritySpinner.setValue(rs.getInt("priority"));
                statusComboBox.setSelectedItem(rs.getString("status"));
                int assignedTo = rs.getInt("assigned_to");
                for (Map.Entry<String, Integer> entry : userNameToId.entrySet()) {
                    if (entry.getValue() == assignedTo) {
                        assigneeComboBox.setSelectedItem(entry.getKey());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveChanges() {
        String selectedTitle = (String) taskSelector.getSelectedItem();
        if (selectedTitle == null) return;
        int taskId = taskTitleToId.get(selectedTitle);
        String status = (String) statusComboBox.getSelectedItem();
        
        try (Connection con = connect()) {
            PreparedStatement ps;
            
            if (canOnlyChangeStatus) {
                // For roles 3 and 4, only update the status
                ps = con.prepareStatement("UPDATE tasks SET status=? WHERE task_id=?");
                ps.setString(1, status);
                ps.setInt(2, taskId);
            } else {
                // For other roles, update all fields
                String newTitle = titleField.getText();
                String desc = descriptionArea.getText();
                Date due = (Date) dueDateSpinner.getValue();
                String assigneeName = (String) assigneeComboBox.getSelectedItem();
                int assigneeId = userNameToId.get(assigneeName);
                
                ps = con.prepareStatement(
                    "UPDATE tasks SET task_name=?, description=?, due_date=?, status=?, assigned_to=? WHERE task_id=?");
                ps.setString(1, newTitle);
                ps.setString(2, desc);
                ps.setDate(3, new java.sql.Date(due.getTime()));
                ps.setString(4, status);
                ps.setInt(5, assigneeId);
                ps.setInt(6, taskId);
            }
            
            ps.executeUpdate();
            
            String successMessage = canOnlyChangeStatus ? 
                "Task status updated successfully." : "Task updated successfully.";
            JOptionPane.showMessageDialog(this, successMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
            returnToDashboard();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating task!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnToDashboard() {
        dispose();
        new DashboardFrame(userId, roleId);
    }
}