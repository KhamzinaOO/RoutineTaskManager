package com.example.routinetaskmanager.featureReminder.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["name"]),
        Index(value = ["repeatType"])
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val instructionsText: String? = null,

    val repeatType: String,
    val repeatRuleJson: String,

    val notificationMode: String,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reminder_images",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["reminder_id"])
    ]
)
data class ReminderImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "reminder_id")
    val reminderId: Long,

    val imagePath: String,

    val sortOrder: Int = 0,

    val createdAt: Long = System.currentTimeMillis()
)