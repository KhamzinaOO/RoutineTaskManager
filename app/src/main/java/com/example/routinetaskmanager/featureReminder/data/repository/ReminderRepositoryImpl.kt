package com.example.routinetaskmanager.featureReminder.data.repository

import android.net.Uri
import com.example.routinetaskmanager.data.storage.ImageStorage
import com.example.routinetaskmanager.featureReminder.data.local.ReminderDao
import com.example.routinetaskmanager.featureReminder.data.local.ReminderEntity
import com.example.routinetaskmanager.featureReminder.data.local.ReminderImageEntity
import com.example.routinetaskmanager.featureReminder.data.mapper.ReminderRepeatRuleJsonMapper
import com.example.routinetaskmanager.featureReminder.data.mapper.toDomain
import com.example.routinetaskmanager.featureReminder.data.mapper.toEntity
import com.example.routinetaskmanager.featureReminder.data.mapper.toRepeatType
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao,
    private val imageStorage: ImageStorage
) : ReminderRepository {

    override suspend fun getAllRemindersSnapshot(): List<Reminder> {
        return reminderDao
            .getRemindersWithImagesSnapshot()
            .map { reminderWithImages ->
                reminderWithImages.toDomain()
            }
    }

    override fun observeReminders(): Flow<List<Reminder>> {
        return reminderDao.observeRemindersWithImages()
            .map { reminderWithImagesList ->
                reminderWithImagesList.map { reminderWithImages ->
                    reminderWithImages.toDomain()
                }
            }
    }

    override fun observeReminderById(
        reminderId: Long
    ): Flow<Reminder?> {
        return reminderDao.observeReminderWithImages(reminderId)
            .map { reminderWithImages ->
                reminderWithImages?.toDomain()
            }
    }

    override suspend fun getReminderById(
        reminderId: Long
    ): Reminder? {
        val reminderWithImages = reminderDao.getReminderWithImagesById(reminderId)
        return reminderWithImages?.toDomain()
    }

    override suspend fun createReminder(
        name: String,
        instructionsText: String?,
        repeatRule: ReminderRepeatRule,
        notificationMode: NotificationMode,
        imageUris: List<Uri>
    ): Long {
        val now = System.currentTimeMillis()

        val reminderEntity = ReminderEntity(
            id = 0,
            name = name.trim(),
            instructionsText = instructionsText
                ?.trim()
                ?.takeIf { it.isNotBlank() },
            repeatType = repeatRule.toRepeatType(),
            repeatRuleJson = ReminderRepeatRuleJsonMapper.toJson(repeatRule),
            notificationMode = notificationMode.name,
            createdAt = now,
            updatedAt = now
        )

        val reminderId = reminderDao.insertReminder(reminderEntity)

        val savedImagePaths = mutableListOf<String>()

        try {
            val imageEntities = imageUris.mapIndexed { index, uri ->
                val imagePath = imageStorage.saveImageToInternalStorage(
                    sourceUri = uri,
                    fileName = "reminder_${reminderId}_${System.currentTimeMillis()}_$index.jpg"
                )

                savedImagePaths.add(imagePath)

                ReminderImageEntity(
                    id = 0,
                    reminderId = reminderId,
                    imagePath = imagePath,
                    sortOrder = index,
                    createdAt = now
                )
            }

            if (imageEntities.isNotEmpty()) {
                reminderDao.insertReminderImages(imageEntities)
            }

            return reminderId
        } catch (e: Exception) {
            savedImagePaths.forEach { imagePath ->
                imageStorage.deleteImage(imagePath)
            }

            reminderDao.deleteReminderById(reminderId)

            throw e
        }
    }

    override suspend fun updateReminder(
        reminder: Reminder
    ) {
        reminderDao.updateReminder(reminder.toEntity())
    }

    override suspend fun deleteReminder(
        reminderId: Long
    ) {
        val images = reminderDao.getImagesByReminderId(reminderId)

        images.forEach { image ->
            imageStorage.deleteImage(image.imagePath)
        }

        reminderDao.deleteReminderById(reminderId)
    }

    override suspend fun addImageToReminder(
        reminderId: Long,
        imageUri: Uri
    ) {
        val currentImages = reminderDao.getImagesByReminderId(reminderId)

        val nextSortOrder = currentImages.maxOfOrNull { it.sortOrder }
            ?.plus(1)
            ?: 0

        val imagePath = imageStorage.saveImageToInternalStorage(
            sourceUri = imageUri,
            fileName = "reminder_${reminderId}_${System.currentTimeMillis()}.jpg"
        )

        try {
            reminderDao.insertReminderImage(
                ReminderImageEntity(
                    reminderId = reminderId,
                    imagePath = imagePath,
                    sortOrder = nextSortOrder,
                    createdAt = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            imageStorage.deleteImage(imagePath)
            throw e
        }
    }

    override suspend fun deleteImage(
        imageId: Long
    ) {
        val image = reminderDao.getImageById(imageId)
            ?: return

        imageStorage.deleteImage(image.imagePath)

        reminderDao.deleteImageById(imageId)
    }

    override suspend fun setReminderEnabled(
        reminderId: Long,
        enabled: Boolean
    ) {
        reminderDao.updateReminderEnabled(
            reminderId = reminderId,
            enabled = enabled
        )
    }

    override suspend fun setNotificationEnabled(
        reminderId: Long,
        enabled: Boolean
    ) {
        reminderDao.updateNotificationEnabled(
            reminderId = reminderId,
            enabled = enabled
        )
    }

    override suspend fun updateNotificationMode(
        reminderId: Long,
        notificationMode: NotificationMode
    ) {
        reminderDao.updateNotificationMode(
            reminderId = reminderId,
            notificationMode = notificationMode.name
        )
    }
}