package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.AttendanceViewModel
import com.example.ui.DashboardScreen

class MainActivity : ComponentActivity() {

    // ViewModel yahan initialize hoga aur room database ko trigger karega
    private val viewModel: AttendanceViewModel by viewModels()

    // Runtime Camera Permission Handler
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.setCameraPermissionGranted(true)
            } else {
                Toast.makeText(this, "Camera permission is strictly required for Biometric scan!", Toast.LENGTH_LONG).show()
                viewModel.setCameraPermissionGranted(false)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch app karte hi camera permission check/request karna
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.setCameraPermissionGranted(true)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Hamari main UI ko call kar rahe hain aur ViewModel pass kar rahe hain
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}