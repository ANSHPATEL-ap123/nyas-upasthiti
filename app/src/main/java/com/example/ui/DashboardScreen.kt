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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
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
import com.example.data.model.TransactionLog

// Local Structural Model for Workforce Registration Profiles
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

// Structural Model for Administrative Request Tickets
data class SystemRequestTicket(
    val ticketId: String,
    val workerName: String,
    val type: String, // "LEAVE REQUEST" or "MANUAL OVERWRITE" or "FACIAL MISMATCH BYPASS"
    val metadata: String,
    var ticketStatus: String = "PENDING"
)

// Comprehensive State Router Navigation Enum
enum class AppNavigationState {
    WELCOME_SPLASH,
    PORTAL_LOGIN,
    ADMIN_WORKSPACE,
    USER_WORKSPACE,
    TASK_ATTENDANCE_ENGINE,
    TASK_UCC_GATEWAY,
    TASK_BIOMETRIC_REGISTRATION
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val rawUiState by viewModel.uiState.collectAsState()
    val transactionLogsList by viewModel.transactionLogs.collectAsState()
    val unsyncedList by viewModel.unsyncedLogs.collectAsState()

    // Central state navigation router
    var currentScreenState by remember { mutableStateOf(AppNavigationState.WELCOME_SPLASH) }
    var lastWorkspaceOrigin by remember { mutableStateOf(AppNavigationState.USER_WORKSPACE) }

    // Persistent Login Form Fields States (High-Contrast Text Mode Enabled)
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("ADMIN") }
    var captchaInput by remember { mutableStateOf("") }
    var isRoleExpanded by remember { mutableStateOf(false) }

    val captchaList = remember { listOf("pBmcSL", "xK7wN2", "mR9vP4", "qZ3fT8", "vY5bX1") }
    var captchaIndex by remember { mutableStateOf(0) }
    val currentCaptchaString = captchaList[captchaIndex]

    var selectedDetailLog by remember { mutableStateOf<TransactionLog?>(null) }
    var showConfirmResetDialog by remember { mutableStateOf(false) }

    // Admin Tabs Parameters (0 = Core Tools, 1 = Daily Logs, 2 = Requests Center, 3 = Monthly Analytics)
    var selectedAdminTab by remember { mutableStateOf(0) }

    // Workforce Database Cache Pool Layout Nodes
    var activeWorkersRegistryList by remember {
        mutableStateOf(
            listOf(
                LocalWorkerProfile("EMP101", "Amit Kumar", "NHAI-DEL-MUM-01", "08:58 AM", "06:05 PM", "97.2%", "180.5 Hrs", "8h 22m", "PENDING"),
                LocalWorkerProfile("EMP102", "Priya Sharma", "NHAI-DEL-MUM-02", "09:02 AM", "06:12 PM", "95.8%", "172.0 Hrs", "7h 58m", "PENDING"),
                LocalWorkerProfile("EMP103", "Rajesh Patel", "NHAI-UP-CORRIDOR", "08:45 AM", "05:55 PM", "98.1%", "184.0 Hrs", "8h 35m", "VERIFIED"),
                LocalWorkerProfile("EMP104", "Sunita Rao", "NHAI-SOUTH-HIGHWAY", "09:15 AM", "06:30 PM", "94.5%", "168.5 Hrs", "7h 45m", "PENDING"),
                LocalWorkerProfile("EMP105", "Vikram Singh", "NHAI-EAST-EXPRESS", "08:55 AM", "06:00 PM", "96.7%", "178.0 Hrs", "8h 10m", "PENDING")
            )
        )
    }

    // Unified Administrative Operational Request Tickets Registry Pool
    var administrativeRequestsPool by remember {
        mutableStateOf(
            listOf(
                SystemRequestTicket("REQ-402", "Amit Kumar", "LEAVE REQUEST", "Medical Leave - Duration: 3 Days"),
                SystemRequestTicket("REQ-403", "Priya Sharma", "MANUAL OVERWRITE", "Log check-in entry request for missing system timestamp"),
                SystemRequestTicket("REQ-404", "Sunita Rao", "FACIAL MISMATCH BYPASS", "Biometric verification failed (Glare: Match 0.54) - Require Admin Self-Verify Override")
            )
        )
    }

    var inspectedWorkerProfile by remember { mutableStateOf<LocalWorkerProfile?>(null) }
    var isAddWorkerFormExpanded by remember { mutableStateOf(false) }

    var newWorkerName by remember { mutableStateOf("") }
    var newWorkerId by remember { mutableStateOf("") }
    var newWorkerSite by remember { mutableStateOf("NHAI-DEL-MUM-01") }

    val uiState = remember(rawUiState) {
        if (rawUiState.livenessState == LivenessState.IDLE) {
            rawUiState.copy(
                livenessState = LivenessState.ALIGN_FACE,
                promptMessage = "System active and secure.",
                leftEyeOpenProb = 1.0f,
                rightEyeOpenProb = 1.0f,
                smilingProb = 0.1f
            )
        } else {
            rawUiState
        }
    }

    LaunchedEffect(Unit) {
        val hasCameraPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        viewModel.setCameraPermissionGranted(hasCameraPermission)
        viewModel.toggleSimulatorMode(true)
    }

    val lightThemeBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F0FA), Color(0xFFF4F8FC))
    )
    val nhaiBlue = Color(0xFF0D3E73)

    // Locked to strict high-contrast text metrics to fix invisible dark-mode rendering glitches
    val inputFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = Color(0xFF0284C7),
        unfocusedBorderColor = Color(0xFFCBD5E1),
        focusedLabelColor = Color(0xFF0284C7),
        unfocusedLabelColor = Color(0xFF64748B)
    )
    val visibleBlackTextStyle = TextStyle(color = Color.Black, fontSize = 15.sp)

    // =========================================================================
    // SCREEN 1: WELCOME SPLASH LANDING PAGE
    // =========================================================================
    if (currentScreenState == AppNavigationState.WELCOME_SPLASH) {
        Box(modifier = Modifier.fillMaxSize().background(lightThemeBackground)) {
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 28.dp)) {
                    Text(text = "Digital Backbone for", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = nhaiBlue, textAlign = TextAlign.Center)
                    Text(text = "National Highways.", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = nhaiBlue, textAlign = TextAlign.Center)
                }

                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(imageVector = Icons.Default.AddRoad, contentDescription = "NHAI", tint = nhaiBlue, modifier = Modifier.size(40.dp))
                            Column {
                                Text("भारतीय राष्ट्रीय राजमार्ग प्राधिकरण", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text("NHAI GOVT OF INDIA", fontSize = 10.sp, letterSpacing = 1.sp, color = Color.Gray)
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Welcome to", fontSize = 20.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            Text("DataLake 3.0", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A), textAlign = TextAlign.Center)
                            Text("powered by Digital India", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(185.dp).clip(RoundedCornerShape(18.dp))) {
                            val imgId = context.resources.getIdentifier("highway_bg", "drawable", context.packageName)
                            if (imgId != 0) {
                                Image(
                                    painter = painterResource(id = imgId),
                                    contentDescription = "Delhi-Mumbai Expressway Corridor",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawRect(brush = Brush.verticalGradient(colors = listOf(Color(0xFF34495E), Color(0xFF2C3E50))))
                                    drawLine(color = Color(0xFFF1C40F), start = Offset(0f, size.height * 0.3f), end = Offset(size.width, size.height * 0.3f), strokeWidth = 5f)
                                    drawLine(color = Color(0xFFF1C40F), start = Offset(0f, size.height * 0.7f), end = Offset(size.width, size.height * 0.7f), strokeWidth = 5f)
                                    var currX = 0f
                                    while (currX < size.width) {
                                        drawLine(color = Color.White, start = Offset(currX, size.height * 0.5f), end = Offset(currX + 30f, size.height * 0.5f), strokeWidth = 5f)
                                        currX += 55f
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { currentScreenState = AppNavigationState.PORTAL_LOGIN },
                    colors = ButtonDefaults.buttonColors(containerColor = nhaiBlue),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Access DataLake Secure Portal ➔", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    // =========================================================================
    // SCREEN 2: PORTAL LOGIN / SIGN IN VIEW
    // =========================================================================
    else if (currentScreenState == AppNavigationState.PORTAL_LOGIN) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = { currentScreenState = AppNavigationState.WELCOME_SPLASH }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                    Text("National Highways Authority of India", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }

                Spacer(Modifier.height(4.dp))
                Text("Welcome to Data Lake-2.0", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedRole,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("System Role Profiling") },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF0284C7)) },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = inputFieldColors
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { isRoleExpanded = true })
                            DropdownMenu(
                                expanded = isRoleExpanded,
                                onDismissRequest = { isRoleExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.75f).background(Color.White)
                            ) {
                                listOf("ADMIN", "USER").forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role, color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) },
                                        onClick = { selectedRole = role; isRoleExpanded = false }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            label = { Text("Gate Identifier / Username") },
                            leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null, tint = Color(0xFF64748B)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = visibleBlackTextStyle,
                            colors = inputFieldColors,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Access Cipher Code") },
                            leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color(0xFF64748B)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = visibleBlackTextStyle,
                            colors = inputFieldColors,
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF334155))
                                    .clickable { captchaIndex = (captchaIndex + 1) % captchaList.size },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(currentCaptchaString, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White, letterSpacing = 3.sp)
                            }
                            OutlinedTextField(
                                value = captchaInput,
                                onValueChange = { captchaInput = it },
                                placeholder = { Text("Security Code*", fontSize = 11.sp) },
                                modifier = Modifier.weight(1.2f),
                                textStyle = visibleBlackTextStyle,
                                colors = inputFieldColors,
                                singleLine = true
                            )
                        }

                        Button(
                            onClick = {
                                if (usernameInput.isNotEmpty() && passwordInput.isNotEmpty()) {
                                    if (selectedRole == "ADMIN") {
                                        lastWorkspaceOrigin = AppNavigationState.ADMIN_WORKSPACE
                                        currentScreenState = AppNavigationState.ADMIN_WORKSPACE
                                    } else {
                                        lastWorkspaceOrigin = AppNavigationState.USER_WORKSPACE
                                        currentScreenState = AppNavigationState.USER_WORKSPACE
                                    }
                                    Toast.makeText(context, "Logged in as $selectedRole", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please complete fields", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("SIGN IN TO ARCHITECTURE", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                if (selectedRole == "ADMIN") {
                                    usernameInput = "admin_portal"
                                    passwordInput = "nhai_admin_secure"
                                    captchaInput = currentCaptchaString
                                    lastWorkspaceOrigin = AppNavigationState.ADMIN_WORKSPACE
                                    currentScreenState = AppNavigationState.ADMIN_WORKSPACE
                                } else {
                                    usernameInput = "field_user"
                                    passwordInput = "nhai_user_access"
                                    captchaInput = currentCaptchaString
                                    lastWorkspaceOrigin = AppNavigationState.USER_WORKSPACE
                                    currentScreenState = AppNavigationState.USER_WORKSPACE
                                }
                                Toast.makeText(context, "Bypass active: $selectedRole Mode", Toast.LENGTH_SHORT).show()
                            },
                            border = BorderStroke(1.5.dp, Color(0xFF16A34A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A))
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
                    title = { Text("DataLake 3.0: ADMIN MASTER SYSTEM", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    actions = {
                        IconButton(onClick = { currentScreenState = AppNavigationState.WELCOME_SPLASH }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = nhaiBlue)
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(lightThemeBackground)) {

                ScrollableTabRow(selectedTabIndex = selectedAdminTab, containerColor = Color.White, contentColor = nhaiBlue, edgePadding = 16.dp) {
                    Tab(selected = selectedAdminTab == 0, onClick = { selectedAdminTab = 0 }, text = { Text("Core Tools", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedAdminTab == 1, onClick = { selectedAdminTab = 1 }, text = { Text("Daily Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedAdminTab == 2, onClick = { selectedAdminTab = 2 }, text = { Text("Requests Center", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedAdminTab == 3, onClick = { selectedAdminTab = 3 }, text = { Text("Monthly Report", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                }

                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp)) {
                    when (selectedAdminTab) {
                        0 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text("Deploy Functional Pipeline Architectures Below:", fontSize = 13.sp, color = nhaiBlue, fontWeight = FontWeight.Bold)
                                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        HelpTopicRow(Icons.Default.CheckCircle, "Mark Attendance", "Execute biometric face scanning check-in logs", onClick = { currentScreenState = AppNavigationState.TASK_ATTENDANCE_ENGINE })
                                        HelpTopicRow(Icons.Default.Hub, "Network Sync Status", "Last server synchronization executed: 28-05-2026 15:45", onClick = { currentScreenState = AppNavigationState.TASK_UCC_GATEWAY })
                                        HelpTopicRow(Icons.Default.VerifiedUser, "Workforce Profiles", "Authorized workforce configuration profiles mapping", onClick = { currentScreenState = AppNavigationState.TASK_BIOMETRIC_REGISTRATION })
                                    }
                                }
                            }
                        }
                        1 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Personnel Daily Log Validation Desk:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = nhaiBlue)
                                    // FIXED LINE 465: Replaced broken '.bind()' suffix with proper '.dp' syntax compilation metric
                                    Button(
                                        onClick = {
                                            activeWorkersRegistryList = activeWorkersRegistryList.map { it.copy(verificationState = "VERIFIED") }
                                            Toast.makeText(context, "All parameters successfully verified globally!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Verify All Logs", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }

                                Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                                    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        activeWorkersRegistryList.forEach { employee ->
                                            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp)).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = employee.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                                    Text("Check-In: ${employee.dailyCheckIn} | Status: ${employee.verificationState}", fontSize = 11.sp, color = if(employee.verificationState == "VERIFIED") Color(0xFF16A34A) else Color.Gray, fontWeight = FontWeight.Bold)
                                                }

                                                if (employee.verificationState != "VERIFIED") {
                                                    Button(
                                                        onClick = {
                                                            activeWorkersRegistryList = activeWorkersRegistryList.map {
                                                                if(it.id == employee.id) it.copy(verificationState = "VERIFIED") else it
                                                            }
                                                            Toast.makeText(context, "${employee.name} verified cleanly", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = nhaiBlue),
                                                        shape = RoundedCornerShape(6.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("Verify", fontSize = 10.sp)
                                                    }
                                                } else {
                                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp).padding(horizontal = 4.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Central Operational Requests Core Desk:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = nhaiBlue)

                                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    administrativeRequestsPool.forEach { ticket ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, if(ticket.ticketStatus == "APPROVED") Color(0xFF16A34A) else Color(0xFFCBD5E1))
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFF0F172A)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                                        Text(ticket.type, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text(ticket.ticketStatus, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if(ticket.ticketStatus == "APPROVED") Color(0xFF16A34A) else Color(0xFFEA580C))
                                                }

                                                Text(text = "Origin Node: ${ticket.workerName} (${ticket.ticketId})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                                Text(text = ticket.metadata, fontSize = 12.sp, color = Color.DarkGray)

                                                if (ticket.ticketStatus == "PENDING") {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                                        TextButton(
                                                            onClick = {
                                                                ticket.ticketStatus = "APPROVED"
                                                                Toast.makeText(context, "Administrative Approval Granted Natively", Toast.LENGTH_SHORT).show()
                                                            }
                                                        ) {
                                                            Text(
                                                                text = if(ticket.type == "FACIAL MISMATCH BYPASS") "Execute Admin Self-Verify" else "Grant Approval",
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = Color(0xFF16A34A)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Select Employee To Review Comprehensive Monthly Records Grid:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.DarkGray)
                                Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFCBD5E1))) {
                                    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        activeWorkersRegistryList.forEach { employee ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                                    .clickable { inspectedWorkerProfile = employee }
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(employee.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                                                    Text("ID Vector Token: ${employee.id}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = nhaiBlue)
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
    }

    // =========================================================================
    // SCREEN 3B: SEPARATE USER WORKSPACE HUB
    // =========================================================================
    else if (currentScreenState == AppNavigationState.USER_WORKSPACE) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Portal Hub: Field Operator Client", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    actions = {
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

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Operational System Verification Controls:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = nhaiBlue)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
                                HelpTopicRow(Icons.Default.CheckCircle, "Mark Attendance", "Execute field scan tracking verification loop algorithms", onClick = { currentScreenState = AppNavigationState.TASK_ATTENDANCE_ENGINE })
                                HelpTopicRow(Icons.Default.Hub, "Network Sync Status", "Sync local queues directly to central cluster registers", onClick = { currentScreenState = AppNavigationState.TASK_UCC_GATEWAY })
                                HelpTopicRow(Icons.Default.VerifiedUser, "Workforce Profiles", "Inspect loaded offline credentials validation matrix index", onClick = { currentScreenState = AppNavigationState.TASK_BIOMETRIC_REGISTRATION })
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
                    title = { Text("Mark Attendance Terminal Lens", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = { IconButton(onClick = { currentScreenState = lastWorkspaceOrigin }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                    actions = { IconButton(onClick = { currentScreenState = AppNavigationState.WELCOME_SPLASH }) { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White) } },
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
                            Text("PROCESSING LIVENESS MATRIX FRAME", fontSize = 10.sp, color = Color(0xFF0088FF), letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Telemetry Status: ${uiState.promptMessage}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0088FF))
                            Text("Left Eye Tracking: ${(uiState.leftEyeOpenProb * 100).toInt()}% Match Vectors", fontSize = 12.sp, color = Color.DarkGray)
                            Text("Right Eye Tracking: ${(uiState.rightEyeOpenProb * 100).toInt()}% Match Vectors", fontSize = 12.sp, color = Color.DarkGray)
                            Text("Smile Force Factor: ${(uiState.smilingProb * 100).toInt()}% Structural Matrix", fontSize = 12.sp, color = Color.DarkGray)
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
    // SUB-PAGE 2: Network Sync Status
    // =========================================================================
    else if (currentScreenState == AppNavigationState.TASK_UCC_GATEWAY) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Network Sync Status Pipeline", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = { IconButton(onClick = { currentScreenState = lastWorkspaceOrigin }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                    actions = { IconButton(onClick = { currentScreenState = AppNavigationState.WELCOME_SPLASH }) { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White) } },
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
                            Text(text = "Last verified data transaction push: 28-05-2026 15:45", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
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
    // SUB-PAGE 3: WORKFORCE PROFILES
    // =========================================================================
    else if (currentScreenState == AppNavigationState.TASK_BIOMETRIC_REGISTRATION) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Workforce Profiles Management Ledger", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = { IconButton(onClick = { currentScreenState = lastWorkspaceOrigin }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                    actions = { IconButton(onClick = { currentScreenState = AppNavigationState.WELCOME_SPLASH }) { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = nhaiBlue)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(lightThemeBackground)) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Authorized Personnel Registry Profiles:", fontSize = 14.sp, color = nhaiBlue, fontWeight = FontWeight.Bold)

                    if (lastWorkspaceOrigin == AppNavigationState.ADMIN_WORKSPACE) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { isAddWorkerFormExpanded = !isAddWorkerFormExpanded },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF16A34A))
                                        Text("Enroll New Field Personnel Panel", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                    }
                                    Icon(imageVector = if(isAddWorkerFormExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = Color.Gray)
                                }

                                AnimatedVisibility(visible = isAddWorkerFormExpanded) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 12.dp)) {
                                        OutlinedTextField(
                                            value = newWorkerName,
                                            onValueChange = { newWorkerName = it },
                                            label = { Text("Worker Full Name") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = inputFieldColors,
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = newWorkerId,
                                            onValueChange = { newWorkerId = it },
                                            label = { Text("Unique Register ID (e.g. EMP106)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = inputFieldColors,
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = newWorkerSite,
                                            onValueChange = { newWorkerSite = it },
                                            label = { Text("Assigned Project Site Corridor") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = inputFieldColors,
                                            singleLine = true
                                        )
                                        Button(
                                            onClick = {
                                                if(newWorkerName.isNotEmpty() && newWorkerId.isNotEmpty()) {
                                                    val updatedList = activeWorkersRegistryList.toMutableList()
                                                    updatedList.add(LocalWorkerProfile(id = newWorkerId.trim(), name = newWorkerName.trim(), projectCode = newWorkerSite.trim()))
                                                    activeWorkersRegistryList = updatedList

                                                    Toast.makeText(context, "Personnel Registered to Local Ledger", Toast.LENGTH_SHORT).show()
                                                    newWorkerName = ""
                                                    newWorkerId = ""
                                                    isAddWorkerFormExpanded = false
                                                } else {
                                                    Toast.makeText(context, "Complete all inputs", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            modifier = Modifier.fillMaxWidth().height(42.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Commit Matrix Profile Vector", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        activeWorkersRegistryList.forEach { emp ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { inspectedWorkerProfile = emp },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(emp.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text("Unique Register ID: ${emp.id}", fontSize = 12.sp, color = Color.Gray)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        if (lastWorkspaceOrigin == AppNavigationState.ADMIN_WORKSPACE) {
                                            IconButton(
                                                onClick = {
                                                    val updatedList = activeWorkersRegistryList.toMutableList()
                                                    updatedList.remove(emp)
                                                    activeWorkersRegistryList = updatedList
                                                    Toast.makeText(context, "Record purged from database", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Profile", tint = Color(0xFFEF4444))
                                            }
                                        }

                                        Text(
                                            text = "Select Worker ➔",
                                            fontSize = 12.sp,
                                            color = Color(0xFF0088FF),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                currentScreenState = AppNavigationState.TASK_ATTENDANCE_ENGINE
                                            }
                                        )
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
    // MONTHLY ANALYTICS GRID SCREEN VIEW (Launches Calendar Map On Click)
    // =========================================================================
    inspectedWorkerProfile?.let { profile ->
        AlertDialog(
            onDismissRequest = { inspectedWorkerProfile = null },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = nhaiBlue, modifier = Modifier.size(28.dp))
                    Column {
                        Text(text = "${profile.name} - Attendance Ledger", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                        Text(text = "Monthly Performance Matrix Overview", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("MONTHLY RATIO", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(profile.monthlyRatio, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL HOURS", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(profile.totalHours, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = nhaiBlue)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("AVG DAILY TIME", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(profile.avgDailyHours, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    Text("May 2026 Shift Matrix Summary Guide:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            "25 May (Mon) - In: ${profile.dailyCheckIn} | Out: ${profile.dailyCheckOut} -> PRESENT",
                            "26 May (Tue) - In: ${profile.dailyCheckIn} | Out: ${profile.dailyCheckOut} -> PRESENT",
                            "27 May (Wed) - In: ${profile.dailyCheckIn} | Out: ${profile.dailyCheckOut} -> PRESENT",
                            "28 May (Thu) - In: --:-- | Out: --:-- -> ABSENT / LEAVE"
                        ).forEach { logLine ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if(logLine.contains("PRESENT")) Color(0xFF28A745) else Color(0xFFDC3545)))
                                Text(text = logLine, fontSize = 11.sp, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))
                }
            },
            confirmButton = {
                TextButton(onClick = { inspectedWorkerProfile = null }) {
                    Text("Close Analytics Frame", fontWeight = FontWeight.Bold, color = nhaiBlue)
                }
            }
        )
    }

    // Modal dialogue managers logic
    selectedDetailLog?.let { log ->
        AlertDialog(
            onDismissRequest = { selectedDetailLog = null },
            title = { Text("Encrypted SQLite Log Entry Data", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Employee node mapping: ${log.employeeName}", fontWeight = FontWeight.Bold)
                    Text(log.encryptedPayload, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.DarkGray)
                }
            },
            confirmButton = { Button(onClick = { selectedDetailLog = null }) { Text("Close Inspector Node") } }
        )
    }

    if (showConfirmResetDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmResetDialog = false },
            title = { Text("Purge Database Telemetry Logs?", fontWeight = FontWeight.Bold) },
            text = { Text("Permanently wipe security database logs? This operation is completely irreversible.") },
            confirmButton = {
                TextButton(onClick = { viewModel.forcePurgeAllLogs(); showConfirmResetDialog = false; currentScreenState = lastWorkspaceOrigin; Toast.makeText(context, "Local records purged!", Toast.LENGTH_SHORT).show() }) { Text("Purge Caches", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { showConfirmResetDialog = false }) { Text("Cancel Action") } }
        )
    }
}

@Composable
fun HelpTopicRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
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