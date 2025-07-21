import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.sql.*;

public class DashboardFrame extends JFrame {
    private int userId;
    private int roleId;
    private Color accentColor = new Color(59, 89, 152);
    private Color backgroundColor = new Color(245, 245, 250);
    
    // Data for charts - will be populated from database
    private Map<String, Integer> taskStatusData = new LinkedHashMap<>();
    private Map<String, Integer> assignmentData = new LinkedHashMap<>();
    
    // Stats counters
    private int totalTasks = 0;
    private int completedTasks = 0;
    private int inProgressTasks = 0;
    private int overdueTasks = 0;
    
    private final Color[] pieColors = {
    	new Color(217, 83, 79),    // Red
        new Color(70, 130, 180),  // Blue
        new Color(240, 173, 78),  // Orange
        new Color(46, 139, 87)  // Green
    };
    
    private final Color[] assignmentColors = {
        new Color(75, 192, 192),  // Teal
        new Color(153, 102, 255), // Purple
        new Color(255, 159, 64),  // Orange
        new Color(255, 99, 132),  // Pink
        new Color(54, 162, 235)   // Blue
    };

    public DashboardFrame(int userId, int roleId) {
        this.userId = userId;
        this.roleId = roleId;
        
        // Load data from database
        loadDataFromDatabase();
        
        setTitle("Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(backgroundColor);
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(backgroundColor);
        
        JLabel welcomeLabel = new JLabel("Welcome, " + getUserName() + " (" + getRoleName() + ")");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcomeLabel.setForeground(accentColor);
        
        JLabel dateLabel = new JLabel(new SimpleDateFormat("EEEE, MMMM d, yyyy").format(new Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        
        headerPanel.add(welcomeLabel, BorderLayout.NORTH);
        headerPanel.add(dateLabel, BorderLayout.SOUTH);
        
        // Charts panel
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        chartsPanel.setBackground(backgroundColor);
        
        // Pie chart panel
        JPanel piePanel = new JPanel(new BorderLayout());
        piePanel.setBorder(BorderFactory.createTitledBorder("Task Status"));
        piePanel.setBackground(backgroundColor);
        piePanel.add(new PieChartPanel(), BorderLayout.CENTER);
        
        // Task Assignment chart panel
        JPanel assignmentPanel = new JPanel(new BorderLayout());
        assignmentPanel.setBorder(BorderFactory.createTitledBorder("Tasks Assigned To"));
        assignmentPanel.setBackground(backgroundColor);
        assignmentPanel.add(new AssignmentChartPanel(), BorderLayout.CENTER);
        
        chartsPanel.add(piePanel);
        chartsPanel.add(assignmentPanel);
        
        // Stats panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        statsPanel.setBackground(backgroundColor);
        
        statsPanel.add(createStatCard("Total Tasks", totalTasks, new Color(52, 152, 219)));
        statsPanel.add(createStatCard("Completed", completedTasks, new Color(46, 204, 113)));
        statsPanel.add(createStatCard("In Progress", inProgressTasks, new Color(230, 126, 34)));
        statsPanel.add(createStatCard("Overdue", overdueTasks, new Color(231, 76, 60)));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(backgroundColor);
        
        buttonPanel.add(createButton("View Tasks", e -> openTaskViewer()));
        buttonPanel.add(createButton("Create Task", e -> createNewTask()));
        buttonPanel.add(createButton("Edit Task", e -> editTask()));
        buttonPanel.add(createButton("Delete Task", e -> deleteTask()));
        buttonPanel.add(createButton("Logout", e -> logout()));
        buttonPanel.add(createButton("Add Employee", e -> NewEmployeeForm()));
        
        // Add all panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(chartsPanel, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.SOUTH);
        mainPanel.add(buttonPanel, BorderLayout.PAGE_END);
        
        setContentPane(mainPanel);
        setVisible(true);
    }
    
    private void loadDataFromDatabase() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            // Establish database connection
            conn = DatabaseConnection.getConnection();
            
            // Get task status data
            String statusQuery = "SELECT status, COUNT(*) as count FROM tasks GROUP BY status";
            pstmt = conn.prepareStatement(statusQuery);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                taskStatusData.put(status, count);
                
                // Update stats counters
                totalTasks += count;
                if ("Completed".equals(status)) {
                    completedTasks = count;
                } else if ("In Progress".equals(status)) {
                    inProgressTasks = count;
                } else if ("Overdue".equals(status)) {
                    overdueTasks = count;
                }
            }
            
            // Close and reset resources
            rs.close();
            pstmt.close();
            
            // Get task assignment data
            String assignmentQuery = 
                "SELECT u.username, COUNT(*) as count " +
                "FROM tasks t JOIN users u ON t.assigned_to = u.user_id " +
                "GROUP BY u.username ORDER BY count DESC LIMIT 5";
            
            pstmt = conn.prepareStatement(assignmentQuery);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String username = rs.getString("username");
                int count = rs.getInt("count");
                assignmentData.put(username, count);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading dashboard data: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            
            // If database fails, use sample data
            loadSampleData();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        // If data is empty, load sample data for showing purpose
        if (taskStatusData.isEmpty() || assignmentData.isEmpty()) {
            loadSampleData();
        }
    }
    
    private void loadSampleData() {
        // Sample task status data
        taskStatusData.clear();
        taskStatusData.put("Completed", 12);
        taskStatusData.put("In Progress", 8);
        taskStatusData.put("Pending", 5);
        taskStatusData.put("Overdue", 3);
        
        // Sample assignment data
        assignmentData.clear();
        assignmentData.put("John", 7);
        assignmentData.put("Sarah", 6);
        assignmentData.put("Mike", 4);
        assignmentData.put("Emma", 5);
        assignmentData.put("Tom", 6);
        
        // Update stats
        totalTasks = 28;
        completedTasks = 12;
        inProgressTasks = 8;
        overdueTasks = 3;
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
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(accentColor.darker()); }
            public void mouseExited(MouseEvent e) { button.setBackground(accentColor); }
        });
        
        return button;
    }
    
    private JPanel createStatCard(String title, int value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(color, 2));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(color);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel valueLabel = new JLabel(String.valueOf(value), SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Chart panel classes
    class PieChartPanel extends JPanel {
        public PieChartPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(200, 200));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            int radius = Math.min(width, height) / 3;
            int centerX = width / 2;
            int centerY = height / 2;
            
            // Calculate total
            int total = taskStatusData.values().stream().mapToInt(Integer::intValue).sum();
            if (total == 0) return;
            
            // Draw pie slices
            double angle = 0;
            int i = 0;
            for (Map.Entry<String, Integer> entry : taskStatusData.entrySet()) {
                double arcAngle = 360.0 * entry.getValue() / total;
                
                g2d.setColor(pieColors[i % pieColors.length]);
                g2d.fill(new Arc2D.Double(centerX - radius, centerY - radius, 
                                       2 * radius, 2 * radius, angle, arcAngle, Arc2D.PIE));
                
                angle += arcAngle;
                i++;
            }
            
            // Draw legend
            int legendX = 10;
            int legendY = 10;
            int boxSize = 10;
            
            i = 0;
            for (String key : taskStatusData.keySet()) {
                g2d.setColor(pieColors[i % pieColors.length]);
                g2d.fillRect(legendX, legendY, boxSize, boxSize);
                
                g2d.setColor(Color.BLACK);
                String label = key + ": " + taskStatusData.get(key);
                g2d.drawString(label, legendX + boxSize + 5, legendY + boxSize);
                
                legendY += 20;
                i++;
            }
        }
    }
    
    // Task Assignment chart panel
    class AssignmentChartPanel extends JPanel {
        public AssignmentChartPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(200, 200));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            int margin = 30;
            int topMargin = 20;
            int chartWidth = width - 2 * margin;
            int chartHeight = height - margin - topMargin;
            
            // Draw axes
            g2d.setColor(Color.BLACK);
            g2d.drawLine(margin, height - margin, width - margin, height - margin); // X-axis
            g2d.drawLine(margin, height - margin, margin, topMargin); // Y-axis
            
            if (assignmentData.isEmpty()) return;
            
            // Find max value for scaling
            int maxValue = assignmentData.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            int barWidth = chartWidth / assignmentData.size() - 10;
            
            // Draw bars
            int x = margin + 5;
            int i = 0;
            for (Map.Entry<String, Integer> entry : assignmentData.entrySet()) {
                int barHeight = (int)((double)entry.getValue() / maxValue * chartHeight);
                
                g2d.setColor(assignmentColors[i % assignmentColors.length]);
                g2d.fillRect(x, height - margin - barHeight, barWidth, barHeight);
                
                // Draw value at top of bar
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.valueOf(entry.getValue()), 
                               x + barWidth/2 - 5, 
                               height - margin - barHeight - 5);
                
                // Draw username at bottom
                FontMetrics fm = g2d.getFontMetrics();
                String userName = entry.getKey();
                int textWidth = fm.stringWidth(userName);
                
                // If username is too long, truncate it
                if (textWidth > barWidth + 10) {
                    userName = userName.substring(0, 5) + "...";
                    textWidth = fm.stringWidth(userName);
                }
                
                g2d.drawString(userName, 
                               x + barWidth/2 - textWidth/2, 
                               height - margin + 15);
                
                x += barWidth + 10;
                i++;
            }
        }
    }
    
    // Helper methods
    private String getUserName() {
        String name = "User";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement("SELECT username FROM users WHERE user_id = ?");
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                name = rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return name;
    }
    
    private String getRoleName() { 
        String role = "Employee";
        
        switch (roleId) {
            case 1:
                role = "Administrator";
                break;
            case 2:
                role = "Manager";
                break;
            default:
                role = "Employee";
        }
        
        return role;
    }
    
    private void openTaskViewer() { dispose(); new TaskViewerFrame(userId, roleId).setVisible(true); }
    
    private void createNewTask() {
        if (roleId <= 2) { 
            dispose(); 
            new NewTaskForm(userId, roleId); 
        } else { 
            JOptionPane.showMessageDialog(this, "You are not allowed to create tasks."); 
        }
    }
    
    private void editTask() {
    	dispose();
    	new EditTaskForm(userId, roleId);
    }
    
    private void deleteTask() {
        if (roleId <= 2) {
            dispose();
            new DeleteTaskForm(userId, roleId);
        } else {
            JOptionPane.showMessageDialog(this, "You do not have permission to delete tasks.");
        }
    }
    
    private void NewEmployeeForm() {
    	dispose();
    	new NewEmployeeForm(userId,roleId);
    }
    
    private void logout() { dispose(); new AppLogin().setVisible(true); }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashboardFrame(1, 1));
    }
}

// Database connection class
class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/task_management_system";
    private static final String USER = "root";
    private static final String PASSWORD = "19K@ran2001";
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
