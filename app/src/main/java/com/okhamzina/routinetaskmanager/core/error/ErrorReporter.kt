package com.okhamzina.routinetaskmanager.core.error

interface ErrorReporter {
    fun record(throwable: Throwable)
}

object NoOpErrorReporter : ErrorReporter{
    override fun record(throwable: Throwable) = Unit
}