package com.lukefz.dragaliafound.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.UnknownHostException

object ApiProvidedValues {
    private val client: OkHttpClient = OkHttpClient()

    var apiUrl: String = ""
        set(value) { field = parseApiUrl(value) }


    private fun parseApiUrl(url: String): String {
        var addr = url

        if (!addr.matches(Regex("^(http|https)://.*$"))) {
            addr = "https://$addr"
        }

        if (!addr.endsWith("/")) {
            addr = addr.plus("/")
        }

        if (addr.length > Constants.URL_MAX_LENGTH) {
            throw IndexOutOfBoundsException("Server address too long: (${addr.length} > ${Constants.URL_MAX_LENGTH})")
        }

        return addr
    }

    fun getApiMode(): ApiMode {
        val client = OkHttpClient()
        try {
            val request = Request.Builder()
                .url(apiUrl.plus(Constants.APIMODE_ENDPOINT))
                .build()

            client.newCall(request).execute().use {
                if (it.isSuccessful && it.body != null)
                    return ApiMode.valueOf(it.body!!.string())
            }
        } catch (_: Exception) { }

        return ApiMode.RAW
    }

    fun getPubKey(): ByteArray {
        try {
            val request = Request.Builder()
                .url(apiUrl.plus(Constants.CONESHELL_ENDPOINT))
                .build()

            client.newCall(request).execute().use {
                if (it.body != null) {
                    if (it.body!!.bytes().size == 32)
                        return it.body!!.bytes()
                    else
                        throw IllegalAccessException("Server provided pubkey that was not 32 bytes in length.")
                }
            }
        } catch (_: Exception) { }

        throw IllegalAccessException("Could not get server pubkey for Coneshell-enabled API.")
    }

    fun getCdnUrl(): String {
        try {
            val request = Request.Builder()
                .url(apiUrl.plus(Constants.CDNURL_ENDPOINT))
                .build()

            client.newCall(request).execute().use {
                if (it.isSuccessful && it.body != null) {
                    if (it.body!!.string().length > 0x24)
                        return it.body!!.string()
                    else
                        throw IllegalAccessException("Server provided cdn url that was too long.")
                }
            }
        } catch (_: UnknownHostException) { }

        return Constants.DEFAULT_CDN_URL
    }
}