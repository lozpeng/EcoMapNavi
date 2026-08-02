

package org.cwcc.open.plugin.setting.di

import org.cwcc.open.plugin.setting.viewmodel.SettingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val diModule =
    module {
        viewModel { SettingViewModel(get()) }
    }
