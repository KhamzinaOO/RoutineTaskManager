package com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model

sealed interface ReminderImageUi {
    val key: String

    data class Saved(
        val id: Long,
        val path: String,
        val sortOrder: Int
    ) : ReminderImageUi {
        override val key: String = "saved:$id"
    }

    data class Picked(
        val uriString: String
    ) : ReminderImageUi {
        override val key: String = "picked:$uriString"
    }
}