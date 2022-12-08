package com.lukefz.dragaliafound.screens

import android.app.Application
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.utils.Constants

@Suppress("DEPRECATION")
class MainScreenViewModel(private val app: Application) : AndroidViewModel(app) {

    var originalAppInfo = ""
    var patchedAppInfo = ""
    var originalAppIcon: Drawable? = null
    var patchedAppIcon: Drawable? = null
    var isPatchable = false
    var customServerUrl = mutableStateOf("")
    //var enableClearDeviceAccountButton = false

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
}