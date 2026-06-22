package com.stadiamaps.ferrostar.maplibreui.runtime

import android.content.res.Configuration
<<<<<<< HEAD
import android.view.Gravity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
=======
import androidx.compose.foundation.layout.PaddingValues
>>>>>>> 01325f3aefd7212d8a67b59d7f47fad5213914c0
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stadiamaps.ferrostar.composeui.runtime.paddingForGridView
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.OrnamentOptions

/**
 * Returns map options that keep the built-in ornaments clear of navigation overlays while leaving
 * gesture handling enabled.
 */
@Composable
internal fun rememberMapOptionsForProgressViewHeight(
    progressViewHeight: Dp = 0.dp,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
): MapOptions {
  val layoutDirection = LocalLayoutDirection.current
  val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
  val gridPadding = paddingForGridView()

  return remember(
      progressViewHeight,
      horizontalPadding,
      verticalPadding,
      contentPadding,
      isLandscape,
      gridPadding,
  ) {
    val startPadding = contentPadding.calculateStartPadding(layoutDirection)
    val endPadding =
        contentPadding.calculateEndPadding(layoutDirection) +
            gridPadding.calculateEndPadding(layoutDirection) +
            horizontalPadding
    val topPadding = contentPadding.calculateTopPadding() + verticalPadding
    val bottomPadding =
        contentPadding.calculateBottomPadding() +
            gridPadding.calculateBottomPadding() +
            if (isLandscape) {
              verticalPadding
            } else {
              progressViewHeight + verticalPadding
            }

<<<<<<< HEAD
        val bottomPaddingDp =
            windowInsetPadding.calculateBottomPadding() + gridPadding.calculateBottomPadding()
        val bottomOffsetDp =
            if (isLandscape) bottomPaddingDp else bottomPaddingDp + progressViewHeight

        // TODO: This could be improved if we want to add pixel width to dp conversion in
        //  maplibre-compose.
        val attributionOffset = 24.dp

//        value =
//            MapControls(
//                attribution =
//                    AttributionSettings.initWithLayoutAndPosition(
//                        layoutDirection,
//                        density,
//                        position =
//                            MapControlPosition.BottomEnd(
//                                horizontal = endOffsetDp,
//                                vertical = bottomOffsetDp + verticalPadding)),
//                compass = CompassSettings(enabled = false),
//                logo =
//                    LogoSettings.initWithLayoutAndPosition(
//                        layoutDirection,
//                        density,
//                        position =
//                            MapControlPosition.BottomEnd(
//                                horizontal = endOffsetDp + attributionOffset,
//                                vertical = bottomOffsetDp + verticalPadding)))

      value = MapControls(
                  attribution = AttributionSettings(enabled=false),
                  compass = CompassSettings.initWithLayoutAndPosition(
                      layoutDirection,
                      density,
                      enabled=true,
                      isFacingNorth=true ,
                      position =
                            MapControlPosition.TopEnd(
                                horizontal = endOffsetDp,
                                vertical =  24.dp)
                  ),
                  logo = LogoSettings(enabled = false)
                )

      }
=======
    MapOptions(
        ornamentOptions =
            OrnamentOptions(
                padding =
                    PaddingValues(
                        start = startPadding,
                        end = endPadding,
                        top = topPadding,
                        bottom = bottomPadding,
                    ),
                isCompassEnabled = false,
                isScaleBarEnabled = false,
                logoAlignment = Alignment.BottomStart,
                attributionAlignment = Alignment.BottomEnd,
            ),
    )
  }
>>>>>>> 01325f3aefd7212d8a67b59d7f47fad5213914c0
}
