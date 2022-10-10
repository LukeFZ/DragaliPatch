package com.lukefz.dragaliafound.utils

import android.annotation.SuppressLint
import android.content.Context
import com.lukefz.dragaliafound.R
import java.io.File
import java.nio.file.Path

@SuppressLint("QueryPermissionsNeeded") // manifest has the correct values set
@Suppress("DEPRECATION") // the non deprecated api is API > 33 - good luck getting people to use that one instead, google
class StorageUtil(ctx: Context) {

    val unsignedApk: File = ctx.externalCacheDir!!.toPath().resolve(Constants.PATCHED_PACKAGE_NAME.plus("_unsigned").plus(Constants.APK_EXTENSION)).toFile()
    val signedApk: File = ctx.externalCacheDir!!.toPath().resolve(Constants.PATCHED_PACKAGE_NAME.plus(Constants.APK_EXTENSION)).toFile()

    val frameworkDir: String = ctx.externalCacheDir!!.toPath().resolve(Constants.FRAMEWORK).toString()
    val aaptPath: File = ctx.externalCacheDir!!.toPath().resolve(Constants.AAPT).toFile()
    val keystorePath: File = ctx.externalCacheDir!!.toPath().resolve(Constants.KEYSTORE).toFile()

    val workDir: Path = ctx.externalCacheDir!!.toPath().resolve(Constants.PATCHING_DIR)

    var sourceApk: File? = null

    init {
        if (!aaptPath.exists()) {
            Utils.copyFile(ctx.resources.openRawResource(R.raw.aapt), aaptPath)
        }

        aaptPath.setExecutable(true)

        if (!keystorePath.exists()) {
            Utils.copyFile(ctx.resources.openRawResource(R.raw.dragaliafound), keystorePath)
        }

        try {
            val info = ctx.packageManager.getPackageInfo(Constants.PACKAGE_NAME, 0).applicationInfo
            sourceApk = File(info.sourceDir)
        } catch (_: Exception) {

        }
    }
}