package org.cwcc.open.plugin.geokori

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import com.stadiamaps.ferrostar.composeui.config.NavigationViewComponentBuilder
import com.stadiamaps.ferrostar.composeui.config.VisualNavigationViewConfig
import com.stadiamaps.ferrostar.composeui.config.withCustomOverlayView
import com.stadiamaps.ferrostar.composeui.config.withSpeedLimitStyle
import com.stadiamaps.ferrostar.composeui.runtime.KeepScreenOnDisposableEffect
import com.stadiamaps.ferrostar.composeui.views.components.speedlimit.SignageStyle
import com.stadiamaps.ferrostar.maplibreui.NavigationMapClickResult
import com.stadiamaps.ferrostar.maplibreui.runtime.rememberNavigationMapState
import com.stadiamaps.ferrostar.maplibreui.views.DynamicallyOrientingNavigationView
import kotlinx.serialization.json.buildJsonObject
import org.cwcc.open.geokori.ui.material3.bottomsheet.FlexibleBottomSheet
import org.cwcc.open.geokori.ui.material3.bottomsheet.core.FlexibleSheetSize
import org.cwcc.open.geokori.ui.material3.bottomsheet.core.FlexibleSheetValue
import org.cwcc.open.geokori.ui.material3.bottomsheet.core.rememberFlexibleBottomSheetState
import org.cwcc.open.plugin.geokori.ui.BottomSheetContentV3
import org.cwcc.open.plugin.geokori.ui.DestinationSelectionBottomSheet
import org.cwcc.open.plugin.geokori.ui.DestinationSelectionCameraEffect
import org.cwcc.open.plugin.geokori.ui.PoiDetailCardV2
import org.cwcc.open.plugin.geokori.ui.PoiItem
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import uniffi.ferrostar.GeographicCoordinate

@Composable
fun DemoNavigationScene(viewModel: DemoNavigationViewModel = AppModule.viewModel) {
  // Keeps the screen on at consistent brightness while this Composable is in the view hierarchy.
  KeepScreenOnDisposableEffect()
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    viewModel.errorEvent.collect { message ->
      Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
  }
  // Get location permissions.
  // NOTE: This is NOT a robust suggestion for how to get permissions in a production app.
  // This is simply minimal sample code in as few lines as possible.
  val allPermissions =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION,
        )
      } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
      }

  val permissionsLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
          permissions ->
        when {
          permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
            viewModel.setLocationPermissions(true)
          }
          permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
            // TODO: Probably alert the user that this is unusable for navigation
          }
          // TODO: Foreground service permissions; we should block access until approved on API 34+
          else -> {
            // TODO
          }
        }
      }

  LaunchedEffect(Unit) {
    if (
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    ) {
      viewModel.setLocationPermissions(true)
    } else {
      permissionsLauncher.launch(allPermissions)
    }
  }
  val sceneState by viewModel.sceneState.collectAsState()
  val navigationMapState = rememberNavigationMapState()
  var destinationPreviewTopPaddingPx by remember { mutableStateOf(0) }
  DestinationSelectionCameraEffect(
      selectedDestination = sceneState.selectedDestination,
      destinationSheetHeightPx = sceneState.destinationSheetHeightPx,
      topOverlayBottomPx = destinationPreviewTopPaddingPx,
      navigationMapState = navigationMapState,
  )

  DynamicallyOrientingNavigationView(
      modifier = Modifier.fillMaxSize(),
      baseStyle = BaseStyle.Uri(AppModule.mapStyleUrl),
      navigationMapState = navigationMapState,
      viewModel = viewModel,
      config = VisualNavigationViewConfig.Default().withSpeedLimitStyle(SignageStyle.MUTCD),
      views =
          NavigationViewComponentBuilder.Default()
              .withCustomOverlayView(
                  customOverlayView = { modifier ->
                    NotNavigatingOverlay(
                        modifier = modifier,
                        viewModel = viewModel,
                        navigationMapState = navigationMapState,
                        onTopOverlayBottomChanged = { destinationPreviewTopPaddingPx = it },
                    )
                  },
              ),
      onTapExit = { viewModel.stopNavigation() },
      onMapLongClick = { position, screenPosition ->
        Log.d(
            "DemoNavigationScene",
            "Long press at lat=${position.lat}, lng=${position.lng}, screen=$screenPosition",
        )
        viewModel.selectDestination(position)
        NavigationMapClickResult.Consume
      },
      mapOptions =
          MapOptions(
              ornamentOptions =
                  OrnamentOptions(
                      isCompassEnabled = true,
                      isScaleBarEnabled = false,
                      isAttributionEnabled = false,
                      isLogoEnabled = false,
                  ),
          ),
  ) {
    DemoDroppedPinOverlay(sceneState.droppedPin)
  }
  //===========底部弹出框
  var targetValue by remember { mutableStateOf(FlexibleSheetValue.IntermediatelyExpanded) }
  var selectedPoi by remember { mutableStateOf<PoiItem?>(null) }
// 关键：记住弹出卡片前的 BottomSheet 状态，关闭后恢复
  var previousSheetValue by remember { mutableStateOf<FlexibleSheetValue?>(null) }
  val sheetState = rememberFlexibleBottomSheetState(
      flexibleSheetSize = FlexibleSheetSize(
          fullyExpanded = 0.9f,
          intermediatelyExpanded = 0.5f,
          slightlyExpanded = 0.1f,
      ),
      isModal = false,
      skipSlightlyExpanded = false,
      skipHiddenState = true,
      allowNestedScroll = true,
  )

// ========== 核心：选中 POI 时自动收起 BottomSheet，关闭后恢复 ==========
  LaunchedEffect(selectedPoi) {
    if (selectedPoi != null) {
      // 首次弹出时记录当前状态
      if (previousSheetValue == null) {
        previousSheetValue = sheetState.currentValue
      }
      // 强制收起到 SlightlyExpanded
      if (sheetState.currentValue != FlexibleSheetValue.SlightlyExpanded) {
        sheetState.slightlyExpand()
      }
    } else {
      // 卡片关闭后恢复到之前记住的状态
      previousSheetValue?.let { prev ->
        if (sheetState.currentValue != prev) {
          sheetState.animateTo(prev)
        }
        previousSheetValue = null
      }
    }
  }

// ========== BottomSheet ==========
  FlexibleBottomSheet(
      sheetState = sheetState,
      containerColor = Color.White,
      onTargetChanges = { targetValue = it },
      dragHandle = null,
      windowInsets = WindowInsets.systemBars,
      modifier = Modifier.fillMaxSize()
  ) {
    if (sceneState.isDestinationSheetVisible) {
      sceneState.selectedDestination?.let { destination ->
        DestinationSelectionBottomSheet(
            destination = destination,
            onClose = { viewModel.clearSelectedDestination() },
            onStartNavigation = { viewModel.startSelectedDestinationNavigation() },
            onSheetHeightChanged = viewModel::setDestinationSheetHeight,
            onAddGeoNote = { viewModel.addDestinationAsGeoNote(destination.coordinate) },
        )
      }
    } else {
      BottomSheetContentV3(
          targetValue = targetValue,
          sheetState = sheetState,
          onPoiClick = { poi ->
            selectedPoi = poi
          },
      )
    }
  }

// ========== POI 详情：非模态 Popup，真正屏幕正中央，点击外部关闭 ==========
  if (selectedPoi != null) {
    // 明确计算卡片宽度，避免 wrap content 测量冲突
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = (screenWidth * 0.85f).coerceAtMost(360.dp)

    Popup(
        alignment = Alignment.Center,           // ← 窗口本身在屏幕正中央
        properties = PopupProperties(
            focusable = false,                  // ← 非模态：不拦截背后地图/BottomSheet 的触摸
            dismissOnClickOutside = true,       // ← 点击卡片外部区域自动关闭
            dismissOnBackPress = true,          // ← 返回键关闭
        ),
        onDismissRequest = { selectedPoi = null }
    ) {
      // 传入明确宽度，确保 Popup 窗口大小 = 卡片大小，不再靠左靠顶
      PoiDetailCardV2(
          poi = selectedPoi!!,
          onClose = { selectedPoi = null },
          onNavigate = {
            // 触发导航逻辑
          },
          modifier = Modifier.width(cardWidth)
      )
    }
  }


}

@Composable
@MaplibreComposable
private fun DemoDroppedPinOverlay(droppedPin: GeographicCoordinate?) {
  val pinFeatureCollection = droppedPinFeatureCollectionOrNull(droppedPin) ?: return
  val pointSource = rememberGeoJsonSource(GeoJsonData.Features(pinFeatureCollection))

  CircleLayer(
      id = "demo-dropped-pin",
      source = pointSource,
      color = const(Color.Red),
      radius = const(10.dp),
      strokeColor = const(Color.White),
      strokeWidth = const(2.dp),
  )
}

internal fun droppedPinFeatureCollectionOrNull(pin: GeographicCoordinate?) = pin?.let {
  droppedPinFeatureCollection(it)
}

internal fun droppedPinFeatureCollection(pin: GeographicCoordinate) =
    FeatureCollection(
        Feature(
            geometry = Point(longitude = pin.lng, latitude = pin.lat),
            properties = buildJsonObject {},
        ),
    )
