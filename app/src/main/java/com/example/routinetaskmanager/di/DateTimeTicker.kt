package com.example.routinetaskmanager.di

import com.example.routinetaskmanager.core.time.DateTimeTicker
import com.example.routinetaskmanager.core.time.SystemDateTimeTicker
import org.koin.dsl.module

val dateTimeModule = module {
    single<DateTimeTicker> {
        SystemDateTimeTicker()
    }
}
