package org.cwcc.open.plugin.common

import androidx.compose.runtime.Composable
import com.combo.core.api.IPluginEntryClass
import com.combo.core.model.PluginContext
import org.cwcc.open.plugin.common.di.navigationModule
import org.cwcc.open.plugin.common.update.UpdateManager
import org.cwcc.open.plugin.common.update.net.RetrofitClient
import org.koin.core.module.Module
import org.koin.dsl.module

class PluginEntryClass : IPluginEntryClass {
    override val pluginModule: List<Module>
        get() =
            listOf(
                navigationModule,
                module {
                    single { RetrofitClient.apiService }
                    single { UpdateManager(get(), get()) }
                }
            )

    @Composable
    override fun Content() {
    }

    override fun onLoad(context: PluginContext) {
    }

    override fun onUnload() {
    }
}
