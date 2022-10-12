package com.lukefz.dragaliafound.steps

import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.Constants
import com.lukefz.dragaliafound.utils.StorageUtil
import com.lukefz.dragaliafound.utils.Utils
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class StepDownloadPatch(private val manager: StepManager, private val storage: StorageUtil) {
    fun run() {
        manager.updateStep(R.string.activity_patcher_step_download)

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(Constants.PATCH_DOWNLOAD_URL)
            .build()

        client.newCall(request).execute().use { response ->
            ZipInputStream(response.body?.byteStream()).use { zip ->
                var entry: ZipEntry?
                while (true) {
                    entry = zip.nextEntry

                    if (entry == null)
                        break

                    if (entry.name.endsWith(".patch")) {
                        val fileName = Paths.get(entry.name).fileName

                        manager.onMessage("Found patch: $fileName")

                        Utils.copyFile(zip, storage.patchDir.resolve(fileName).toFile())

                        zip.closeEntry()
                    }
                }
            }
        }

        manager.onMessage("Finished downloading patches!")
    }
}