package org.cwcc.open.plugin.geokori.support

import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.geojson.Point
import uniffi.ferrostar.GeographicCoordinate

class ChinaMapViewCameraDefaults {
  companion object {
    //北纬34°32'27.00",东经108°55'25.00" //中国中心点
    const val LONGITUDE_MAP_CENTER: Double = 116.39132004  //(116.0+23/60.0+51.5/3600.0)
    const val LATITUDE_MAP_CENTER: Double = 39.90564979        //(39.0+32/54.0+25.12/3600.0)

    val BND_BOX = LatLngBounds.from(
        53.55,
        135.08,
        3.85,
        73.55
    )
    val MAP_CENTER:Point = Point.fromLngLat(LONGITUDE_MAP_CENTER,LATITUDE_MAP_CENTER)

    val InitLocation = GeographicCoordinate(39.90564979, 116.39132004)
  }
}
