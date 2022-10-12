package com.lukefz.dragaliafound.steps

import brut.androlib.Androlib
import brut.androlib.ApkDecoder
import brut.androlib.res.AndrolibResources
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.logging.PatchLogger
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.StorageUtil
import com.lukefz.dragaliafound.utils.Utils

class StepDecompile(private val manager: StepManager, private val storage: StorageUtil) {
    fun run() {
        manager.updateStep(R.string.activity_patcher_step_decompile)

        val androlibLogger = PatchLogger(manager, Androlib::class.java.name)
        Utils.setField(Androlib::class.java.getDeclaredField("LOGGER"), androlibLogger)

        val androlibResLogger = PatchLogger(manager, AndrolibResources::class.java.name)
        Utils.setField(AndrolibResources::class.java.getDeclaredField("LOGGER"), androlibResLogger)

        val decoder = ApkDecoder()
        try {
            decoder.setApkFile(storage.sourceApk)
            decoder.setOutDir(storage.workDir.toFile())
            decoder.setForceDelete(true)
            decoder.setFrameworkDir(storage.frameworkDir)
            decoder.decode()
            manager.onMessage("Finished decompiling!")
        } finally {
            try {
                decoder.close()
            } catch (_: Exception) {}
        }
    }
}