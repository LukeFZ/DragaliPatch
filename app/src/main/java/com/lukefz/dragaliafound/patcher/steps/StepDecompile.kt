package com.lukefz.dragaliafound.patcher.steps

import brut.androlib.Androlib
import brut.androlib.ApkDecoder
import brut.androlib.res.AndrolibResources
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.logging.PatchLogger
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.Constants
import com.lukefz.dragaliafound.utils.StorageUtil
import com.lukefz.dragaliafound.utils.Utils
import kotlin.io.path.createDirectory
import kotlin.io.path.deleteExisting
import kotlin.io.path.readText
import kotlin.io.path.writeText

class StepDecompile(private val manager: StepManager, private val storage: StorageUtil) : Step {
    override suspend fun run() {
        manager.updateStep(R.string.activity_patcher_step_decompile)

        Utils.setField(Androlib::class.java.getDeclaredField("LOGGER"), PatchLogger(manager, Androlib::class.java.name))
        Utils.setField(AndrolibResources::class.java.getDeclaredField("LOGGER"), PatchLogger(manager, AndrolibResources::class.java.name))

        storage.appPatchDir.toFile().deleteRecursively()
        storage.appPatchDir.createDirectory()

        val apkFiles = if (storage.isSplitApk) listOf(storage.sourceApk!!) + storage.sourceApks!! else listOf(storage.sourceApk!!)
        var manifest: String? = null
        val decoder = ApkDecoder()
        decoder.setForceDelete(false)
        decoder.setFrameworkDir(storage.frameworkDir)

        for (apk in apkFiles) {
            val tempFolder = storage.appPatchDir.resolve(Constants.TEMP_PREFIX.plus(apk.nameWithoutExtension))

            decoder.setOutDir(tempFolder.toFile())
            decoder.setApkFile(apk)
            decoder.decode()

            if (apk.nameWithoutExtension == "base") {
                manifest = tempFolder.resolve(Constants.MANIFEST).readText()
            } else {
                tempFolder.resolve(Constants.APKTOOL).deleteExisting()
            }

            tempFolder.toFile().copyRecursively(storage.appPatchDir.toFile(), true)
            tempFolder.toFile().deleteRecursively()

            manager.addProgress(0.1f / apkFiles.size)
        }

        if (manifest == null)
            throw UnsupportedOperationException("Did not read valid app manifest during decompilation.")

        storage.appPatchDir.resolve(Constants.MANIFEST).writeText(manifest)

        decoder.close()

        manager.onMessage("Finished decompiling!")
    }
}