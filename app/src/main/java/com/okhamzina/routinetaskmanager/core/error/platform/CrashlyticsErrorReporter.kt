package com.okhamzina.routinetaskmanager.core.error.platform

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.okhamzina.routinetaskmanager.core.error.ErrorReporter

class CrashlyticsErrorReporter : ErrorReporter {
    override fun record(throwable: Throwable) {
        Firebase.crashlytics.recordException(throwable)
    }
}