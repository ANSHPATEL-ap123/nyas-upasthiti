package com.example.ui

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceLivenessAnalyzer(
    private val onLivenessResult: (isLive: Boolean, leftEye: Float, rightEye: Float, smile: Float) -> Unit
) : ImageAnalysis.Analyzer {

    // ML Kit Face Detector ko configure kar rahe hain
    // CLASSIFICATION_MODE_ALL zaroori hai taaki smile aur eyes ki probabilities mil sakein
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            // CameraX frame ko ML Kit InputImage mein convert karna
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        // Sabse main/front face ko target karo
                        val face = faces.first()

                        val leftEye = face.leftEyeOpenProbability ?: 0f
                        val rightEye = face.rightEyeOpenProbability ?: 0f
                        val smile = face.smilingProbability ?: 0f

                        // Liveness Logic: Dono aankhein thodi open honi chahiye aur slight smile
                        // (In values ko tum UI testing ke baad adjust kar sakte ho)
                        val isLive = leftEye > 0.4f && rightEye > 0.4f && smile > 0.2f

                        // Result wapas ViewModel/UI ko bhej do
                        onLivenessResult(isLive, leftEye, rightEye, smile)
                    } else {
                        // Agar frame mein koi face nahi hai
                        onLivenessResult(false, 0f, 0f, 0f)
                    }
                }
                .addOnFailureListener { e ->
                    // Error handling
                    e.printStackTrace()
                    onLivenessResult(false, 0f, 0f, 0f)
                }
                .addOnCompleteListener {
                    // YEH SABSE IMPORTANT HAI: Frame ko close karna taaki next frame aa sake
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}