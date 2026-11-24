package com.vela.nativetools

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.ActivityCallback
import com.getcapacitor.annotation.CapacitorPlugin
import androidx.activity.result.ActivityResult

@CapacitorPlugin(name = "NativeTools")
class NativeToolsPlugin : Plugin() {

    private val implementation = NativeTools()

    private val EXTRA_CALL_ID = "com.vela.nativetools.CALL_ID"
    private val EXTRA_SESSION_ID = "com.vela.nativetools.SESSION_ID"
    private val EXTRA_REGION = "com.vela.nativetools.REGION"

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
            android.app.AlertDialog.Builder(act)
                .setTitle("Captura de fotografía")
                .setMessage("¿Deseas continuar y tomar una fotografía?")
                .setPositiveButton("Tomar foto") { _, _ ->
                    Log.d("NativeTools", "Usuario seleccionó tomar foto")

                    val ret = JSObject()
                    ret.put("action", "takePhoto")

                    bridge.saveCall(call)
                    call.resolve(ret)
                    bridge.releaseCall(call)
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    Log.d("NativeTools", "Usuario seleccionó cancelar")

                    val ret = JSObject()
                    ret.put("action", "cancel")

                    bridge.saveCall(call)
                    call.resolve(ret)
                    bridge.releaseCall(call)
                }
                .setOnCancelListener {
                    Log.d("NativeTools", "Usuario cerró el diálogo")

                    val ret = JSObject()
                    ret.put("action", "cancel")

                    bridge.saveCall(call)
                    call.resolve(ret)
                    bridge.releaseCall(call)
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
            val intent = Intent().apply {
                setClassName(act.packageName, "io.ionic.starter.com.vela.nativetools.FaceLivenessActivity")
                putExtra(EXTRA_CALL_ID, call.callbackId)
                putExtra(EXTRA_SESSION_ID, sessionId)
                putExtra(EXTRA_REGION, region)
            }

            bridge.saveCall(call)
            call.setKeepAlive(true)
            startActivityForResult(call, intent, "onFaceLivenessResult")
        } catch (e: Exception) {
            Log.e("NativeTools", "No se pudo lanzar FaceLivenessActivity", e)
            bridge.releaseCall(call)
            call.reject("FaceLivenessActivity no disponible: ${e.message}")
        }
    }

    @ActivityCallback
    fun onFaceLivenessResult(call: PluginCall, result: ActivityResult) {
        val data = result.data
        Log.d("NativeTools", "onFaceLivenessResult: resultCode=${result.resultCode}")

        val ret = JSObject()

        // Read the status from Intent extras (set by FaceLivenessActivity)
        val status = data?.getStringExtra("result_status") ?: 
            when (result.resultCode) {
                android.app.Activity.RESULT_OK -> "complete"
                android.app.Activity.RESULT_CANCELED -> "cancelled"
                else -> "error"
            }
        val resultData = data?.getStringExtra("result_data") ?: ""

        when (status) {
            "complete" -> {
                ret.put("status", "complete")
                ret.put("result", resultData)
                Log.d("NativeTools", "Face Liveness completado: $resultData")
                call.resolve(ret)
            }
            "cancelled" -> {
                ret.put("status", "cancelled")
                ret.put("result", resultData)
                Log.d("NativeTools", "Face Liveness cancelado por el usuario: $resultData")
                call.resolve(ret)
            }
            "error" -> {
                ret.put("status", "error")
                ret.put("error", resultData)
                Log.e("NativeTools", "Face Liveness error: $resultData")
                call.resolve(ret)
            }
            else -> {
                ret.put("status", "error")
                ret.put("error", "Unknown status: $status (resultCode=${result.resultCode})")
                Log.e("NativeTools", "Estado desconocido: $status")
                call.resolve(ret)
            }
        }

        bridge.releaseCall(call)
    }
}
