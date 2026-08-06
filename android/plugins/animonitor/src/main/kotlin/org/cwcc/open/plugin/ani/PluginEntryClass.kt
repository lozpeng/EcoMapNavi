package org.cwcc.open.plugin.ani

import androidx.compose.runtime.Composable
import com.combo.core.api.IPluginEntryClass
import com.combo.core.model.PluginContext
import org.cwcc.open.plugin.ani.di.diModule
import org.cwcc.open.plugin.ani.utils.net.AniRetrofitClient
import org.koin.core.module.Module
import org.koin.dsl.module

class PluginEntryClass : IPluginEntryClass{
  override val pluginModule: List<Module>
    get() =
      listOf(
          diModule,
          module {
            single { AniRetrofitClient.apiService }
            //single { UpdateManager(get(), get()) }
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
