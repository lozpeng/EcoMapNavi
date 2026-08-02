package org.cwcc.open.plugin.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.combo.core.api.IPluginEntryClass
import com.combo.core.model.PluginContext
import org.cwcc.open.plugin.common.navigation.IHubComposeNavigator
import org.cwcc.open.plugin.common.navigation.LocalComposeNavigator
import org.cwcc.open.plugin.home.di.diModule
import org.koin.core.module.Module
import org.koin.java.KoinJavaComponent.inject

/**
 * Compose主插件实现
 *
 * 提供应用的主界面内容，是插件框架的核心插件。
 * 包含了应用的完整UI和导航逻辑。
 *
 * @author IHUB Plugin Framework
 * @since 2.0.0
 */
class PluginEntryClass : IPluginEntryClass {
    override val pluginModule: List<Module>
        get() =
            listOf(
                diModule,
            )

    @Composable
    override fun Content() {
        val composeNavigator: IHubComposeNavigator by inject(
            clazz = IHubComposeNavigator::class.java,
        )

        CompositionLocalProvider(
            LocalComposeNavigator provides composeNavigator,
        ) {
            AppMain(composeNavigator = composeNavigator)
        }
    }

    override fun onLoad(context: PluginContext) {
    }

    override fun onUnload() {
    }
}
