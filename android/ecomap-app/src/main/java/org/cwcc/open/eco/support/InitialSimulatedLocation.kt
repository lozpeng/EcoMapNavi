package org.cwcc.open.eco.support

import java.time.Instant
import uniffi.ferrostar.GeographicCoordinate
import uniffi.ferrostar.UserLocation

val initialSimulatedLocation =
    UserLocation(
        ChinaMapViewCameraDefaults.InitLocation,
        6.0,
        null,
        Instant.now(),
        null,
    )
