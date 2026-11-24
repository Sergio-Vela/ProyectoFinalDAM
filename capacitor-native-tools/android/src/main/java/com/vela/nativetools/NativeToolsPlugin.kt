package com.vela.nativetools

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.ActivityCallback
import com.getcapacitor.annotation.CapacitorPlugin
import android.app.AlertDialog

@CapacitorPlugin(name = "NativeTools")
class NativeToolsPlugin : Plugin() {

    private val implementation = NativeTools()

    // Clase y claves de la FaceLivenessActivity (definidas en el módulo app).
    // Se referencian por String para evitar dependencias de compilación cruzada.
    private val FACE_LIVENESS_ACTIVITY = "io.ionic.starter.com.vela.nativetools.FaceLivenessActivity"
    private val EXTRA_CALL_ID = "com.vela.nativetools.CALL_ID"
    private val EXTRA_RESULT = "com.vela.nativetools.RESULT"
    private val EXTRA_ERROR = "com.vela.nativetools.ERROR"
    private val EXTRA_SESSION_ID = "com.vela.nativetools.SESSION_ID"
    private val EXTRA_REGION = "com.vela.nativetools.REGION"

    private val RESULT_LIVENESS_COMPLETE = 1001
    private val RESULT_LIVENESS_ERROR = 1002
    private val RESULT_PERMISSION_DENIED = 1003

    @PluginMethod
    fun echo(call: PluginCall) {
        val value = call.getString("value")

        if (value == null) {
            call.reject("El parámetro 'value' es obligatorio")
            return
        }

        val ret = JSObject()
        ret.put("value", implementation.echo(value))
        call.resolve(ret)
    }

    @PluginMethod
    fun showNativeAlert(call: PluginCall) {
        Log.d("NativeTools", "showNativeAlert fue invocado")

        call.setKeepAlive(true)

        val act = activity ?: run {
            call.reject("No hay Activity disponible")
            return
        }

        act.runOnUiThread {
            AlertDialog.Builder(act)
                .setTitle("Captura de fotografía")
                .setMessage("¿Deseas continuar y tomar una fotografía?")
                .setPositiveButton("Tomar foto") { _, _ ->
                    Log.d("NativeTools", "Usuario seleccionó tomar foto")

                    val ret = JSObject()
                    ret.put("action", "takePhoto")

                    bridge.saveCall(call)
                    call.resolve(ret)
                    bridge.releaseCall(call)

                    Log.d("NativeTools", "Respuesta takePhoto enviada")
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    Log.d("NativeTools", "Usuario seleccionó cancelar")

                    val ret = JSObject()
                    ret.put("action", "cancel")

                    bridge.saveCall(call)
                    call.resolve(ret)
                    bridge.releaseCall(call)

                    Log.d("NativeTools", "Respuesta cancel enviada")
                }
                .setOnCancelListener {
                    Log.d("NativeTools", "Usuario cerró el diálogo")

                    val ret = JSObject()
                    ret.put("action", "cancel")

                    bridge.saveCall(call)
                    call.resolve(ret)
                    bridge.releaseCall(call)

                    Log.d("NativeTools", "Respuesta cancel por cierre")
                }
                .show()
        }
    }

    @PluginMethod
    fun startFaceLiveness(call: PluginCall) {
        val sessionId = call.getString("sessionId")
        val region = call.getString("region") ?: "us-east-1"

        if (sessionId == null || sessionId.isEmpty()) {
            call.reject("El parámetro 'sessionId' es obligatorio")
            return
        }

        Log.d("NativeTools", "startFaceLiveness: sessionId=$sessionId region=$region")

        val act = activity ?: run {
            call.reject("No hay Activity disponible")
            return
        }

        try {
            val intent = Intent().setClassName(
                act.packageName,
                FACE_LIVENESS_ACTIVITY
            ).apply {
                putExtra(EXTRA_SESSION_ID, sessionId)
                putExtra(EXTRA_REGION, region)
                putExtra(EXTRA_CALL_ID, call.callbackId)
            }

            // Lanzamos la Activity usando el patrón moderno de Capacitor 8:
            // startActivityForResult(PluginCall, Intent, callbackName) donde
            // callbackName coincide con el nombre del método anotado con @ActivityCallback.
            bridge.saveCall(call)
            call.setKeepAlive(true)
            startActivityForResult(call, intent, "onFaceLivenessResult")
        } catch (e: Exception) {
            Log.e("NativeTools", "No se pudo lanzar FaceLivenessActivity", e)
            bridge.releaseCall(call)
            call.reject("FaceLivenessActivity no disponible: ${e.message}")
        }
    }

    /**
     * Manejador de resultados de Face Liveness anotado con @ActivityCallback.
     * Capacitor 8 invoca este método automáticamente cuando la Activity resultado
     * devuelve el control. Sustituye al obsoleto handleOnActivityResult().
     */
    @ActivityCallback
    fun onFaceLivenessResult(call: PluginCall, result: ActivityResult) {
        val data = result.data
        val callId = data?.extras?.getString(EXTRA_CALL_ID) ?: call.callbackId
        Log.d("NativeTools", "onFaceLivenessResult: resultCode=${result.resultCode} callId=$callId")

        // Capacitor ya entrega el PluginCall guardado; si no coincide usamos el guardado por id.
        val targetCall = if (call.callbackId == callId) call else (bridge.getSavedCall(callId) ?: call)

        val ret = JSObject()

        when {
            result.resultCode == RESULT_LIVENESS_COMPLETE -> {
                val resultData = data?.extras?.getString(EXTRA_RESULT) ?: "complete"
                ret.put("status", "complete")
                ret.put("result", resultData)
                Log.d("NativeTools", "Face Liveness completado")
                targetCall.resolve(ret)
            }
            result.resultCode == RESULT_LIVENESS_ERROR -> {
                val error = data?.extras?.getString(EXTRA_ERROR) ?: "UnknownError"
                ret.put("status", "error")
                ret.put("error", error)
                Log.e("NativeTools", "Error en Face Liveness: $error")
                targetCall.resolve(ret)
            }
            result.resultCode == RESULT_PERMISSION_DENIED -> {
                val error = data?.extras?.getString(EXTRA_ERROR) ?: "CameraPermissionDeniedException"
                ret.put("status", "error")
                ret.put("error", error)
                Log.w("NativeTools", "Permiso de cámara denegado")
                targetCall.resolve(ret)
            }
            result.resultCode == Activity.RESULT_CANCELED -> {
                ret.put("status", "cancelled")
                ret.put("result", "user_cancelled")
                Log.d("NativeTools", "Face Liveness cancelado por el usuario")
                targetCall.resolve(ret)
            }
            else -> {
                ret.put("status", "error")
                ret.put("error", "Unknown result code: ${result.resultCode}")
                Log.e("NativeTools", "Código de resultado desconocido: ${result.resultCode}")
                targetCall.resolve(ret)
            }
        }

        bridge.releaseCall(targetCall)
    }
}