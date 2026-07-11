package com.okhamzina.routinetaskmanager.di

import com.okhamzina.routinetaskmanager.core.coroutines.DefaultDispatcherProvider
import com.okhamzina.routinetaskmanager.core.coroutines.DispatcherProvider
import org.koin.dsl.module

val coroutineModule = module {
    single<DispatcherProvider>{
        DefaultDispatcherProvider()
    }
}