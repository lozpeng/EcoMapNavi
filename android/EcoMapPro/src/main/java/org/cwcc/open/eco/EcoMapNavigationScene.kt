package org.cwcc.open.eco

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.maplibre.compose.camera.CameraState
import com.maplibre.compose.camera.MapViewCamera
import com.maplibre.compose.camera.models.CameraPadding
import com.maplibre.compose.rememberSaveableMapViewCamera
import com.maplibre.compose.symbols.Circle
import com.stadiamaps.ferrostar.composeui.config.NavigationViewComponentBuilder
import com.stadiamaps.ferrostar.composeui.config.VisualNavigationViewConfig
import com.stadiamaps.ferrostar.composeui.config.withCustomOverlayView
import com.stadiamaps.ferrostar.composeui.config.withSpeedLimitStyle
import com.stadiamaps.ferrostar.composeui.runtime.KeepScreenOnDisposableEffect
import com.stadiamaps.ferrostar.composeui.views.components.speedlimit.SignageStyle
import com.stadiamaps.ferrostar.maplibreui.helper.getNextCamera
import com.stadiamaps.ferrostar.maplibreui.helper.rememberLocationPermissionLauncher
import com.stadiamaps.ferrostar.maplibreui.helper.rememberSynchronizedMapViewCamera
import com.stadiamaps.ferrostar.maplibreui.views.DynamicallyOrientingNavigationView
import kotlin.math.min
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

@Composable
fun EcoMapNavigationScene(
    savedInstanceState: Bundle?,
    viewModel: EcoMapNavigationViewModel = AppModule.viewModel
) {
  // Keeps the screen on at consistent brightness while this Composable is in the view hierarchy.
  KeepScreenOnDisposableEffect()

  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  // Get location permissions.
  // NOTE: This is NOT a robust suggestion for how to get permissions in a production app.
  // This is simply minimal sample code in as few lines as possible.
  val allPermissions =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION)
      } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
      }

  val navigationUiState by viewModel.navigationUiState.collectAsState(scope.coroutineContext)
  val location by viewModel.location.collectAsState()

  val permissionsLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
          permissions ->
        when {
          permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
            viewModel.startLocationUpdates()
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

  // FIXME: This is restarting navigation every time the screen is rotated.
  LaunchedEffect(savedInstanceState) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED) {
      viewModel.startLocationUpdates()
    } else {
      permissionsLauncher.launch(allPermissions)
    }
  }

  // Set up the map!

    val canChangeCamera = remember { mutableStateOf(false) }

    val cameraPadding = CameraPadding.fractionOfScreen(top = 0.5f)

    val mapViewCamera = rememberSaveableMapViewCamera(
        initialCamera =MapViewCamera.BoundingBox(AppModule.chinaBund)) // Or rememberMapViewCamera()
    val nextCameraState = getNextCamera(mapViewCamera.value.state)
    val permissionLauncher =
        rememberLocationPermissionLauncher(
            onAccess = {
                canChangeCamera.value = true
                mapViewCamera.value = MapViewCamera.TrackingUserLocation()
            },
            onFailed = { Log.w("CameraExample", "Location permission denied") })
    //
    val camera = rememberSynchronizedMapViewCamera(
        mapViewCamera,
        {
            when (it.state) {
                is CameraState.TrackingUserLocationWithBearing ->
                    it.copy(padding = cameraPadding)
                else -> it.copy(padding = CameraPadding())

            }
        })
    //val camera = rememberSaveableMapViewCamera(MapViewCamera.TrackingUserLocation())
  DynamicallyOrientingNavigationView(
      modifier = Modifier.fillMaxSize(),
      styleUrl = AppModule.mapStyleUrl, //MaplibreMap(baseStyle = BaseStyle.Uri(Res.getUri("files/style.json")))
      camera = camera,
      viewModel = viewModel,
      // Configure speed limit signage based on user preference or location
      config = VisualNavigationViewConfig.Default().withSpeedLimitStyle(SignageStyle.MUTCD),
      views =
          NavigationViewComponentBuilder.Default()
              .withCustomOverlayView(
                  customOverlayView = { modifier ->
                    location?.let { loc ->
                      AutocompleteOverlay(
                          modifier = modifier,
                          scope = scope,
                          isNavigating = navigationUiState.isNavigating(),
                          locationProvider = viewModel.locationProvider,
                          loc = loc)
                    }
                  }),
      onTapExit = { viewModel.stopNavigation() }) { uiState ->
        // Trivial, if silly example of how to add your own overlay layers.
        // (Also incidentally highlights the lag inherent in MapLibre location tracking
        // as-is.)
        uiState.location?.let { location ->
          Circle(
              center = LatLng(location.coordinates.lat, location.coordinates.lng),
              radius = 10f,
              color = "Blue",
              zIndex = 3,
          )

          if (location.horizontalAccuracy > 15) {
            Circle(
                center = LatLng(location.coordinates.lat, location.coordinates.lng),
                radius = min(location.horizontalAccuracy.toFloat(), 150f),
                color = "Blue",
                opacity = 0.2f,
                zIndex = 2,
            )
          }
        }
      }
}
