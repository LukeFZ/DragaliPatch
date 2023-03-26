package com.lukefz.dragaliafound.screens

import android.app.Application
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.lukefz.dragaliafound.patcher.PatcherWorker
import com.lukefz.dragaliafound.utils.StorageUtil
import com.lukefz.dragaliafound.utils.Utils

class PatcherScreenViewModel(private val app: Application) : AndroidViewModel(app) {

    private val storage = StorageUtil(app.applicationContext)

    var hasFailed = false
    var hasFinished = false
    var logString = ""
    var currentStep = ""
    var currentProgress = 0f

    fun updateFromWorker(info: WorkInfo) {
        hasFailed = info.state == WorkInfo.State.FAILED || info.state == WorkInfo.State.CANCELLED
        hasFinished = info.state == WorkInfo.State.SUCCEEDED
        if (hasFailed || hasFinished) return

        currentStep = info.progress.getString(PatcherWorker.Step) ?: ""
        logString = (info.progress.getStringArray(PatcherWorker.Messages)?.toList() ?: listOf()).joinToString("\n")
        currentProgress = info.progress.getFloat(PatcherWorker.Progress, 0f)
    }

    fun installPatchedApp() {
        val uri = FileProvider.getUriForFile(app.applicationContext, app.applicationContext.packageName.plus(".provider"), storage.signedApk)
        app.startActivity(Utils.configureInstallIntent(uri))
    }

    fun shareLog(combinedMessages: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, combinedMessages)
        intent.type = "text/plain"
        val shareIntent = Intent.createChooser(intent, null)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        app.startActivity(shareIntent)
    }

    fun startWorker() {
        val request = OneTimeWorkRequestBuilder<PatcherWorker>()
            .build()

        WorkManager.getInstance(app.applicationContext)
            .enqueueUniqueWork(PatcherWorker.Tag, ExistingWorkPolicy.KEEP, request)
    }
}