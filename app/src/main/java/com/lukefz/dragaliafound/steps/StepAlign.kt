package com.lukefz.dragaliafound.steps

import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.StorageUtil

class StepAlign(private val manager: StepManager, private val storage: StorageUtil) {
    fun run() {
        val zipalignPath = storage.zipalignPath

        val builder = ProcessBuilder(listOf(
            zipalignPath.absolutePath, "-f", "4", storage.unsignedApk.absolutePath, storage.alignedApk.absolutePath
        ))

        manager.onMessage("Aligning apk...")

        builder.start().waitFor()

        manager.onMessage("Finished aligning apk!")
    }
}