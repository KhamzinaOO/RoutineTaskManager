package com.example.routinetaskmanager

import android.app.Application
import com.example.routinetaskmanager.core.notifications.AppNotificationChannel
import com.example.routinetaskmanager.di.appModules
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModules)
        }

        getKoin().get<AppNotificationChannel>().createChannels()
    }
}