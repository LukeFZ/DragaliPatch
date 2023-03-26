package com.lukefz.dragaliafound.logging

interface StepManager : LogListener {
    suspend fun updateStep(id: Int)
    suspend fun updateProgress(progress: Float)
    suspend fun addProgress(progress: Float)
}