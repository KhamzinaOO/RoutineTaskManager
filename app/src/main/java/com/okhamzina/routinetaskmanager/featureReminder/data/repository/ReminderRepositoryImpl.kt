package com.okhamzina.routinetaskmanager.featureReminder.data.repository

import androidx.core.net.toUri
import androidx.room.withTransaction
import com.okhamzina.routinetaskmanager.data.local.AppDatabase
import com.okhamzina.routinetaskmanager.data.storage.ImageStorage
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderDao
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderEntity
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderImageEntity
import com.okhamzina.routinetaskmanager.featureReminder.data.mapper.ReminderRepeatRuleJsonMapper
import com.okhamzina.routinetaskmanager.featureReminder.data.mapper.toDomain
import com.okhamzina.routinetaskmanager.featureReminder.data.mapper.toRepeatType
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderDraft
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderImageInput
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
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
        draft: ReminderDraft
    ): Long {
        val now = System.currentTimeMillis()

        val preparedImages = prepareNewImages(
            images = draft.images,
            fileNamePrefix = "reminder_new_$now"
        )

        return try {
            database.withTransaction {
                val reminderId = reminderDao.insertReminder(
                    ReminderEntity(
                        id = 0,
                        name = draft.name.trim(),
                        instructionsText = draft.instructionsText
                            ?.trim()
                            ?.takeIf { it.isNotBlank() },
                        repeatType = draft.repeatRule.toRepeatType(),
                        repeatRuleJson = ReminderRepeatRuleJsonMapper.toJson(draft.repeatRule),
                        notificationMode = draft.notificationMode.name,
                        createdAt = now,
                        updatedAt = now
                    )
                )

                val imageEntities = preparedImages.mapIndexed { index, image ->
                    ReminderImageEntity(
                        id = 0,
                        reminderId = reminderId,
                        imagePath = image.path,
                        sortOrder = index,
                        createdAt = now
                    )
                }

                if (imageEntities.isNotEmpty()) {
                    reminderDao.insertReminderImages(imageEntities)
                }

                reminderId
            }
        } catch (throwable: Throwable) {
            preparedImages.forEach { image ->
                if (image.isNewFile) {
                    runCatching {
                        imageStorage.deleteImage(image.path)
                    }
                }
            }

            throw throwable
        }
    }

    override suspend fun updateReminder(
        reminderId: Long,
        draft: ReminderDraft
    ) {
        val now = System.currentTimeMillis()

        val currentReminder = reminderDao.getReminderById(reminderId)
            ?: throw NoSuchElementException()

        val currentImages = reminderDao.getImagesByReminderId(reminderId)

        val plan = buildImagesUpdatePlan(
            reminderId = reminderId,
            currentImages = currentImages,
            inputImages = draft.images,
            now = now
        )

        try {
            database.withTransaction {
                reminderDao.updateReminder(
                    currentReminder.copy(
                        name = draft.name.trim(),
                        instructionsText = draft.instructionsText
                            ?.trim()
                            ?.takeIf { it.isNotBlank() },
                        repeatType = draft.repeatRule.toRepeatType(),
                        repeatRuleJson = ReminderRepeatRuleJsonMapper.toJson(draft.repeatRule),
                        notificationMode = draft.notificationMode.name,
                        updatedAt = now
                    )
                )

                if (plan.imagesToUpdate.isNotEmpty()) {
                    reminderDao.updateReminderImages(plan.imagesToUpdate)
                }

                if (plan.imagesToInsert.isNotEmpty()) {
                    reminderDao.insertReminderImages(plan.imagesToInsert)
                }

                plan.imageIdsToDelete.forEach { imageId ->
                    reminderDao.deleteImageById(imageId)
                }
            }

            plan.filePathsToDeleteAfterTransaction.forEach { path ->
                runCatching {
                    imageStorage.deleteImage(path)
                }
            }
        } catch (throwable: Throwable) {
            plan.filePathsToCleanupOnFailure.forEach { path ->
                runCatching {
                    imageStorage.deleteImage(path)
                }
            }

            throw throwable
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

    private data class ReminderImagesUpdatePlan(
        val imagesToInsert: List<ReminderImageEntity>,
        val imagesToUpdate: List<ReminderImageEntity>,
        val imageIdsToDelete: List<Long>,
        val filePathsToDeleteAfterTransaction: List<String>,
        val filePathsToCleanupOnFailure: List<String>
    )

    private data class PreparedReminderImage(
        val path: String,
        val isNewFile: Boolean
    )

    private fun prepareNewImages(
        images: List<ReminderImageInput>,
        fileNamePrefix: String
    ): List<PreparedReminderImage> {
        return images.mapIndexed { index, image ->
            when (image) {
                is ReminderImageInput.Existing -> {
                    PreparedReminderImage(
                        path = image.path,
                        isNewFile = false
                    )
                }

                is ReminderImageInput.NewExternal -> {
                    val path = imageStorage.saveImageToInternalStorage(
                        sourceUri = image.uriString.toUri(),
                        fileName = "${fileNamePrefix}_$index.jpg"
                    )

                    PreparedReminderImage(
                        path = path,
                        isNewFile = true
                    )
                }
            }
        }
    }
    private fun buildImagesUpdatePlan(
        reminderId: Long,
        currentImages: List<ReminderImageEntity>,
        inputImages: List<ReminderImageInput>,
        now: Long
    ): ReminderImagesUpdatePlan {
        val currentById = currentImages.associateBy { it.id }
        val keptIds = mutableSetOf<Long>()
        val imagesToInsert = mutableListOf<ReminderImageEntity>()
        val imagesToUpdate = mutableListOf<ReminderImageEntity>()
        val savedNewPaths = mutableListOf<String>()

        inputImages.forEachIndexed { index, input ->
            when (input) {
                is ReminderImageInput.Existing -> {
                    val current = currentById[input.id]
                        ?: return@forEachIndexed

                    keptIds += current.id

                    if (current.sortOrder != index || current.imagePath != input.path) {
                        imagesToUpdate += current.copy(
                            imagePath = input.path,
                            sortOrder = index
                        )
                    }
                }

                is ReminderImageInput.NewExternal -> {
                    val savedPath = imageStorage.saveImageToInternalStorage(
                        sourceUri = input.uriString.toUri(),
                        fileName = "reminder_${reminderId}_${System.currentTimeMillis()}_$index.jpg"
                    )

                    savedNewPaths += savedPath

                    imagesToInsert += ReminderImageEntity(
                        id = 0,
                        reminderId = reminderId,
                        imagePath = savedPath,
                        sortOrder = index,
                        createdAt = now
                    )
                }
            }
        }

        val imagesToDelete = currentImages.filter { it.id !in keptIds }

        return ReminderImagesUpdatePlan(
            imagesToInsert = imagesToInsert,
            imagesToUpdate = imagesToUpdate,
            imageIdsToDelete = imagesToDelete.map { it.id },
            filePathsToDeleteAfterTransaction = imagesToDelete.map { it.imagePath },
            filePathsToCleanupOnFailure = savedNewPaths
        )
    }
}
