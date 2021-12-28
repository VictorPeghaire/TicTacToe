package com.lisny.tictactoe

import android.os.Handler

fun formatSeconds(timeInSeconds: Int): String {
    val minutes = timeInSeconds / 60
    val seconds = timeInSeconds - minutes * 60
    var formattedTime = ""
    if (minutes < 10) formattedTime += "0"
    formattedTime += "$minutes:"
    if (seconds < 10) formattedTime += "0"
    formattedTime += seconds
    return formattedTime
}

fun setTimeout(delayMillis: Long, function: () -> Unit) {
    Handler().postDelayed({ function() }, delayMillis)
}