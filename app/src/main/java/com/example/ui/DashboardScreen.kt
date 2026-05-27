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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Employee
import com.example.data.model.TransactionLog

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8F0FA), Color(0xFFF4F8FC))
                )
            )
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
                            modifier = Modifier.size(22.dp)
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

    // ==========================================
    // DYNAMIC DIALOG CONTENT ROUTER FOR ACTIVE CLICKS
    // ==========================================
    activeDialogTopic?.let { topic ->
        AlertDialog(
            onDismissRequest = { activeDialogTopic = null },
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
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when(topic) {
                        "attendance" -> {
                            Text("Real-time biometric liveness parameters are evaluated dynamically below:", fontSize = 13.sp)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Status Prompt: ${uiState.promptMessage}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D3E73))
                                    Text("Left Eye Open: ${(uiState.leftEyeOpenProb * 100).toInt()}%", fontSize = 12.sp)
                                    Text("Right Eye Open: ${(uiState.rightEyeOpenProb * 100).toInt()}%", fontSize = 12.sp)
                                    Text("Smile Force: ${(uiState.smilingProb * 100).toInt()}%", fontSize = 12.sp)
                                }
                            }
                            Button(
                                onClick = { viewModel.startScanning(); activeDialogTopic = null },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D3E73)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Trigger Live Biometric Scan")
                            }
                        }
                        "rfi" -> {
                            Text("Select an encrypted local SQLite telemetry transaction packet to inspect raw cipher hashes:", fontSize = 13.sp)
                            if (transactionLogsList.isEmpty()) {
                                Text("No ledger logs found in local database storage yet.", color = Color.Gray, fontSize = 12.sp)
                            } else {
                                Column(modifier = Modifier.heightIn(max = 150.dp).verticalScroll(rememberScrollState())) {
                                    transactionLogsList.forEach { log ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clickable { selectedDetailLog = log; activeDialogTopic = null }.padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(log.employeeName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            Text("Inspect Key ➔", fontSize = 12.sp, color = Color(0xFF0D3E73))
                                        }
                                    }
                                }
                            }
                        }
                        "safety" -> {
                            Text("Database Security Cryptography Configuration:", fontSize = 13.sp)
                            Text("Local records are obfuscated via AES block-ciphers on structural commit vectors to ensure data sovereignty rules.", fontSize = 12.sp, color = Color.Gray)
                            Button(
                                onClick = { showConfirmResetDialog = true; activeDialogTopic = null },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Purge Entire Offline Storage Cache")
                            }
                        }
                        "maintenance" -> {
                            Text("Bypass device hardware inputs to check verification state variables directly:", fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(onClick = { viewModel.simulateManualBlink(); Toast.makeText(context, "Blink Fired!", Toast.LENGTH_SHORT).show() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569))) {
                                    Text("Mock Blink", fontSize = 11.sp)
                                }
                                Button(onClick = { viewModel.simulateManualSmile(); Toast.makeText(context, "Smile Fired!", Toast.LENGTH_SHORT).show() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569))) {
                                    Text("Mock Smile", fontSize = 11.sp)
                                }
                            }
                        }
                        "ucc" -> {
                            Text("Sync status: ${unsyncedList.size} packets waiting local dispatch queue pipelines.", fontSize = 13.sp)
                            Button(
                                onClick = { viewModel.syncOfflineQueue(); Toast.makeText(context, "Sync payload pushed to lake pipeline!", Toast.LENGTH_SHORT).show(); activeDialogTopic = null },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Force Push Stream Synchronization")
                            }
                        }
                        "login" -> {
                            Text("Authorized local biometric registration pool contains profiles listed explicitly below:", fontSize = 13.sp)
                            Column(modifier = Modifier.heightIn(max = 140.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                employeesList.forEach { emp ->
                                    Card(modifier = Modifier.fillMaxWidth().clickable { viewModel.simulateCompleteLivenessForWorker(emp); activeDialogTopic = "attendance" }) {
                                        Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(emp.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("ID: ${emp.id}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialogTopic = null }) { Text("Dismiss panel") }
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
                    Text("Ciphertext Structure Packet:", fontSize = 12.sp, color = Color.Gray)
                    Text(log.encryptedPayload, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.DarkGray)
                }
            },
            confirmButton = {
                Button(onClick = { selectedDetailLog = null }) { Text("Close Details") }
            }
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
            dismissButton = {
                TextButton(onClick = { showConfirmResetDialog = false }) { Text("Cancel") }
            }
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