package com.stadiamaps.ferrostar.maplibreui.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.maplibre.compose.camera.CameraState
import com.maplibre.compose.camera.MapViewCamera
import com.maplibre.compose.rememberSaveableMapViewCamera
import com.stadiamaps.ferrostar.maplibreui.camera.ChinaMapViewCameraDefaults
import org.maplibre.android.geometry.LatLngBounds

fun getNextCamera(currentState: CameraState): MapViewCamera {
    return when (currentState) {
        is CameraState.BoundingBox -> MapViewCamera.Centered(
            ChinaMapViewCameraDefaults.LATITUDE_MAP_CENTER,
            ChinaMapViewCameraDefaults.LONGITUDE_MAP_CENTER)
        is CameraState.TrackingUserLocationWithBearing ->
            MapViewCamera.BoundingBox(ChinaMapViewCameraDefaults.BND_BOX)
        is CameraState.Centered -> MapViewCamera.TrackingUserLocation(zoom = 13.0, pitch = 45.0)
        is CameraState.TrackingUserLocation ->
            MapViewCamera.TrackingUserLocationWithBearing(zoom = 13.0, pitch = 45.0)
    }
}

@Composable
fun getNextCameraState(currentState: CameraState): MutableState<MapViewCamera>
{
    return rememberSaveable { mutableStateOf(getNextCamera(currentState)) }
}

@Composable
fun rememberSynchronizedMapViewCamera(
    externalCamera: MutableState<MapViewCamera>,
    transformExternalToLocal: (MapViewCamera) -> MapViewCamera = { it },
    transformLocalToExternal: (MapViewCamera) -> MapViewCamera = { it },
): MutableState<MapViewCamera> {
    val localCamera = rememberSaveableMapViewCamera(transformExternalToLocal(externalCamera.value))

    LaunchedEffect(externalCamera.value) {
        val transformed = transformExternalToLocal(externalCamera.value)
        if (localCamera.value != transformed) {
            localCamera.value = transformed
        }
    }

    LaunchedEffect(localCamera.value) {
        val transformed = transformLocalToExternal(localCamera.value)
        if (externalCamera.value != transformed) {
            externalCamera.value = transformed
        }
    }

    return localCamera
}