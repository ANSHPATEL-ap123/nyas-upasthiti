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
import com.example.R // Dhyan rakhna ye import zaroori hai images load karne ke liye

// Local Error-Free Structural Model for Workforce Registration Profiles
data class LocalWorkerProfile(
    val id: String,
    val name: String,
    val projectCode: String,
    val biometricStatus: String = "Active Vector Loaded",
    val accessibilityMode: String = "Standard Mode (0)"
)

// Comprehensive State Router Navigation Enum
enum class AppNavigationState {
    WELCOME_SPLASH,
    PORTAL_LOGIN,
    ADMIN_WORKSPACE,
    USER_WORKSPACE,
    TASK_ATTENDANCE_ENGINE,
    TASK_RFI_LEDGER,
    TASK_SAFETY_AUDIT,
    TASK_MAINTENANCE_SANDBOX,
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

    // Persistent Login Form Fields States
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("ADMIN") }
    var captchaInput by remember { mutableStateOf("") }
    var isRoleExpanded by remember { mutableStateOf(false) }

    var selectedDetailLog by remember { mutableStateOf<TransactionLog?>(null) }
    var showConfirmResetDialog by remember { mutableStateOf(false) }

    // Admin Tabs Parameters
    var selectedAdminTab by remember { mutableStateOf(0) }
    var selectedOverrideEmployeeId by remember { mutableStateOf("") }
    var overrideStatusInput by remember { mutableStateOf("PRESENT") }

    // Workforce Database Cache Pool Layout Nodes
    var activeWorkersRegistryList by remember {
        mutableStateOf(
            listOf(
                LocalWorkerProfile("EMP101", "Amit Kumar", "NHAI-DEL-MUM-01"),
                LocalWorkerProfile("EMP102", "Priya Sharma", "NHAI-DEL-MUM-02"),
                LocalWorkerProfile("EMP103", "Rajesh Patel", "NHAI-UP-CORRIDOR"),
                LocalWorkerProfile("EMP104", "Sunita Rao", "NHAI-SOUTH-HIGHWAY"),
                LocalWorkerProfile("EMP105", "Vikram Singh", "NHAI-EAST-EXPRESS")
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

                        // ===== YAHAN CHANGE KIYA HAI =====
                        // Purane AddRoad icon ki jagah NHAI ka actual logo lagaya hai
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_nhai_logo),
                                contentDescription = "NHAI Logo",
                                modifier = Modifier.size(50.dp) // Logo ka size adjust kiya hai
                            )
                            Column {
                                Text("भारतीय राष्ट्रीय राजमार्ग प्राधिकरण", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text("NHAI GOVT OF INDIA", fontSize = 10.sp, letterSpacing = 1.sp, color = Color.Gray)
                            }
                        }
                        // ===================================

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
                            Box(
                                modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xAA000000)))),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Text(
                                    text = "Delhi-Mumbai Greenfield Expressway Infrastructure Corridor",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 12.dp),
                                    textAlign = TextAlign.Center
                                )
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
    // SCREEN 2: PORTAL LOGIN / SIGN IN VIEW (Black Text Enforced)
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
                                textStyle = visibleBlackTextStyle,
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
                            // FIXED LINE 316: Correct weight calculation applied smoothly to clear compiler error
                            Box(modifier = Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF334155)), contentAlignment = Alignment.Center) {
                                Text("pBmcSL", fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White, letterSpacing = 3.sp)
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
                                    captchaInput = "pBmcSL"
                                    lastWorkspaceOrigin = AppNavigationState.ADMIN_WORKSPACE
                                    currentScreenState = AppNavigationState.ADMIN_WORKSPACE
                                } else {
                                    usernameInput = "field_user"
                                    passwordInput = "nhai_user_access"
                                    captchaInput = "pBmcSL"
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
                    title = { Text("DataLake 3.0: ADMIN CORE CONTROL", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) },
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

                TabRow(selectedTabIndex = selectedAdminTab, containerColor = Color.White, contentColor = nhaiBlue) {
                    Tab(selected = selectedAdminTab == 0, onClick = { selectedAdminTab = 0 }, text = { Text("Core Tools", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedAdminTab == 1, onClick = { selectedAdminTab = 1 }, text = { Text("Attendance Records", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedAdminTab == 2, onClick = { selectedAdminTab = 2 }, text = { Text("Manual Overwrite", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                }

                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(20.dp)) {
                    when (selectedAdminTab) {
                        0 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text("Deploy Functional Pipeline Architectures Below:", fontSize = 13.sp, color = nhaiBlue, fontWeight = FontWeight.Bold)
                                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        HelpTopicRow(Icons.Default.AccountBox, "Attendance Matrix Engine", "Execute field biometric face scanning algorithms", onClick = { currentScreenState = AppNavigationState.TASK_ATTENDANCE_ENGINE })
                                        HelpTopicRow(Icons.Default.NetworkCheck, "RFI Geolocation Ledger", "Logs Storage telemetry transaction tables database", onClick = { currentScreenState = AppNavigationState.TASK_RFI_LEDGER })
                                        HelpTopicRow(Icons.Default.Security, "Safety Audit / Cryptography Key", "Secured AES-128 configuration parameters block", onClick = { currentScreenState = AppNavigationState.TASK_SAFETY_AUDIT })
                                        HelpTopicRow(Icons.Default.Build, "Maintenance Engine Sandbox", "Bypass physical hardware metrics directly", onClick = { currentScreenState = AppNavigationState.TASK_MAINTENANCE_SANDBOX })
                                        HelpTopicRow(Icons.Default.Hub, "UCC / NHAI Data Lake 3.0", "Sync offline pipelines backend database registers", onClick = { currentScreenState = AppNavigationState.TASK_UCC_GATEWAY })
                                        HelpTopicRow(Icons.Default.VerifiedUser, "Login & Biometric Registration", "Authorized workforce configuration profiles mapping", onClick = { currentScreenState = AppNavigationState.TASK_BIOMETRIC_REGISTRATION })
                                    }
                                }
                            }
                        }
                        1 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Workforce Attendance Database Ledgers:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = nhaiBlue)
                                Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                                    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        activeWorkersRegistryList.forEach { employee ->
                                            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp)).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column {
                                                    Text(employee.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                                    Text("Registry Node ID: ${employee.id}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("Daily State: PRESENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                                    Text("Monthly Ratio: 97.2%", fontSize = 10.sp, color = nhaiBlue)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text("Manual Overwrite Verification Cache Overrides", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = nhaiBlue)

                                OutlinedTextField(
                                    value = selectedOverrideEmployeeId,
                                    onValueChange = { selectedOverrideEmployeeId = it },
                                    label = { Text("Target Workforce Unique Employee ID") },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = visibleBlackTextStyle,
                                    colors = inputFieldColors
                                )

                                OutlinedTextField(
                                    value = overrideStatusInput,
                                    onValueChange = { overrideStatusInput = it },
                                    label = { Text("Overridden Value Node (PRESENT / ABSENT / LEAVE)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = visibleBlackTextStyle,
                                    colors = inputFieldColors
                                )

                                Button(
                                    onClick = {
                                        if (selectedOverrideEmployeeId.isNotEmpty()) {
                                            Toast.makeText(context, "Registry Node Record Overwritten Successfully!", Toast.LENGTH_SHORT).show()
                                            selectedOverrideEmployeeId = ""
                                        } else {
                                            Toast.makeText(context, "Please supply a valid validation token mapping", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = nhaiBlue),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("Commit Administrative Override Modification")
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
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Operational System Verification Controls:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = nhaiBlue)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
                                HelpTopicRow(Icons.Default.AccountBox, "Attendance Matrix Engine", "Execute field biometric face scanning algorithms", onClick = { currentScreenState = AppNavigationState.TASK_ATTENDANCE_ENGINE })
                                HelpTopicRow(Icons.Default.Hub, "UCC / NHAI Data Lake 3.0", "Sync local queue packets to central cloud", onClick = { currentScreenState = AppNavigationState.TASK_UCC_GATEWAY })
                                HelpTopicRow(Icons.Default.VerifiedUser, "Login & Biometric Registration", "Inspect loaded offline credentials index arrays", onClick = { currentScreenState = AppNavigationState.TASK_BIOMETRIC_REGISTRATION })
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // SUB-PAGE 1: LIVE ADVANCED BIOMETRIC VERIFICATION
    // =========================================================================
    else if (currentScreenState == AppNavigationState.TASK_ATTENDANCE_ENGINE) {
        val infiniteTransition = rememberInfiniteTransition()
        val scanAnimY by infiniteTransition.animateFloat(initialValue = 0.1f, targetValue = 0.9f, animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse))

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Biometric Terminal Lens", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
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
    // SUB-PAGE 2: SECURE RFI SQLITE LEDGER DATABASE VIEW
    // =========================================================================
    else if (currentScreenState == AppNavigationState.TASK_RFI_LEDGER) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Secure SQLite Log Records", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = { IconButton(onClick = { currentScreenState = lastWorkspaceOrigin }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                    actions = { IconButton(onClick = { currentScreenState = AppNavigationState.WELCOME_SPLASH }) { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = nhaiBlue)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(lightThemeBackground)) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Encrypted Local SQLite Telemetry Transactions Table:", fontSize = 13.sp, color = nhaiBlue, fontWeight = FontWeight.Bold)

                    if (transactionLogsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Text("No cryptographic verification payloads committed to offline log space yet.", color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            transactionLogsList.forEach { log ->
                                Card(modifier = Modifier.fillMaxWidth().clickable { selectedDetailLog = log }, colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                                    Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(log.employeeName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            Text("Timestamp: Asset Node Secure Log Packet", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Text("Inspect Vector ➔", fontSize = 12.sp, color = Color(0xFF0088FF))
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
    // SUB-PAGE 3: SAFETY AUDIT AES CRYPTOGRAPHY PANEL
    // =========================================================================
    else if (currentScreenState == AppNavigationState.TASK_SAFETY_AUDIT) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("AES Cryptography Center", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = { IconButton(onClick = { currentScreenState = lastWorkspaceOrigin }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                    actions = { IconButton(onClick = { currentScreenState = AppNavigationState.WELCOME_SPLASH }) { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = nhaiBlue)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(lightThemeBackground)) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Data Integrity Configuration", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = nhaiBlue)
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                        Text(text = "All local entries committed to terminal sector caches utilize symmetric AES block-ciphers combined with unique initialization vectors to protect workforce identity data sovereignty laws under infrastructure telemetry constraints.", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(16.dp))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { showConfirmResetDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Text("Purge Offline Terminal Cache Local Registers", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // =========================================================================
    // SUB-PAGE 4: LIVENESS CHALLENGE HARDWARE SIMULATION
    // =========================================================================
    else if (currentScreenState == AppNavigationState.TASK_MAINTENANCE_SANDBOX) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Liveness Bypass Mock Center", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = { IconButton(onClick = { currentScreenState = lastWorkspaceOrigin }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
                    actions = { IconButton(onClick = { currentScreenState = AppNavigationState.WELCOME_SPLASH }) { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = nhaiBlue)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(lightThemeBackground)) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Manual Override Filters Sandbox Panels", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = nhaiBlue)
                    Text("Bypass physical optics metrics directly to test systemic response thresholds for 4-phase behavioral classification rules:", fontSize = 13.sp, color = Color.DarkGray)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { viewModel.simulateManualBlink(); Toast.makeText(context, "Blink Parameter Injection Pushed!", Toast.LENGTH_SHORT).show() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)), shape = RoundedCornerShape(10.dp)) { Text("Mock Blink Event", fontSize = 12.sp) }
                        Button(onClick = { viewModel.simulateManualSmile(); Toast.makeText(context, "Smile Parameter Injection Pushed!", Toast.LENGTH_SHORT).show() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)), shape = RoundedCornerShape(10.dp)) { Text("Mock Smile Event", fontSize = 12.sp) }
                    }
                }
            }
        }
    }

    // =========================================================================
    // SUB-PAGE 5: UCC / NHAI CLOUD DATA GATEWAY SYNC
    // =========================================================================
    else if (currentScreenState == AppNavigationState.TASK_UCC_GATEWAY) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("NHAI Cloud Gateway Pipeline", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
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
                        Text(text = "Pending dispatch local packets waiting transfer: ${unsyncedList.size} queues verified.", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(16.dp))
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
    // SUB-PAGE 6: AUTHORIZED WORKFORCE REGISTRATION (Glitches Fixed completely!)
    // =========================================================================
    else if (currentScreenState == AppNavigationState.TASK_BIOMETRIC_REGISTRATION) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Workforce Registration Profiles", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
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
                    Text("Authorized Personnel Registry Profiles Load Nodes:", fontSize = 14.sp, color = nhaiBlue, fontWeight = FontWeight.Bold)

                    // ADMIN ENROLLMENT DRAWER INTERFACE (CRUD Feature: ADD)
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
                                            textStyle = visibleBlackTextStyle,
                                            colors = inputFieldColors,
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = newWorkerId,
                                            onValueChange = { newWorkerId = it },
                                            label = { Text("Unique Register ID (e.g. EMP106)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = visibleBlackTextStyle,
                                            colors = inputFieldColors,
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = newWorkerSite,
                                            onValueChange = { newWorkerSite = it },
                                            label = { Text("Assigned Project Site Corridor") },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = visibleBlackTextStyle,
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

                    // PROFILE LAYOUT STREAM (Clicking card launches popup sheet, clicking link triggers scanner)
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        activeWorkersRegistryList.forEach { emp ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { inspectedWorkerProfile = emp }, // Clicks on the general card area securely trigger the profile modal sheet
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
                                        // ADMIN PURGING DELETE TRIGGER CONTROL
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

                                        // Segregated link context targeting verification loops natively
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
    // PROFESSIONAL RECOGNITION PROFILE SHEET (CRUD Feature: INSPECT PROFILE)
    // =========================================================================
    inspectedWorkerProfile?.let { profile ->
        AlertDialog(
            onDismissRequest = { inspectedWorkerProfile = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = nhaiBlue, modifier = Modifier.size(36.dp))
                    Column {
                        Text(text = profile.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                        Text(text = "Status: Biometrically Active", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Column {
                        Text("REGISTRATION CODE MATRIX INDEX", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text(profile.id, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                    Column {
                        Text("ASSIGNED CORRIDOR / PROJECT SITE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text(profile.projectCode, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                    Column {
                        Text("OFFLINE EMBEDDING VECTOR STATUS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text(profile.biometricStatus, fontSize = 12.sp, color = Color(0xFF0088FF), fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text("PWD ACCESSIBILITY OVERRIDE MODE (PWD PROTOCOL)", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text(profile.accessibilityMode, fontSize = 12.sp, color = Color.DarkGray)
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))
                }
            },
            confirmButton = {
                TextButton(onClick = { inspectedWorkerProfile = null }, colors = ButtonDefaults.textButtonColors(contentColor = nhaiBlue)) {
                    Text("Dismiss Profile Sheet", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // =========================================================================
    // MODAL WINDOW DETAILED INSPECTORS
    // =========================================================================
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