package org.cwcc.open.plugin.geokori

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.stadiamaps.ferrostar.composeui.views.components.controls.NavigationUIButton
import com.stadiamaps.ferrostar.composeui.views.components.gridviews.InnerGridView
import com.stadiamaps.ferrostar.maplibreui.runtime.NavigationCameraMode
import com.stadiamaps.ferrostar.maplibreui.runtime.NavigationMapState
import org.cwcc.open.geokori.ui.material3.bottomsheet.FlexibleBottomSheet
import org.cwcc.open.geokori.ui.material3.bottomsheet.core.FlexibleSheetSize
import org.cwcc.open.geokori.ui.material3.bottomsheet.core.FlexibleSheetState
import org.cwcc.open.geokori.ui.material3.bottomsheet.core.FlexibleSheetValue
import org.cwcc.open.geokori.ui.material3.bottomsheet.core.rememberFlexibleBottomSheetState
import org.cwcc.open.plugin.geokori.ui.BottomSheetContentV3
import org.cwcc.open.plugin.geokori.ui.PoiDetailCardV2
import org.cwcc.open.plugin.geokori.ui.PoiItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotNavigatingOverlay(
    modifier: Modifier = Modifier,
    viewModel: DemoNavigationViewModel,
    navigationMapState: NavigationMapState,
    onTopOverlayBottomChanged: (Int) -> Unit = {},
) {
  val location by viewModel.location.collectAsState()
  val isSimulating by viewModel.simulated.collectAsState()
  val uiState by viewModel.navigationUiState.collectAsState()
  val stadiaApiKey = AppModule.stadiaApiKey

  LaunchedEffect(stadiaApiKey) {
    if (stadiaApiKey == null) {
      onTopOverlayBottomChanged(0)
    }
  }
  if (!uiState.isNavigating()) {
    InnerGridView(
        modifier = modifier.fillMaxSize().padding(bottom = 16.dp, top = 16.dp),
        topCenter = {
//          stadiaApiKey?.let { apiKey ->
//            Box(
//                modifier =
//                    Modifier.onGloballyPositioned { coordinates ->
//                      onTopOverlayBottomChanged(coordinates.boundsInRoot().bottom.roundToInt())
//                    }
//            ) {
//              AutocompleteSearch(apiKey = apiKey, userLocation = location?.toAndroidLocation()) {
//                  feature ->
//                feature.center()?.let { center ->
//                  viewModel.selectDestination(
//                      location = center,
//                      label = feature.properties.name,
//                      origin = DestinationSelectionOrigin.SearchResult,
//                  )
//                }
//              }
//
//            }
//          }
        },
        bottomStart = {

        },
        bottomEnd = {
            NavigationUIButton(
                onClick = {
                  AppModule.locationProvider.disableSimulation()
                  navigationMapState.cameraMode = NavigationCameraMode.FOLLOW_USER
                },
                buttonSize = DpSize(48.dp, 48.dp),
            ) {
              Icon(
                  painter = painterResource(R.drawable.my_location_24px),
                  contentDescription = stringResource(R.string.center_on_my_location),
              )
            }

//          Column(modifier = Modifier.padding(bottom = 24.dp), horizontalAlignment = Alignment.End) {
//            Button({ viewModel.toggleSimulation() }) {
//              val nextLocationText =
//                  if (!isSimulating) {
//                    stringResource(R.string.set_location_to_simulated)
//                  } else {
//                    stringResource(R.string.set_location_to_gps)
//                  }
//              Text(nextLocationText)
//            }
//
//            val currentLocationText =
//                if (isSimulating) {
//                  stringResource(R.string.location_is_simulated)
//                } else {
//                  stringResource(R.string.location_is_gps)
//                }
//
//            Text(
//                currentLocationText,
//                style =
//                    MaterialTheme.typography.titleSmall.copy(
//                        color = MaterialTheme.colorScheme.onTertiary,
//                        shadow = Shadow(blurRadius = 4.0f),
//                    ),
//            )
//          }
        },
    )
  }
}

// 辅助函数：根据 BottomSheet 状态计算偏移量
@Composable
private fun getBottomSheetOffset(
    sheetState: FlexibleSheetState,
    targetValue: FlexibleSheetValue
): Dp {
  val progress = sheetState.visibilityProgress // 需要在 FlexibleBottomSheetState 中暴露
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp
  val bottomSheetHeight = when (targetValue) {
    FlexibleSheetValue.FullyExpanded -> screenHeight * 0.9f
    FlexibleSheetValue.IntermediatelyExpanded -> screenHeight * 0.5f
    FlexibleSheetValue.SlightlyExpanded -> screenHeight * 0.18f
    else -> screenHeight * 0.5f
  }
  return bottomSheetHeight + 16.dp // 额外间距
}
