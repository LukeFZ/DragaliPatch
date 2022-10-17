package com.lukefz.dragaliafound.logging

interface StepManager : LogListener {
    fun updateStep(id: Int)
    fun updateProgress(progress: Float)
    fun addProgress(progress: Float)
    fun hasFailed()
    fun installPatchedApp()
}