package com.lukefz.dragaliafound.screens

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.StorageUtil
import com.lukefz.dragaliafound.utils.Utils
import kotlin.Float

class PatcherScreenViewModel(private val app: Application) : AndroidViewModel(app), StepManager {

    private val storage = StorageUtil(app.applicationContext)
    private val maxProgress = mutableStateOf(1f)
    val logMessages = mutableStateOf("")
    val currentStep = mutableStateOf("")
    val currentProgress = mutableStateOf(0f)
    val hasFailed = mutableStateOf(false)

    override fun onMessage(line: String) {
        logMessages.value = logMessages.value.plus("$line\n")
    }

    override fun hasFailed() {
        hasFailed.value = true
    }

    override fun updateStep(id: Int) {
        currentStep.value = app.getString(id)
        currentProgress.value = 0f
    }

    override fun setMaxProgress(progress: Float) {
        maxProgress.value = progress
    }

    override fun updateProgress(progress: Float) {
        currentProgress.value = progress / maxProgress.value
    }

    override fun launchInstallIntent() {
        val uri = FileProvider.getUriForFile(app.applicationContext, app.applicationContext.packageName.plus(".provider"), storage.signedApk)
        app.startActivity(Utils.configureInstallIntent(uri))
    }
}