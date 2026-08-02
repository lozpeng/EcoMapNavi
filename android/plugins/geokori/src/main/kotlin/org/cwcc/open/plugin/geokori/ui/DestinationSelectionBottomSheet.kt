package org.cwcc.open.plugin.geokori.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions        // 改为这个
import androidx.compose.material.icons.filled.AddLocation     // 改为这个
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cwcc.open.plugin.geokori.DestinationSelection
import org.cwcc.open.plugin.geokori.R
import java.util.Locale
import uniffi.ferrostar.GeographicCoordinate

@Composable
fun DestinationSelectionBottomSheet(
    destination: DestinationSelection,
    onClose: () -> Unit,
    onStartNavigation: () -> Unit,
    onSheetHeightChanged: (Int) -> Unit,
    onAddGeoNote: (DestinationSelection) -> Unit
) {
  Box(
      modifier = Modifier.fillMaxSize().systemBarsPadding(),
      contentAlignment = Alignment.BottomStart,
  ) {
    Surface(
        modifier = Modifier.width(500.dp).onSizeChanged { onSheetHeightChanged(it.height) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
    ) {
      DestinationSelectionBottomSheetContent(
          destination = destination,
          onClose = onClose,
          onStartNavigation = onStartNavigation,
          onAddGeoBookMarker = onAddGeoNote
      )
    }
  }
}

@Composable
private fun DestinationSelectionBottomSheetContent(
    destination: DestinationSelection,
    onClose: () -> Unit,
    onStartNavigation: () -> Unit,
    onAddGeoBookMarker: (DestinationSelection) -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier =
          modifier.padding(
              horizontal = 24.dp,
              vertical = 16.dp,
          )
  ) {
    Text(
        text =
            destination.label?.takeUnless { it.isBlank() }
              ?: stringResource(R.string.dropped_pin_title),
        style = MaterialTheme.typography.headlineSmall,
    )
    Text(
        text =
            stringResource(
                R.string.destination_coordinates,
                formatCoordinates(destination.coordinate),
            ),
        modifier = Modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    // ===== 改造后的按钮区域：一行两个圆形 FAB + 文字 =====
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
      // 开始导航
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FloatingActionButton(
            onClick = onStartNavigation,
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
          Icon(
              imageVector = Icons.Filled.Directions,
              contentDescription = stringResource(R.string.start_navigation)
          )
        }
        Text(
            text = stringResource(R.string.start_navigation),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
      }

      // 添加标记
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FloatingActionButton(
            onClick = { onAddGeoBookMarker(destination) },
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
          Icon(
              imageVector = Icons.Default.AddLocation,
              contentDescription = stringResource(R.string.add_geomarker)
          )
        }
        Text(
            text = stringResource(R.string.add_geomarker),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    OutlinedButton(
        onClick = onClose,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp),
    ) {
      Text(stringResource(R.string.close_destination_sheet))
    }
  }
}

private fun formatCoordinates(coordinate: GeographicCoordinate): String =
    String.format(Locale.getDefault(), "%.5f, %.5f", coordinate.lat, coordinate.lng)

@Preview(showBackground = true)
@Composable
private fun DestinationSelectionBottomSheetContentPreview() {
  MaterialTheme {
    DestinationSelectionBottomSheetContent(
        destination =
            DestinationSelection(
                coordinate =
                    GeographicCoordinate(
                        lat = 51.507778,
                        lng = -0.1275,
                    ),
                label = "Trafalgar Square",
            ),
        onClose = {},
        onStartNavigation = {},
        onAddGeoBookMarker = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun DestinationSelectionBottomSheetContentWithoutLabelPreview() {
  MaterialTheme {
    DestinationSelectionBottomSheetContent(
        destination =
            DestinationSelection(
                coordinate =
                    GeographicCoordinate(
                        lat = 34.5678,
                        lng = 45.6789,
                    ),
            ),
        onClose = {},
        onStartNavigation = {},
        onAddGeoBookMarker = {},
    )
  }
}
