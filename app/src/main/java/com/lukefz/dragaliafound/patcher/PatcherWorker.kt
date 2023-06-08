package com.lukefz.dragaliafound.patcher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.patcher.steps.*
import com.lukefz.dragaliafound.utils.PatcherConfig
import com.lukefz.dragaliafound.utils.StorageUtil
import com.lukefz.dragaliafound.utils.Utils
import kotlinx.coroutines.runBlocking
import kotlin.io.path.absolutePathString

class PatcherWorker(context: Context, parameters: WorkerParameters)
    : CoroutineWorker(context, parameters), StepManager {

    companion object {
        const val Progress = "Progress"
        const val Step = "Step"
        const val Tag = "DragaliPatch-Patcher"
        const val Messages = "Messages"
        const val ChannelId = "dragalipatch-work"
    }

    private var currentProgress = 0f
    private var currentStep = ""
    private var logMessages = mutableListOf<String>()

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        val storage = StorageUtil(applicationContext)

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

        val download = StepDownloadPatch(this, storage)
        val decompile = StepDecompile(this, storage)
        val patchManifest = StepPatchSplitManifest(this, storage)
        val patch = StepPatch(this, storage)
        val recompile = StepRecompile(this, storage)
        val align = StepAlign(this, storage)
        val sign = StepSign(this, storage)

        val retrieveConfigResult = PatcherConfig.retrieveApiOptions()
        if (!retrieveConfigResult)
            onMessage("WARNING: Failed to download config from API server! Using fallback values.")

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
            updateProgress(0.5f)
            recompile.run()
            updateProgress(0.8f)
            align.run()
            updateProgress(0.95f)
            sign.run()
            updateProgress(1.0f)

            onMessage("Ready to install.")
            updateStep(R.string.activity_patcher_completed)
        } catch (ex: Exception) {
            onMessage(Utils.getStackTrace(ex))
            showFailedNotification()
            return Result.failure()
        }

        val apkUri = FileProvider.getUriForFile(applicationContext, applicationContext.packageName.plus(".provider"), storage.signedApk)
        showFinishedNotification(Utils.configureInstallIntent(apkUri))
        return Result.success()
    }

    override suspend fun updateStep(id: Int) {
        currentStep = applicationContext.getString(id)
        logMessages.add("Next step: $currentStep")
        update()
    }

    override suspend fun updateProgress(progress: Float) {
        currentProgress = progress
        update()
    }

    override suspend fun addProgress(progress: Float) {
        currentProgress += progress
        update()
    }

    private suspend fun update() {
        setForeground(createForegroundInfo(createRunningNotification()))
        setProgress(workDataOf(Progress to currentProgress, Step to currentStep, Messages to logMessages.toTypedArray()))
    }

    override fun onMessage(line: String) {
        logMessages.add(line)
        runBlocking {
            update()
        }
    }

    private fun createForegroundInfo(notification: Notification): ForegroundInfo {
        createNotificationChannel()

        notificationManager.notify(1337, notification)
        return ForegroundInfo(1337, notification)
    }

    private fun createRunningNotification(): Notification {
        val title = "DragaliPatch Progress"
        val cancel = "Cancel"
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)

        return NotificationCompat.Builder(applicationContext, ChannelId)
            .setContentTitle("Patching in progress")
            .setContentText(currentStep)
            .setTicker(title)
            .setProgress(100, (currentProgress * 100).toInt(), false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()
    }

    private fun showFailedNotification() {
        val notification = NotificationCompat.Builder(applicationContext, ChannelId)
            .setContentTitle(applicationContext.getString(R.string.activity_patcher_title_failed))
            .setOngoing(false)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        notificationManager.notify(1338, notification)
    }

    private fun showFinishedNotification(installIntent: Intent) {
        val notification = NotificationCompat.Builder(applicationContext, ChannelId)
            .setContentTitle(applicationContext.getString(R.string.activity_patcher_title_completed))
            .setOngoing(false)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(android.R.drawable.ic_input_add, applicationContext.getString(R.string.install), PendingIntent.getActivity(applicationContext, 0, installIntent, PendingIntent.FLAG_IMMUTABLE))
            .build()

        notificationManager.notify(1338, notification)
    }

    private fun createNotificationChannel() {
        val name = "Current patch status"
        val description = "dragalipatch notification channel"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(ChannelId, name, importance)
        channel.description = description
        notificationManager.createNotificationChannel(channel)
    }
}