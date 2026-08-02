

package org.cwcc.open.plugin.setting.state

import com.combo.core.model.PluginInfo
import org.cwcc.open.plugin.common.viewmodel.BaseUiState

data class SettingState(
    var installedPlugins: List<PluginInfo> = emptyList(),
    override val isLoading: Boolean = false,
    override val isError: Boolean = false,
    override val errorMessage: String? = null,
) : BaseUiState
