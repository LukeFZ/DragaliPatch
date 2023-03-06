package com.lukefz.dragaliafound.screens

import android.app.Application
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.utils.Constants
import com.lukefz.dragaliafound.utils.StorageUtil
import com.lukefz.dragaliafound.utils.Utils
import java.io.FileOutputStream

@Suppress("DEPRECATION")
class MainScreenViewModel(private val app: Application) : AndroidViewModel(app) {

    var originalAppInfo = ""
    var patchedAppInfo = ""
    var originalAppIcon: Drawable? = null
    var patchedAppIcon: Drawable? = null
    var isPatchable = false
    var customServerUrl = mutableStateOf("")

    init {
        val packageManager = app.packageManager
        try {
            val originalApp = packageManager.getPackageInfo(Constants.PACKAGE_NAME, 0)
            originalAppInfo = app.getString(
                R.string.activity_app_version_info,
                originalApp.versionName,
                originalApp.versionCode
            ).plus("\n")

            if (originalApp.versionCode == Constants.SUPPORTED_PACKAGE_VERSION) {
                isPatchable = true
            }

            originalAppInfo = originalAppInfo.plus(
                app.getString(
                    R.string.activity_app_is_patchable,
                    isPatchable.toString()
                )
            )
            originalAppIcon = originalApp.applicationInfo.loadIcon(packageManager)
        } catch (ex: Exception) {
            isPatchable = false
            originalAppInfo = "\nOriginal game not installed!\n"
        }

        try {
            val patchedApp = packageManager.getPackageInfo(Constants.PATCHED_PACKAGE_NAME, 0)
            patchedAppInfo = app.getString(
                R.string.activity_app_version_info,
                patchedApp.versionName,
                patchedApp.versionCode
            )
            patchedAppIcon = patchedApp.applicationInfo.loadIcon(packageManager)
        } catch (_: Exception) {
            patchedAppInfo = "\nPatched app not installed!\n"
        }
    }

    fun estimateApiUrlLength() : Int {
        var length = customServerUrl.value.length

        if (!customServerUrl.value.matches(Regex("^(http|https)://.*$"))) {
            length += 8
        }

        if (!customServerUrl.value.endsWith("/")) {
            length += 1
        }

        return length
    }

    fun openWebsite(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        app.startActivity(intent)
    }

    fun backupOriginalGame(output: DocumentFile): Boolean {
        val storage = StorageUtil(app.applicationContext)
        val apks = if (storage.isSplitApk) listOf(storage.sourceApk!!) + storage.sourceApks!! else listOf(storage.sourceApk!!)
        val resolver = app.contentResolver

        for (apk in apks) {
            val outputFile = output.createFile("application/octet-stream", apk.name)

            if (outputFile == null) {
                Toast.makeText(app.applicationContext, app.getString(R.string.app_backup_failed), Toast.LENGTH_LONG).show()
                return false
            }

            apk.inputStream().use { inp ->
                resolver.openFileDescriptor(outputFile.uri, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { out ->
                        Utils.copyFile(inp, out)
                    }
                }
            }
        }

        Toast.makeText(app.applicationContext, app.getString(R.string.app_backup_succeeded), Toast.LENGTH_LONG).show()
        return true
    }

    /*@SuppressLint("SdCardPath")
    fun clearDeviceAccount() {
        try {
            val cmd = "run-as ${Constants.PATCHED_PACKAGE_NAME} rm /data/data/${Constants.PATCHED_PACKAGE_NAME}/shared_prefs/deviceAccount:.xml"
            val builder = ProcessBuilder(listOf(
                "sh", "-c", cmd
            ))
            builder.start().waitFor()
            Toast.makeText(app.applicationContext, app.getString(R.string.utility_clear_device_account_success), Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(app.applicationContext, app.getString(R.string.utility_clear_device_account_failed), Toast.LENGTH_LONG).show()
        }
    }*/
}