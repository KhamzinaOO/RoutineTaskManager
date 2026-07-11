package com.okhamzina.routinetaskmanager.core.time

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class SystemDateTimeTicker(
    private val clock: Clock = Clock.systemDefaultZone()
) : DateTimeTicker {

    override fun todayFlow(): Flow<LocalDate> = flow {
        while (currentCoroutineContext().isActive) {
            val today = LocalDate.now(clock)
            emit(today)

            val now = ZonedDateTime.now(clock)
            val nextMidnight = today
                .plusDays(1)
                .atStartOfDay(clock.zone)

            delay(
                Duration.between(now, nextMidnight)
                    .toMillis()
                    .coerceAtLeast(1_000L)
            )
        }
    }.distinctUntilChanged()

    override fun nowMinuteFlow(): Flow<LocalDateTime> = flow {
        while (currentCoroutineContext().isActive) {
            val now = ZonedDateTime.now(clock)
            emit(now.toLocalDateTime())

            val nextMinute = now
                .truncatedTo(ChronoUnit.MINUTES)
                .plusMinutes(1)

            delay(
                Duration.between(now, nextMinute)
                    .toMillis()
                    .coerceAtLeast(1_000L)
            )
        }
    }.distinctUntilChanged()
}