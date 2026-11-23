package com.vela.nativetools

import com.getcapacitor.Logger

class NativeTools {

    fun echo(value: String): String {
        Logger.info("Echo", value)
        return value
    }
}
