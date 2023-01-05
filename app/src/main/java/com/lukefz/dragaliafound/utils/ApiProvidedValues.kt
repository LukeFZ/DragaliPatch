package com.lukefz.dragaliafound.utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

object ApiProvidedValues {
    var apiUrl: String = ""
        set(value) { field = parseApiUrl(value) }

    val isHttp: Boolean
        get() = apiUrl.startsWith("http://")

    var config: DragalipatchConfig = DragalipatchConfig()

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

    fun getConfig(): Boolean {
        val client = OkHttpClient()
        try {
            val request = Request.Builder()
                .url(apiUrl.plus(Constants.CONFIG_ENDPOINT))
                .build()

            client.newCall(request).execute().use {
                if (it.isSuccessful && it.body != null)
                    config = Json.decodeFromString(it.body!!.string())
            }
        } catch (_: Exception) {
            config = DragalipatchConfig()
            return false
        }

        if (config.mode == ApiMode.CONESHELL && config.coneshellKey == null)
            throw IllegalAccessException("Could not get server public key for Coneshell-enabled API.")

        if (config.cdnUrl.length > 0x24)
            throw IllegalAccessException("Server provided CDN url that was too long.")

        return true
    }
}