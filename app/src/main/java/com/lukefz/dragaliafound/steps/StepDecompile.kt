package com.lukefz.dragaliafound.steps

import brut.androlib.Androlib
import brut.androlib.ApkDecoder
import brut.androlib.res.AndrolibResources
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.logging.PatchLogger
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.Constants
import com.lukefz.dragaliafound.utils.StorageUtil
import com.lukefz.dragaliafound.utils.Utils
import kotlin.io.path.readText
import kotlin.io.path.writeText

class StepDecompile(private val manager: StepManager, private val storage: StorageUtil) {
    fun run() {
        manager.updateStep(R.string.activity_patcher_step_decompile)

        val androlibLogger = PatchLogger(manager, Androlib::class.java.name)
        Utils.setField(Androlib::class.java.getDeclaredField("LOGGER"), androlibLogger)

        val androlibResLogger = PatchLogger(manager, AndrolibResources::class.java.name)
        Utils.setField(AndrolibResources::class.java.getDeclaredField("LOGGER"), androlibResLogger)

        val decoder = ApkDecoder()

        try {
            decoder.setApkFile(if (storage.isSplitApk) storage.sourceApks?.get(0) else storage.sourceApk)
            decoder.setOutDir(storage.workDir.toFile())
            decoder.setForceDelete(true)
            decoder.setFrameworkDir(storage.frameworkDir)
            decoder.decode()

            if (storage.isSplitApk) {
                manager.updateProgress(0.1f / storage.sourceApks!!.size)

                val manifest = storage.workDir.resolve(Constants.MANIFEST)
                val originalManifest = manifest.readText()
                decoder.setForceDelete(false)
                for (file in storage.sourceApks!!.drop(1)) {
                    val tempFolder = storage.workDir.resolve(Constants.TEMP_PREFIX.plus(file.nameWithoutExtension))

                    decoder.setOutDir(tempFolder.toFile())
                    decoder.setApkFile(file)
                    decoder.decode()

                    tempFolder.toFile().copyRecursively(storage.workDir.toFile(), true)
                    tempFolder.toFile().deleteRecursively()

                    manager.updateProgress(0.1f / storage.sourceApks!!.size)
                }

                manifest.writeText(originalManifest)
            }

            manager.onMessage("Finished decompiling!")
        } finally {
            try {
                decoder.close()
            } catch (_: Exception) {}
        }
    }
}