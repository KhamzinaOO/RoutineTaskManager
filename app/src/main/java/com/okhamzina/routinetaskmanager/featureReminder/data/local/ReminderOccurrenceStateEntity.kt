package com.okhamzina.routinetaskmanager.featureReminder.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminder_occurrence_states",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("reminder_id"),
        Index("scheduledAtMillis"),
        Index("status")
    ]

)
data class ReminderOccurrenceStateEntity(
    @PrimaryKey
    val occurrenceKey: String,
    @ColumnInfo(name = "reminder_id") val reminderId: Long,
    val scheduledAtMillis: Long,
    val status: String,
    val actedAtMillis: Long = System.currentTimeMillis(),
    val occurrenceKind: String = "REGULAR"
)