package com.lukefz.dragaliafound.screens

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.steps.*
import com.lukefz.dragaliafound.utils.PatcherState
import com.lukefz.dragaliafound.utils.StorageUtil
import com.lukefz.dragaliafound.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.Float
import kotlin.io.path.absolutePathString

class PatcherScreenViewModel(private val app: Application) : AndroidViewModel(app), StepManager {

    private val ref = this
    private val storage = StorageUtil(app.applicationContext)
    var state by mutableStateOf(PatcherState())
        private set

    override fun onMessage(line: String) {
        state = state.copy(logMessages = state.logMessages.plus("$line\n"))
    }

    override fun hasFailed() {
        state = state.copy(hasFailed = true)
    }

    override fun updateStep(id: Int) {
        state = state.copy(currentStep = app.getString(id))
        onMessage("Next step: ${state.currentStep}")
    }

    override fun updateProgress(progress: Float) {
        state = state.copy(currentProgress = progress)
    }

    override fun addProgress(progress: Float) {
        state = state.copy(currentProgress = state.currentProgress + progress)
    }

    override fun installPatchedApp() {
        val uri = FileProvider.getUriForFile(app.applicationContext, app.applicationContext.packageName.plus(".provider"), storage.signedApk)
        app.startActivity(Utils.configureInstallIntent(uri))
    }

    /*override fun installPatchedApp() {
        val installer = app.applicationContext.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        val sessionId = installer.createSession(params)

        installer.openSession(sessionId).use {session ->
            storage.signedApk.inputStream().use { fileInput ->
                session.openWrite(storage.signedApk.name, 0, storage.signedApk.length()).use {
                    val buf = ByteArray(8192)
                    var count: Int

                    while (true) {
                        count = fileInput.read(buf)
                        if (count == -1)
                            break
                        it.write(buf, 0, count)
                    }

                    session.fsync(it)
                }
            }

            val intent = PendingIntent.getBroadcast(
                app.applicationContext,
                sessionId,
                Intent(),
                PendingIntent.FLAG_IMMUTABLE)
                .intentSender

            session.commit(intent)
        }
    }*/

    fun startPatch() {
        // Fix for OSDetection in apktool
        System.setProperty(
            "sun.arch.data.model",
            if (System.getProperty("os.arch")?.contains("64") == true) "64" else "32"
        )

        // Fix for awt color profile loading
        System.setProperty(
            "java.iccprofile.path",
            storage.colorDir.absolutePathString()
        )

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val download = StepDownloadPatch(ref, storage)
                val decompile = StepDecompile(ref, storage)
                val patchManifest = StepPatchSplitManifest(ref, storage)
                val patch = StepPatch(ref, storage)
                val recompile = StepRecompile(ref, storage)
                val align = StepAlign(ref, storage)
                val sign = StepSign(ref, storage)

                runBlocking {
                    try {
                        download.run()
                        updateProgress(0.2f)
                        decompile.run()
                        if (storage.isSplitApk) {
                            updateProgress(0.3f)
                            patchManifest.run()
                        }
                        updateProgress(0.4f)
                        patch.run()
                        updateProgress(0.6f)
                        recompile.run()
                        updateProgress(0.7f)
                        align.run()
                        updateProgress(0.8f)
                        sign.run()
                        updateProgress(1.0f)

                        onMessage("Ready to install.")
                        updateStep(R.string.activity_patcher_completed)
                        state = state.copy(hasFinished = true)
                    } catch (ex: Exception) {
                        onMessage(Utils.getStackTrace(ex))
                        hasFailed()
                    }
                }
            }
        }
    }
}