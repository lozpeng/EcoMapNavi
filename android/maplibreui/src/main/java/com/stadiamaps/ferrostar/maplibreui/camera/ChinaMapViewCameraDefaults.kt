package com.stadiamaps.ferrostar.maplibreui.camera

import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.geojson.Point

class ChinaMapViewCameraDefaults {
    companion object {
        //北纬34°32'27.00",东经108°55'25.00"
        const val LONGITUDE_MAP_CENTER: Double =(108+55/60.0+25/3600.0)
        const val LATITUDE_MAP_CENTER: Double =(34.0+32/60.0+27/3600.0)

        val BND_BOX = LatLngBounds.from(
            53.55,
            135.08,
            3.85,
            73.55
        )
        val MAP_CENTER:Point = Point.fromLngLat(LONGITUDE_MAP_CENTER,LATITUDE_MAP_CENTER)
    }
}