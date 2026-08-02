package org.cwcc.open.plugin.home.di

import org.cwcc.open.plugin.home.viewmodel.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val diModule =
    module {
        viewModel { HomeViewModel(get()) }
    }
