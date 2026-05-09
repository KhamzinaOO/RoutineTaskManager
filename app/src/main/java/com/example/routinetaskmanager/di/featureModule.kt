package com.example.routinetaskmanager.di

import com.example.routinetaskmanager.featureHome.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureHomeModule = module {
    viewModelOf(::HomeViewModel)
}