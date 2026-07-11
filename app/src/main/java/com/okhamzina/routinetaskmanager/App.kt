package com.okhamzina.routinetaskmanager

import android.app.Application
import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.runAppResultCatching
import com.okhamzina.routinetaskmanager.core.notifications.android.AppNotificationChannels
import com.okhamzina.routinetaskmanager.di.appModules
import com.okhamzina.routinetaskmanager.featureReminder.application.notifications.RescheduleRemindersUseCase
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
            runAppResultCatching(
                errorReporter = getKoin().get<ErrorReporter>()
            ) {
                getKoin().get<RescheduleRemindersUseCase>()()
            }
        }
    }
}
