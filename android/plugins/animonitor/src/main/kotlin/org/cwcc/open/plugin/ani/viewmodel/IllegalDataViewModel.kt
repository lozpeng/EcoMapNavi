package org.cwcc.open.plugin.ani.viewmodel

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import com.combo.core.runtime.PluginManager
import kotlinx.coroutines.launch
import org.cwcc.open.plugin.ani.state.AniDataState
import org.cwcc.open.plugin.ani.utils.net.AniApiService
import org.cwcc.open.plugin.common.viewmodel.BaseViewModel
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.toJson

/**
 * 野生动物数据视图模式
 */

class IllegalDataViewModel(
    private val application: Application,
    private val apiService : AniApiService
): BaseViewModel<AniDataState>(  AniDataState())
{
    init{
      loadIllegalData()
    }
  private fun loadIllegalData()
  {
    viewModelScope.launch {
      updateState { copy(isLoading = true) }
      val result = apiService.fecthIllegalData()

      updateState { copy(isLoading = false)  }
    }
  }
}

@Composable
@MaplibreComposable
fun addIllegalSitesDataSource(){
  val illegalSites = rememberGeoJsonSource(
      data = GeoJsonData.JsonString("""{"type":"FeatureCollection","features":[]}"""),
      options = GeoJsonOptions(synchronousUpdate = true),)

  CircleLayer(
      id = "illegal-positions",
      source = illegalSites,
      onClick = { features ->
        println("Clicked on ${features[0].toJson()}")
        ClickResult.Consume
      },
  )
}
