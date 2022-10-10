package com.lukefz.dragaliafound.utils

import android.annotation.SuppressLint
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