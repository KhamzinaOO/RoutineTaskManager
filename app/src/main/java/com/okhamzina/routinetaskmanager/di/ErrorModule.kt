package com.okhamzina.routinetaskmanager.di

import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.platform.CrashlyticsErrorReporter
import org.koin.dsl.module

val errorModule = module {
    single<ErrorReporter> {
        CrashlyticsErrorReporter()
    }
}