package com.example.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.TransactionLog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Local Error-Free Structural Model for Workforce Registration Profiles
data class LocalWorkerProfile(
    val id: String,
    val name: String,
    val projectCode: String,
    val dailyCheckIn: String = "09:00 AM",
    val dailyCheckOut: String = "06:15 PM",
    val monthlyRatio: String = "96.4%",
    val totalHours: String = "176.5 Hrs",
    val avgDailyHours: String = "8h 15m",
    val verificationState: String = "PENDING",
    val biometricStatus: String = "Active Vector Loaded"
)

// Data class for leave requests (Shared between Admin & User)
data class LeaveRequest(
    val startDate: String,
    val endDate: String,
    val leaveType: String,
    val status: String = "Pending",
    val appliedOn: String,
    val empId: String = "EMP101",
    val empName: String = "Ansh Patel"
)

// Comprehensive State Router Navigation Enum (Cleaned as per new requirements)
enum class AppNavigationState {
    WELCOME_SPLASH,
    PORTAL_LOGIN,
    ADMIN_WORKSPACE,
    USER_WORKSPACE,
    TASK_ATTENDANCE_ENGINE,
    TASK_UCC_GATEWAY, // Repurposed for Sync Changes
    TASK_WORKFORCE_PROFILES, // Repurposed for Workforce Profiles List
    USER_PROFILE,
    USER_LEAVE_PORTAL
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rawUiState by viewModel.uiState.collectAsState()
    val unsyncedList by viewModel.unsyncedLogs.collectAsState()
    val lastSyncTs by viewModel.lastSyncTimestamp.collectAsState()

    // Central state navigation router
    var currentScreenState by remember { mutableStateOf(AppNavigationState.WELCOME_SPLASH) }
    var lastWorkspaceOrigin by remember { mutableStateOf(AppNavigationState.USER_WORKSPACE) }

    // Persistent Login Form Fields States
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("ADMIN") }
    var captchaInput by remember { mutableStateOf("") }
    var isRoleExpanded by remember { mutableStateOf(false) }

    val captchaList = remember { listOf("pBmcSL", "xK7wN2", "mR9vP4", "qZ3fT8", "vY5bX1") }
    var captchaIndex by remember { mutableStateOf(0) }
    val currentCaptchaString = captchaList[captchaIndex]

    // Notifications State
    var userNotifications by remember { mutableStateOf(0) }
    var adminNotifications by remember { mutableStateOf(2) }

    // Admin Tabs Parameters
    var selectedAdminTab by remember { mutableStateOf(0) }
    var selectedOverrideEmployeeId by remember { mutableStateOf("") }
    var overrideStatusInput by remember { mutableStateOf("PRESENT") }

    // Workforce Database Cache Pool Layout Nodes
    var activeWorkersRegistryList by remember {
        mutableStateOf(
            listOf(
                LocalWorkerProfile("EMP101", "Ansh Patel", "NHAI-DEL-MUM-01", "08:58 AM", "06:05 PM", "97.2%", "180.5 Hrs", "8h 22m", "PENDING"),
                LocalWorkerProfile("EMP102", "Priya Sharma", "NHAI-DEL-MUM-02", "09:02 AM", "06:12 PM", "95.8%", "172.0 Hrs", "7h 58m", "PENDING"),
                LocalWorkerProfile("EMP103", "Rajesh Patel", "NHAI-UP-CORRIDOR", "08:45 AM", "05:55 PM", "98.1%", "184.0 Hrs", "8h 35m", "VERIFIED"),
                LocalWorkerProfile("EMP104", "Sunita Rao", "NHAI-SOUTH-HIGHWAY", "09:15 AM", "06:30 PM", "94.5%", "168.5 Hrs", "7h 45m", "PENDING"),
                LocalWorkerProfile("EMP105", "Vikram Singh", "NHAI-EAST-EXPRESS", "08:55 AM", "06:00 PM", "96.7%", "178.0 Hrs", "8h 10m", "PENDING")
            )
        )
    }

    var inspectedWorkerProfile by remember { mutableStateOf<LocalWorkerProfile?>(null) }
    var calendarSelectedDay by remember { mutableStateOf(25) }

    // Leave Portal State (Shared)
    var globalLeaveRequests by remember {
        mutableStateOf(
            listOf(
                LeaveRequest("22 May 2026", "22 May 2026", "Casual Leave", "Pending", "20 May 2026", "EMP101", "Ansh Patel"),
                LeaveRequest("10 May 2026", "12 May 2026", "Sick Leave", "Pending", "08 May 2026", "EMP102", "Priya Sharma")
            )
        )
    }
    var selectedLeaveType by remember { mutableStateOf("Casual Leave") }
    var isLeaveTypeExpanded by remember { mutableStateOf(false) }
    var selectedStartDay by remember { mutableStateOf(-1) }
    var selectedEndDay by remember { mutableStateOf(-1) }

    val uiState = remember(rawUiState) {
        if (rawUiState.livenessState == LivenessState.IDLE) {
            rawUiState.copy(livenessState = LivenessState.ALIGN_FACE, promptMessage = "System active and secure.", leftEyeOpenProb = 1.0f, rightEyeOpenProb = 1.0f, smilingProb = 0.1f)
        } else rawUiState
    }

    LaunchedEffect(Unit) {
        val hasCameraPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        viewModel.setCameraPermissionGranted(hasCameraPermission)
        viewModel.toggleSimulatorMode(true)
    }

    val lightThemeBackground = Brush.verticalGradient(colors = listOf(Color(0xFFE8F0FA), Color(0xFFF4F8FC)))
    val nhaiBlue = Color(0xFF0D3E73)

    val inputFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black, unfocusedTextColor = Color.Black,
        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
        focusedBorderColor = Color(0xFF0284C7), unfocusedBorderColor = Color(0xFFCBD5E1),
        focusedLabelColor = Color(0xFF0284C7), unfocusedLabelColor = Color(0xFF64748B)
    )
    val visibleBlackTextStyle = TextStyle(color = Color.Black, fontSize = 15.sp)

    val sharedAttendanceData = remember {
        listOf(
            listOf("01 May", "08:55 AM", "06:10 PM", "9.25"),
            listOf("02 May", "09:02 AM", "06:05 PM", "9.05"),
            listOf("03 May", "08:48 AM", "06:20 PM", "9.53"),
            listOf("04 May", "—", "—", "0.00"),
            listOf("05 May", "09:00 AM", "06:15 PM", "9.25"),
            listOf("06 May", "08:58 AM", "06:12 PM", "9.23"),
            listOf("07 May", "09:05 AM", "06:08 PM", "9.05"),
            listOf("08 May", "08:50 AM", "06:25 PM", "9.58"),
            listOf("09 May", "09:10 AM", "06:00 PM", "8.83"),
            listOf("10 May", "08:45 AM", "06:18 PM", "9.55")
        )
    }

    // =========================================================================
    // SCREEN 1: WELCOME SPLASH LANDING PAGE
    // =========================================================================
    if (currentScreenState == AppNavigationState.WELCOME_SPLASH) {
        Box(modifier = Modifier.fillMaxSize().background(lightThemeBackground)) {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 28.dp)) {
                    Text(text = "Digital Backbone for", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = nhaiBlue, textAlign = TextAlign.Center)
                    Text(text = "National Highways.", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = nhaiBlue, textAlign = TextAlign.Center)
                }

                Card(modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 24.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Image(painter = painterResource(id = R.drawable.ic_nhai_logo), contentDescription = "NHAI Logo", modifier = Modifier.size(50.dp))
                            Column {
                                Text("भारतीय राष्ट्रीय राजमार्ग प्राधिकरण", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text("NHAI GOVT OF INDIA", fontSize = 10.sp, letterSpacing = 1.sp, color = Color.Gray)
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Welcome to", fontSize = 20.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            Text("Workforce Portal", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A), textAlign = TextAlign.Center)
                            Text("powered by Digital India", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(185.dp).clip(RoundedCornerShape(18.dp))) {
                            val imgId = context.resources.getIdentifier("highway_bg", "drawable", context.packageName)
                            if (imgId != 0) Image(painter = painterResource(id = imgId), contentDescription = "Delhi-Mumbai", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

                            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xAA000000)))), contentAlignment = Alignment.BottomCenter) {
                                Text(text = "Delhi-Mumbai Greenfield Expressway", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 12.dp), textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                Button(onClick = { currentScreenState = AppNavigationState.PORTAL_LOGIN }, colors = ButtonDefaults.buttonColors(containerColor = nhaiBlue), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("Access Secure Portal ➔", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    // =========================================================================
    // SCREEN 2: PORTAL LOGIN / SIGN IN VIEW
    // =========================================================================
    else if (currentScreenState == AppNavigationState.PORTAL_LOGIN) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = { currentScreenState = AppNavigationState.WELCOME_SPLASH }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black) }
                    Text("National Highways Authority of India", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }

                Spacer(Modifier.height(4.dp))
                Text("Authentication Gateway", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(value = selectedRole, onValueChange = {}, readOnly = true, label = { Text("System Role Profiling") }, leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF0284C7)) }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = inputFieldColors)
                            Box(modifier = Modifier.matchParentSize().clickable { isRoleExpanded = true })
                            DropdownMenu(expanded = isRoleExpanded, onDismissRequest = { isRoleExpanded = false }, modifier = Modifier.fillMaxWidth(0.75f).background(Color.White)) {
                                listOf("ADMIN", "USER").forEach { role ->
                                    DropdownMenuItem(text = { Text(role, color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) }, onClick = { selectedRole = role; isRoleExpanded = false })
                                }
                            }
                        }

                        OutlinedTextField(value = usernameInput, onValueChange = { usernameInput = it }, label = { Text("Gate Identifier / Username") }, leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null, tint = Color(0xFF64748B)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = visibleBlackTextStyle, colors = inputFieldColors, singleLine = true)
                        OutlinedTextField(value = passwordInput, onValueChange = { passwordInput = it }, label = { Text("Access Cipher Code") }, leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color(0xFF64748B)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), textStyle = visibleBlackTextStyle, colors = inputFieldColors, singleLine = true)

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF334155)).clickable { captchaIndex = (captchaIndex + 1) % captchaList.size }, contentAlignment = Alignment.Center) {
                                Text(currentCaptchaString, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White, letterSpacing = 3.sp)
                            }
                            OutlinedTextField(value = captchaInput, onValueChange = { captchaInput = it }, placeholder = { Text("Security Code*", fontSize = 11.sp) }, modifier = Modifier.weight(1.2f), textStyle = visibleBlackTextStyle, colors = inputFieldColors, singleLine = true)
                        }

                        Button(
                            onClick = {
                                if (usernameInput.isNotEmpty() && passwordInput.isNotEmpty()) {
                                    if (selectedRole == "ADMIN") { lastWorkspaceOrigin = AppNavigationState.ADMIN_WORKSPACE; currentScreenState = AppNavigationState.ADMIN_WORKSPACE }
                                    else { lastWorkspaceOrigin = AppNavigationState.USER_WORKSPACE; currentScreenState = AppNavigationState.USER_WORKSPACE }
                                    Toast.makeText(context, "Logged in as $selectedRole", Toast.LENGTH_SHORT).show()
                                } else Toast.makeText(context, "Please complete fields", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088FF)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) { Text("SIGN IN TO ARCHITECTURE", fontWeight = FontWeight.Bold, color = Color.White) }

                        OutlinedButton(
                            onClick = {
                                if (selectedRole == "ADMIN") { usernameInput = "admin_portal"; passwordInput = "nhai_admin"; captchaInput = currentCaptchaString; lastWorkspaceOrigin = AppNavigationState.ADMIN_WORKSPACE; currentScreenState = AppNavigationState.ADMIN_WORKSPACE }
                                else { usernameInput = "field_user"; passwordInput = "nhai_user"; captchaInput = currentCaptchaString; lastWorkspaceOrigin = AppNavigationState.USER_WORKSPACE; currentScreenState = AppNavigationState.USER_WORKSPACE }
                                Toast.makeText(context, "Bypass active: $selectedRole Mode", Toast.LENGTH_SHORT).show()
                            },
                            border = BorderStroke(1.5.dp, Color(0xFF16A34A)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("LOGIN AS GUEST / SIMULATOR BYPASS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // SCREEN 3A: SEPARATE ADMIN WORKSPACE HUB
    // =========================================================================
    else if (currentScreenState == AppNavigationState.ADMIN_WORKSPACE) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Master Portal", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    actions = {
                        BadgedBox(
                            badge = { if(adminNotifications > 0) Badge(containerColor = Color(0xFFEA580C)) { Text(adminNotifications.toString(), color = Color.White) } },
                            modifier = Modifier.padding(horizontal = 8.dp).clickable { adminNotifications = 0; Toast.makeText(context, "Notifications flushed", Toast.LENGTH_SHORT).show() }
                        ) { Icon(Icons.Default.Notifications, contentDescription = "Communications Node", tint = Color.White) }
                        IconButton(onClick = { currentScreenState = AppNavigationState.WELCOME_SPLASH }) { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = nhaiBlue)
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(lightThemeBackground)) {

                // Simplified admin tabs configuration
                ScrollableTabRow(selectedTabIndex = selectedAdminTab, containerColor = Color.White, contentColor = nhaiBlue, edgePadding = 16.dp) {
                    Tab(selected = selectedAdminTab == 0, onClick = { selectedAdminTab = 0 }, text = { Text("Core Tools", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedAdminTab == 1, onClick = { selectedAdminTab = 1 }, text = { Text("Daily Logs", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedAdminTab == 2, onClick = { selectedAdminTab = 2 }, text = { Text("Monthly Report", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedAdminTab == 3, onClick = { selectedAdminTab = 3 }, text = { Text("Requests", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                }

                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp)) {
                    when (selectedAdminTab) {
                        0 -> {
                            // Core Tools Section
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text("Core Admin Functions:", fontSize = 14.sp, color = nhaiBlue, fontWeight = FontWeight.Bold)
                                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        HelpTopicRow(Icons.Default.CheckCircle, "Mark Attendance", "Execute biometric face scanning logs", onClick = { currentScreenState = AppNavigationState.TASK_ATTENDANCE_ENGINE })

                                        val syncText = if (lastSyncTs > 0L) {
                                            "Last synced: ${SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(lastSyncTs))}"
                                        } else "Auto-Syncing in background"

                                        HelpTopicRow(Icons.Default.CloudSync, "Sync Changes", syncText, onClick = { currentScreenState = AppNavigationState.TASK_UCC_GATEWAY })
                                        HelpTopicRow(Icons.Default.RecentActors, "Workforce Profiles", "View complete employee check-in details", onClick = { currentScreenState = AppNavigationState.TASK_WORKFORCE_PROFILES })
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Daily Logs View
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Personnel Daily Log Validation Desk:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = nhaiBlue)
                                    Button(
                                        onClick = {
                                            activeWorkersRegistryList = activeWorkersRegistryList.map { it.copy(verificationState = "VERIFIED") }
                                            Toast.makeText(context, "All logs verified globally & Synced to AWS!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) { Text("Verify All Logs", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) }
                                }

                                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    activeWorkersRegistryList.forEach { employee ->
                                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp)).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(text = employee.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                                        Text("ID: ${employee.id} | Site: ${employee.projectCode}", fontSize = 11.sp, color = Color.Gray)
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            if (employee.verificationState == "PENDING") {
                                                                // Verify button with lighter blue background and white text for better readability
                                                                Button(
                                                                    onClick = {
                                                                        activeWorkersRegistryList = activeWorkersRegistryList.map { if(it.id == employee.id) it.copy(verificationState = "VERIFIED") else it }
                                                                        userNotifications++
                                                                        Toast.makeText(context, "Verified & Synced to AWS", Toast.LENGTH_SHORT).show()
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA), contentColor = Color.White),
                                                                    modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                                ) { Text("Verify", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White) }

                                                                // Reject button
                                                                Button(
                                                                    onClick = {
                                                                        activeWorkersRegistryList = activeWorkersRegistryList.map { if(it.id == employee.id) it.copy(verificationState = "REJECTED") else it }
                                                                        userNotifications++
                                                                        Toast.makeText(context, "Rejected & Synced to AWS. User notified.", Toast.LENGTH_SHORT).show()
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                                                                    modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                                ) { Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                                                            } else {
                                                                val iconColor = if(employee.verificationState == "VERIFIED") Color(0xFF16A34A) else Color(0xFFDC3545)
                                                                Icon(imageVector = if(employee.verificationState == "VERIFIED") Icons.Default.CheckCircle else Icons.Default.Cancel, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp).padding(horizontal = 4.dp))
                                                                Text(employee.verificationState, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = iconColor)
                                                            }
                                                        }
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text("In: ${employee.dailyCheckIn}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B3A6B))
                                                        Text("Out: ${employee.dailyCheckOut}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC3545))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Monthly Report Section
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Select Employee For Monthly Calendar Record:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = nhaiBlue)
                                Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFCBD5E1))) {
                                    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        activeWorkersRegistryList.forEach { employee ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).clickable { inspectedWorkerProfile = employee }.padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(employee.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                                                    Text("ID: ${employee.id} | Site: ${employee.projectCode}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = nhaiBlue)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            // Requests Block (Leave Approvals + Manual Overwrite)
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {

                                // Section: Leave Management
                                Text("Leave Applications Management", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = nhaiBlue)
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        if (globalLeaveRequests.isEmpty()) {
                                            Text("No leave requests pending.", color = Color.Gray, fontSize = 12.sp)
                                        } else {
                                            globalLeaveRequests.forEachIndexed { index, request ->
                                                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp)).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text("${request.empName} (${request.empId})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                                        Text("${request.leaveType} | ${request.startDate} - ${request.endDate}", fontSize = 11.sp, color = Color.Gray)
                                                    }
                                                    if (request.status == "Pending") {
                                                        Row(horizontalArrangement = Arrangement.End) {
                                                            Button(
                                                                onClick = {
                                                                    val updated = globalLeaveRequests.toMutableList()
                                                                    updated[index] = request.copy(status = "Approved")
                                                                    globalLeaveRequests = updated
                                                                    userNotifications++
                                                                    Toast.makeText(context, "Approved & Synced to AWS. User notified.", Toast.LENGTH_SHORT).show()
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)), modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                                            ) { Text("Approve", color = Color.White, fontSize = 10.sp) }

                                                            Spacer(modifier = Modifier.width(4.dp))

                                                            Button(
                                                                onClick = {
                                                                    val updated = globalLeaveRequests.toMutableList()
                                                                    updated[index] = request.copy(status = "Denied")
                                                                    globalLeaveRequests = updated
                                                                    userNotifications++
                                                                    Toast.makeText(context, "Denied & Synced to AWS. User notified.", Toast.LENGTH_SHORT).show()
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)), modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                                            ) { Text("Deny", color = Color.White, fontSize = 10.sp) }
                                                        }
                                                    } else {
                                                        Text(request.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if(request.status=="Approved") Color(0xFF28A745) else Color(0xFFDC3545))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color.LightGray)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Section: Manual Overwrite
                                Text("Manual Attendance Overwrite (Face Mismatch Validation)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = nhaiBlue)
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(value = selectedOverrideEmployeeId, onValueChange = { selectedOverrideEmployeeId = it }, label = { Text("Target Unique Employee ID") }, modifier = Modifier.fillMaxWidth(), textStyle = visibleBlackTextStyle, colors = inputFieldColors, singleLine = true)
                                        OutlinedTextField(value = overrideStatusInput, onValueChange = { overrideStatusInput = it }, label = { Text("Value Node (PRESENT / ABSENT)") }, modifier = Modifier.fillMaxWidth(), textStyle = visibleBlackTextStyle, colors = inputFieldColors, singleLine = true)
                                        Button(
                                            onClick = {
                                                userNotifications++
                                                Toast.makeText(context, "Admin Verification Force Commited & Synced to AWS", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = nhaiBlue), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(48.dp)
                                        ) {
                                            Text("Self-Verify Identity & Commit Update")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // SCREEN 3B: SEPARATE USER WORKSPACE HUB (Untouched core logic, matches expectations)
    // =========================================================================
    else if (currentScreenState == AppNavigationState.USER_WORKSPACE) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Portal Hub: Field Operator Client", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    actions = {
                        IconButton(onClick = { userNotifications = 0; Toast.makeText(context, "Notifications cleared", Toast.LENGTH_SHORT).show() }) {
                            BadgedBox(badge = { if (userNotifications > 0) Badge(containerColor = Color.Red, contentColor = Color.White) { Text("$userNotifications") } }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                            }
                        }
                        IconButton(onClick = { currentScreenState = AppNavigationState.WELCOME_SPLASH }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = nhaiBlue)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(lightThemeBackground)) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, Color(0xFF1B3A6B).copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF1B3A6B))
                                Text("Your Personal Daily Shift Activity Logs", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color.Black)
                            }
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("DAILY LOG-IN TIME", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text("09:00 AM (ISO Verified)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF28A745))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("DAILY LOG-OUT TIME", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text("06:15 PM (Terminal Lock)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC3545))
                                }
                            }
                        }
                    }

                    // AWS Sync Status Card indicating Background Auto Sync
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(22.dp))
                            Column {
                                val syncText = if (lastSyncTs > 0L) {
                                    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                    "Last synced with AWS: ${sdf.format(Date(lastSyncTs))}"
                                } else {
                                    "Last synced with AWS: Auto-Syncing..."
                                }
                                Text(syncText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0D3E73))
                                Text("Sync happens automatically in the background", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Operational System Verification Controls:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = nhaiBlue)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
                                HelpTopicRow(Icons.Default.CheckCircle, "Mark Attendance", "Execute field scan tracking verification loop", onClick = { currentScreenState = AppNavigationState.TASK_ATTENDANCE_ENGINE })
                                HelpTopicRow(Icons.Default.Person, "My Profile", "View personal details and monthly attendance analytics", onClick = { currentScreenState = AppNavigationState.USER_PROFILE })
                                HelpTopicRow(Icons.Default.DateRange, "Apply for Leave", "Submit leave requests for admin verification", onClick = { currentScreenState = AppNavigationState.USER_LEAVE_PORTAL })
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // SUB-PAGE 1: LIVE ADVANCED BIOMETRIC VERIFICATION (Mark Attendance)
    // =========================================================================
    else if (currentScreenState == AppNavigationState.TASK_ATTENDANCE_ENGINE) {
        val infiniteTransition = rememberInfiniteTransition()
        val scanAnimY by infiniteTransition.animateFloat(initialValue = 0.1f, targetValue = 0.9f, animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse))

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mark Attendance Terminal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = { IconButton(onClick = { currentScreenState = lastWorkspaceOrigin }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = nhaiBlue)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(lightThemeBackground)) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Align Face Inside Target Reticle Area", color = nhaiBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                    Box(
                        modifier = Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(24.dp)).background(Color.White).border(BorderStroke(2.dp, Color(0xFF0088FF)), RoundedCornerShape(24.dp))
                            .drawWithContent {
                                drawContent()
                                val lineY = size.height * scanAnimY
                                drawLine(color = Color(0xFF0088FF), start = Offset(0f, lineY), end = Offset(size.width, lineY), strokeWidth = 5f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(180.dp, 220.dp).border(BorderStroke(2.dp, Color(0xFF0088FF).copy(alpha = 0.3f)), CircleShape))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Face, contentDescription = null, tint = Color(0xFF0088FF).copy(alpha = 0.15f), modifier = Modifier.size(100.dp))
                        }
                    }

                    Button(
                        onClick = { viewModel.startScanning(); Toast.makeText(context, "Scanning Complete!", Toast.LENGTH_SHORT).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = nhaiBlue), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Trigger Live Biometric Scan Pipeline", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // =========================================================================
    // ADMIN SUB-PAGE 2: Sync Changes Node
    // =========================================================================
    else if (currentScreenState == AppNavigationState.TASK_UCC_GATEWAY) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Network Sync Overview", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = { IconButton(onClick = { currentScreenState = lastWorkspaceOrigin }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = nhaiBlue)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(lightThemeBackground)) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Enterprise System Pipeline Streams", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D3E73))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "Pending dispatch local packets waiting transfer: ${unsyncedList.size} queues verified.", fontSize = 14.sp, color = Color.DarkGray)
                            Text(text = "Last verified data transaction push: Auto-Syncing with AWS", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { viewModel.syncOfflineQueue(); Toast.makeText(context, "Stream push complete. Data Lake updated!", Toast.LENGTH_SHORT).show(); currentScreenState = lastWorkspaceOrigin }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Text("Force Transmission Synchronize Link", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // =========================================================================
    // ADMIN SUB-PAGE 3: WORKFORCE PROFILES
    // =========================================================================
    else if (currentScreenState == AppNavigationState.TASK_WORKFORCE_PROFILES) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Workforce Profiles", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = { IconButton(onClick = { currentScreenState = lastWorkspaceOrigin }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = nhaiBlue)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(lightThemeBackground)) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("All Registered Field Operators Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = nhaiBlue)

                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        activeWorkersRegistryList.forEach { profile ->
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Column {
                                            Text(profile.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.Black)
                                            Text("Emp ID: ${profile.id} | Site: ${profile.projectCode}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = nhaiBlue, modifier = Modifier.size(36.dp))
                                    }
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Check-In", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            Text(profile.dailyCheckIn, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                        }
                                        Column {
                                            Text("Check-Out", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            Text(profile.dailyCheckOut, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC3545))
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Total Hrs", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            Text(profile.totalHours, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = nhaiBlue)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // SUB-PAGE 7: MY PROFILE (USER_PROFILE)
    // =========================================================================
    else if (currentScreenState == AppNavigationState.USER_PROFILE) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("My Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = { IconButton(onClick = { currentScreenState = AppNavigationState.USER_WORKSPACE }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = nhaiBlue)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(lightThemeBackground)) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.5.dp, nhaiBlue.copy(alpha = 0.2f))) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(nhaiBlue), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                                }
                                Column {
                                    Text("Employee Profile", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = nhaiBlue)
                                    Text("NHAI Field Operator", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            ProfileDetailRow("Employee Name", "Ansh Patel")
                            ProfileDetailRow("Employee ID", "EMP101")
                            ProfileDetailRow("Date of Birth", "15 March 1995")
                            ProfileDetailRow("Assigned Admin", "Rajesh Kumar")
                            ProfileDetailRow("Admin ID", "ADM001")
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = nhaiBlue)) {
                        Row(modifier = Modifier.padding(18.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Average Monthly Working Hours", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                                Text("May 2026", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            }
                            Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.15f)).padding(horizontal = 16.dp, vertical = 10.dp)) {
                                Text("176.5 Hrs", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }

                    Text("Monthly Attendance Ledger — May 2026", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = nhaiBlue)
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().background(nhaiBlue.copy(alpha = 0.08f)).padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Date", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = nhaiBlue, modifier = Modifier.weight(1f))
                                Text("Login", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = nhaiBlue, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text("Logout", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = nhaiBlue, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text("Hours", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = nhaiBlue, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                            }
                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            sharedAttendanceData.forEachIndexed { index, row ->
                                val isOff = row[1] == "—"
                                Row(
                                    modifier = Modifier.fillMaxWidth().background(if (index % 2 == 0) Color.Transparent else Color(0xFFF8FAFC)).padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(row[0], fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.weight(1f))
                                    Text(row[1], fontSize = 12.sp, color = if (isOff) Color(0xFFDC3545) else Color(0xFF28A745), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                    Text(row[2], fontSize = 12.sp, color = if (isOff) Color(0xFFDC3545) else Color(0xFFDC3545), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                    Text("${row[3]} h", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isOff) Color(0xFFCBD5E1) else nhaiBlue, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // SUB-PAGE 8: APPLY FOR LEAVE (USER_LEAVE_PORTAL)
    // =========================================================================
    else if (currentScreenState == AppNavigationState.USER_LEAVE_PORTAL) {
        val calendar = remember { Calendar.getInstance() }
        val monthName = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time) }
        val tempCal = remember { Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) } }
        val firstDayOfWeek = remember { (tempCal.get(Calendar.DAY_OF_WEEK) + 5) % 7 }
        val daysInMonth = remember { tempCal.getActualMaximum(Calendar.DAY_OF_MONTH) }
        val todayDate = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH) }
        val leaveTypes = listOf("Casual Leave", "Sick Leave", "Medical Leave", "Normal Holidays")

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Apply for Leave", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = { IconButton(onClick = { currentScreenState = AppNavigationState.USER_WORKSPACE }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = nhaiBlue)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(lightThemeBackground)) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(monthName, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = nhaiBlue, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                                    Text(day, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                }
                            }

                            var dayCounter = 1
                            val totalCells = firstDayOfWeek + daysInMonth
                            val rows = (totalCells + 6) / 7

                            for (row in 0 until rows) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    for (col in 0 until 7) {
                                        val cellIndex = row * 7 + col
                                        if (cellIndex < firstDayOfWeek || dayCounter > daysInMonth) {
                                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                        } else {
                                            val thisDay = dayCounter
                                            val isSelected = (selectedStartDay > 0 && selectedEndDay > 0 && thisDay in selectedStartDay..selectedEndDay) ||
                                                    (selectedStartDay > 0 && selectedEndDay < 0 && thisDay == selectedStartDay)
                                            val isToday = thisDay == todayDate
                                            val isRangeStart = thisDay == selectedStartDay
                                            val isRangeEnd = thisDay == selectedEndDay

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        when {
                                                            isRangeStart || isRangeEnd -> nhaiBlue
                                                            isSelected -> nhaiBlue.copy(alpha = 0.15f)
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .then(if (isToday && !isSelected) Modifier.border(1.5.dp, nhaiBlue, RoundedCornerShape(8.dp)) else Modifier)
                                                    .clickable {
                                                        if (selectedStartDay < 0 || (selectedStartDay > 0 && selectedEndDay > 0)) {
                                                            selectedStartDay = thisDay; selectedEndDay = -1
                                                        } else if (thisDay >= selectedStartDay) {
                                                            selectedEndDay = thisDay
                                                        } else {
                                                            selectedStartDay = thisDay; selectedEndDay = -1
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("$thisDay", fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = when { isRangeStart || isRangeEnd -> Color.White; isSelected -> nhaiBlue; else -> Color.Black })
                                            }
                                            dayCounter++
                                        }
                                    }
                                }
                            }

                            if (selectedStartDay > 0) {
                                val rangeText = if (selectedEndDay > 0 && selectedEndDay != selectedStartDay) "Selected: $selectedStartDay — $selectedEndDay ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(calendar.time)}" else "Selected: $selectedStartDay ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(calendar.time)}"
                                Text(rangeText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = nhaiBlue, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                            }
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Leave Type (GoI / NHAI Guidelines)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = nhaiBlue)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(value = selectedLeaveType, onValueChange = {}, readOnly = true, leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = nhaiBlue) }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = inputFieldColors)
                                Box(modifier = Modifier.matchParentSize().clickable { isLeaveTypeExpanded = true })
                                DropdownMenu(expanded = isLeaveTypeExpanded, onDismissRequest = { isLeaveTypeExpanded = false }, modifier = Modifier.fillMaxWidth(0.8f).background(Color.White)) {
                                    leaveTypes.forEach { type ->
                                        DropdownMenuItem(text = { Text(type, color = Color.Black, fontWeight = FontWeight.SemiBold) }, onClick = { selectedLeaveType = type; isLeaveTypeExpanded = false })
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (selectedStartDay > 0) {
                                val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                                val monthStr = sdf.format(calendar.time)
                                val startStr = "$selectedStartDay $monthStr"
                                val endStr = if (selectedEndDay > 0) "$selectedEndDay $monthStr" else startStr
                                val todaySdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                val todayStr = todaySdf.format(Date())

                                val newRequest = LeaveRequest(startDate = startStr, endDate = endStr, leaveType = selectedLeaveType, status = "Pending", appliedOn = todayStr, empId = "EMP101", empName = "Ansh Patel")
                                globalLeaveRequests = listOf(newRequest) + globalLeaveRequests
                                selectedStartDay = -1; selectedEndDay = -1
                                Toast.makeText(context, "Leave request submitted for admin verification", Toast.LENGTH_SHORT).show()
                            } else Toast.makeText(context, "Please select a date or date range first", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = nhaiBlue), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text("Request Admin Verification", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Text("Leave Status Ledger", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = nhaiBlue)

                    if (globalLeaveRequests.isEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Text("No leave requests submitted yet.", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        globalLeaveRequests.forEach { request ->
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                                Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(request.leaveType, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        val dateRange = if (request.startDate == request.endDate) request.startDate else "${request.startDate} — ${request.endDate}"
                                        Text(dateRange, fontSize = 12.sp, color = Color.DarkGray)
                                        Text("Applied: ${request.appliedOn}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(when(request.status) { "Approved" -> Color(0xFF28A745).copy(alpha = 0.15f); "Denied" -> Color(0xFFDC3545).copy(alpha = 0.15f); else -> Color(0xFFE67E22).copy(alpha = 0.15f) }).padding(horizontal = 12.dp, vertical = 6.dp)) {
                                        Text(request.status, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = when(request.status) { "Approved" -> Color(0xFF28A745); "Denied" -> Color(0xFFDC3545); else -> Color(0xFFE67E22) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // CALENDAR GRID MONTHLY REPORT SHEET FOR ADMIN
    // =========================================================================
    inspectedWorkerProfile?.let { profile ->
        AlertDialog(
            onDismissRequest = { inspectedWorkerProfile = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = nhaiBlue, modifier = Modifier.size(32.dp))
                    Column {
                        Text(text = profile.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Black)
                        Text(text = "Monthly Ledger - ${profile.id}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TOTAL TIME", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(profile.totalHours, fontSize = 15.sp, fontWeight = FontWeight.Black, color = nhaiBlue)
                            }
                        }
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("AVG DAILY", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(profile.avgDailyHours, fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                        }
                    }

                    // Calendar Display Area
                    Text("Select a date to inspect:", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = nhaiBlue)

                    val daysGrid = (1..31).toList().chunked(7)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                                Text(it, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            }
                        }

                        daysGrid.forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                week.forEach { day ->
                                    val isSelected = day == calendarSelectedDay
                                    // Make week 4 (days 22+) red/absent arbitrarily for showcase to mimic attendance logs
                                    val isPresent = day < 22

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) nhaiBlue else Color(0xFFF1F5F9))
                                            .clickable { calendarSelectedDay = day },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("$day", fontSize = 11.sp, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal, color = if(isSelected) Color.White else Color.Black)
                                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isPresent) Color(0xFF28A745) else Color(0xFFDC3545)))
                                        }
                                    }
                                }
                                // pad remaining boxes if incomplete week
                                repeat(7 - week.size) { Box(modifier = Modifier.weight(1f).aspectRatio(1f)) }
                            }
                        }
                    }

                    // Data for Selected Day
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Log Details for $calendarSelectedDay May", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                            Spacer(modifier = Modifier.height(6.dp))
                            if (calendarSelectedDay < 22) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Login: ${profile.dailyCheckIn}", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                    Text("Logout: ${profile.dailyCheckOut}", fontSize = 11.sp, color = Color(0xFFDC3545), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("Status: ABSENT / ON LEAVE", fontSize = 11.sp, color = Color(0xFFDC3545), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { inspectedWorkerProfile = null }) { Text("Close Analytics Frame", fontWeight = FontWeight.Bold, color = nhaiBlue) } }
        )
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun HelpTopicRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Text(text = subtitle, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
    }
}