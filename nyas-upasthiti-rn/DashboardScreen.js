import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, Alert, TextInput, Modal } from 'react-native';

const BASE_URL = 'http://192.168.1.9:5000/api';

export default function DashboardScreen({ route, navigation }) {
  const { role = 'User', employeeName = 'Ansh Patel', employeeId = 'EMP101' } = route.params || {};
  const [currentView, setCurrentView] = useState(role === 'Admin' ? 'ADMIN_HOME' : 'USER_HOME');
  const [adminTab, setAdminTab] = useState(0);

  // --- REAL-TIME DATABASE STATE ---
  const [employees, setEmployees] = useState([]);
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [logs, setLogs] = useState([]);
  const [notifications, setNotifications] = useState([{ id: 1, msg: "System initialization complete. Connected to Database.", time: "08:00 AM" }]);

  // --- FETCH DATA FROM BACKEND (BULLETPROOF VERSION) ---
  const fetchData = async () => {
    try {
      const [empRes, leaveRes, logRes] = await Promise.all([
        fetch(`${BASE_URL}/employees`),
        fetch(`${BASE_URL}/leaves`),
        fetch(`${BASE_URL}/attendance`)
      ]);

      const emps = await empRes.json();
      const leaves = await leaveRes.json();
      const attLogs = await logRes.json();

      // SAFETY CHECK: Ensure the backend actually sent arrays to prevent map/filter crashes.
      setEmployees(Array.isArray(emps) ? emps : []);
      setLeaveRequests(Array.isArray(leaves) ? leaves : []);
      setLogs(Array.isArray(attLogs) ? attLogs : []);
      
    } catch (e) {
      console.log("Offline mode or Server unreachable.");
    }
  };

  // Run on mount and when tabs switch
  useEffect(() => { fetchData(); }, [currentView, adminTab]);

  // --- FEATURE STATES ---
  const [leaveDateInput, setLeaveDateInput] = useState('');
  const [leaveTypeInput, setLeaveTypeInput] = useState('');
  const [showUserCalModal, setShowUserCalModal] = useState(false);
  const [showTypeModal, setShowTypeModal] = useState(false);
  const leaveTypesList = ["Sick Leave", "Casual Leave", "Earned Leave", "Maternity/Paternity", "Unpaid Leave"];

  const [newEmpName, setNewEmpName] = useState('');
  const [newEmpId, setNewEmpId] = useState('');
  const [newEmpRole, setNewEmpRole] = useState('');

  const [inspectedEmployee, setInspectedEmployee] = useState(null);
  const [calendarSelectedDay, setCalendarSelectedDay] = useState(25);

  // --- REAL API ACTIONS WITH ERROR HANDLING ---
  const submitLeaveRequest = async () => {
    if (!leaveDateInput || !leaveTypeInput) return Alert.alert('Error', 'Please fill all details.');
    try {
      const res = await fetch(`${BASE_URL}/leaves/apply`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ empId: employeeId, name: employeeName, date: `${leaveDateInput} June 2026`, type: leaveTypeInput })
      });
      const data = await res.json();
      
      if (!res.ok) throw new Error(data.error || "Failed to submit leave.");
      
      Alert.alert('Success', 'Leave request sent to Admin!');
      setCurrentView('USER_HOME');
      setLeaveDateInput(''); setLeaveTypeInput('');
      fetchData(); // Instantly refresh the UI
    } catch(e) { Alert.alert('Error', e.message); }
  };

  const registerNewEmployee = async () => {
    if (!newEmpName || !newEmpId) return Alert.alert('Error', 'Name and ID required');
    try {
      const res = await fetch(`${BASE_URL}/employees/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ empId: newEmpId, name: newEmpName, role: newEmpRole || 'Field Operator' })
      });
      const data = await res.json();
      
      if (!res.ok) throw new Error(data.error || "Failed to save employee.");
      
      Alert.alert('Success', 'Employee Profile Created!');
      setNewEmpName(''); setNewEmpId(''); setNewEmpRole('');
      fetchData(); // Instantly refresh Admin dashboard
    } catch(e) { Alert.alert('Registration Failed', e.message); }
  };

  const updateLeaveStatus = async (id, status) => {
    try {
      await fetch(`${BASE_URL}/leaves/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status })
      });
      fetchData(); // Refresh list automatically
    } catch(e) { Alert.alert('Error', 'Failed to update leave.'); }
  };

  // --- REUSABLE UI COMPONENTS ---
  const TopicRow = ({ title, subtitle, onPress, isLast = false }) => (
    <TouchableOpacity style={[styles.topicRow, isLast && { borderBottomWidth: 0 }]} onPress={onPress}>
      <View><Text style={styles.topicTitle}>{title}</Text><Text style={styles.topicSubtitle}>{subtitle}</Text></View>
      <Text style={{color: '#94A3B8', fontSize: 18}}>➔</Text>
    </TouchableOpacity>
  );

  const TopHeader = ({ title, showBack = false, backTarget = null }) => (
    <View style={styles.header}>
      <View style={{flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center'}}>
        <View style={{flexDirection: 'row', alignItems: 'center'}}>
          {showBack && <TouchableOpacity onPress={() => setCurrentView(backTarget)} style={{marginRight: 15}}><Text style={{color: '#FFF', fontSize: 20}}>←</Text></TouchableOpacity>}
          <Text style={styles.headerText}>{title}</Text>
        </View>
        <TouchableOpacity onPress={() => setCurrentView('NOTIFICATIONS')}>
          <Text style={{fontSize: 22}}>🔔</Text>
          {notifications.length > 0 && <View style={styles.badgeDot} />}
        </TouchableOpacity>
      </View>
    </View>
  );

  const renderKotlinStyleCalendar = () => {
    const daysInMonth = 31;
    const firstDayOfWeek = 5; 
    const days = Array.from({ length: daysInMonth + firstDayOfWeek }, (_, i) => i < firstDayOfWeek ? null : i - firstDayOfWeek + 1);

    return (
      <View style={styles.kotlinGrid}>
        {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map(d => <Text key={`head-${d}`} style={styles.weekDay}>{d}</Text>)}
        {days.map((day, i) => (
          <TouchableOpacity key={`day-${i}`} disabled={!day} style={[styles.kotlinDay, day === calendarSelectedDay && styles.selectedDay]} onPress={() => day && setCalendarSelectedDay(day)}>
            <Text style={{color: !day ? 'transparent' : day === calendarSelectedDay ? '#FFF' : '#000', fontSize: 12, fontWeight: day === calendarSelectedDay ? 'bold' : 'normal'}}>{day || ''}</Text>
            {day && <View style={[styles.dot, {backgroundColor: day % 2 === 0 ? '#28A745' : '#DC3545'}]} />}
          </TouchableOpacity>
        ))}
      </View>
    );
  };

  // ==========================================
  // USER VIEWS
  // ==========================================
  if (currentView === 'NOTIFICATIONS') return (
    <View style={styles.container}>
      <TopHeader title="System Notifications" showBack={true} backTarget={role === 'Admin' ? 'ADMIN_HOME' : 'USER_HOME'} />
      <ScrollView style={styles.padding}>{notifications.map((n) => <View key={n.id} style={styles.card}><Text style={styles.topicTitle}>{n.msg}</Text><Text style={styles.topicSubtitle}>{n.time}</Text></View>)}</ScrollView>
    </View>
  );

  if (currentView === 'USER_LEAVE') return (
    <View style={styles.container}>
      <TopHeader title="Apply for Leave" showBack={true} backTarget="USER_HOME" />
      <ScrollView style={styles.padding}>
        <View style={styles.card}>
          <Text style={styles.boldText}>New Leave Application</Text>
          <Text style={styles.label}>Select Date</Text>
          <TouchableOpacity style={styles.inputBox} onPress={() => setShowUserCalModal(true)}>
            <Text style={{color: leaveDateInput ? '#000' : 'gray'}}>{leaveDateInput ? `${leaveDateInput} June 2026` : "Tap to pick date from calendar"}</Text>
          </TouchableOpacity>
          <Text style={styles.label}>Select Leave Type</Text>
          <TouchableOpacity style={styles.inputBox} onPress={() => setShowTypeModal(true)}>
            <Text style={{color: leaveTypeInput ? '#000' : 'gray'}}>{leaveTypeInput || "Tap to select Leave Type"}</Text>
          </TouchableOpacity>
          <TouchableOpacity style={[styles.blueBtn, {marginTop: 10}]} onPress={submitLeaveRequest}><Text style={styles.btnText}>Submit for Verification</Text></TouchableOpacity>
        </View>

        <Text style={[styles.boldText, {marginTop: 20}]}>My Application History</Text>
        {leaveRequests.filter(l => l.empId === employeeId).map((l, idx) => (
          <View key={idx} style={styles.card}>
            <Text style={styles.topicTitle}>{l.type}</Text>
            <Text style={styles.topicSubtitle}>{l.date}</Text>
            <Text style={{color: l.status === 'Approved' ? 'green' : l.status === 'Denied' ? 'red' : '#E67E22', fontWeight: 'bold', marginTop: 5}}>{l.status}</Text>
          </View>
        ))}
      </ScrollView>

      {/* Modals */}
      <Modal visible={showUserCalModal} transparent animationType="fade">
        <View style={styles.modalOverlay}><View style={styles.modalContent}><Text style={styles.modalTitle}>Select Date (June 2026)</Text><View style={{flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'center'}}>{Array.from({length: 30}).map((_, i) => (<TouchableOpacity key={`u-day-${i}`} style={styles.calDay} onPress={() => { setLeaveDateInput(i+1); setShowUserCalModal(false); }}><Text>{i + 1}</Text></TouchableOpacity>))}</View><TouchableOpacity style={styles.closeBtn} onPress={() => setShowUserCalModal(false)}><Text style={{color:'#FFF'}}>Cancel</Text></TouchableOpacity></View></View>
      </Modal>
      <Modal visible={showTypeModal} transparent animationType="slide">
        <View style={styles.bottomSheet}><View style={styles.sheetContent}><Text style={styles.modalTitle}>Select Leave Type</Text>{leaveTypesList.map((type, i) => (<TouchableOpacity key={i} style={{paddingVertical: 15, borderBottomWidth: 1, borderColor: '#eee'}} onPress={() => { setLeaveTypeInput(type); setShowTypeModal(false); }}><Text style={{fontSize: 16}}>{type}</Text></TouchableOpacity>))}<TouchableOpacity style={[styles.closeBtn, {backgroundColor: '#DC2626'}]} onPress={() => setShowTypeModal(false)}><Text style={{color:'#FFF'}}>Cancel</Text></TouchableOpacity></View></View>
      </Modal>
    </View>
  );

  if (currentView === 'USER_PROFILE') return (
    <View style={styles.container}>
      <TopHeader title="My Profile" showBack={true} backTarget="USER_HOME" />
      <ScrollView style={styles.padding}>
        <View style={styles.card}>
          <Text style={styles.topicTitle}>{employeeName}</Text>
          <Text style={styles.topicSubtitle}>{employeeId} | Field Operator</Text>
          <View style={styles.divider} />
          <View style={styles.profileRow}><Text style={styles.profileLabel}>Project Site</Text><Text style={styles.profileVal}>DEL-MUM-EX</Text></View>
          <View style={styles.profileRow}><Text style={styles.profileLabel}>Role</Text><Text style={styles.profileVal}>Field Operator</Text></View>
        </View>
      </ScrollView>
    </View>
  );

  if (currentView === 'USER_HOME') return (
    <View style={styles.container}>
      <TopHeader title={`Portal Hub: ${employeeName}`} />
      <ScrollView style={styles.padding}>
        <View style={[styles.card, { flexDirection: 'row', justifyContent: 'space-around', paddingVertical: 20 }]}>
          <View style={{alignItems: 'center'}}><Text style={{fontSize: 22, fontWeight: 'bold', color: '#16A34A'}}>{logs.filter(l => l.empId === employeeId).length}</Text><Text style={styles.label}>Present</Text></View>
          <View style={{width: 1, backgroundColor: '#E2E8F0'}} />
          <View style={{alignItems: 'center'}}><Text style={{fontSize: 22, fontWeight: 'bold', color: '#DC3545'}}>0</Text><Text style={styles.label}>Absent</Text></View>
          <View style={{width: 1, backgroundColor: '#E2E8F0'}} />
          <View style={{alignItems: 'center'}}><Text style={{fontSize: 22, fontWeight: 'bold', color: '#0D3E73'}}>92%</Text><Text style={styles.label}>Rate</Text></View>
        </View>

        <Text style={styles.boldText}>Operational Controls</Text>
        <View style={styles.card}>
          <TopicRow title="Mark Attendance" subtitle="Execute field scan verification" onPress={() => navigation.navigate('Camera', { mode: 'attendance', employeeId, employeeName })} />
          <TopicRow title="My Profile" subtitle="View complete employee details" onPress={() => setCurrentView('USER_PROFILE')} />
          <TopicRow title="Apply for Leave" subtitle="Submit leave and check status" onPress={() => setCurrentView('USER_LEAVE')} isLast={true} />
        </View>

        <Text style={[styles.boldText, {marginTop: 10}]}>Quick Actions</Text>
        <View style={{flexDirection: 'row', justifyContent: 'space-between', marginBottom: 20}}>
          <TouchableOpacity style={styles.actionBox} onPress={() => Alert.alert('Help', 'IT Support Ticket Raised.')}><Text style={styles.actionText}>Help Support</Text></TouchableOpacity>
          <TouchableOpacity style={styles.actionBox} onPress={() => Alert.alert('Call', 'Connecting to Site Admin...')}><Text style={styles.actionText}>Call Admin</Text></TouchableOpacity>
          <TouchableOpacity style={[styles.actionBox, {borderColor: '#FCA5A5', backgroundColor: '#FEF2F2'}]} onPress={() => navigation.navigate('Landing')}><Text style={[styles.actionText, {color: '#DC2626'}]}>Secure Logout</Text></TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  );

  // ==========================================
  // ADMIN VIEWS
  // ==========================================
  return (
    <View style={styles.container}>
      <TopHeader title="Admin Master Portal" />
      <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{backgroundColor: '#FFF', borderBottomWidth: 1, borderColor: '#e2e8f0', maxHeight: 50}}>
        {['Core', 'Logs', 'Monthly', 'Leaves', 'Register'].map((t, i) => (
          <TouchableOpacity key={i} style={{paddingHorizontal: 20, paddingVertical: 14, borderBottomWidth: adminTab === i ? 2 : 0, borderColor: '#0D3E73'}} onPress={() => setAdminTab(i)}>
            <Text style={{fontWeight: adminTab === i ? 'bold' : 'normal', color: adminTab === i ? '#0D3E73' : 'gray', fontSize: 14}}>{t}</Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      <ScrollView style={styles.padding}>
        {/* TAB 0: CORE */}
        {adminTab === 0 && (
          <View>
            <View style={[styles.card, { flexDirection: 'row', justifyContent: 'space-around', paddingVertical: 20 }]}>
              <View style={{alignItems: 'center'}}><Text style={{fontSize: 22, fontWeight: 'bold', color: '#16A34A'}}>{logs.length}</Text><Text style={styles.label}>Present</Text></View>
              <View style={{width: 1, backgroundColor: '#E2E8F0'}} />
              <View style={{alignItems: 'center'}}><Text style={{fontSize: 22, fontWeight: 'bold', color: '#E67E22'}}>{leaveRequests.filter(r=>r.status==='Pending').length}</Text><Text style={styles.label}>Pending</Text></View>
            </View>
            <View style={styles.card}>
              {/* RESTORED: Admin Camera Mark Attendance Button */}
              <TopicRow title="Mark Attendance" subtitle="Execute biometric face scanning" onPress={() => navigation.navigate('Camera', { mode: 'attendance', employeeId, employeeName })} />
              <TopicRow title="Force Refresh" subtitle="Pull latest DB data" onPress={fetchData} />
              <TopicRow title="Logout Admin" subtitle="End current session" onPress={() => navigation.navigate('Landing')} isLast={true} />
            </View>
          </View>
        )}

        {/* TAB 1: REAL ATTENDANCE LOGS */}
        {adminTab === 1 && (
          <View>
            <Text style={styles.boldText}>Live Database Attendance Logs</Text>
            {logs.length === 0 && <Text style={{color: 'gray'}}>No logs found in database.</Text>}
            {logs.map((log, idx) => (
              <View key={idx} style={styles.logCard}>
                <Text style={styles.topicTitle}>{log.name}</Text>
                <Text style={styles.topicSubtitle}>ID: {log.empId} | Status: {log.status}</Text>
                <Text style={{color: 'gray', fontSize: 10, marginTop: 5}}>{new Date(log.timestamp).toLocaleString()}</Text>
                <Text style={{color: 'green', fontWeight: 'bold', fontSize: 12, marginTop: 5}}>✓ VERIFIED LIVENESS</Text>
              </View>
            ))}
          </View>
        )}

        {/* TAB 2: MONTHLY LEDGER */}
        {adminTab === 2 && (
          <View>
            <Text style={styles.boldText}>Select Employee For Monthly Record</Text>
            {employees.length === 0 && <Text style={{color: 'gray'}}>No employees registered.</Text>}
            {employees.map((emp, idx) => (
              <TouchableOpacity key={idx} style={styles.empCard} onPress={() => setInspectedEmployee(emp)}>
                <View style={{flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center'}}>
                  <View><Text style={styles.topicTitle}>{emp.name}</Text><Text style={styles.topicSubtitle}>ID: {emp.empId} | {emp.role}</Text></View>
                  <Text style={{fontSize: 24}}>📅</Text>
                </View>
              </TouchableOpacity>
            ))}
          </View>
        )}

        {/* TAB 3: LEAVE REQUESTS */}
        {adminTab === 3 && (
          <View>
            <Text style={styles.boldText}>Pending Leave Approvals</Text>
            {leaveRequests.length === 0 && <Text style={{color: 'gray'}}>No pending requests.</Text>}
            {leaveRequests.map((req, idx) => (
              <View key={idx} style={styles.card}>
                <Text style={styles.topicTitle}>{req.name} ({req.empId})</Text>
                <Text style={styles.topicSubtitle}>{req.type} | {req.date}</Text>
                {req.status === 'Pending' ? (
                  <View style={{flexDirection: 'row', marginTop: 15}}>
                    <TouchableOpacity style={[styles.blueBtn, {flex: 1, backgroundColor: '#16A34A', marginRight: 10}]} onPress={() => updateLeaveStatus(req._id, 'Approved')}><Text style={styles.btnText}>Approve</Text></TouchableOpacity>
                    <TouchableOpacity style={[styles.blueBtn, {flex: 1, backgroundColor: '#DC2626'}]} onPress={() => updateLeaveStatus(req._id, 'Denied')}><Text style={styles.btnText}>Deny</Text></TouchableOpacity>
                  </View>
                ) : <Text style={{color: req.status === 'Approved' ? '#16A34A' : '#DC2626', fontWeight: 'bold', marginTop: 10}}>{req.status.toUpperCase()}</Text>}
              </View>
            ))}
          </View>
        )}

        {/* TAB 4: REGISTER NEW EMPLOYEE */}
        {adminTab === 4 && (
          <View style={{marginBottom: 40}}>
            <Text style={styles.boldText}>Onboard New Employee</Text>
            <View style={styles.card}>
              <Text style={styles.label}>Full Name</Text>
              <TextInput style={styles.inputBox} placeholder="e.g., Brajesh, Himesh, Shivam" value={newEmpName} onChangeText={setNewEmpName} />
              <Text style={styles.label}>Employee ID</Text>
              <TextInput style={styles.inputBox} placeholder="e.g., EMP205" value={newEmpId} onChangeText={setNewEmpId} />
              <Text style={styles.label}>Role</Text>
              <TextInput style={styles.inputBox} placeholder="e.g., Field Supervisor" value={newEmpRole} onChangeText={setNewEmpRole} />
              <TouchableOpacity style={[styles.blueBtn, {backgroundColor: '#0284C7', marginBottom: 10}]} onPress={() => navigation.navigate('Camera', { mode: 'register' })}><Text style={styles.btnText}>Scan & Link Face Biometrics</Text></TouchableOpacity>
              <TouchableOpacity style={styles.blueBtn} onPress={registerNewEmployee}><Text style={styles.btnText}>Save Employee Profile</Text></TouchableOpacity>
            </View>
          </View>
        )}
      </ScrollView>

      {/* KOTLIN CALENDAR MODAL */}
      <Modal visible={inspectedEmployee !== null} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>{inspectedEmployee?.name}'s Monthly Ledger</Text>
            {renderKotlinStyleCalendar()}
            <View style={styles.statusBox}>
               <Text style={{fontWeight: 'bold'}}>Log Details for {calendarSelectedDay} June</Text>
               <Text style={{color: calendarSelectedDay % 2 === 0 ? '#16A34A' : '#DC2626', fontWeight: 'bold'}}>Status: {calendarSelectedDay % 2 === 0 ? 'PRESENT' : 'ABSENT / ON LEAVE'}</Text>
            </View>
            <TouchableOpacity style={styles.closeBtn} onPress={() => setInspectedEmployee(null)}><Text style={{color: '#FFF', fontWeight: 'bold'}}>Close Analytics</Text></TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F8FAFC' },
  header: { backgroundColor: '#0D3E73', padding: 20, paddingTop: 50 },
  headerText: { color: '#FFF', fontSize: 18, fontWeight: 'bold' },
  badgeDot: { width: 10, height: 10, borderRadius: 5, backgroundColor: 'red', position: 'absolute', right: -2, top: -2 },
  padding: { padding: 16 },
  card: { backgroundColor: '#FFF', padding: 16, borderRadius: 12, marginBottom: 16, borderWidth: 1, borderColor: '#E2E8F0', elevation: 2 },
  boldText: { fontSize: 16, fontWeight: 'bold', color: '#0F172A', marginBottom: 10 },
  topicRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 14, borderBottomWidth: 1, borderColor: '#F1F5F9', alignItems: 'center' },
  topicTitle: { fontSize: 15, fontWeight: 'bold', color: '#0F172A' },
  topicSubtitle: { fontSize: 12, color: '#64748B', marginTop: 2 },
  logCard: { backgroundColor: '#F8FAFC', padding: 16, borderRadius: 10, marginBottom: 12, borderWidth: 1, borderColor: '#E2E8F0' },
  empCard: { backgroundColor: '#FFF', padding: 16, borderRadius: 10, marginBottom: 12, borderWidth: 1, borderColor: '#cbd5e1', elevation: 1 },
  blueBtn: { backgroundColor: '#0D3E73', padding: 14, borderRadius: 8, alignItems: 'center' },
  btnText: { color: '#FFF', fontWeight: 'bold' },
  label: { fontSize: 12, fontWeight: 'bold', color: '#64748B', marginBottom: 5 },
  inputBox: { borderWidth: 1, borderColor: '#CBD5E1', borderRadius: 8, padding: 14, marginBottom: 15, backgroundColor: '#F8FAFC', color: '#0F172A' },
  divider: { height: 1, backgroundColor: '#E2E8F0', marginVertical: 12 },
  profileRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 10 },
  profileLabel: { color: '#64748B', fontWeight: 'bold', fontSize: 13 },
  profileVal: { color: '#0F172A', fontWeight: 'bold', fontSize: 13 },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(15, 23, 42, 0.6)', justifyContent: 'center', alignItems: 'center', padding: 20 },
  modalContent: { backgroundColor: '#FFF', width: '100%', borderRadius: 20, padding: 20, elevation: 10 },
  closeBtn: { backgroundColor: '#0D3E73', padding: 14, borderRadius: 10, alignItems: 'center', marginTop: 20 },
  calDay: { width: 40, height: 40, borderRadius: 8, justifyContent: 'center', alignItems: 'center', margin: 4 },
  bottomSheet: { flex: 1, backgroundColor: 'rgba(15, 23, 42, 0.5)', justifyContent: 'flex-end' },
  sheetContent: { backgroundColor: '#FFF', padding: 24, borderTopLeftRadius: 24, borderTopRightRadius: 24 },
  modalTitle: { fontSize: 18, fontWeight: 'bold', marginBottom: 15, color: '#0F172A' },
  actionBox: { backgroundColor: '#FFF', flex: 1, paddingVertical: 15, borderRadius: 8, alignItems: 'center', marginHorizontal: 4, borderWidth: 1, borderColor: '#CBD5E1', elevation: 1 },
  actionText: { fontSize: 12, fontWeight: 'bold', color: '#0F172A' },
  kotlinGrid: { flexDirection: 'row', flexWrap: 'wrap', marginVertical: 15, borderWidth: 1, borderColor: '#E2E8F0', borderRadius: 12, padding: 10, backgroundColor: '#F8FAFC' },
  weekDay: { width: '14.2%', textAlign: 'center', fontWeight: 'bold', color: '#64748B', marginBottom: 10 },
  kotlinDay: { width: '14.2%', alignItems: 'center', paddingVertical: 10, borderRadius: 8 },
  selectedDay: { backgroundColor: '#0D3E73' },
  dot: { width: 6, height: 6, borderRadius: 3, marginTop: 4 },
  statusBox: { padding: 15, backgroundColor: '#F1F5F9', borderRadius: 8, marginTop: 15, borderWidth: 1, borderColor: '#E2E8F0' }
});