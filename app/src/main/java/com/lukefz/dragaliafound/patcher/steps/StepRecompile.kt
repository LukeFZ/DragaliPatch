package com.lukefz.dragaliafound.patcher.steps

import brut.androlib.Androlib
import brut.androlib.options.BuildOptions
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.logging.PatchLogger
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.StorageUtil
import com.lukefz.dragaliafound.utils.Utils

class StepRecompile(private val manager: StepManager, private val storage: StorageUtil) : Step {
    override suspend fun run() {
        manager.updateStep(R.string.activity_patcher_step_recompile)

        Utils.setField(Androlib::class.java.getDeclaredField("LOGGER"), PatchLogger(manager, Androlib::class.java.name))

        val options = BuildOptions()
        options.frameworkFolderLocation = storage.frameworkDir
        options.aaptPath = storage.aaptPath.absolutePath

        manager.onMessage("Recompiling...")
        Androlib(options).build(storage.appPatchDir.toFile(), storage.unsignedApk)
        manager.onMessage("Finished recompiling!")
    }
}