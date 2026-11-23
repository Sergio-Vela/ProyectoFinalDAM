package com.vela.nativetools

import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import android.app.AlertDialog
import android.util.Log

@CapacitorPlugin(name = "NativeTools")
class NativeToolsPlugin : Plugin() {

    private val implementation = NativeTools()

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

        // Indicamos a Capacitor que conserve esta llamada asíncrona.
        call.setKeepAlive(true)

        activity.runOnUiThread {
            AlertDialog.Builder(activity)
                .setTitle("Captura de fotografía")
                .setMessage("¿Deseas continuar y tomar una fotografía?")
                .setPositiveButton("Tomar foto") { _, _ ->
                    Log.d("NativeTools", "Usuario seleccionó tomar foto")

                    val ret = JSObject()
                    ret.put("action", "takePhoto")

                    call.resolve(ret)
                    call.release(bridge)

                    Log.d("NativeTools", "Respuesta takePhoto enviada")
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    Log.d("NativeTools", "Usuario seleccionó cancelar")

                    val ret = JSObject()
                    ret.put("action", "cancel")

                    call.resolve(ret)
                    call.release(bridge)

                    Log.d("NativeTools", "Respuesta cancel enviada")
                }
                .setOnCancelListener {
                    Log.d("NativeTools", "Usuario cerró el diálogo")

                    val ret = JSObject()
                    ret.put("action", "cancel")

                    call.resolve(ret)
                    call.release(bridge)
                }
                .show()
        }
    }


    
}
