package com.lukefz.dragaliafound.utils

import kotlinx.serialization.Serializable

@Serializable
data class ApiProvidedOptions(
    val mode: ApiMode = ApiMode.RAW,
    val cdnUrl: String = "",
    val coneshellKey: String? = null,
    val useUnifiedLogin: Boolean = true
)
