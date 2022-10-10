package com.lukefz.dragaliafound.steps

import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.StorageUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class StepRunner(manager: StepManager, storage: StorageUtil) {
    val decompile = StepDecompile(manager, storage)
    val patch = StepPatch(manager, storage)
    val recompile = StepRecompile(manager, storage)
    val sign = StepSign(manager, storage)

    fun run() {
        runBlocking {
            launch {
                decompile.run()
                patch.run()
                recompile.run()
                sign.run()
            }
        }
    }
}