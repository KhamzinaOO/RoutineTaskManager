package com.example.routinetaskmanager.di

import com.example.routinetaskmanager.core.coroutines.DefaultDispatcherProvider
import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import org.koin.dsl.module

val coroutineModule = module {
    single<DispatcherProvider>{
        DefaultDispatcherProvider()
    }
}