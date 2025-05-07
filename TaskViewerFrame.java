import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class TaskViewerFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private int userId, roleId;
    private JPanel filterPanel;
    private JComboBox<String> statusFilter;
    private JComboBox<String> departmentFilter;
    private JTextField fromDateField, toDateField;
    private JTextField searchField;
    private JButton refreshButton, clearFilterButton, applyFilterButton, searchButton, back;
    private JLabel statusLabel, resultCountLabel;
    private ArrayList<String> departments = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    // Define colors - less saturated for better contrast
    private Color headerColor = new Color(70, 100, 170);
    private Color buttonColor = new Color(90, 120, 180);
    private Color backgroundColor = new Color(245, 245, 250);
    
    public TaskViewerFrame(int userId, int roleId) {
        this.userId = userId;
        this.roleId = roleId;
        
        // Frame setup
        setTitle("Task Management System");
        setSize(1100, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main layout
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        
        // Try to set the look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create filter panel
        filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.WEST);
        
        // Create table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
        
        // Load data
        loadDepartments();
        loadDataFromDatabase();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        headerPanel.setBackground(headerColor);
        
        JLabel titleLabel = new JLabel("📋 Task Dashboard", SwingConstants.LEFT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.black);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        searchField = new JTextField(20);
        searchField.setToolTipText("Search tasks...");
        searchButton = new JButton("Search");
        searchButton.setBackground(buttonColor);
        searchButton.setForeground(Color.black);
        searchButton.setFocusPainted(false);
        
        searchButton.addActionListener(e -> applyFilters());
        searchField.addActionListener(e -> applyFilters());
        
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.setPreferredSize(new Dimension(220, getHeight()));
        panel.setBackground(backgroundColor);
        
        JLabel filterTitle = new JLabel("Filters");
        filterTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        filterTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(filterTitle);
        panel.add(Box.createVerticalStrut(15));
        
        // Status filter
        JLabel statusFilterLabel = new JLabel("Status:");
        statusFilterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(statusFilterLabel);
        panel.add(Box.createVerticalStrut(5));
        
        statusFilter = new JComboBox<>(new String[]{"All", "Pending", "In Progress", "Completed", "Delayed"});
        statusFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusFilter.setMaximumSize(new Dimension(200, 30));
        panel.add(statusFilter);
        panel.add(Box.createVerticalStrut(15));
        
        // Department filter
        JLabel deptFilterLabel = new JLabel("Department:");
        deptFilterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(deptFilterLabel);
        panel.add(Box.createVerticalStrut(5));
        
        departmentFilter = new JComboBox<>(new String[]{"All"});
        departmentFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        departmentFilter.setMaximumSize(new Dimension(200, 30));
        panel.add(departmentFilter);
        panel.add(Box.createVerticalStrut(15));
        
        // Date range filter
        JLabel dateFilterLabel = new JLabel("Due Date Range (yyyy-MM-dd):");
        dateFilterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(dateFilterLabel);
        panel.add(Box.createVerticalStrut(5));
        
        // From date field
        JLabel fromLabel = new JLabel("From:");
        fromLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(fromLabel);
        panel.add(Box.createVerticalStrut(3));
        
        fromDateField = new JTextField();
        fromDateField.setAlignmentX(Component.LEFT_ALIGNMENT);
        fromDateField.setMaximumSize(new Dimension(200, 30));
        fromDateField.setToolTipText("Format: yyyy-MM-dd");
        panel.add(fromDateField);
        panel.add(Box.createVerticalStrut(5));
        
        // To date field
        JLabel toLabel = new JLabel("To:");
        toLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(toLabel);
        panel.add(Box.createVerticalStrut(3));
        
        toDateField = new JTextField();
        toDateField.setAlignmentX(Component.LEFT_ALIGNMENT);
        toDateField.setMaximumSize(new Dimension(200, 30));
        toDateField.setToolTipText("Format: yyyy-MM-dd");
        panel.add(toDateField);
        panel.add(Box.createVerticalStrut(20));
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(200, 35));
        
        // Apply filter button
        applyFilterButton = new JButton("Apply");
        applyFilterButton.setBackground(buttonColor);
        applyFilterButton.setForeground(Color.black);
        applyFilterButton.setFocusPainted(false);
        applyFilterButton.addActionListener(e -> applyFilters());
        
        // Clear filter button
        clearFilterButton = new JButton("Clear");
        clearFilterButton.setBackground(buttonColor);
        clearFilterButton.setForeground(Color.black);
        clearFilterButton.setFocusPainted(false);
        clearFilterButton.addActionListener(e -> clearFilters());
        
        buttonPanel.add(applyFilterButton);
        buttonPanel.add(clearFilterButton);
        
        panel.add(buttonPanel);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        panel.setBackground(Color.WHITE);
        
        // Create table model with non-editable cells
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Define columns
        model.addColumn("ID");
        model.addColumn("Task Name");
        model.addColumn("Description");
        model.addColumn("Assigned To");
        model.addColumn("Assigned By");
        model.addColumn("Department");
        model.addColumn("Status");
        model.addColumn("Due Date");
        
        // Create table
        table = new JTable(model);
        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(10, 5));
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setFillsViewportHeight(true);
        
        // Custom cell renderer for status column
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);
        
        // Style the table header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(headerColor);
        header.setForeground(Color.black);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.GRAY));
        
        // Row striping
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 250));
                }
                setBorder(new EmptyBorder(5, 5, 5, 5));
                return c;
            }
        });
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusBar.setBackground(backgroundColor);
        
        statusLabel = new JLabel("Ready ");
        resultCountLabel = new JLabel(" 0 tasks found");
        
        refreshButton = new JButton("Refresh Data");
        refreshButton.setBackground(buttonColor);
        refreshButton.setForeground(Color.black);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> loadDataFromDatabase());
        
        back = new JButton("Back To Dashboard");
        back.setBackground(buttonColor);
        back.setForeground(Color.black);
        back.setFocusPainted(false);
        back.addActionListener(e -> { 
        	dispose(); // close current frame
        	new DashboardFrame(userId, roleId).setVisible(true);
        	}); // open Dashboard);
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(resultCountLabel, BorderLayout.CENTER);
        statusBar.add(refreshButton, BorderLayout.EAST);
        statusBar.add(back, BorderLayout.SOUTH);
        
        return statusBar;
    }
    
   
    
    private void loadDepartments() {
        departments.clear();
        departments.add("All");
        departmentFilter.removeAllItems();
        departmentFilter.addItem("All");
        
        try {
            Connection conn = getConnection();
            String query = "SELECT DISTINCT department_name FROM departments";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                String dept = rs.getString("department_name");
                departments.add(dept);
                departmentFilter.addItem(dept);
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading departments: " + e.getMessage());
        }
    }
    
    private void loadDataFromDatabase() {
        // Clear table
        model.setRowCount(0);
        statusLabel.setText("Loading data...");
        
        try {
            Connection conn = getConnection();
            StringBuilder queryBuilder = new StringBuilder();
            
            queryBuilder.append("""
                SELECT 
                    t.task_id,
                    t.task_name,
                    t.description,
                    u1.username AS assigned_to,
                    u2.username AS assigned_by,
                    d.department_name AS department,
                    t.status,
                    t.due_date
                FROM tasks t
                JOIN users u1 ON t.assigned_to = u1.user_id
                JOIN users u2 ON t.assigned_by = u2.user_id
                JOIN departments d ON t.department_id = d.department_id
            """);
            
            // Add filtering for non-boss/manager users
            if (roleId != 1 && roleId != 2) {
                queryBuilder.append(" WHERE t.assigned_to = ?");
            }
            
            PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());
            
            // Bind user ID if needed
            if (roleId != 1 && roleId != 2) {
                stmt.setInt(1, userId);
            }
            
            ResultSet rs = stmt.executeQuery();
            int rowCount = 0;
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("task_id"),
                    rs.getString("task_name"),
                    rs.getString("description"),
                    rs.getString("assigned_to"),
                    rs.getString("assigned_by"),
                    rs.getString("department"),
                    rs.getString("status"),
                    rs.getDate("due_date")
                });
                rowCount++;
            }
            
            resultCountLabel.setText(rowCount + " task" + (rowCount == 1 ? "" : "s") + " found");
            statusLabel.setText("Ready");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error");
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + e.getMessage());
        }
    }
    
    private void applyFilters() {
        statusLabel.setText("Applying filters...");
        
        try {
            Connection conn = getConnection();
            StringBuilder queryBuilder = new StringBuilder();
            ArrayList<Object> params = new ArrayList<>();
            
            queryBuilder.append("""
                SELECT 
                    t.task_id,
                    t.task_name,
                    t.description,
                    u1.username AS assigned_to,
                    u2.username AS assigned_by,
                    d.department_name AS department,
                    t.status,
                    t.due_date
                FROM tasks t
                JOIN users u1 ON t.assigned_to = u1.user_id
                JOIN users u2 ON t.assigned_by = u2.user_id
                JOIN departments d ON t.department_id = d.department_id
                WHERE 1=1
            """);
            
            // Restrict view for regular employees
            if (roleId != 1 && roleId != 2) {
                queryBuilder.append(" AND t.assigned_to = ?");
                params.add(userId);
            }
            
            // Status filter
            String selectedStatus = (String) statusFilter.getSelectedItem();
            if (selectedStatus != null && !"All".equals(selectedStatus)) {
                queryBuilder.append(" AND t.status = ?");
                params.add(selectedStatus);
            }
            
            // Department filter
            String selectedDept = (String) departmentFilter.getSelectedItem();
            if (selectedDept != null && !"All".equals(selectedDept)) {
                queryBuilder.append(" AND d.department_name = ?");
                params.add(selectedDept);
            }
            
            // Date range filter - from date
            String fromDateStr = fromDateField.getText().trim();
            if (!fromDateStr.isEmpty()) {
                try {
                    Date fromDate = dateFormat.parse(fromDateStr);
                    queryBuilder.append(" AND t.due_date >= ?");
                    params.add(new java.sql.Date(fromDate.getTime()));
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(this, 
                            "Invalid from date format. Please use yyyy-MM-dd", 
                            "Format Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Date range filter - to date
            String toDateStr = toDateField.getText().trim();
            if (!toDateStr.isEmpty()) {
                try {
                    Date toDate = dateFormat.parse(toDateStr);
                    queryBuilder.append(" AND t.due_date <= ?");
                    params.add(new java.sql.Date(toDate.getTime()));
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(this, 
                            "Invalid to date format. Please use yyyy-MM-dd", 
                            "Format Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Search filter
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                queryBuilder.append(" AND (t.task_name LIKE ? OR t.description LIKE ?)");
                params.add("%" + searchText + "%");
                params.add("%" + searchText + "%");
            }
            
            PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());
            
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            // Execute query and update table
            ResultSet rs = stmt.executeQuery();
            model.setRowCount(0);
            int rowCount = 0;
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("task_id"),
                    rs.getString("task_name"),
                    rs.getString("description"),
                    rs.getString("assigned_to"),
                    rs.getString("assigned_by"),
                    rs.getString("department"),
                    rs.getString("status"),
                    rs.getDate("due_date")
                });
                rowCount++;
            }
            
            resultCountLabel.setText(rowCount + " task" + (rowCount == 1 ? "" : "s") + " found");
            statusLabel.setText("Filters applied");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error");
            JOptionPane.showMessageDialog(this, "Error applying filters: " + e.getMessage());
        }
    }
    
    private void clearFilters() {
        statusFilter.setSelectedItem("All");
        departmentFilter.setSelectedItem("All");
        fromDateField.setText("");
        toDateField.setText("");
        searchField.setText("");
        loadDataFromDatabase();
        statusLabel.setText("Filters cleared");
    }
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/task_management_system", "root", "19K@ran2001");
    }
    
    // Custom renderer for status column
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String status = value.toString();
                label.setOpaque(true);
                
                switch (status) {
                    case "Pending":
                        label.setBackground(isSelected ? table.getSelectionBackground() : new Color(255, 240, 200));
                        label.setForeground(new Color(150, 90, 0));
                        break;
                    case "In Progress":
                        label.setBackground(isSelected ? table.getSelectionBackground() : new Color(200, 230, 255));
                        label.setForeground(new Color(0, 90, 150));
                        break;
                    case "Completed":
                        label.setBackground(isSelected ? table.getSelectionBackground() : new Color(220, 255, 220));
                        label.setForeground(new Color(0, 120, 0));
                        break;
                    case "Delayed":
                        label.setBackground(isSelected ? table.getSelectionBackground() : new Color(255, 220, 220));
                        label.setForeground(new Color(150, 0, 0));
                        break;
                    default:
                        label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                        label.setForeground(table.getForeground());
                }
            }
            
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            return label;
        }
    }
    
    public static void main(String[] args) {
        // For testing, pass your actual logged-in user's ID and role
        int userId = 1;  // example
        int roleId = 1;  // example (1=Admin, 2=Manager, 3=Employee)
        
        SwingUtilities.invokeLater(() -> {
        	TaskViewerFrame frame = new TaskViewerFrame(userId, roleId);
            frame.setVisible(true);
        });
    }
}