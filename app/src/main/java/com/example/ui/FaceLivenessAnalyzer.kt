package com.example.ui

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceLivenessAnalyzer(
    private val onFaceDetected: (leftEyeOpen: Float?, rightEyeOpen: Float?, smilingProb: Float?) -> Unit,
    private val onLuminanceMeasured: (luminance: Double) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .build()

    private val detector = FaceDetection.getClient(options)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            // 1. Compute ambient pixel intensity of Y plane for light sensing
            val plane = imageProxy.planes[0]
            val buffer = plane.buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)
            var sum = 0L
            for (pixel in data) {
                sum += (pixel.toInt() and 0xFF)
            }
            val averageLuminance = if (data.isNotEmpty()) sum.toDouble() / data.size else 0.0
            onLuminanceMeasured(averageLuminance)

            // 2. Compute Google ML Kit Face Attributes
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val firstFace = faces[0]
                        onFaceDetected(
                            firstFace.leftEyeOpenProbability,
                            firstFace.rightEyeOpenProbability,
                            firstFace.smilingProbability
                        )
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
