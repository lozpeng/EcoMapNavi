package org.cwcc.open.plugin.ani.state

import org.cwcc.open.plugin.ani.layers.EcoLibreLayer
import org.cwcc.open.plugin.common.update.model.RemotePlugin
import org.cwcc.open.plugin.common.viewmodel.BaseUiState

/**
 * 野生动物数据加载状态
 */
data class AniDataState(
    val addLayers:List<EcoLibreLayer> = emptyList(), //添加的图层
    override val isLoading: Boolean = false,
    override val isError: Boolean = false,
    override val errorMessage: String? = null,
): BaseUiState
