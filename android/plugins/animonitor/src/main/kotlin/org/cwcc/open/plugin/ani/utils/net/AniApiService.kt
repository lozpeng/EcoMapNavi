package org.cwcc.open.plugin.ani.utils.net

import retrofit2.http.GET

interface AniApiService {
  /**
   * 获取插件配置文件
   */
  @GET("api/illegal/illegal")
  suspend fun fecthIllegalData(result: String ="geojson",page: Int =-1): String

}
