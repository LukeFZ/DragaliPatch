package com.lukefz.dragaliafound.logging

abstract class LogRunnable(listener: LogListener) : Runnable {
    protected var logListener: LogListener = listener
}