package com.example.routinetaskmanager.featureReminder.domain.model

import android.net.Uri

data class ReminderSaveData(
    val name: String,
    val instructionsText: String?,
    val repeatRule: ReminderRepeatRule,
    val notificationMode: NotificationMode,
    val imageUris: List<Uri>
)