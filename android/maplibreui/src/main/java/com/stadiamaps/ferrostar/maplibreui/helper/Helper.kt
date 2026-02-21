package com.stadiamaps.ferrostar.maplibreui.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import com.maplibre.compose.camera.CameraState
import com.maplibre.compose.camera.MapViewCamera
import com.maplibre.compose.rememberSaveableMapViewCamera
import org.maplibre.android.geometry.LatLngBounds

fun getNextCamera(currentState: CameraState): MapViewCamera {
    return when (currentState) {
        is CameraState.TrackingUserLocationWithBearing ->
            MapViewCamera.BoundingBox(
                LatLngBounds.from(
                    53.55,
                    135.08,
                    3.85,
                    73.55
                ))
        is CameraState.BoundingBox -> MapViewCamera.Centered((34.0+32/60.0+27/3600.0),
            (108+55/60.0+25/3600.0))
        is CameraState.Centered -> MapViewCamera.TrackingUserLocation(zoom = 13.0, pitch = 45.0)
        is CameraState.TrackingUserLocation ->
            MapViewCamera.TrackingUserLocationWithBearing(zoom = 13.0, pitch = 45.0)
    }
    //北纬34°32'27.00",东经108°55'25.00"
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