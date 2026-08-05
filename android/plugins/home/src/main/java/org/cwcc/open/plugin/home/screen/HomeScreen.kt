package org.cwcc.open.plugin.home.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import com.combo.core.runtime.PluginManager
import org.cwcc.open.plugin.common.component.EmptyPage
import org.cwcc.open.plugin.home.state.PluginStatus
import org.cwcc.open.plugin.home.viewmodel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cwcc.open.plugin.common.navigation.NavigationAnimations.fadeIn
import org.cwcc.open.plugin.common.navigation.NavigationAnimations.fadeOut
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

  // 面板状态
  val sheetState = rememberImmersiveSheetState(SheetAnchor.PEEK)  // 默认 PEEK（显示底部快捷栏）
  var sheetProgress by remember { mutableFloatStateOf(0f) }
  var selectedPoi by remember { mutableStateOf<PoiItem?>(null) }

  // 监听错误消息
  LaunchedEffect(state.isError, state.errorMessage) {
    if (state.isError && state.errorMessage != null) {
      Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
    }
  }

  val isWidScreen = currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(
      WIDTH_DP_EXPANDED_LOWER_BOUND
  )

  NavigationSuiteScaffold(
      navigationSuiteItems = {
        AppDestinations.entries.forEach {
          item(
              icon = { Icon(it.icon, contentDescription = it.label) },
              label = { Text(it.label) },
              selected = it == currentDestination,
              onClick = { currentDestination = it },
          )
        }
      },
      layoutType = if (isWidScreen) {
        NavigationSuiteType.NavigationDrawer
      } else {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
      },
  ) {
    // ✅ 关键：在 NavigationSuiteScaffold 的 content 内部用 Box 包裹
    Box(modifier = Modifier.fillMaxSize()) {
      // 1. 最底层：地图/插件内容
      when (currentDestination) {
        AppDestinations.GeoKori -> PluginScreenContent(
            pluginId = HomeViewModel.PLUGIN_GEOKORI,
            viewModel = viewModel
        )
        AppDestinations.SAMPLE -> PluginScreenContent(
            pluginId = HomeViewModel.PLUGIN_EXAMPLE,
            viewModel = viewModel
        )
        AppDestinations.SETTING -> PluginScreenContent(
            pluginId = HomeViewModel.PLUGIN_SETTING,
            viewModel = viewModel
        )
      }

      // 2. 覆盖层：底部沉浸式面板
      ImmersiveBottomSheet(
          sheetState = sheetState,
          onSheetProgress = { sheetProgress = it },
          onAnchorChanged = { anchor ->
            if (anchor == SheetAnchor.COLLAPSED) {
              selectedPoi = null
            }
          }
      ) {
        BottomSheetContentV2(
            progress = sheetProgress,
            sheetState = sheetState,
            onPoiClick = { poi ->
              selectedPoi = poi
            }
        )
      }

      // 3. POI 详情浮层（点击列表项后弹出）
      AnimatedVisibility(
          visible = selectedPoi != null && sheetState.currentAnchor != SheetAnchor.EXPANDED,
          enter = slideInVertically { it } + fadeIn(),
          exit = slideOutVertically { it } + fadeOut(),
          modifier = Modifier.align(Alignment.BottomCenter)
      ) {
        selectedPoi?.let { poi ->
          PoiDetailCardV2(
              poi = poi,
              onClose = { selectedPoi = null },
              onNavigate = {
                // 触发导航逻辑
              },
              modifier = Modifier
                  .padding(horizontal = 12.dp)
                  .padding(bottom = 240.dp)
                  .fillMaxWidth()
          )
        }
      }
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
}
