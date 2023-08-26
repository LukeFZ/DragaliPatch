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
    var customAppName = mutableStateOf("")

    var customServerUrl = mutableStateOf("")

    var customCdnUrl = mutableStateOf("")

    var backingUpOriginalGame = false
    var backingUpModifiedGame = false

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

    fun estimateUrlLength(url: String, preferHttps: Boolean = true) : Int {
        var length = url.length

        if (!url.matches(Regex("^(http|https)://.*$"))) {
            length += if (preferHttps) 8 else 7
        }

        if (!url.endsWith("/")) {
            length += 1
        }

        return length
    }

    fun openWebsite(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        app.startActivity(intent)
    }

    fun startOriginalGameBackup() {
        backingUpOriginalGame = true
    }

    fun startModifiedGameBackup() {
        backingUpModifiedGame = true
    }

    fun handleOpenDocumentTree(output: DocumentFile) {
        if (backingUpOriginalGame) {
            backupOriginalGame(output)
            backingUpOriginalGame = false
        }

        if (backingUpModifiedGame) {
            backupModifiedGame(output)
            backingUpModifiedGame = false
        }
    }

    private fun backupModifiedGame(output: DocumentFile): Boolean {
        val storage = StorageUtil(app.applicationContext)
        val resolver = app.contentResolver

        if (!storage.signedApk.exists()) {
            Toast.makeText(app.applicationContext, app.getString(R.string.app_backup_failed), Toast.LENGTH_LONG).show()
            return false
        }

        val outputFile = output.createFile("application/octet-stream", storage.signedApk.name)

        if (outputFile == null) {
            Toast.makeText(app.applicationContext, app.getString(R.string.app_backup_failed), Toast.LENGTH_LONG).show()
            return false
        }

        storage.signedApk.inputStream().use { inp ->
            resolver.openFileDescriptor(outputFile.uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { out ->
                    Utils.copyFile(inp, out)
                }
            }
        }

        Toast.makeText(app.applicationContext, app.getString(R.string.app_backup_succeeded), Toast.LENGTH_LONG).show()
        return true
    }

    private fun backupOriginalGame(output: DocumentFile): Boolean {
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
}