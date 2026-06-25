package com.example.routinetaskmanager.featureReminder.domain.model

sealed interface ReminderImageInput {
    data class Existing(
        val id: Long,
        val path: String,
        val sortOrder: Int
    ) : ReminderImageInput

    data class NewExternal(
        val uriString: String
    ) : ReminderImageInput
}