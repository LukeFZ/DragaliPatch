package com.lukefz.dragaliafound.utils

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.experimental.inv

object Utils {
    fun obfuscateUrl(url: String, length: Int): ByteArray {
        val array = ByteArray(length)
        for (i in 0 until length) {
            if (i < url.length)
                array[i] = url[i].code.toByte().inv()
            else
                array[i] = -1
        }

        return array
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
            if (!output.exists()) output.createNewFile()

            output.outputStream().use { out ->
                copyFile(input, out)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun copyFile(input: InputStream, out: FileOutputStream) {
        val buffer = ByteArray(1024)
        var len: Int
        while (true) {
            len = input.read(buffer, 0, buffer.size)
            if (len == -1)
                break
            out.write(buffer, 0, len)
        }
    }

    // https://stackoverflow.com/a/9456947/5299903
    fun normalizeFileEndings(file: File) {
        if (!file.exists()) {
            throw IOException("Could not find file to open: ${file.absolutePath}")
        }

        val temp = File(file.absolutePath.plus(".normalized"))
        temp.createNewFile()

        file.inputStream().use { fileIn ->
            DataInputStream(fileIn).use { dataIn ->
                InputStreamReader(dataIn).use { inputStream ->
                    BufferedReader(inputStream).use { bufferedIn ->
                        temp.outputStream().use { fileOut ->
                            DataOutputStream(fileOut).use { dataOut ->
                                OutputStreamWriter(dataOut).use { outputStream ->
                                    BufferedWriter(outputStream).use { bufferedOut ->
                                        var line: String?
                                        while (true) {
                                            line = bufferedIn.readLine()
                                            if (line == null)
                                                break
                                            bufferedOut.write(line.plus("\n"))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        file.delete()
        temp.renameTo(file)
    }

    fun fixUrl(url: String, maxLength: Int): String {
        var addr = url

        if (!addr.matches(Regex("^(http|https)://.*$"))) {
            addr = "https://$addr"
        }

        if (!addr.endsWith("/")) {
            addr = addr.plus("/")
        }

        if (addr.length > maxLength) {
            throw IndexOutOfBoundsException("Server address too long: (${addr.length} > ${maxLength})")
        }

        return addr
    }
}