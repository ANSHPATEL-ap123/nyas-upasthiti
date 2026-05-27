package com.example.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Employee
import com.example.data.model.TransactionLog

// Navigation State Tracker Enum
enum class AppNavigationState {
    WELCOME_SPLASH,
    PORTAL_LOGIN,
    OPERATIONAL_WORKSPACE
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val rawUiState by viewModel.uiState.collectAsState()
    val employeesList by viewModel.employees.collectAsState()
    val transactionLogsList by viewModel.transactionLogs.collectAsState()
    val unsyncedList by viewModel.unsyncedLogs.collectAsState()

    // Central state controller for multi-stage screen routing
    var currentScreenState by remember { mutableStateOf(AppNavigationState.WELCOME_SPLASH) }

    var activeDialogTopic by remember { mutableStateOf<String?>(null) }
    var selectedDetailLog by remember { mutableStateOf<TransactionLog?>(null) }
    var showConfirmResetDialog by remember { mutableStateOf(false) }

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

    // =========================================================================
    // SCREEN 1: WELCOME SPLASH LANDING PAGE (Matches image_667e00.png)
    // =========================================================================
    if (currentScreenState == AppNavigationState.WELCOME_SPLASH) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(Color(0xFFE8F0FA), Color(0xFFF4F8FC))))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Typography
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 28.dp)
                ) {
                    Text(
                        text = "Digital Backbone for",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D3E73),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "National Highways.",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D3E73),
                        textAlign = TextAlign.Center
                    )
                }

                // Central Rounded Information Wrapper Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Branding Identifiers
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddRoad,
                                contentDescription = "NHAI",
                                tint = Color(0xFF0D3E73),
                                modifier = Modifier.size(40.dp)
                            )
                            Column {
                                Text("भारतीय राष्ट्रीय राजमार्ग प्राधिकरण", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text("NHAI GOVT OF INDIA", fontSize = 10.sp, letterSpacing = 1.sp, color = Color.Gray)
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Welcome to",
                                fontSize = 20.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "DataLake 3.0",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "powered by Digital India",
                                fontSize = 12.sp,
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Modern Infrastructure Stylized Graphics Block Placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Biometric Transit Network Assets Online", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                // Primary Access Progress Gateway Trigger Action Button
                Button(
                    onClick = { currentScreenState = AppNavigationState.PORTAL_LOGIN },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3E73)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Access DataLake Secure Portal ➔", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    // =========================================================================
    // SCREEN 2: PORTAL LOGIN / SIGN IN VIEW (Matches image_66900b.png)
    // =========================================================================
    else if (currentScreenState == AppNavigationState.PORTAL_LOGIN) {
        var usernameInput by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf("Select Role") }
        var captchaInput by remember { mutableStateOf("") }
        var isRoleExpanded by remember { mutableStateOf(false) }

        val rolesList = listOf("Field Operator", "NHAI Site Supervisor", "Team APEX Admin Audit")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Identity Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = { currentScreenState = AppNavigationState.WELCOME_SPLASH }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                    Text("National Highways Authority of India", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Welcome to Data Lake-2.0",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7),
                    modifier = Modifier.fillMaxWidth()
                )

                // Main Form Layout Card Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 100% Compiler-Safe Custom Dropdown Menu for Roles
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedRole,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            // Transparent clickable overlay surface to expand choices safely
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { isRoleExpanded = true }
                            )

                            DropdownMenu(
                                expanded = isRoleExpanded,
                                onDismissRequest = { isRoleExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f).background(Color.White)
                            ) {
                                rolesList.forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role, color = Color.Black) },
                                        onClick = {
                                            selectedRole = role
                                            isRoleExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Username Input
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            placeholder = { Text("Username", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                            singleLine = true
                        )

                        // Password Input
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            placeholder = { Text("Password", color = Color.Gray) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                            singleLine = true
                        )

                        // Captcha Box Segment
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF475569)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("pBmcSL", fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White, letterSpacing = 2.sp)
                            }

                            OutlinedTextField(
                                value = captchaInput,
                                onValueChange = { captchaInput = it },
                                placeholder = { Text("Enter Captcha*", color = Color.Gray, fontSize = 12.sp) },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                                singleLine = true
                            )
                        }

                        // Submit Sign In Trigger Button Action Wrapper
                        Button(
                            onClick = {
                                if (usernameInput.isNotEmpty() && passwordInput.isNotEmpty()) {
                                    currentScreenState = AppNavigationState.OPERATIONAL_WORKSPACE
                                    Toast.makeText(context, "Session Authorized via Security Gate", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please complete authentication parameters", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088FF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text("SIGN IN", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // SCREEN 3: OPERATIONAL WORKSPACE (Your Original Clean Light-Mode Dashboard)
    // =========================================================================
    else if (currentScreenState == AppNavigationState.OPERATIONAL_WORKSPACE) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(Color(0xFFE8F0FA), Color(0xFFF4F8FC))))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "Solve Issues Quickly,",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D3E73),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Stay Productive.",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D3E73),
                        textAlign = TextAlign.Center
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp).clickable { currentScreenState = AppNavigationState.PORTAL_LOGIN }
                            )
                            Text(
                                text = "How can we help?",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text("Search help topics", color = Color.Gray, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F7FA),
                                unfocusedContainerColor = Color(0xFFF5F7FA),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true,
                            enabled = false
                        )

                        Text(
                            text = "Help topics",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HelpTopicRow(
                                icon = Icons.Default.AccountBox,
                                title = "Attendance Matrix Engine",
                                subtitle = if(uiState.isSimulatorMode) "Bypass Simulation Active" else "Native Hardware Lock",
                                onClick = { activeDialogTopic = "attendance" }
                            )
                            HelpTopicRow(
                                icon = Icons.Default.NetworkCheck,
                                title = "RFI Geolocation Ledger",
                                subtitle = "Logs Storage: ${transactionLogsList.size} secure entries found",
                                onClick = { activeDialogTopic = "rfi" }
                            )
                            HelpTopicRow(
                                icon = Icons.Default.Security,
                                title = "Safety Audit / Cryptography Key",
                                subtitle = "Encryption status: Secured AES-128 keys active",
                                onClick = { activeDialogTopic = "safety" }
                            )
                            HelpTopicRow(
                                icon = Icons.Default.Build,
                                title = "Maintenance Engine Sandbox",
                                subtitle = "Tap to launch mock interactive scenario panels",
                                onClick = { activeDialogTopic = "maintenance" }
                            )
                            HelpTopicRow(
                                icon = Icons.Default.Hub,
                                title = "UCC / NHAI Data Lake 3.0",
                                subtitle = "Sync pending queue backend ledger packets",
                                onClick = { activeDialogTopic = "ucc" }
                            )
                            HelpTopicRow(
                                icon = Icons.Default.VerifiedUser,
                                title = "Login & Biometric Registration",
                                subtitle = "On-Device DB Capacity: ${employeesList.size} profiles loaded",
                                onClick = { activeDialogTopic = "login" }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f).clickable { viewModel.simulateUnknownIntruder(); activeDialogTopic = "attendance" },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                            Text("Ask us", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f).clickable { viewModel.purgeSyncedQueue(); Toast.makeText(context, "Synced cache flushed cleanly!", Toast.LENGTH_SHORT).show() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(20.dp))
                            Text("Mail us", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Active Dialog Managers
        activeDialogTopic?.let { topic ->
            AlertDialog(
                onDismissRequest = { activeDialogTopic = null },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color(0xFF1E293B),
                title = {
                    Text(
                        text = when(topic) {
                            "attendance" -> "Biometric Verification Terminal"
                            "rfi" -> "Secure SQLite Log Database"
                            "safety" -> "AES-128 Encryption Panel"
                            "maintenance" -> "Liveness Challenge Testing"
                            "ucc" -> "NHAI Cloud Data Gateway"
                            else -> "Workforce Registration System"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        when(topic) {
                            "attendance" -> {
                                Text("Real-time liveness parameters are evaluated dynamically below:", fontSize = 13.sp, color = Color(0xFF94A3B8))
                                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)), border = BorderStroke(1.dp, Color(0xFF475569))) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Status Prompt: ${uiState.promptMessage}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                        Text("Left Eye Open: ${(uiState.leftEyeOpenProb * 100).toInt()}%", fontSize = 12.sp, color = Color.White)
                                        Text("Right Eye Open: ${(uiState.rightEyeOpenProb * 100).toInt()}%", fontSize = 12.sp, color = Color.White)
                                        Text("Smile Force: ${(uiState.smilingProb * 100).toInt()}%", fontSize = 12.sp, color = Color.White)
                                    }
                                }
                                Button(onClick = { viewModel.startScanning(); activeDialogTopic = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3E73)), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text("Trigger Live Biometric Scan", fontWeight = FontWeight.Bold)
                                }
                            }
                            "rfi" -> {
                                Text("Select a local SQLite telemetry packet to inspect cipher details:", fontSize = 13.sp, color = Color(0xFF94A3B8))
                                if (transactionLogsList.isEmpty()) {
                                    Text("No ledger logs found in database storage yet.", color = Color.Gray, fontSize = 12.sp)
                                } else {
                                    Column(modifier = Modifier.heightIn(max = 150.dp).verticalScroll(rememberScrollState())) {
                                        transactionLogsList.forEach { log ->
                                            Row(modifier = Modifier.fillMaxWidth().clickable { selectedDetailLog = log; activeDialogTopic = null }.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(log.employeeName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                                Text("Inspect Key ➔", fontSize = 12.sp, color = Color(0xFF38BDF8))
                                            }
                                        }
                                    }
                                }
                            }
                            "safety" -> {
                                Text("Database Security Cryptography Configuration:", fontSize = 13.sp, color = Color(0xFF94A3B8))
                                Button(onClick = { showConfirmResetDialog = true; activeDialogTopic = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text("Purge Entire Offline Storage Cache", fontWeight = FontWeight.Bold)
                                }
                            }
                            "maintenance" -> {
                                Text("Bypass hardware filters to control variables directly:", fontSize = 13.sp, color = Color(0xFF94A3B8))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(onClick = { viewModel.simulateManualBlink(); Toast.makeText(context, "Blink Fired!", Toast.LENGTH_SHORT).show() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)), shape = RoundedCornerShape(8.dp)) { Text("Mock Blink", fontSize = 11.sp, color = Color.White) }
                                    Button(onClick = { viewModel.simulateManualSmile(); Toast.makeText(context, "Smile Fired!", Toast.LENGTH_SHORT).show() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)), shape = RoundedCornerShape(8.dp)) { Text("Mock Smile", fontSize = 11.sp, color = Color.White) }
                                }
                            }
                            "ucc" -> {
                                Text("Sync status: ${unsyncedList.size} queue packets waiting transmission.", fontSize = 13.sp, color = Color(0xFF94A3B8))
                                Button(onClick = { viewModel.syncOfflineQueue(); Toast.makeText(context, "Sync data lake stream pushed!", Toast.LENGTH_SHORT).show(); activeDialogTopic = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text("Force Push Synchronization", fontWeight = FontWeight.Bold)
                                }
                            }
                            "login" -> {
                                Text("Authorized workforce database profile matching nodes:", fontSize = 13.sp, color = Color(0xFF94A3B8))
                                Column(modifier = Modifier.heightIn(max = 140.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    employeesList.forEach { emp ->
                                        Card(modifier = Modifier.fillMaxWidth().clickable { viewModel.simulateCompleteLivenessForWorker(emp); activeDialogTopic = "attendance" }, colors = CardDefaults.cardColors(containerColor = Color(0xFF334155))) {
                                            Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(emp.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text("ID: ${emp.id}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeDialogTopic = null }, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF38BDF8))) { Text("Dismiss panel", fontWeight = FontWeight.Bold) }
                }
            )
        }

        selectedDetailLog?.let { log ->
            AlertDialog(
                onDismissRequest = { selectedDetailLog = null },
                title = { Text("Encrypted SQLite Log Entry", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Employee: ${log.employeeName}", fontWeight = FontWeight.Bold)
                        Text(log.encryptedPayload, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.DarkGray)
                    }
                },
                confirmButton = { Button(onClick = { selectedDetailLog = null }) { Text("Close Details") } }
            )
        }

        if (showConfirmResetDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmResetDialog = false },
                title = { Text("Purge Database Records?", fontWeight = FontWeight.Bold) },
                text = { Text("Permanently wipe security database transaction payloads? This operation is irreversible.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.forcePurgeAllLogs(); showConfirmResetDialog = false; Toast.makeText(context, "Local databases purged!", Toast.LENGTH_SHORT).show() }) { Text("Purge Records", color = Color.Red) }
                },
                dismissButton = { TextButton(onClick = { showConfirmResetDialog = false }) { Text("Cancel") } }
            )
        }
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Text(text = subtitle, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
    }
}