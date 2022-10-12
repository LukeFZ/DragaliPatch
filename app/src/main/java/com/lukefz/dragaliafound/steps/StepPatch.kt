package com.lukefz.dragaliafound.steps

import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.Constants
import com.lukefz.dragaliafound.utils.StorageUtil
import com.lukefz.dragaliafound.utils.Utils
import org.eclipse.jgit.api.Git
import java.io.FileInputStream
import java.io.RandomAccessFile

class StepPatch(private val manager: StepManager, private val storage: StorageUtil) {
    fun run() {
        manager.updateStep(R.string.activity_patcher_step_server_patch)

        var addr = Constants.currentCustomUrl

        if (!addr.matches(Regex("^(http|https)://.*$"))) {
            addr = "https://$addr"
        }

        if (!addr.endsWith("/")) {
            addr = addr.plus("/")
        }

        if (addr.length > Constants.URL_MAX_LENGTH) {
            throw IndexOutOfBoundsException("Server address too long: (${addr.length} > ${Constants.URL_MAX_LENGTH})")
        }

        val obfuscatedUrl = Utils.obfuscateUrl(addr)

        RandomAccessFile(
            storage.workDir.resolve("lib/arm64-v8a/libil2cpp.so").toString(),
            "rw"
        ).use {
            it.seek(Constants.URL_OFFSET_ARM64)
            it.write(obfuscatedUrl)

            it.seek(Constants.NETWORK_PACK_OFFSET_ARM64)
            it.writeByte(Constants.ARM64_RET.ushr(24).and(0xff).toInt())
            it.writeByte(Constants.ARM64_RET.ushr(16).and(0xff).toInt())
            it.writeByte(Constants.ARM64_RET.ushr(8).and(0xff).toInt())
            it.writeByte(Constants.ARM64_RET.and(0xff).toInt())

            it.seek(Constants.NETWORK_UNPACK_OFFSET_ARM64)
            it.writeByte(Constants.ARM64_RET.ushr(24).and(0xff).toInt())
            it.writeByte(Constants.ARM64_RET.ushr(16).and(0xff).toInt())
            it.writeByte(Constants.ARM64_RET.ushr(8).and(0xff).toInt())
            it.writeByte(Constants.ARM64_RET.and(0xff).toInt())
        }

        manager.onMessage("Patched server address!")
        manager.updateStep(R.string.patcher_step_patch_other)

        Git.init().setDirectory(storage.workDir.toFile()).call().use { git ->
            val files = storage.patchDir.toFile().listFiles()?.sorted()

            if (files != null) {
                for (file in files) {
                    if (file.extension == "patch") {
                        manager.onMessage("Installing patch ${file.nameWithoutExtension}")
                    }

                    FileInputStream(file).use {
                        git.apply().setPatch(it).call()
                    }
                }
            }

            manager.onMessage("Finished applying patches!")
        }
    }
}