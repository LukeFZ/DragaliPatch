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
import kotlin.io.path.deleteExisting
import kotlin.io.path.readText
import kotlin.io.path.writeText

class StepDecompile(private val manager: StepManager, private val storage: StorageUtil) {
    fun run() {
        manager.updateStep(R.string.activity_patcher_step_decompile)

        val androlibLogger = PatchLogger(manager, Androlib::class.java.name)
        Utils.setField(Androlib::class.java.getDeclaredField("LOGGER"), androlibLogger)

        val androlibResLogger = PatchLogger(manager, AndrolibResources::class.java.name)
        Utils.setField(AndrolibResources::class.java.getDeclaredField("LOGGER"), androlibResLogger)

        val decoder = ApkDecoder(storage.sourceApk)

        decoder.setOutDir(storage.appPatchDir.toFile())
        decoder.setForceDelete(true)
        decoder.setFrameworkDir(storage.frameworkDir)
        decoder.decode()

        if (storage.isSplitApk) {
            manager.addProgress(0.1f / (storage.sourceApks!!.size + 1))

            val manifest = storage.appPatchDir.resolve(Constants.MANIFEST)
            val originalManifest = manifest.readText()
            decoder.setForceDelete(false)
            for (file in storage.sourceApks!!) {
                val tempFolder = storage.appPatchDir.resolve(Constants.TEMP_PREFIX.plus(file.nameWithoutExtension))

                decoder.setOutDir(tempFolder.toFile())
                decoder.setApkFile(file)
                decoder.decode()

                tempFolder.resolve("apktool.yml").deleteExisting()
                tempFolder.toFile().copyRecursively(storage.appPatchDir.toFile(), true)
                tempFolder.toFile().deleteRecursively()

                manager.addProgress(0.1f / (storage.sourceApks!!.size + 1))
            }

            manifest.writeText(originalManifest)
        }

        decoder.close()

        manager.onMessage("Finished decompiling!")
    }
}