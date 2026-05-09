package com.example.routinetaskmanager.core.utills

import java.util.Locale

fun formatTime(timeMillis: Long): String {
    val minutes = (timeMillis / 1000) / 60
    val seconds = (timeMillis / 1000) % 60

    return String.format(locale = Locale.getDefault(), "%02d:%02d", minutes, seconds)
}