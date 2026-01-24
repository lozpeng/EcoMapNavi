package org.eco.map

import uniffi.ferrostar.CourseFiltering
import uniffi.ferrostar.NavigationCachingConfig
import uniffi.ferrostar.NavigationControllerConfig
import uniffi.ferrostar.RouteDeviationTracking
import uniffi.ferrostar.WaypointAdvanceMode
import uniffi.ferrostar.stepAdvanceDistanceEntryAndExit
import uniffi.ferrostar.stepAdvanceDistanceToEndOfStep

fun NavigationControllerConfig.Companion.ecoMapConfig(): NavigationControllerConfig {
    return NavigationControllerConfig(
        WaypointAdvanceMode.WaypointWithinRange(100.0),
        stepAdvanceDistanceEntryAndExit(30u, 5u, 32u),
        stepAdvanceDistanceToEndOfStep(10u, 32u),
        RouteDeviationTracking.StaticThreshold(15U, 50.0),
        CourseFiltering.SNAP_TO_ROUTE)
}

fun NavigationCachingConfig.Companion.ecoMapConfig(): NavigationCachingConfig {
    return NavigationCachingConfig(
        cacheIntervalSeconds = 300L,
        maxAgeSeconds = 86400L,
    )
}