plugins {
  alias(libs.plugins.androidLibrary)
}

android {
  namespace = "org.cwcc.geokori.dependencies"
  compileSdk = 36
}

dependencies {
  // ========== AndroidX 基础 ==========
  api(libs.material)
  api(libs.androidx.appcompat)
  api(libs.androidx.constraintlayout)
  api(libs.androidx.navigation.fragment.ktx)
  api(libs.androidx.navigation.ui.ktx)

  // ========== Compose BOM（统一管理所有 Compose 版本） ==========
  api(platform(libs.androidx.compose.bom))

  // ========== 基础 Compose 依赖（版本由 BOM 控制） ==========
  api("androidx.compose.runtime:runtime")
  api(libs.androidx.compose.ui)
  api(libs.androidx.compose.ui.tooling)
  api(libs.androidx.compose.ui.tooling.preview)
  api("androidx.compose.animation:animation")
  api("androidx.compose.material:material")
  api(libs.androidx.compose.material3)
  api("androidx.compose.foundation:foundation")
  api("androidx.compose.foundation:foundation-layout")

  // ========== Compose 相关库 ==========
  api(libs.androidx.activity.compose)
  // navigation-compose 未在 toml 定义，建议补充（见下方）或固定版本
  api("androidx.navigation:navigation-compose:2.9.3")
  api(libs.androidx.lifecycle.viewmodel.compose)
  api(libs.androidx.lifecycle.process)
  api(libs.androidx.material3.adaptive.navigation.suite)

  // ========== 生命周期相关依赖 ==========
  api(libs.androidx.lifecycle.runtime.ktx)
  api(libs.androidx.lifecycle.viewmodel.ktx)
  api(libs.androidx.savedstate.ktx)

  // ========== 图片加载和 UI 增强 ==========
  api(libs.landscapist.glide)
  api(libs.landscapist.animation)
  api(libs.landscapist.placeholder)
  api(libs.landscapist.palette)
  // ⚠️ shimmer 未在 toml 定义，如需使用请补充（见下方）
  api(libs.shimmer)
  api(libs.lottie)
  api(libs.coil.kt)
  api(libs.coil.okhttp)
  api(libs.coil.kt.compose)
  api(libs.coil.kt.svg)
  api(libs.coil.kt.gif)

  // ========== 网络和序列化 ==========
  // ⚠️ retrofit 相关库未在 toml 定义，如需使用请补充（见下方）
  api(platform(libs.retrofit.bom))
  api(libs.retrofit)
  api(libs.retrofit.kotlinx.serialization)
  api(platform(libs.okhttp.bom))
  // logging-interceptor 版本由 okhttp-bom 管理
  api("com.squareup.okhttp3:logging-interceptor")
  // ⚠️ sandwich / converter.gson 未在 toml 定义
   api(libs.sandwich)
  api(libs.kotlinx.serialization)
   api(libs.converter.gson)

  // ========== 工具库 ==========
  api(libs.timber)
  // ⚠️ accompanist.permissions 未在 toml 定义
  api(libs.accompanist.permissions)
  api(libs.kotlinx.coroutines.android)
  // ⚠️ kotlinx-immutable-collection 未在 toml 定义
   api(libs.kotlinx.immutable.collection)

  // ========== 数据库 ==========
  api(libs.androidx.room.runtime)
  api(libs.androidx.room.ktx)

  // ========== 依赖注入 ==========
  // ⚠️ koin 未在 toml 定义，如需使用请补充（见下方）
   api(libs.koin.android)
   api(libs.koin.androidx.compose)

  // ========== 其他常用库 ==========
  // ⚠️ kotlin.reflect 未在 toml 定义
   api(libs.kotlin.reflect)


  ///==============maplbire native 地图
  api(libs.maplibre.compose)
  api(project(":ui-maplibre"))
}
