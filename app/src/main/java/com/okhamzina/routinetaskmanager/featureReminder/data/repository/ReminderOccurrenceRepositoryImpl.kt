package com.okhamzina.routinetaskmanager.featureReminder.data.repository

import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderOccurrenceDAO
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderOccurrenceStateEntity
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceState
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderOccurrenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderOccurrenceRepositoryImpl(
    private val occurrenceDao: ReminderOccurrenceDAO
) : ReminderOccurrenceRepository {

    override suspend fun upsertState(state: ReminderOccurrence) {
        occurrenceDao.upsertState(
            ReminderOccurrenceStateEntity(
                occurrenceKey = state.occurrenceKey,
                reminderId = state.reminderId,
                scheduledAtMillis = state.scheduledAtMillis,
                status = state.status.name,
                actedAtMillis = System.currentTimeMillis(),
                occurrenceKind = state.occurrenceKind.name
            )
        )
    }

    override suspend fun upsertState(state: ReminderOccurrenceState) {
        occurrenceDao.upsertState(
            ReminderOccurrenceStateEntity(
                occurrenceKey = state.occurrenceKey,
                reminderId = state.reminderId,
                scheduledAtMillis = state.scheduledAtMillis,
                status = state.status.name,
                actedAtMillis = state.actedAtMillis,
                occurrenceKind = state.occurrenceKind.name
            )
        )
    }

    override suspend fun getStateByKey(key: String): ReminderOccurrenceState? {
        return occurrenceDao.getStateByKey(key)?.toDomain()
    }

    override fun observeByReminderAndRange(
        reminderId: Long,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<ReminderOccurrenceState>> {
        return occurrenceDao.observeByReminderAndRange(
            reminderId = reminderId,
            startMillis = startMillis,
            endMillis = endMillis
        ).map { entities ->
            entities.map {
                it.toDomain()
            }
        }
    }

    override suspend fun getByReminderAndRange(
        reminderId: Long,
        startMillis: Long,
        endMillis: Long
    ): List<ReminderOccurrenceState> {
        return occurrenceDao.getByReminderAndRange(
            reminderId = reminderId,
            startMillis = startMillis,
            endMillis = endMillis
        ).map { entities ->
            entities.toDomain()
        }
    }

    override fun observeByRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<ReminderOccurrenceState>> {
        return occurrenceDao.observeByRange(
            startMillis = startMillis,
            endMillis = endMillis
        ).map { entities ->
            entities.map {
                it.toDomain()
            }
        }
    }

    override suspend fun getByRange(
        startMillis: Long,
        endMillis: Long
    ): List<ReminderOccurrenceState> {
        return occurrenceDao.getByRange(
            startMillis = startMillis,
            endMillis = endMillis
        ).map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun deleteByReminderId(reminderId: Long) {
        occurrenceDao.deleteByReminderId(reminderId)
    }

    private fun ReminderOccurrenceStateEntity.toDomain(): ReminderOccurrenceState {
        return ReminderOccurrenceState(
            occurrenceKey = occurrenceKey,
            reminderId = reminderId,
            scheduledAtMillis = scheduledAtMillis,
            status = status.toOccurrenceStatus(),
            actedAtMillis = actedAtMillis,
            occurrenceKind = occurrenceKind.toOccurrenceKind()
        )
    }

    private fun String.toOccurrenceStatus(): ReminderOccurrenceStatus {
        return runCatching {
            ReminderOccurrenceStatus.valueOf(this)
        }.getOrDefault(ReminderOccurrenceStatus.PLANNED)
    }

    private fun String.toOccurrenceKind(): NotificationOccurrenceKind {
        return runCatching {
            NotificationOccurrenceKind.valueOf(this)
        }.getOrDefault(NotificationOccurrenceKind.REGULAR)
    }
}
