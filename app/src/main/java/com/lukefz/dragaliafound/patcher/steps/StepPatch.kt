package com.lukefz.dragaliafound.patcher.steps

import android.util.Base64
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.*
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.patch.Patch
import java.io.RandomAccessFile
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.experimental.inv
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class StepPatch(private val manager: StepManager, private val storage: StorageUtil) : Step {
    private val apiValues = PatcherConfig

    override suspend fun run() {
        manager.updateStep(R.string.activity_patcher_step_native_patch)

        applyNativePatches(apiValues.apiOptions.mode)

        manager.onMessage("Applied native patches!")

        val urlWithoutPrefixAndSuffix = apiValues.apiUrl.split("://")[1].dropLast(1)
        patchBaasUrl(urlWithoutPrefixAndSuffix, apiValues.apiOptions.useUnifiedLogin, apiValues.isHttp)
        patchPackageName(urlWithoutPrefixAndSuffix)

        manager.updateStep(R.string.patcher_step_patch_other)

        applyRepoPatches()

        manager.onMessage("Finished applying patches!")
    }

    private fun patchBaasUrl(baasUrl: String, useBaas: Boolean, isHttp: Boolean) {
        val replacementUrl = if (useBaas) Constants.BAAS_URL else baasUrl
        val npf = storage.appPatchDir.resolve(Constants.BAAS_URL_LOCATION)
        var contents = npf
            .readText()
            .replace(Constants.DEFAULT_BAAS_URL, replacementUrl)
            .replace(Constants.DEFAULT_ACCOUNTS_URL, replacementUrl)

        if (!useBaas && isHttp)
            contents = contents.replace("\"purchaseMock\":false", "\"purchaseMock\":false, \"useHttp\":true")

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

    private fun applyRepoPatches() {
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

                        manager.onMessage("Finished installing patch.")
                    } else if (file.name == "addedFiles.zip") {
                        manager.onMessage("Adding files from addedFiles.zip into apk.")

                        file.inputStream().use { input ->
                            ZipInputStream(input).use { zip ->
                                var entry: ZipEntry? = zip.nextEntry
                                while (entry != null) {
                                    manager.onMessage("Adding file $entry.name")
                                    Utils.copyFile(zip, storage.appPatchDir.resolve(entry.name).toFile())
                                    zip.closeEntry()
                                    entry = zip.nextEntry
                                }
                            }
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
        val obfuscatedUrl = Utils.obfuscateUrl(apiValues.apiUrl, Constants.URL_MAX_LENGTH)

        val isArm64 = storage.appPatchDir.resolve("lib/arm64-v8a").exists()
        val libName = if (isArm64) "arm64-v8a" else "armeabi-v7a"

        val metadataOffset = if (isArm64) Constants.Arm64Constants.METADATA_OFFSET else Constants.Arm32Constants.METADATA_OFFSET

        val ret = if (isArm64) Constants.Arm64Constants.RET else Constants.Arm32Constants.RET
        val urlOffset = metadataOffset + Constants.MetadataConstants.URL_OFFSET
        val urlLengthOffset = metadataOffset + Constants.MetadataConstants.URL_LENGTH_OFFSET

        val tokenOffset = metadataOffset + Constants.MetadataConstants.RELIABLE_TOKEN_OFFSET
        val tokenLengthOffset = metadataOffset + Constants.MetadataConstants.RELIABLE_TOKEN_LENGTH_OFFSET

        val cdnUrl = apiValues.cdnUrl

        RandomAccessFile(
            storage.appPatchDir.resolve("lib/$libName/libil2cpp.so").toString(),
            "rw"
        ).use {
            it.seek(urlLengthOffset)
            it.writeByte(apiValues.apiUrl.length.toByte().inv().toInt())

            it.seek(urlOffset)
            it.write(obfuscatedUrl)

            it.seek(tokenLengthOffset)
            it.writeByte(apiValues.apiUrl.length.toByte().inv().toInt())

            it.seek(tokenOffset)
            it.write(Utils.obfuscateUrl(apiValues.apiUrl, 40))

            if (cdnUrl != Constants.DEFAULT_CDN_URL) {
                val cdnUrl1 = cdnUrl.plus("dl/assetbundles/Android//")
                val obfCdnUrl1 = Utils.obfuscateUrl(cdnUrl1, Constants.CDN_URL_1_MAX_LENGTH) // yes it actually has two backslashes at the end
                val cdnUrlOffset = metadataOffset + Constants.MetadataConstants.CDN_URL_OFFSET_1
                val cdnUrlLengthOffset = metadataOffset + Constants.MetadataConstants.CDN_URL_LENGTH_OFFSET_1

                // Write the version with two backslashes

                it.seek(cdnUrlLengthOffset)
                it.writeByte(cdnUrl1.length.toByte().inv().toInt())

                it.seek(cdnUrlOffset)
                it.write(obfCdnUrl1)

                // Now write the version without the second backslash

                val cdnUrl2Offset = metadataOffset + Constants.MetadataConstants.CDN_URL_OFFSET_2
                val cdnUrl2LengthOffset = metadataOffset + Constants.MetadataConstants.CDN_URL_LENGTH_OFFSET_2

                it.seek(cdnUrl2LengthOffset)
                it.writeByte((cdnUrl1.length - 1).toByte().inv().toInt())

                it.seek(cdnUrl2Offset)
                it.write(obfCdnUrl1.sliceArray(0 until obfCdnUrl1.size-1))
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
                    val publicKeyOffset = if (isArm64) Constants.Arm64Constants.CONESHELL_PUBKEY else Constants.Arm32Constants.CONESHELL_PUBKEY
                    val publicKey = Base64.decode(apiValues.apiOptions.coneshellKey, 0)
                    if (publicKey.size != 32)
                        throw IllegalArgumentException("Provided coneshell public key was not the required 32 bytes in size.")

                    it.seek(publicKeyOffset)
                    it.write(publicKey)
                }
            }

            val sunsetOffset = if (isArm64) Constants.Arm64Constants.SUNSET_OFFSET else Constants.Arm32Constants.SUNSET_OFFSET
            val sunsetPatch = if (isArm64) Constants.Arm64Constants.SUNSET_PATCH else Constants.Arm32Constants.SUNSET_PATCH

            it.seek(sunsetOffset)
            it.write(sunsetPatch.toBytesBE())
        }
    }
}