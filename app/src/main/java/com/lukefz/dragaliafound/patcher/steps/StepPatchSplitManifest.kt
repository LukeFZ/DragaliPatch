package com.lukefz.dragaliafound.patcher.steps

import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.StorageUtil

class StepPatchSplitManifest(private val manager: StepManager, private val storage: StorageUtil) : Step {
    override suspend fun run() {
        manager.onMessage("Split APK detected - Patching manifest...")

        val manifest = storage.appPatchDir.resolve("AndroidManifest.xml").toFile()
        val manifestContent = manifest
            .readText()
            .replace(" android:extractNativeLibs=\"true\"", "")
            .replace(" android:isSplitRequired=\"true\"", "")
            .replace(
                "<meta-data android:name=\"com.android.vending.splits.required\" android:value=\"true\"/>",
                "<meta-data android:name=\"com.android.dynamic.apk.fused.modules\" android:value=\"base\"/>"
            )
            .replace(
                "STAMP_TYPE_DISTRIBUTION_APK",
                "STAMP_TYPE_STANDALONE_APK")
            .replace(
                "android:name=\"com.android.vending.derived.apk.id\" android:value=\"2\"",
                "android:name=\"com.android.vending.derived.apk.id\" android:value=\"3\""
            )

        manifest.writeText(manifestContent)

        manager.onMessage("Finished patching manifest!")
    }
}