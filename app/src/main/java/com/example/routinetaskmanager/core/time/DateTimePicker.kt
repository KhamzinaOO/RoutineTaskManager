package com.example.routinetaskmanager.core.time

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface DateTimeTicker {
    fun todayFlow(): Flow<LocalDate>
    fun nowMinuteFlow(): Flow<LocalDateTime>
}