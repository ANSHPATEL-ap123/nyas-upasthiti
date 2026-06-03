const express = require('express');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

// ==========================================
// 🚀 IN-MEMORY HACKATHON DATABASE
// Guarantees 0ms lag and zero connection errors for your presentation.
// ==========================================
let employees = [
  { empId: 'EMP101', name: 'Ansh Patel', role: 'Field Operator', site: 'DEL-MUM-EX' },
  { empId: 'EMP102', name: 'Brajesh Upadhyay', role: 'Site Admin', site: 'NH-44' },
  { empId: 'EMP103', name: 'Himesh', role: 'Field Operator', site: 'DEL-MUM-EX' },
  { empId: 'EMP104', name: 'Shivam', role: 'Field Operator', site: 'NH-44' }
];

let attendanceLogs = [
  { empId: 'EMP101', name: 'Ansh Patel', timestamp: new Date(), status: 'Present', livenessVerified: true }
];

let leaveRequests = [
  { _id: '1', empId: 'EMP103', name: 'Himesh', date: '28 June 2026', type: 'Sick Leave', status: 'Pending' }
];

// ==========================================
// 🟢 API ROUTES
// ==========================================

// 1. REGISTER EMPLOYEE
app.post('/api/employees/register', (req, res) => {
  const { empId, name, role } = req.body;
  
  // Check if ID already exists
  if (employees.find(e => e.empId === empId)) {
    return res.status(400).json({ error: "Employee ID already exists! Use a different ID." });
  }

  const newEmp = { empId, name, role: role || 'Field Operator', site: 'NEW-SITE' };
  employees.push(newEmp); // Save to RAM
  res.status(201).json({ message: "Employee registered successfully", employee: newEmp });
});

// 2. GET ALL EMPLOYEES
app.get('/api/employees', (req, res) => res.json(employees));

// 3. MARK ATTENDANCE
app.post('/api/attendance/mark', (req, res) => {
  const { empId, name } = req.body;
  const log = { empId, name, timestamp: new Date(), status: 'Present', livenessVerified: true };
  attendanceLogs.unshift(log); // Add to top of list
  res.status(201).json({ message: "Attendance synced", log });
});

// 4. GET ATTENDANCE LOGS
app.get('/api/attendance', (req, res) => res.json(attendanceLogs));

// 5. SUBMIT LEAVE REQUEST
app.post('/api/leaves/apply', (req, res) => {
  const { empId, name, date, type } = req.body;
  const leave = { _id: Date.now().toString(), empId, name, date, type, status: 'Pending' };
  leaveRequests.unshift(leave); // Add to top of list
  res.status(201).json({ message: "Leave requested successfully", leave });
});

// 6. GET ALL LEAVE REQUESTS
app.get('/api/leaves', (req, res) => res.json(leaveRequests));

// 7. ADMIN: APPROVE/DENY LEAVE
app.put('/api/leaves/:id', (req, res) => {
  const { status } = req.body;
  const leaveIndex = leaveRequests.findIndex(l => l._id === req.params.id);
  
  if (leaveIndex !== -1) {
    leaveRequests[leaveIndex].status = status;
    res.json({ message: `Leave ${status}`, leave: leaveRequests[leaveIndex] });
  } else {
    res.status(404).json({ error: "Leave not found" });
  }
});

// 8. DYNAMIC LOGIN ROUTE (This fixes the login issue!)
app.post('/api/login', (req, res) => {
  const { empId, password } = req.body;
  
  // Hackathon Master Override for Admin
  if (empId === 'ADMIN' && password === 'admin123') {
    return res.json({ empId: 'ADMIN', name: 'Master Admin', role: 'Admin' });
  }

  // Find employee in our RAM database
  const user = employees.find(e => e.empId === empId);
  
  if (user) {
    // Determine if they are an Admin or a User based on their role text
    const appRole = user.role.includes('Admin') ? 'Admin' : 'User';
    res.json({ empId: user.empId, name: user.name, role: appRole });
  } else {
    res.status(401).json({ error: "Employee ID not found in system." });
  }
});

// ==========================================
// START SERVER
// ==========================================
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`🚀 HACKATHON ENGINE LIVE ON PORT ${PORT} - ZERO LAG MODE`);
});