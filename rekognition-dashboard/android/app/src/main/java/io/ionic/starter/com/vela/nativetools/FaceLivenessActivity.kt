package io.ionic.starter.com.vela.nativetools

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.core.Action
import com.amplifyframework.core.Consumer
import com.amplifyframework.ui.liveness.model.FaceLivenessDetectionException
import com.amplifyframework.ui.liveness.ui.FaceLivenessDetector
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat

class FaceLivenessActivity : AppCompatActivity() {

    private val TAG = "FaceLivenessActivity"
    private val CAMERA_PERMISSION_REQUEST = 100

    private lateinit var callbackId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callbackId = intent.extras?.getString("com.vela.nativetools.CALL_ID") ?: ""
        val sessionId = intent.extras?.getString("com.vela.nativetools.SESSION_ID") ?: ""
        val region = intent.extras?.getString("com.vela.nativetools.REGION") ?: "us-east-1"

        Log.d(TAG, "onCreate - initializing Face Liveness, sessionId=$sessionId, callbackId=$callbackId")

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        } else {
            startFaceLiveness(sessionId, region)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (granted) {
                val sessionId = intent.extras?.getString("com.vela.nativetools.SESSION_ID") ?: ""
                val region = intent.extras?.getString("com.vela.nativetools.REGION") ?: "us-east-1"
                startFaceLiveness(sessionId, region)
            } else {
                Log.w(TAG, "Camera permission denied - keeping activity open for user to grant")
                // Do NOT finish() - keep activity open so user can grant permission
            }
        }
    }

    private fun startFaceLiveness(sessionId: String, region: String) {
        Log.d(TAG, "startFaceLiveness: sessionId=$sessionId region=$region")

        try {
            setContent {
                FaceLivenessDetector(
                    sessionId = sessionId,
                    region = region,
                    disableStartView = false,
                    onComplete = Action {
                        Log.d(TAG, "Face Liveness completed successfully")
                        sendResult("complete", "complete")
                        finish()
                    },
                    onError = Consumer { exception ->
                        Log.e(
                            TAG,
                            "Face Liveness error - exception=${exception.javaClass.simpleName} message=${exception.message}"
                        )
                        sendResult("error", exception.message ?: "Unknown error")
                        finish()
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Face Liveness", e)
            sendResult("error", e.message ?: "Exception occurred")
            finish()
        }
    }

    private fun sendResult(status: String, result: String) {
        Log.d(TAG, "sendResult: status=$status, result=$result")

        val intent = Intent().apply {
            putExtra("result_status", status)
            putExtra("result_data", result)
            putExtra("callback_id", callbackId)
        }
        setResult(if (status == "complete") Activity.RESULT_OK else if (status == "cancelled") Activity.RESULT_CANCELED else Activity.RESULT_OK, intent)
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed - canceling Face Liveness")
        sendResult("cancelled", "user_cancelled")
    }
}