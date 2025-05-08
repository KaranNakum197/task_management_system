import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class DeleteTaskForm extends JFrame {
    private int userId;
    private int roleId;
    private Color accentColor = new Color(59, 89, 152);
    private Color backgroundColor = new Color(245, 245, 250);

    private JList<String> taskList;
    private DefaultListModel<String> taskListModel;
    private JButton deleteButton;
    private JButton cancelButton;
    private JTextArea taskDetailsArea;

    private Connection conn;
    private HashMap<String, Integer> taskMap = new HashMap<>();

    public DeleteTaskForm(int userId, int roleId) {
        this.userId = userId;
        this.roleId = roleId;

        setTitle("Delete Task");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(backgroundColor);

        JLabel headerLabel = new JLabel("Delete Task");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(accentColor);

        JLabel warningLabel = new JLabel("Warning: This action cannot be undone!");
        warningLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        warningLabel.setForeground(new Color(217, 83, 79));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(backgroundColor);
        headerPanel.add(headerLabel, BorderLayout.NORTH);
        headerPanel.add(warningLabel, BorderLayout.SOUTH);

        JPanel listPanel = new JPanel(new BorderLayout(5, 5));
        listPanel.setBackground(backgroundColor);
        listPanel.setBorder(BorderFactory.createTitledBorder("Select Task to Delete"));

        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        taskList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = taskList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedValue = taskList.getSelectedValue();
                    int taskId = taskMap.get(selectedValue);
                    showTaskDetails(taskId);
                    deleteButton.setEnabled(true);
                } else {
                    taskDetailsArea.setText("");
                    deleteButton.setEnabled(false);
                }
            }
        });

        JScrollPane listScrollPane = new JScrollPane(taskList);
        listScrollPane.setPreferredSize(new Dimension(300, 300));
        listPanel.add(listScrollPane, BorderLayout.CENTER);

        JPanel detailsPanel = new JPanel(new BorderLayout(5, 5));
        detailsPanel.setBackground(backgroundColor);
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Task Details"));

        taskDetailsArea = new JTextArea();
        taskDetailsArea.setEditable(false);
        taskDetailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taskDetailsArea.setLineWrap(true);
        taskDetailsArea.setWrapStyleWord(true);

        JScrollPane detailsScrollPane = new JScrollPane(taskDetailsArea);
        detailsScrollPane.setPreferredSize(new Dimension(300, 300));
        detailsPanel.add(detailsScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundColor);

        deleteButton = createButton("Delete Task", e -> deleteSelectedTask());
        deleteButton.setBackground(new Color(217, 83, 79));
        deleteButton.setEnabled(false);

        cancelButton = createButton("Cancel", e -> returnToDashboard());

        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, detailsPanel);
        splitPane.setDividerLocation(350);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        connectToDB();
        loadTasksFromDatabase();

        setVisible(true);
    }

    private void connectToDB() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/task_management_system", "root", "19K@ran2001");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
        }
    }

    private void loadTasksFromDatabase() {
        try {
            String query;
            if (roleId == 1 || roleId == 2) {
                query = "SELECT task_id, task_name, status FROM tasks";
            } else {
                query = "SELECT task_id, task_name, status FROM tasks WHERE assigned_to = ?";
            }

            PreparedStatement stmt = conn.prepareStatement(query);
            if (roleId != 1 && roleId != 2) {
                stmt.setInt(1, userId);
            }

            ResultSet rs = stmt.executeQuery();
            taskListModel.clear();
            taskMap.clear();

            while (rs.next()) {
                int taskId = rs.getInt("task_id");
                String taskName = rs.getString("task_name");
                String status = rs.getString("status");

                String listEntry = "Task: " + taskName + " (" + status + ") - ID: " + taskId;
                taskListModel.addElement(listEntry);
                taskMap.put(listEntry, taskId);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load tasks: " + e.getMessage());
        }
    }

    private void showTaskDetails(int taskId) {
        try {
            String query = "SELECT * FROM tasks WHERE task_id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, taskId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                StringBuilder details = new StringBuilder();
                details.append("Title: ").append(rs.getString("task_name")).append("\n");
                details.append("Status: ").append(rs.getString("status")).append("\n");
                details.append("Due Date: ").append(rs.getDate("due_date")).append("\n");
                //details.append("Priority: ").append(rs.getInt("priority")).append("\n");
                details.append("Description: ").append(rs.getString("description")).append("\n");
                taskDetailsArea.setText(details.toString());
            }
        } catch (Exception e) {
            taskDetailsArea.setText("Error fetching task details: " + e.getMessage());
        }
    }

    private void deleteSelectedTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            String selectedValue = taskList.getSelectedValue();
            int taskId = taskMap.get(selectedValue);

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this task?\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM tasks WHERE task_id = ?");
                    ps.setInt(1, taskId);
                    int rows = ps.executeUpdate();

                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this, "Task deleted successfully.");
                        loadTasksFromDatabase();
                        taskDetailsArea.setText("");
                        deleteButton.setEnabled(false);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete task.");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
            }
        }
    }

    private void returnToDashboard() {
        dispose();
        new DashboardFrame(userId, roleId); // Replace with your actual dashboard frame class
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(accentColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 36));
        button.addActionListener(listener);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(button.getBackground().darker());
            }

            public void mouseExited(MouseEvent e) {
                if (text.equals("Delete Task")) {
                    button.setBackground(new Color(217, 83, 79));
                } else {
                    button.setBackground(accentColor);
                }
            }
        });

        return button;
    }
}
