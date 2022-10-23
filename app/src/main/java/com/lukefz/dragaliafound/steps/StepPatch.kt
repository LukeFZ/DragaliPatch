package com.lukefz.dragaliafound.steps

import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.logging.StepManager
import com.lukefz.dragaliafound.utils.Constants
import com.lukefz.dragaliafound.utils.StorageUtil
import com.lukefz.dragaliafound.utils.Utils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.patch.Patch
import java.io.RandomAccessFile
import android.os.Process
import com.lukefz.dragaliafound.utils.ApiMode
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.experimental.inv
import kotlin.io.path.readText
import kotlin.io.path.writeText

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

        Constants.currentCustomUrl = addr

        patchServerAddress(getApiMode())

        manager.onMessage("Patched server address!")

        val urlWithoutPrefixAndSuffix = addr.split("://")[1].dropLast(1)

        val npf = storage.appPatchDir.resolve(Constants.BAAS_URL_LOCATION)
        val contents = npf
            .readText()
            .replace(Constants.DEFAULT_BAAS_URL, urlWithoutPrefixAndSuffix)
            .replace(Constants.DEFAULT_ACCOUNTS_URL, urlWithoutPrefixAndSuffix)
        npf.writeText(contents)

        manager.updateStep(R.string.patcher_step_patch_other)

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

            manager.onMessage("Finished applying patches!")
        }
    }

    private fun patchServerAddress(mode: ApiMode) {
        val obfuscatedUrl = Utils.obfuscateUrl(Constants.currentCustomUrl)

        val isArm64 = Process.is64Bit()
        val libName = if (isArm64) "arm64-v8a" else "armeabi-v7a"

        val ret = if (isArm64) Constants.Arm64Constants.RET else Constants.Arm32Constants.RET
        val urlOffset = if (isArm64) Constants.Arm64Constants.URL_OFFSET else Constants.Arm32Constants.URL_OFFSET
        val urlLengthOffset = if (isArm64) Constants.Arm64Constants.URL_LENGTH_OFFSET else Constants.Arm32Constants.URL_LENGTH_OFFSET

        RandomAccessFile(
            storage.appPatchDir.resolve("lib/$libName/libil2cpp.so").toString(),
            "rw"
        ).use {
            it.seek(urlLengthOffset)
            it.writeByte(Constants.currentCustomUrl.length.toByte().inv().toInt())

            it.seek(urlOffset)
            it.write(obfuscatedUrl)

            when (mode) {
                ApiMode.RAW -> {
                    val coneshellOffset = if (isArm64) Constants.Arm64Constants.CONESHELL_OFFSET else Constants.Arm32Constants.CONESHELL_OFFSET

                    it.seek(coneshellOffset)
                    it.writeByte(ret.ushr(24).and(0xff).toInt())
                    it.writeByte(ret.ushr(16).and(0xff).toInt())
                    it.writeByte(ret.ushr(8).and(0xff).toInt())
                    it.writeByte(ret.and(0xff).toInt())
                }
                ApiMode.COMPRESSED -> {
                    val networkPackOffset = if (isArm64) Constants.Arm64Constants.NETWORK_PACK else Constants.Arm32Constants.NETWORK_PACK
                    val networkUnpackOffset = if (isArm64) Constants.Arm64Constants.NETWORK_UNPACK else Constants.Arm32Constants.NETWORK_UNPACK

                    it.seek(networkPackOffset)
                    it.writeByte(ret.ushr(24).and(0xff).toInt())
                    it.writeByte(ret.ushr(16).and(0xff).toInt())
                    it.writeByte(ret.ushr(8).and(0xff).toInt())
                    it.writeByte(ret.and(0xff).toInt())

                    it.seek(networkUnpackOffset)
                    it.writeByte(ret.ushr(24).and(0xff).toInt())
                    it.writeByte(ret.ushr(16).and(0xff).toInt())
                    it.writeByte(ret.ushr(8).and(0xff).toInt())
                    it.writeByte(ret.and(0xff).toInt())
                }
                ApiMode.CONESHELL -> {
                    val pubkeyOffset = if (isArm64) Constants.Arm64Constants.CONESHELL_PUBKEY else Constants.Arm32Constants.CONESHELL_PUBKEY
                    val pubkey = getPubKey()

                    it.seek(pubkeyOffset)
                    it.write(pubkey)
                }
            }
        }
    }

    private fun getApiMode(): ApiMode {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(Constants.currentCustomUrl.plus(Constants.APIMODE_ENDPOINT))
            .build()

        client.newCall(request).execute().use {
            if (it.isSuccessful && it.body != null)
                return ApiMode.valueOf(it.body!!.string())
        }

        return ApiMode.RAW
    }

    private fun getPubKey(): ByteArray {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(Constants.currentCustomUrl.plus(Constants.CONESHELL_ENDPOINT))
            .build()

        client.newCall(request).execute().use {
            if (it.body != null) {
                if (it.body!!.bytes().size == 32)
                    return it.body!!.bytes()
                else
                    throw IllegalAccessException("Server provided pubkey that was not 32 bytes in length.")
            }
        }

        throw IllegalAccessException("Could not get server pubkey for Coneshell-enabled API.")
    }
}