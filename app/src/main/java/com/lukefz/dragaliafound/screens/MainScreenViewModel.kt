package com.lukefz.dragaliafound.screens

import android.app.Application
import android.graphics.drawable.Drawable
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
    var customServerUrl = Constants.DEFAULT_CUSTOM_URL

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
}