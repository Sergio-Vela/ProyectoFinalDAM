package io.ionic.starter.com.vela.nativetools

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.amplifyframework.Amplify
import com.amplifyframework.ui.liveness.FaceLivenessDetector
import com.amplifyframework.ui.liveness.FaceLivenessDetectionException
import com.amplifyframework.ui.liveness.LivenessColorScheme

/**
 * Actividad nativa dedicada a la experiencia oficial de AWS Face Liveness.
 *
 * Se lanza desde el plugin Capacitor y utiliza el componente oficial
 * `com.amplifyframework.ui:liveness` (FaceLivenessDetector) mediante Jetpack Compose.
 * No reemplaza la Activity principal de Capacitor; es una Activity independiente.
 */
class FaceLivenessActivity : ComponentActivity() {

    companion object {
        const val TAG = "FaceLivenessActivity"

        const val EXTRA_RESULT = "com.vela.nativetools.RESULT"
        const val EXTRA_ERROR = "com.vela.nativetools.ERROR"
        const val EXTRA_CALL_ID = "com.vela.nativetools.CALL_ID"

        const val EXTRA_SESSION_ID = "com.vela.nativetools.SESSION_ID"
        const val EXTRA_REGION = "com.vela.nativetools.REGION"

        const val RESULT_LIVENESS_COMPLETE = 1001
        const val RESULT_LIVENESS_ERROR = 1002
        const val RESULT_PERMISSION_DENIED = 1003
    }

    private lateinit var sessionId: String
    private lateinit var region: String
    private lateinit var callId: String

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras ?: return
        sessionId = extras.getString(EXTRA_SESSION_ID) ?: ""
        region = extras.getString(EXTRA_REGION) ?: "us-east-1"
        callId = extras.getString(EXTRA_CALL_ID) ?: ""

        if (sessionId.isEmpty() || callId.isEmpty()) {
            Log.e(TAG, "SessionId o CallId vacíos")
            Toast.makeText(this, "Parámetros no válidos", Toast.LENGTH_LONG).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                launchLiveness()
            } else {
                Log.w(TAG, "Permiso de cámara denegado")
                val result = Intent().apply {
                    putExtra(EXTRA_CALL_ID, callId)
                    putExtra(EXTRA_ERROR, "CameraPermissionDeniedException")
                }
                setResult(RESULT_PERMISSION_DENIED, result)
                finish()
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        } else {
            launchLiveness()
        }
    }

    private fun launchLiveness() {
        Log.d(TAG, "Iniciando FaceLivenessDetector con sessionId=$sessionId region=$region")

        setContent {
            MaterialTheme(
                colorScheme = LivenessColorScheme.default()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    FaceLivenessDetector(
                        sessionId = sessionId,
                        region = region,
                        onComplete = {
                            Log.i(TAG, "Flujo de Face Liveness completado")
                            val result = Intent().apply {
                                putExtra(EXTRA_CALL_ID, callId)
                                putExtra(EXTRA_RESULT, "complete")
                            }
                            setResult(RESULT_LIVENESS_COMPLETE, result)
                            finish()
                        },
                        onError = { error: FaceLivenessDetectionException ->
                            Log.e(TAG, "Error en Face Liveness", error)
                            val result = Intent().apply {
                                putExtra(EXTRA_CALL_ID, callId)
                                putExtra(EXTRA_ERROR, error::class.java.simpleName ?: "UnknownError")
                            }
                            setResult(RESULT_LIVENESS_ERROR, result)
                            finish()
                        }
                    )
                }
            }
        }
    }
}