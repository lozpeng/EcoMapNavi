package org.cwcc.open.plugin.ani.di

import org.cwcc.open.plugin.ani.viewmodel.IllegalDataViewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val diModule=
    module{
      viewModel{ IllegalDataViewModel(get(),get()) }
    }
