package org.cwcc.open.plugin.setting

import androidx.compose.runtime.Composable
import com.combo.core.api.IPluginEntryClass
import com.combo.core.model.PluginContext
import org.cwcc.open.plugin.common.theme.FerrostarTheme
import org.cwcc.open.plugin.setting.di.diModule
import org.cwcc.open.plugin.setting.screen.SettingMainScreen
import org.koin.core.module.Module

class PluginEntryClass : IPluginEntryClass {
    override val pluginModule: List<Module>
        get() = listOf(
            diModule
        )

    @Composable
    override fun Content() {
      FerrostarTheme {
        SettingMainScreen()
      }
    }

    override fun onLoad(context: PluginContext) {
    }

    override fun onUnload() {
    }
}
