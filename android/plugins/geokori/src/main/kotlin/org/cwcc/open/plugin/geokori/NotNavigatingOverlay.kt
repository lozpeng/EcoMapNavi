package org.cwcc.open.plugin.geokori

import android.graphics.Color.alpha
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.stadiamaps.autocomplete.AutocompleteSearch
import com.stadiamaps.autocomplete.center
import com.stadiamaps.ferrostar.composeui.views.components.controls.NavigationUIButton
import com.stadiamaps.ferrostar.composeui.views.components.gridviews.InnerGridView
import com.stadiamaps.ferrostar.core.location.toAndroidLocation
import com.stadiamaps.ferrostar.maplibreui.runtime.NavigationCameraMode
import com.stadiamaps.ferrostar.maplibreui.runtime.NavigationMapState
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.cwcc.open.geokori.ui.material3.bottomsheet.FlexibleBottomSheet
import org.cwcc.open.geokori.ui.material3.bottomsheet.core.FlexibleSheetSize
import org.cwcc.open.geokori.ui.material3.bottomsheet.core.FlexibleSheetValue
import org.cwcc.open.geokori.ui.material3.bottomsheet.core.rememberFlexibleBottomSheetState
import org.cwcc.open.plugin.geokori.ui.BottomSheetContentV2
import org.cwcc.open.plugin.geokori.ui.BottomSheetContentV3
import org.cwcc.open.plugin.geokori.ui.ImmersiveBottomSheet
import org.cwcc.open.plugin.geokori.ui.PoiDetailCardV2
import org.cwcc.open.plugin.geokori.ui.PoiItem
import org.cwcc.open.plugin.geokori.ui.SheetAnchor
import org.cwcc.open.plugin.geokori.ui.rememberImmersiveSheetState
import org.maplibre.android.location.LocationComponentOptions

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
  var targetValue by remember { mutableStateOf(FlexibleSheetValue.IntermediatelyExpanded) }
  var selectedPoi by remember { mutableStateOf<PoiItem?>(null) }
  var sheetProgress by remember { mutableFloatStateOf(0f) }
  val sheetState = rememberFlexibleBottomSheetState(
      flexibleSheetSize = FlexibleSheetSize(
          fullyExpanded = 0.9f,
          intermediatelyExpanded = 0.5f,
          slightlyExpanded = 0.18f,
      ),
      isModal = false,
      skipSlightlyExpanded = false,
  )
//  Box(modifier = Modifier.fillMaxSize()) {
//    //1.定义参数 面板状态
//    val sheetState = rememberImmersiveSheetState(SheetAnchor.PEEK)
//    var sheetProgress by remember { mutableFloatStateOf(0f) }
//    var selectedPoi by remember { mutableStateOf<PoiItem?>(null) }
//    // 2. 覆盖层：底部沉浸式面板
//    ImmersiveBottomSheet(
//        sheetState = sheetState,
//        onSheetProgress = { sheetProgress = it },
//        onAnchorChanged = { anchor ->
//          if (anchor == SheetAnchor.COLLAPSED) {
//            selectedPoi = null
//          }
//        }
//    ) {
//      BottomSheetContentV2(
//          progress = sheetProgress,
//          sheetState = sheetState,
//          onPoiClick = { poi ->
//            selectedPoi = poi
//          }
//      )
//    }
//    // 3. POI 详情浮层
  Box(modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Auto }
      .alpha(sheetState.visibilityProgress),
      )
  {
    FlexibleBottomSheet(
        sheetState = sheetState,
        containerColor = Color.White,
        onTargetChanges = { targetValue = it },
        dragHandle = null,
        windowInsets = WindowInsets.systemBars,
    ) {
      BottomSheetContentV3(
          targetValue = targetValue,
          sheetState = sheetState,
          onPoiClick = { poi ->
            selectedPoi = poi
          },
      )
    }
    AnimatedVisibility(
        visible =true,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
      selectedPoi?.let { poi ->
        PoiDetailCardV2(
            poi = poi,
            onClose = {
              selectedPoi = null
            },
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
