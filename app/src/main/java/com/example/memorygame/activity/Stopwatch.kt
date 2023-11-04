package com.example.memorygame.activity

import android.os.Handler
import android.widget.TextView

class Stopwatch(private val timeView: TextView) {
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var isRunning = false
    private val handler: Handler = Handler()

    init {
        timeView.text = "0:00:00" // marcação zero
    }

    fun start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            handler.postDelayed(updateTimer, 0)
            isRunning = true
        }
    }

    fun stop() {
        if (isRunning) {
            handler.removeCallbacks(updateTimer)
            isRunning = false
            elapsedTime = System.currentTimeMillis() - startTime
        }
    }

    fun reset() {
        elapsedTime = 0
        if (isRunning) {
            startTime = System.currentTimeMillis()
        } else {
            startTime = 0
        }
    }

    private val updateTimer: Runnable = object : Runnable {
        override fun run() {
            val currentTime = System.currentTimeMillis()
            elapsedTime = currentTime - startTime
            handler.postDelayed(this, 1000)

            val seconds = (elapsedTime / 1000 % 60).toInt()
            val minutes = ((elapsedTime / (1000 * 60)) % 60).toInt()
            val hours = (elapsedTime / (1000 * 60 * 60)).toInt()

            val time = String.format("%d:%02d:%02d", hours, minutes, seconds)
            timeView.text = time
        }
    }
}


