package org.cwcc.open.plugin.home.screen

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import com.combo.core.runtime.PluginManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cwcc.open.plugin.common.component.EmptyPage
import org.cwcc.open.plugin.home.state.PluginStatus
import org.cwcc.open.plugin.home.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * 主页屏幕
 *
 * 提供插件测试功能的主界面，包含导航、插件管理等功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
  val state by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.GeoKori) }

  // 监听错误消息
  LaunchedEffect(state.isError, state.errorMessage) {
    if (state.isError && state.errorMessage != null) {
      Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
    }
  }

  val isWidScreen = currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(
      WIDTH_DP_EXPANDED_LOWER_BOUND
  )

  // ========== 使用 Box 包裹所有内容 ==========
  Box(modifier = Modifier.fillMaxSize()) {
    // 1. 主内容（底层）
    PluginScreenContent(
        pluginId = HomeViewModel.PLUGIN_GEOKORI,
        viewModel = viewModel
    )

    // 2. 底部工具栏（上层）
    val toolbarAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300),
        label = "toolbar_alpha"
    )

    val toolbarOffsetY by animateDpAsState(
        targetValue = 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "toolbar_offset"
    )

    // 底部工具栏 - 使用 wrap_content 高度，让工具栏自己决定高度
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)  // 固定在底部
            .offset(y = toolbarOffsetY)
            .alpha(toolbarAlpha)
            .zIndex(1f)
            // 移除固定高度限制，让内容自己决定高度
            // 添加底部 padding 确保凸起按钮不被屏幕底部裁剪
            .padding(bottom = 0.dp)
    ) {
      GeoKoriCenterToolBar(
          onItemSelected = { index ->
            when (index) {
              0 -> currentDestination = AppDestinations.GeoKori
              1 -> currentDestination = AppDestinations.SAMPLE
              2 -> { /* 发布按钮 */ }
              3 -> currentDestination = AppDestinations.SETTING
              4 -> currentDestination = AppDestinations.PROFILE
            }
          },
          modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

/**
 * 插件页面的通用内容布局
 */
@Composable
private fun PluginScreenContent(pluginId: String, viewModel: HomeViewModel) {
  val state by viewModel.uiState.collectAsState()

  val entryClass = when (pluginId) {
    HomeViewModel.PLUGIN_GEOKORI -> state.guideEntryClass
    HomeViewModel.PLUGIN_EXAMPLE -> state.exampleEntryClass
    HomeViewModel.PLUGIN_SETTING -> state.settingEntryClass
    else -> null
  }

  when {
    // 1. 检查是否下载失败
    state.failedDownloads.contains(pluginId) -> {
      Column(
          modifier = Modifier.fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
      ) {
        Text("插件[$pluginId]下载失败！")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.retryDownload(pluginId) }) {
          Text("重试")
        }
      }
    }

    // 2. 检查是否正在下载
    state.downloadingPlugins.containsKey(pluginId) -> {
      val progress = state.downloadingPlugins[pluginId] ?: 0f
      Column(
          modifier = Modifier.fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
      ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "下载中... ${(progress * 100).toInt()}%")
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.width(200.dp))
      }
    }

    // 3. 显示正常状态
    else -> {
      val pluginStatus = viewModel.getPluginStatus(pluginId)
      EmptyPage(
          entryClass = entryClass,
          message =
              when (pluginStatus) {
                PluginStatus.NOT_INSTALLED -> "插件[$pluginId]未安装"
                PluginStatus.INSTALLED_NOT_STARTED -> "插件[$pluginId]未启动"
                PluginStatus.INSTALLED_AND_STARTED -> "插件[$pluginId]已启动"
              },
          buttonText =
              when (pluginStatus) {
                PluginStatus.NOT_INSTALLED -> "下载并安装最新插件"
                PluginStatus.INSTALLED_NOT_STARTED -> "启动插件"
                PluginStatus.INSTALLED_AND_STARTED -> "进入插件"
              },
          onButtonClick = {
            when (pluginStatus) {
              PluginStatus.NOT_INSTALLED -> {
                viewModel.installLatestPlugin(pluginId)
              }

              else -> {
                CoroutineScope(Dispatchers.Main).launch {
                  PluginManager.launchPlugin(pluginId)
                }
              }
            }
          },
      )
    }
  }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
  GeoKori("地图", Icons.Default.Home),
  SAMPLE("示例", Icons.Default.Star),
  SETTING("设置", Icons.Default.Settings),
  PROFILE("我的", Icons.Default.Person),
}
