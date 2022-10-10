package com.lukefz.dragaliafound.logging

import android.util.Log
import java.util.logging.Logger

class PatchLogger(name: String?, resourceBundleName: String?, listener: LogListener) : Logger(name, resourceBundleName) {
    private val logEventListener = listener

    override fun info(msg: String?) {
        if (msg != null) {
            logEventListener.onMessage(msg)
            Log.i(name, msg)
        }
        super.info(msg)
    }

    override fun warning(msg: String?) {
        if (msg != null) {
            logEventListener.onMessage(msg)
            Log.w(name, msg)
        }
        super.warning(msg)
    }

    override fun fine(msg: String?) {
        if (msg != null) {
            logEventListener.onMessage(msg)
            Log.i(name, msg)
        }
        super.fine(msg)
    }
}