package com.lukefz.dragaliafound.utils

data class PatcherState(
    val logMessages: String = "",
    val currentStep: String = "",
    val currentProgress: Float = 0f,
    val hasFailed: Boolean = false,
    val hasFinished: Boolean = false
)
