package com.lukefz.dragaliafound.logging

import android.util.Log
import java.util.logging.Logger

class PatchLogger(private val listener: LogListener, name: String?, resourceBundleName: String? = null) : Logger(name, resourceBundleName) {
    override fun info(msg: String?) {
        if (msg != null) {
            listener.onMessage(msg)
            Log.i(name, msg)
        }
        super.info(msg)
    }

    override fun warning(msg: String?) {
        if (msg != null) {
            listener.onMessage(msg)
            Log.w(name, msg)
        }
        super.warning(msg)
    }

    override fun fine(msg: String?) {
        if (msg != null) {
            listener.onMessage(msg)
            Log.i(name, msg)
        }
        super.fine(msg)
    }
}