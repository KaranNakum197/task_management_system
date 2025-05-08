# 🗂️ Task Management System (Java + MySQL)

A Java-based desktop application for managing tasks within an organization using **AWT/Swing** for the GUI and **MySQL** for backend data storage. The system supports role-based access control, real-time task editing, and team collaboration features.

---

## 🚀 Features

- 🔐 **Role-Based Access**:
  - **Boss/Manager**: Can assign, update, and view all tasks.
  - **Team Leader/Employee**: Can view and update only their assigned tasks.

- 📋 **Task Management**:
  - Create, view, update, and delete tasks
  - Assign tasks to specific users based on role
  - Live editable `JTable` connected to MySQL database

- 🔍 **Filtering and Search**:
  - Search tasks by name, status, project, or department

- ⏰ **Deadline Alerts**:
  - Visual alerts for overdue tasks

- ✅ **Validation Rules**:
  - Managers can’t assign tasks to themselves
  - Auto-fill "Assigned By" based on logged-in user

---

## 🛠️ Tech Stack

- **Java SE (AWT/Swing)**
- **MySQL**
- **JDBC (Java Database Connectivity)**

---

## 🗃️ Database Structure

- **Tables**:
  - `users (user_id, name, role_id)`
  - `roles (role_id, role_name)`
  - `tasks (task_id, name, description, assigned_to, assigned_by, status, deadline)`

- **Procedures/Triggers/Functions**:
  - Example MySQL stored procedure: `createTask(...)`
  - Example trigger: `prevent_self_assignment`

> Full SQL dump included in the `/database` folder.

---

## 🖥️ How to Run

1. Clone this repository:
   ```bash
   git clone https://github.com/your-username/task-management-system.git
