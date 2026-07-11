package com.okhamzina.routinetaskmanager.featureReminder.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class ReminderWithImages(
    @Embedded
    val reminder: ReminderEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "reminder_id"
    )
    val images: List<ReminderImageEntity>
)