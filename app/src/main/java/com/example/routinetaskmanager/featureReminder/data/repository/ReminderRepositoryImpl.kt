package com.example.routinetaskmanager.featureReminder.data.repository

import android.net.Uri
import androidx.room.withTransaction
import com.example.routinetaskmanager.data.local.AppDatabase
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
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderSaveData
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepositoryImpl(
    private val database: AppDatabase,
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
        data: ReminderSaveData
    ): Long {
        val now = System.currentTimeMillis()
        val savedImagePaths = mutableListOf<String>()

        try {
            return database.withTransaction {
                val reminderEntity = ReminderEntity(
                    id = 0,
                    name = data.name.trim(),
                    instructionsText = data.instructionsText
                        ?.trim()
                        ?.takeIf { it.isNotBlank() },
                    repeatType = data.repeatRule.toRepeatType(),
                    repeatRuleJson = ReminderRepeatRuleJsonMapper.toJson(data.repeatRule),
                    notificationMode = data.notificationMode.name,
                    createdAt = now,
                    updatedAt = now
                )

                val reminderId = reminderDao.insertReminder(reminderEntity)

                val imageEntities = data.imageUris.mapIndexed { index, uri ->
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

                reminderId
            }
        } catch (e: Exception) {
            savedImagePaths.forEach { imagePath ->
                runCatching {
                    imageStorage.deleteImage(imagePath)
                }
            }

            throw e
        }
    }

    override suspend fun updateReminder(
        id: Long,
        data: ReminderSaveData
    ) {
        val now = System.currentTimeMillis()

        val savedNewImagePaths = mutableListOf<String>()

        try {
            database.withTransaction {
                val currentReminder = reminderDao.getReminderById(id)
                    ?: throw IllegalArgumentException("Reminder not found")

                val currentImages = reminderDao.getImagesByReminderId(id)
                val currentImagePaths = currentImages.map { it.imagePath }.toSet()
                val currentImagesByPath = currentImages.associateBy { it.imagePath }
                val finalImagePaths = data.imageUris.mapIndexed { index, uri ->
                    val existingImagePath = findExistingImagePath(
                        uri = uri,
                        currentImagePaths = currentImagePaths
                    )

                    if (existingImagePath != null) {
                        existingImagePath
                    } else {
                        val imagePath = imageStorage.saveImageToInternalStorage(
                            sourceUri = uri,
                            fileName = "reminder_${id}_${System.currentTimeMillis()}_$index.jpg"
                        )

                        savedNewImagePaths.add(imagePath)

                        imagePath
                    }
                }

                val updatedReminderEntity = currentReminder.copy(
                    name = data.name.trim(),
                    instructionsText = data.instructionsText
                        ?.trim()
                        ?.takeIf { it.isNotBlank() },
                    repeatType = data.repeatRule.toRepeatType(),
                    repeatRuleJson = ReminderRepeatRuleJsonMapper.toJson(data.repeatRule),
                    notificationMode = data.notificationMode.name,
                    updatedAt = now
                )

                val imageEntities = finalImagePaths.mapIndexed { index, imagePath ->
                    ReminderImageEntity(
                        id = currentImagesByPath[imagePath]?.id ?: 0,
                        reminderId = id,
                        imagePath = imagePath,
                        sortOrder = index,
                        createdAt = currentImagesByPath[imagePath]?.createdAt ?: now
                    )
                }
                reminderDao.updateReminder(updatedReminderEntity)

                reminderDao.deleteImagesByReminderId(id)

                if (imageEntities.isNotEmpty()) {
                    reminderDao.insertReminderImages(imageEntities)
                }

                val removedImagePaths = currentImagePaths - finalImagePaths.toSet()

                removedImagePaths.forEach { imagePath ->
                    runCatching {
                        imageStorage.deleteImage(imagePath)
                    }
                }
            }
        } catch (e: Exception) {
            savedNewImagePaths.forEach { imagePath ->
                runCatching {
                    imageStorage.deleteImage(imagePath)
                }
            }

            throw e
        }
    }

    override suspend fun deleteReminder(
        reminderId: Long
    ) {
        val imagePaths = database.withTransaction {
            val images = reminderDao.getImagesByReminderId(reminderId)

            reminderDao.deleteReminderById(reminderId)

            images.map { image ->
                image.imagePath
            }
        }

        imagePaths.forEach { imagePath ->
            runCatching {
                imageStorage.deleteImage(imagePath)
            }
        }
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
        val imagePath = database.withTransaction {
            val image = reminderDao.getImageById(imageId)
                ?: return@withTransaction null

            reminderDao.deleteImageById(imageId)

            image.imagePath
        } ?: return

        runCatching {
            imageStorage.deleteImage(imagePath)
        }
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

    private fun findExistingImagePath(
        uri: Uri,
        currentImagePaths: Set<String>
    ): String? {
        val uriString = uri.toString()
        val uriPath = uri.path

        return currentImagePaths.firstOrNull { imagePath ->
            imagePath == uriString || imagePath == uriPath
        }
    }
}