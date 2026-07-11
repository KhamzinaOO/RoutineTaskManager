package com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderImageInput
import com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.ReminderImageUi

fun ReminderImageUi.toInput(): ReminderImageInput {
    return when (this) {
        is ReminderImageUi.Saved -> {
            ReminderImageInput.Existing(
                id = id,
                path = path,
                sortOrder = sortOrder
            )
        }

        is ReminderImageUi.Picked -> {
            ReminderImageInput.NewExternal(
                uriString = uriString
            )
        }
    }
}