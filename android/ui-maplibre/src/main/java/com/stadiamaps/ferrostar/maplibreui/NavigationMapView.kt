package com.stadiamaps.ferrostar.maplibreui

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
<<<<<<< HEAD
import com.maplibre.compose.MapView
import com.maplibre.compose.StaticLocationEngine
import com.maplibre.compose.camera.MapViewCamera
import com.maplibre.compose.mapLibreStyleUrl
import com.maplibre.compose.ramani.LocationRequestProperties
import com.maplibre.compose.ramani.MapLibreComposable
import com.maplibre.compose.settings.MapControls
=======
>>>>>>> 01325f3aefd7212d8a67b59d7f47fad5213914c0
import com.stadiamaps.ferrostar.core.NavigationUiState
import com.stadiamaps.ferrostar.maplibreui.routeline.RouteOverlayBuilder
<<<<<<< HEAD
import com.stadiamaps.ferrostar.maplibreui.runtime.navigationMapViewCamera
import org.maplibre.android.location.engine.LocationEngineResult
import org.maplibre.android.maps.Style
=======
import com.stadiamaps.ferrostar.maplibreui.runtime.NavigationCameraMode
import com.stadiamaps.ferrostar.maplibreui.runtime.NavigationCameraOptions
import com.stadiamaps.ferrostar.maplibreui.runtime.NavigationMapState
import com.stadiamaps.ferrostar.maplibreui.runtime.TrackingCameraEffect
import com.stadiamaps.ferrostar.maplibreui.runtime.courseDegrees
import com.stadiamaps.ferrostar.maplibreui.runtime.defaultNavigationCameraMode
import com.stadiamaps.ferrostar.maplibreui.runtime.navigationCameraOptions
import com.stadiamaps.ferrostar.maplibreui.runtime.rememberDisplayedNavigationLocation
import com.stadiamaps.ferrostar.maplibreui.runtime.rememberFerrostarLocationState
import com.stadiamaps.ferrostar.maplibreui.runtime.rememberNavigationMapState
import com.stadiamaps.ferrostar.maplibreui.runtime.snapTrackingCameraToUserLocation
import kotlinx.coroutines.flow.collectLatest
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationPuckColors
import org.maplibre.compose.location.LocationPuckSizes
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Position
import uniffi.ferrostar.GeographicCoordinate
>>>>>>> 01325f3aefd7212d8a67b59d7f47fad5213914c0

/**
 * The base MapLibre map configured for navigation with a route line, location puck, gesture
 * callbacks, and Ferrostar-specific camera behavior for phone and tablet use.
 *
 * @param baseStyle The MapLibre base style to use for the map.
 * @param navigationMapState The Ferrostar-owned map state used to control follow, overview, free
 *   camera, and zoom behavior.
 * @param uiState The navigation UI state.
 * @param mapOptions The official MapLibre Compose options for ornaments, gestures, and map
 *   behavior.
 * @param routeOverlayBuilder The route overlay builder to use for rendering the route line.
 * @param navigationCameraOptions The camera templates applied when following the user in browsing
 *   and navigation modes.
 * @param locationPuckStyle The style to use for the official MapLibre location puck.
 * @param showDefaultPuck Whether Ferrostar should render its built-in location puck.
 * @param onMapLoadFinished A callback that is invoked when the map finished loading.
 * @param onMapLoadFailed A callback that is invoked when the map failed to load.
 * @param onMapClick Callback invoked for taps on the map with geographic coordinates and screen
 *   position.
 * @param onMapLongClick Callback invoked for long presses on the map with geographic coordinates
 *   and screen position.
 * @param content Any additional composable map symbol content to render.
 */
@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
@Composable
fun NavigationMapView(
    baseStyle: BaseStyle,
    navigationMapState: NavigationMapState = rememberNavigationMapState(),
    uiState: NavigationUiState,
    mapOptions: MapOptions,
    routeOverlayBuilder: RouteOverlayBuilder? = RouteOverlayBuilder.Default(),
    navigationCameraOptions: NavigationCameraOptions = navigationCameraOptions(),
    locationPuckStyle: NavigationMapPuckStyle = NavigationMapPuckStyle(),
    showDefaultPuck: Boolean = true,
    onMapLoadFinished: () -> Unit = {},
    onMapLoadFailed: (String?) -> Unit = {},
    onMapClick: NavigationMapClickHandler = { _, _ -> NavigationMapClickResult.Pass },
    onMapLongClick: NavigationMapClickHandler = { _, _ -> NavigationMapClickResult.Pass },
    content: @Composable @MaplibreComposable ((NavigationUiState) -> Unit)? = null,
) {
  val cameraState = navigationMapState.cameraState
  val userLocationState = rememberFerrostarLocationState(uiState.location)
  val displayedNavigationLocation = rememberDisplayedNavigationLocation(uiState)
  var lastKnownNavigationPuckBearing by remember { mutableStateOf(0.0) }
  navigationMapState.navigationCameraOptions = navigationCameraOptions

  var isNavigating by remember { mutableStateOf(uiState.isNavigating()) }
  if (uiState.isNavigating() != isNavigating) {
    isNavigating = uiState.isNavigating()
    navigationMapState.cameraMode = defaultNavigationCameraMode(isNavigating)
  }

  LaunchedEffect(displayedNavigationLocation?.courseDegrees) {
    displayedNavigationLocation?.courseDegrees?.let { lastKnownNavigationPuckBearing = it }
  }

  TrackingCameraEffect(
      navigationMapState = navigationMapState,
      userLocation = displayedNavigationLocation,
  )

  LaunchedEffect(cameraState, navigationMapState) {
    snapshotFlow { cameraState.moveReason }
        .collectLatest { moveReason ->
          if (moveReason == CameraMoveReason.GESTURE && navigationMapState.isTrackingUser) {
            navigationMapState.cameraMode = NavigationCameraMode.FREE
          }
        }
  }

  MaplibreMap(
      modifier = Modifier.fillMaxSize(),
<<<<<<< HEAD
      styleUrl,
      camera,
      mapControls,
      locationRequestProperties = locationRequestProperties,
      locationEngine = locationEngine,
      onMapReadyCallback =
          onMapReadyCallback ?: {
              if (isNavigating) camera.value = navigationCamera
               val mapStyle: Style = it
               val aa = mapStyle.json

                                },
  ) {
    routeOverlayBuilder.navigationPath(uiState)
=======
      baseStyle = baseStyle,
      cameraState = cameraState,
      onMapClick = { position, screenPosition ->
        onMapClick(position.toGeographicCoordinate(), screenPosition).toComposeClickResult()
      },
      onMapLongClick = { position, screenPosition ->
        onMapLongClick(position.toGeographicCoordinate(), screenPosition).toComposeClickResult()
      },
      onMapLoadFailed = onMapLoadFailed,
      onMapLoadFinished = {
        if (displayedNavigationLocation != null && navigationMapState.isTrackingUser) {
          navigationMapState.snapTrackingCameraToUserLocation(displayedNavigationLocation)
        }
        onMapLoadFinished()
      },
      options = mapOptions,
  ) {
    routeOverlayBuilder?.navigationPath(uiState)

    if (showDefaultPuck) {
      if (shouldRenderNavigationPuck(uiState) && displayedNavigationLocation != null) {
        NavigationPuckOverlay(
            target =
                NavigationPuckTarget(
                    longitude = displayedNavigationLocation.position.value.longitude,
                    latitude = displayedNavigationLocation.position.value.latitude,
                    bearingDegrees =
                        navigationPuckBearingDegrees(
                            currentBearing = displayedNavigationLocation.courseDegrees,
                            lastKnownBearing = lastKnownNavigationPuckBearing,
                        ),
                ),
            style = locationPuckStyle,
        )
      } else {
        LocationPuck(
            idPrefix = "ferrostar-location",
            location = userLocationState.location,
            cameraState = cameraState,
            colors =
                LocationPuckColors(
                    dotFillColorCurrentLocation = locationPuckStyle.dotFillColorCurrentLocation,
                    dotFillColorOldLocation = locationPuckStyle.dotFillColorOldLocation,
                    dotStrokeColor = locationPuckStyle.dotStrokeColor,
                    shadowColor = locationPuckStyle.shadowColor,
                    accuracyStrokeColor = locationPuckStyle.accuracyStrokeColor,
                    accuracyFillColor = locationPuckStyle.accuracyFillColor,
                    bearingColor = locationPuckStyle.bearingColor,
                ),
            sizes =
                LocationPuckSizes(
                    dotRadius = locationPuckStyle.dotRadius,
                    dotStrokeWidth = locationPuckStyle.dotStrokeWidth,
                ),
            showBearing = locationPuckStyle.showBearing,
            showBearingAccuracy = locationPuckStyle.showBearingAccuracy,
        )
      }
    }

>>>>>>> 01325f3aefd7212d8a67b59d7f47fad5213914c0
    if (content != null) {
      content(uiState)
    }
  }
}

<<<<<<< HEAD
fun locationCallBack(locResult:LocationEngineResult)
{

}
=======
private fun Position.toGeographicCoordinate(): GeographicCoordinate =
    GeographicCoordinate(lat = latitude, lng = longitude)

private fun NavigationMapClickResult.toComposeClickResult(): ClickResult =
    when (this) {
      NavigationMapClickResult.Pass -> ClickResult.Pass
      NavigationMapClickResult.Consume -> ClickResult.Consume
    }
>>>>>>> 01325f3aefd7212d8a67b59d7f47fad5213914c0
