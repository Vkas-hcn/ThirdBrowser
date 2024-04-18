package com.blue.cat.fast.thirdbrowser.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blue.cat.fast.thirdbrowser.App
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {
    private var startTime = 0L
    private var timerJob: Job? = null

    fun startTimer() {
        if (timerJob == null || timerJob?.isCancelled == true) {
            startTime = System.currentTimeMillis()
            timerJob = GlobalScope.launch {
                while (true) {
                    val currentTime = System.currentTimeMillis()
                    val diff = currentTime - startTime
                    val h = diff / 3600000
                    val m = (diff % 3600000) / 60000
                    val s = (diff % 60000) / 1000
                    App.timerText = String.format("%02d:%02d:%02d", h, m, s)
                    delay(1000)
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        App.timerText = "00:00:00"
    }
}