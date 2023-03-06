package com.lukefz.dragaliafound.utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

object ApiProvidedValues {
    var apiUrl: String = ""
        set(value) { field = Utils.fixUrl(value, Constants.URL_MAX_LENGTH) }

    var cdnUrl: String = Constants.DEFAULT_CDN_URL
        set(value) { field = Utils.fixUrl(value, Constants.CDN_URL_MAX_LENGTH) }

    val isHttp: Boolean
        get() = apiUrl.startsWith("http://")

    var config: DragalipatchConfig = DragalipatchConfig()

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

        if (config.cdnUrl.length > Constants.CDN_URL_MAX_LENGTH)
            throw IllegalAccessException("Server provided CDN url that was too long.")

        if (config.cdnUrl == Constants.DEFAULT_CDN_URL && cdnUrl != Constants.DEFAULT_CDN_URL)
            config = config.copy(cdnUrl = cdnUrl)

        return true
    }
}