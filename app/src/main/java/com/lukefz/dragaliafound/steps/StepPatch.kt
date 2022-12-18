package com.lukefz.dragaliafound.steps

import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.*
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.patch.Patch
import java.io.RandomAccessFile
import kotlin.experimental.inv
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class StepPatch(private val manager: StepManager, private val storage: StorageUtil) {
    private val apiValues = ApiProvidedValues

    fun run() {
        manager.updateStep(R.string.activity_patcher_step_native_patch)

        applyNativePatches(apiValues.getApiMode())

        manager.onMessage("Applied native patches!")

        val urlWithoutPrefixAndSuffix = apiValues.apiUrl.split("://")[1].dropLast(1)
        patchBaasUrl(urlWithoutPrefixAndSuffix)
        patchPackageName(urlWithoutPrefixAndSuffix)

        manager.updateStep(R.string.patcher_step_patch_other)

       applyGitPatches()

        manager.onMessage("Finished applying patches!")
    }

    private fun patchBaasUrl(baasUrl: String) {
        val npf = storage.appPatchDir.resolve(Constants.BAAS_URL_LOCATION)
        val contents = npf
            .readText()
            .replace(Constants.DEFAULT_BAAS_URL, baasUrl)
            .replace(Constants.DEFAULT_ACCOUNTS_URL, baasUrl)
        npf.writeText(contents)
    }

    private fun patchPackageName(nameSuffix: String) {
        val patchedValues = listOf("en-rUS", "ja-rJP", "zh-rCN", "zh-rHK", "zh-rTW")
        val appNameRegex = Regex("<string name=\"app_name\">.*</string>\n")
        for (value in patchedValues) {
            val locValXml = storage.appPatchDir.resolve("res/${"values-".plus(value)}/strings.xml")
            val locContents = locValXml
                .readText()
                .replace(appNameRegex, "")

            locValXml.writeText(locContents)
        }

        val stringsXml = storage.appPatchDir.resolve(Constants.STRINGS_XML_LOCATION)
        val contents = stringsXml
            .readText() // NOTE: When adding localizations, pass in context or the localized patched app name directly
            .replace(Constants.DEFAULT_APP_NAME, Constants.PATCHED_APP_NAME.plus("\n$nameSuffix"))

        stringsXml.writeText(contents)
    }

    private fun applyGitPatches() {
        Git.init().setDirectory(storage.appPatchDir.toFile()).call().use { git ->
            val files = storage.downloadedPatchDir.toFile().listFiles()?.sorted()

            if (files != null) {
                for (file in files) {
                    if (file.extension == "patch") {
                        manager.onMessage("Installing patch ${file.nameWithoutExtension}")

                        file.inputStream().use { input ->
                            val patch = Patch()
                            patch.parse(input)
                            for (header in patch.files) {
                                if (header.patchType == FileHeader.PatchType.UNIFIED)
                                    Utils.normalizeFileEndings(storage.appPatchDir.resolve(header.oldPath).toFile())
                            }
                        }

                        file.inputStream().use {
                            git.apply().setPatch(it).call()
                        }
                    }
                }
            }
        }
    }

    private fun Long.toBytesBE(): ByteArray {
        return byteArrayOf(
            this.ushr(24).and(0xff).toByte(),
            this.ushr(16).and(0xff).toByte(),
            this.ushr(8).and(0xff).toByte(),
            this.and(0xff).toByte()
        )
    }

    private fun applyNativePatches(mode: ApiMode) {
        val obfuscatedUrl = Utils.obfuscateUrl(apiValues.apiUrl)

        val isArm64 = storage.appPatchDir.resolve("lib/arm64-v8a").exists()
        val libName = if (isArm64) "arm64-v8a" else "armeabi-v7a"

        val ret = if (isArm64) Constants.Arm64Constants.RET else Constants.Arm32Constants.RET
        val urlOffset = if (isArm64) Constants.Arm64Constants.URL_OFFSET else Constants.Arm32Constants.URL_OFFSET
        val urlLengthOffset = if (isArm64) Constants.Arm64Constants.URL_LENGTH_OFFSET else Constants.Arm32Constants.URL_LENGTH_OFFSET

        val cdnUrl = apiValues.getCdnUrl()

        RandomAccessFile(
            storage.appPatchDir.resolve("lib/$libName/libil2cpp.so").toString(),
            "rw"
        ).use {
            it.seek(urlLengthOffset)
            it.writeByte(apiValues.apiUrl.length.toByte().inv().toInt())

            it.seek(urlOffset)
            it.write(obfuscatedUrl)

            if (cdnUrl != Constants.DEFAULT_CDN_URL) {
                val cdnUrl1 = Utils.obfuscateUrl(cdnUrl.plus("/dl/assetbundles/Android//")) // yes it actually has two backslashes at the end
                val cdnUrlOffset = if (isArm64) Constants.Arm64Constants.CDN_URL_OFFSET_1 else Constants.Arm32Constants.CDN_URL_OFFSET_1
                val cdnUrlLengthOffset = if (isArm64) Constants.Arm64Constants.CDN_URL_LENGTH_OFFSET_1 else Constants.Arm32Constants.CDN_URL_LENGTH_OFFSET_1

                it.seek(cdnUrlLengthOffset)
                it.writeByte(cdnUrl1.count().toByte().inv().toInt())

                it.seek(cdnUrlOffset)
                it.write(cdnUrl1)

                val cdnUrl2 = Utils.obfuscateUrl(cdnUrl.plus("dl/assetbundles/Android/"))
                val cdnUrl2Offset = if (isArm64) Constants.Arm64Constants.CDN_URL_OFFSET_2 else Constants.Arm32Constants.CDN_URL_OFFSET_2
                val cdnUrl2LengthOffset = if (isArm64) Constants.Arm64Constants.CDN_URL_LENGTH_OFFSET_2 else Constants.Arm32Constants.CDN_URL_LENGTH_OFFSET_2

                it.seek(cdnUrl2LengthOffset)
                it.writeByte(cdnUrl2.count().toByte().inv().toInt())

                it.seek(cdnUrl2Offset)
                it.write(cdnUrl2)
            }

            when (mode) {
                ApiMode.RAW -> {
                    val coneshellOffset = if (isArm64) Constants.Arm64Constants.CONESHELL_OFFSET else Constants.Arm32Constants.CONESHELL_OFFSET

                    it.seek(coneshellOffset)
                    it.write(ret.toBytesBE())
                }
                ApiMode.COMPRESSED -> {
                    val networkPackOffset = if (isArm64) Constants.Arm64Constants.NETWORK_PACK else Constants.Arm32Constants.NETWORK_PACK
                    val networkUnpackOffset = if (isArm64) Constants.Arm64Constants.NETWORK_UNPACK else Constants.Arm32Constants.NETWORK_UNPACK

                    it.seek(networkPackOffset)
                    it.write(ret.toBytesBE())

                    it.seek(networkUnpackOffset)
                    it.write(ret.toBytesBE())
                }
                ApiMode.CONESHELL -> {
                    val pubkeyOffset = if (isArm64) Constants.Arm64Constants.CONESHELL_PUBKEY else Constants.Arm32Constants.CONESHELL_PUBKEY
                    val pubkey = apiValues.getPubKey()

                    it.seek(pubkeyOffset)
                    it.write(pubkey)
                }
            }

            val sunsetOffset = if (isArm64) Constants.Arm64Constants.SUNSET_OFFSET else Constants.Arm32Constants.SUNSET_OFFSET
            val sunsetPatch = if (isArm64) Constants.Arm64Constants.SUNSET_PATCH else Constants.Arm32Constants.SUNSET_PATCH

            it.seek(sunsetOffset)
            it.write(sunsetPatch.toBytesBE())
        }
    }
}