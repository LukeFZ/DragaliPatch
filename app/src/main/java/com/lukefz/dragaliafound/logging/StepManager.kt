package com.lukefz.dragaliafound.logging

interface StepManager : LogListener {
    fun updateStep(id: Int)
    fun updateProgress(progress: Float)
    fun setMaxProgress(progress: Float)
    fun hasFailed()
    fun launchInstallIntent()
}