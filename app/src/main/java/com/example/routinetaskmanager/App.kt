package com.example.routinetaskmanager

import android.app.Application
import com.example.routinetaskmanager.core.error.runSuspendCatching
import com.example.routinetaskmanager.core.notifications.android.AppNotificationChannels
import com.example.routinetaskmanager.di.appModules
import com.example.routinetaskmanager.featureReminder.application.notifications.RescheduleRemindersUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModules)
        }

        getKoin().get<AppNotificationChannels>().createChannels()
        rescheduleRegularReminderNotifications()
    }

    private fun rescheduleRegularReminderNotifications() {
        applicationScope.launch {
            runSuspendCatching {
                getKoin().get<RescheduleRemindersUseCase>()()
            }
        }
    }
}
