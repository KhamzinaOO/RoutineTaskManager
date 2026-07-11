package com.okhamzina.routinetaskmanager.di

import com.okhamzina.routinetaskmanager.core.time.DateTimeTicker
import com.okhamzina.routinetaskmanager.core.time.SystemDateTimeTicker
import org.koin.dsl.module

val dateTimeModule = module {
    single<DateTimeTicker> {
        SystemDateTimeTicker()
    }
}
