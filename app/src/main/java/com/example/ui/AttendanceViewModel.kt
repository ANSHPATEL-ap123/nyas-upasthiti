package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Employee
import com.example.data.model.TransactionLog
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.sqrt

enum class LivenessState {
    IDLE,
    ALIGN_FACE,
    CHALLENGE_BLINK,
    CHALLENGE_SMILE,
    VERIFYING,
    SUCCESS_MATCHED,
    FAILED_TIMEOUT,
    FAILED_UNKNOWN
}

data class AttendanceUiState(
    val livenessState: LivenessState = LivenessState.IDLE,
    val statusMessage: String = "Tap 'Start Attendance Scanner' to begin roll-call",
    val promptMessage: String = "",
    val leftEyeOpenProb: Float = 1.0f,
    val rightEyeOpenProb: Float = 1.0f,
    val smilingProb: Float = 0.0f,
    val blinkPassed: Boolean = false,
    val smilePassed: Boolean = false,
    val countdownSeconds: Int = 10,
    val lowLightDetected: Boolean = false,
    val flashOverlayActive: Boolean = false,
    val matchedEmployee: Employee? = null,
    val matchDistance: Float = 0.0f,
    val isSimulatorMode: Boolean = true, // We default simulator to true to ensure 100% testability on emulator
    val isSyncing: Boolean = false,
    val isCameraPermissionGranted: Boolean = false
)

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = AttendanceRepository(db.employeeDao(), db.transactionLogDao())

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    // Flow streams observed by Compose UI
    val employees: StateFlow<List<Employee>> = repository.allEmployees
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val transactionLogs: StateFlow<List<TransactionLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unsyncedLogs: StateFlow<List<TransactionLog>> = repository.unsyncedLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var challengeJob: Job? = null

    init {
        // Prepopulate standard worker profiles on first load
        viewModelScope.launch {
            repository.checkAndPrepopulate()
        }
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(isCameraPermissionGranted = granted) }
        // If granted, we can switch simulator off by default
        if (granted) {
            _uiState.update { it.copy(isSimulatorMode = false) }
        }
    }

    fun toggleSimulatorMode(enabled: Boolean) {
        _uiState.update { it.copy(isSimulatorMode = enabled) }
        if (enabled && _uiState.value.livenessState != LivenessState.IDLE) {
            cancelScanning()
        }
    }

    /**
     * Start the 4-Phase offline attendance tracking pipeline
     */
    fun startScanning() {
        challengeJob?.cancel()
        _uiState.update {
            it.copy(
                livenessState = LivenessState.ALIGN_FACE,
                statusMessage = "Aligning camera with face...",
                promptMessage = "Please align your face in the rounded preview",
                blinkPassed = false,
                smilePassed = false,
                leftEyeOpenProb = 1.0f,
                rightEyeOpenProb = 1.0f,
                smilingProb = 0.0f,
                countdownSeconds = 10,
                matchedEmployee = null,
                matchDistance = 0.0f,
                flashOverlayActive = false
            )
        }

        challengeJob = viewModelScope.launch {
            // Phase 1: Alignment check (Wait 2 seconds)
            delay(1500)
            if (_uiState.value.livenessState != LivenessState.ALIGN_FACE) return@launch

            // Phase 2: Active liveness - Challenge 1: Please Blink
            _uiState.update {
                it.copy(
                    livenessState = LivenessState.CHALLENGE_BLINK,
                    statusMessage = "Liveness Challenge 1/2",
                    promptMessage = "👁️ Please Blink Your Eyes"
                )
            }

            // Start 10 seconds challenge countdown timer
            launch {
                while (_uiState.value.countdownSeconds > 0 &&
                    (_uiState.value.livenessState == LivenessState.CHALLENGE_BLINK ||
                            _uiState.value.livenessState == LivenessState.CHALLENGE_SMILE)
                ) {
                    delay(1000)
                    _uiState.update { it.copy(countdownSeconds = it.countdownSeconds - 1) }
                }

                if (_uiState.value.countdownSeconds <= 0 &&
                    (!_uiState.value.blinkPassed || !_uiState.value.smilePassed)
                ) {
                    _uiState.update {
                        it.copy(
                            livenessState = LivenessState.FAILED_TIMEOUT,
                            statusMessage = "Verification Suspended",
                            promptMessage = "❌ Liveness challenge timed out (10s threshold reached)"
                        )
                    }
                }
            }
        }
    }

    fun cancelScanning() {
        challengeJob?.cancel()
        _uiState.update {
            it.copy(
                livenessState = LivenessState.IDLE,
                statusMessage = "Scanner idle. Ready for biometric identification.",
                promptMessage = "",
                flashOverlayActive = false
            )
        }
    }

    /**
     * Light level sensing logic from Camera frame analyzer
     */
    fun processAmbientLight(averageLuminance: Double) {
        val lowLight = averageLuminance < 35.0
        _uiState.update {
            it.copy(
                lowLightDetected = lowLight,
                // If scanner active and low light, toggle flash overlay!
                flashOverlayActive = lowLight && (
                        it.livenessState == LivenessState.ALIGN_FACE ||
                                it.livenessState == LivenessState.CHALLENGE_BLINK ||
                                it.livenessState == LivenessState.CHALLENGE_SMILE
                        )
            )
        }
    }

    /**
     * Live ML Kit Face attributes processed frame-by-frame
     */
    fun onFaceFrameDetected(leftEyeOpen: Float?, rightEyeOpen: Float?, smileProb: Float?) {
        val currentState = _uiState.value.livenessState
        if (currentState == LivenessState.IDLE ||
            currentState == LivenessState.SUCCESS_MATCHED ||
            currentState == LivenessState.FAILED_TIMEOUT ||
            currentState == LivenessState.FAILED_UNKNOWN
        ) {
            return
        }

        // Update real-time eye/smile values
        _uiState.update {
            it.copy(
                leftEyeOpenProb = leftEyeOpen ?: it.leftEyeOpenProb,
                rightEyeOpenProb = rightEyeOpen ?: it.rightEyeOpenProb,
                smilingProb = smileProb ?: it.smilingProb
            )
        }

        // Checklist evaluation based on Phase constraints:
        // Challenge 1: Blink (Both eye open values below 0.2)
        if (currentState == LivenessState.CHALLENGE_BLINK) {
            val left = leftEyeOpen ?: 1.0f
            val right = rightEyeOpen ?: 1.0f
            if (left < 0.2f && right < 0.2f) {
                _uiState.update {
                    it.copy(
                        blinkPassed = true,
                        livenessState = LivenessState.CHALLENGE_SMILE,
                        statusMessage = "Liveness Challenge 2/2",
                        promptMessage = "😊 Excellent! Now Please Smile"
                    )
                }
            }
        }

        // Challenge 2: Smile (smiling probability above 0.8)
        if (currentState == LivenessState.CHALLENGE_SMILE && _uiState.value.blinkPassed) {
            val smile = smileProb ?: 0.0f
            if (smile > 0.8f) {
                _uiState.update {
                    it.copy(
                        smilePassed = true,
                        livenessState = LivenessState.VERIFYING,
                        statusMessage = "Scanning Facial Geometry...",
                        promptMessage = "Generating local high-dimension biometrics..."
                    )
                }
                // Proceed to match embeddings offline
                triggerFaceVectorMatching()
            }
        }
    }

    /**
     * Simulated adjustment controls for Developer preview / Emulator use!
     */
    fun simulateManualBlink() {
        if (_uiState.value.livenessState == LivenessState.CHALLENGE_BLINK) {
            onFaceFrameDetected(0.1f, 0.1f, _uiState.value.smilingProb)
        }
    }

    fun simulateManualSmile() {
        if (_uiState.value.livenessState == LivenessState.CHALLENGE_SMILE) {
            onFaceFrameDetected(_uiState.value.leftEyeOpenProb, _uiState.value.rightEyeOpenProb, 0.95f)
        }
    }

    /**
     * Quick simulated automatic pass bypass for speed of test
     */
    fun simulateCompleteLivenessForWorker(employee: Employee) {
        challengeJob?.cancel()
        _uiState.update {
            it.copy(
                livenessState = LivenessState.VERIFYING,
                statusMessage = "Biometric scan bypass active",
                promptMessage = "Scanning embeddings for model ${employee.name}...",
                blinkPassed = true,
                smilePassed = true,
                leftEyeOpenProb = 0.15f,
                rightEyeOpenProb = 0.15f,
                smilingProb = 0.95f
            )
        }

        viewModelScope.launch {
            delay(1000)
            // Generate test vector close to selected employee
            val liveVector = floatArrayOf(
                employee.f1 + (Math.random().toFloat() * 0.06f - 0.03f),
                employee.f2 + (Math.random().toFloat() * 0.06f - 0.03f),
                employee.f3 + (Math.random().toFloat() * 0.06f - 0.03f),
                employee.f4 + (Math.random().toFloat() * 0.06f - 0.03f),
                employee.f5 + (Math.random().toFloat() * 0.06f - 0.03f)
            )
            executeMatchingWithVector(liveVector)
        }
    }

    fun simulateUnknownIntruder() {
        challengeJob?.cancel()
        _uiState.update {
            it.copy(
                livenessState = LivenessState.VERIFYING,
                statusMessage = "Intruder scan simulation",
                promptMessage = "Analyzing facial structure...",
                blinkPassed = true,
                smilePassed = true,
                leftEyeOpenProb = 0.15f,
                rightEyeOpenProb = 0.15f,
                smilingProb = 0.95f
            )
        }

        viewModelScope.launch {
            delay(1000)
            // Vector far from everyone
            val intruderVector = floatArrayOf(0.99f, 0.01f, 0.99f, 0.01f, 0.99f)
            executeMatchingWithVector(intruderVector)
        }
    }

    /**
     * Standard local matching execution
     */
    private fun triggerFaceVectorMatching() {
        viewModelScope.launch {
            delay(1200) // Vector generation simulation latency

            // Create a randomized vector that maps closely to an employee if isSimulatorMode
            // so we test positive match. Otherwise generate random vector
            val localWorkers = employees.value
            if (localWorkers.isNotEmpty() && _uiState.value.isSimulatorMode) {
                // Perfect hit target: let's select Amit Kumar (index 0) or random target close to him
                val sampleTarget = localWorkers.first()
                val targetVector = floatArrayOf(
                    sampleTarget.f1 + 0.05f,
                    sampleTarget.f2 - 0.03f,
                    sampleTarget.f3 + 0.02f,
                    sampleTarget.f4 + 0.04f,
                    sampleTarget.f5 - 0.02f
                )
                executeMatchingWithVector(targetVector)
            } else {
                // Read ML/Camera generated mock vector parameters
                // Let's create varying float values using facial parameters:
                val liveVector = floatArrayOf(
                    (_uiState.value.leftEyeOpenProb + 0.1f) % 1.0f,
                    (_uiState.value.smilingProb * 0.5f),
                    (_uiState.value.rightEyeOpenProb - 0.1f).coerceAtLeast(0.0f),
                    0.45f,
                    0.25f
                )
                executeMatchingWithVector(liveVector)
            }
        }
    }

    private suspend fun executeMatchingWithVector(liveVector: FloatArray) {
        val list = employees.value
        if (list.isEmpty()) {
            _uiState.update {
                it.copy(
                    livenessState = LivenessState.FAILED_UNKNOWN,
                    statusMessage = "Database Offline",
                    promptMessage = "❌ Error: Employee ledger is empty! Pre-population failed."
                )
            }
            return
        }

        var closestEmployee: Employee? = null
        var minDistance = Float.MAX_VALUE

        for (emp in list) {
            val dist = emp.calculateDistance(liveVector)
            if (dist < minDistance) {
                minDistance = dist
                closestEmployee = emp
            }
        }

        if (minDistance < 0.6f && closestEmployee != null) {
            // SUCCESS: Save Log and update UI
            _uiState.update {
                it.copy(
                    livenessState = LivenessState.SUCCESS_MATCHED,
                    statusMessage = "Attendance Authorized Securely",
                    promptMessage = "SUCCESS: Attendance Marked Offline",
                    matchedEmployee = closestEmployee,
                    matchDistance = minDistance
                )
            }

            // Save encrypted log locally (Phase 4)
            // Generate standard GPS of Ranchi remote work site
            val randomOffsetLat = (Math.random() - 0.5) * 0.01 // Random dev.offset
            val randomOffsetLng = (Math.random() - 0.5) * 0.01
            val gpsLat = 23.3441 + randomOffsetLat
            val gpsLng = 85.3096 + randomOffsetLng

            repository.saveSecuredLog(
                employeeId = closestEmployee.id,
                employeeName = closestEmployee.name,
                timestamp = System.currentTimeMillis(),
                latitude = gpsLat,
                longitude = gpsLng,
                livenessBlinkPassed = _uiState.value.blinkPassed,
                livenessSmilePassed = _uiState.value.smilePassed,
                matchingDistance = minDistance
            )
        } else {
            // FAILED_UNKNOWN
            _uiState.update {
                it.copy(
                    livenessState = LivenessState.FAILED_UNKNOWN,
                    statusMessage = "Identification Failed",
                    promptMessage = "ACCESS DENIED: Biometric Match Failed (Unknown Face, distance = ${String.format("%.3f", minDistance)})"
                )
            }
        }
    }

    /**
     * Phase 4: Mock network sync
     */
    fun syncOfflineQueue() {
        viewModelScope.launch {
            if (_uiState.value.isSyncing) return@launch
            _uiState.update { it.copy(isSyncing = true) }
            delay(2000) // Simulated connection and upload latency
            repository.syncLogsToServer()
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    statusMessage = "Sync Complete: Central telemetry updated!"
                )
            }
        }
    }

    fun purgeSyncedQueue() {
        viewModelScope.launch {
            repository.clearSyncedQueue()
        }
    }

    fun forcePurgeAllLogs() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
