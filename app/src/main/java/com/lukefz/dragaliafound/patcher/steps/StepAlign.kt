package com.lukefz.dragaliafound.patcher.steps

import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.StorageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class StepAlign(private val manager: StepManager, private val storage: StorageUtil) : Step {
    override suspend fun run() {
        val inputPath = storage.unsignedApk
        val outputPath = storage.alignedApk

        if (outputPath.exists())
            outputPath.delete()

        manager.onMessage("Aligning apk...")

        // Doing our own zipalign here, so we do not need the native version
        val zin = withContext(Dispatchers.IO) {
            ZipFile(inputPath)
        }
        val progressPerEntry = 0.15f / zin.size()
        var offset = 0

        ZipOutputStream(outputPath.outputStream()).use { zout ->
            for (entry in zin.entries()) {
                val newEntry = ZipEntry(entry)
                val extraSize = if (newEntry.extra != null) newEntry.extra.size else 0
                offset += 30 /* kLFHLen */ + newEntry.name.length + extraSize

                if (newEntry.method == ZipEntry.STORED && !newEntry.isDirectory) {
                    val alignment = if (newEntry.name.endsWith(".so")) 4096 else 4

                    val neededAlignment = alignment - (offset % alignment)
                    if (neededAlignment != alignment) {
                        offset += neededAlignment
                        val newExtra = ByteArray(extraSize + neededAlignment)
                        if (extraSize != 0) {
                            val existingExtra = newEntry.extra
                            existingExtra.copyInto(newExtra)
                        }
                        newEntry.extra = newExtra
                    }
                }

                zout.putNextEntry(newEntry)

                if (!newEntry.isDirectory) {
                    offset += newEntry.compressedSize.toInt()
                    val buffer = ByteArray(1024)
                    var len: Int
                    zin.getInputStream(entry).use { zinStream ->
                        while (true) {
                            len = zinStream.read(buffer, 0, buffer.size)
                            if (len == -1)
                                break
                            zout.write(buffer, 0, len)
                        }
                    }
                }

                manager.addProgress(progressPerEntry)
            }
        }

        if (!outputPath.exists())
            throw FileNotFoundException("Failed to zipalign apk.")

        manager.onMessage("Finished aligning apk!")
    }
}