package org.cwcc.open.plugin.common.di

import org.cwcc.open.plugin.common.navigation.AppComposeNavigator
import org.cwcc.open.plugin.common.navigation.AppScreen
import org.cwcc.open.plugin.common.navigation.IHubComposeNavigator
import org.koin.dsl.module

val navigationModule =
    module {
        single { IHubComposeNavigator() }

        single<AppComposeNavigator<AppScreen>> { get() }
    }
