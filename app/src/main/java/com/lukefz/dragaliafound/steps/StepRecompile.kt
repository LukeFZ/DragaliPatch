package com.lukefz.dragaliafound.steps

import brut.androlib.Androlib
import brut.androlib.ApkOptions
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.logging.PatchLogger
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.StorageUtil
import com.lukefz.dragaliafound.utils.Utils

class StepRecompile(private val manager: StepManager, private val storage: StorageUtil) {
    fun run() {
        manager.updateStep(R.string.activity_patcher_step_recompile)

        val androlibLogger = PatchLogger(manager, Androlib::class.java.name)
        Utils.setField(Androlib::class.java.getDeclaredField("LOGGER"), androlibLogger)

        val options = ApkOptions()
        options.frameworkFolderLocation = storage.frameworkDir
        options.aaptPath = storage.aaptPath.absolutePath

        Androlib(options).build(storage.appPatchDir.toFile(), storage.unsignedApk)
        manager.onMessage("Finished recompiling!")
    }
}