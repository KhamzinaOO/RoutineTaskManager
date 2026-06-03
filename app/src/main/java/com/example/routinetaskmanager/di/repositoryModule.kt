package com.example.routinetaskmanager.di

import com.example.routinetaskmanager.data.storage.ImageStorage
import com.example.routinetaskmanager.featureReminder.data.repository.ReminderRepositoryImpl
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val reminderRepositoryModule = module {

    single {
        ImageStorage(
            context = androidContext()
        )
    }

    single<ReminderRepository> {
        ReminderRepositoryImpl(
            reminderDao = get(),
            imageStorage = get()
        )
    }
}