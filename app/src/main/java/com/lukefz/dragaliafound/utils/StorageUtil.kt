package com.lukefz.dragaliafound.utils

import android.annotation.SuppressLint
import android.content.Context
import com.lukefz.dragaliafound.R
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

@SuppressLint("QueryPermissionsNeeded") // manifest has the correct values set
@Suppress("DEPRECATION") // the non deprecated api is API > 33 - good luck getting people to use that one instead, google
class StorageUtil(ctx: Context) {

    val unsignedApk: File = ctx.externalCacheDir!!.toPath().resolve(Constants.PATCHED_PACKAGE_NAME.plus(Constants.UNSIGNED_SUFFIX).plus(Constants.APK_EXTENSION)).toFile()
    val alignedApk: File = ctx.externalCacheDir!!.toPath().resolve(Constants.PATCHED_PACKAGE_NAME.plus(Constants.ALIGNED_SUFFIX).plus(Constants.APK_EXTENSION)).toFile()
    val signedApk: File = ctx.getExternalFilesDir("")!!.toPath().resolve(Constants.PATCHED_PACKAGE_NAME.plus(Constants.APK_EXTENSION)).toFile()

    val aaptPath: File = Path(ctx.applicationInfo.nativeLibraryDir).resolve(Constants.AAPT).toFile()
    val zipalignPath: File = Path(ctx.applicationInfo.nativeLibraryDir).resolve(Constants.ZIPALIGN).toFile()
    val keystorePath: File = ctx.externalCacheDir!!.toPath().resolve(Constants.KEYSTORE).toFile()
    val frameworkDir: String = ctx.externalCacheDir!!.toPath().resolve(Constants.FRAMEWORK).toString()

    val appPatchDir: Path = ctx.externalCacheDir!!.toPath().resolve(Constants.PATCHING_DIR)
    val downloadedPatchDir: Path = ctx.externalCacheDir!!.toPath().resolve(Constants.PATCH_DOWNLOAD_DIR)

    var sourceApk: File? = null
    var isSplitApk: Boolean = false
    var sourceApks: List<File>? = null

    val colorDir: Path = ctx.externalCacheDir!!.toPath().resolve(Constants.COLOR_DIR)

    init {
        if (!keystorePath.exists()) {
            Utils.copyFile(ctx.resources.openRawResource(R.raw.dragaliafound), keystorePath)
        }

        if (!downloadedPatchDir.exists())
            downloadedPatchDir.toFile().mkdir()

        try {
            val info = ctx.packageManager.getPackageInfo(Constants.PACKAGE_NAME, 0).applicationInfo
            sourceApk = File(info.sourceDir)
            isSplitApk = info.splitSourceDirs != null
            if (isSplitApk)
                sourceApks = info.splitSourceDirs.map { a -> File(a) }

        } catch (_: Exception) {

        }

        if (!colorDir.exists()) {
            colorDir.toFile().mkdir()
           val colors = arrayOf(R.raw.srgb, R.raw.ciexyz, R.raw.gray, R.raw.pycc, R.raw.linear_rgb)
            val colorNames = arrayOf("sRGB", "CIEXYZ", "GRAY", "PYCC", "LINEAR_RGB")
            for (i in colors.indices) {
                Utils.copyFile(ctx.resources.openRawResource(colors[i]), colorDir.resolve(colorNames[i].plus(".pf")).toFile())
            }
        }
    }
}