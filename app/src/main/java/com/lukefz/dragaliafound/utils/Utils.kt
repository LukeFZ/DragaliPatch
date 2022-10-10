package com.lukefz.dragaliafound.utils

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.lukefz.dragaliafound.R
import java.io.File
import java.io.InputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.experimental.inv

object Utils {
    fun obfuscateUrl(url: String): ByteArray {
        val array = ByteArray(url.length)
        for (i in 0..url.length) {
            array[i] = url[i].code.toByte().inv()
        }

        return array;
    }

    fun configureInstallIntent(fileUri: Uri): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return intent
    }

    fun getStackTrace(ex: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        return sw.toString()
    }

    @SuppressLint("DiscouragedPrivateApi")
    @SuppressWarnings("JavaReflectionMemberAccess")
    fun setField(field: Field, value: Any) {
        field.isAccessible = true

        val modifiers = Field::class.java.getDeclaredField("accessFlags")
        modifiers.isAccessible = true
        modifiers.setInt(field, field.modifiers.and(Modifier.FINAL.inv()))

        field.set(null, value)
    }

    fun copyFile(input: InputStream, output: File) {
        try {
            output.outputStream().use { out -> {
                val buffer = ByteArray(1024)
                var len: Int
                while (true) {
                    len = input.read(buffer, 0, buffer.size)
                    if (len == -1)
                        break
                    out.write(buffer, 0, len)
                }
            }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}