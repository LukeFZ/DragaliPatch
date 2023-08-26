package com.lukefz.dragaliafound.utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

object PatcherConfig {
    var appName: String = ""

    var apiUrl: String = ""
        set(value) { field = fixUrl(value, Constants.URL_MAX_LENGTH) }

    var cdnUrl: String = ""
        set(value) { field = fixUrl(value, Constants.CDN_URL_MAX_LENGTH, false) }

    val isHttp: Boolean
        get() = apiUrl.startsWith("http://")

    var apiOptions: ApiProvidedOptions = ApiProvidedOptions()

    fun retrieveApiOptions(): Boolean {
        val client = OkHttpClient()
        try {
            val request = Request.Builder()
                .url(apiUrl.plus(Constants.CONFIG_ENDPOINT))
                .build()

            client.newCall(request).execute().use {
                if (it.isSuccessful && it.body != null)
                    apiOptions = Json.decodeFromString(it.body!!.string())
            }
        } catch (_: Exception) {
            apiOptions = ApiProvidedOptions()
            return false
        }

        if (apiOptions.mode == ApiMode.CONESHELL && apiOptions.coneshellKey == null)
            throw IllegalAccessException("Could not get server public key for Coneshell-enabled API.")

        if (apiOptions.cdnUrl.length > Constants.CDN_URL_MAX_LENGTH)
            throw IllegalAccessException("Server provided CDN url that was too long.")

        if (cdnUrl.isEmpty()) {
            cdnUrl = apiOptions.cdnUrl
        }

        return true
    }

    private fun fixUrl(url: String, maxLength: Int, preferHttps: Boolean = true): String {
        if (url.isEmpty())
            return url

        var addr = url

        if (!addr.matches(Regex("^(http|https)://.*$"))) {
            addr = if (preferHttps) "https://$addr" else "http://$addr"
        }

        if (!addr.endsWith("/")) {
            addr = addr.plus("/")
        }

        if (addr.length > maxLength) {
            throw IndexOutOfBoundsException("Server address too long: (${addr.length} > ${maxLength})")
        }

        return addr
    }
}