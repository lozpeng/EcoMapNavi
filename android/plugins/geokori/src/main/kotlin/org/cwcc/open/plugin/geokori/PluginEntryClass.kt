package org.cwcc.open.plugin.geokori

import android.speech.tts.TextToSpeech
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.combo.core.api.IPluginEntryClass
import com.combo.core.model.PluginContext
import com.stadiamaps.ferrostar.core.AndroidTtsStatusListener
import java.util.Locale
import org.cwcc.open.plugin.common.theme.FerrostarTheme
import org.koin.core.module.Module
import timber.log.Timber
import uniffi.ferrostar.createFerrostarLogger

class PluginEntryClass : IPluginEntryClass,AndroidTtsStatusListener {
    override val pluginModule: List<Module>
        get() = emptyList()

    @Composable
    override fun Content() {
      FerrostarTheme {
        // A surface container using the 'background' color from the theme
        Surface { DemoNavigationScene() }
      }
    }

    override fun onLoad(context: PluginContext) {
      initTTs(context)
    }

    override fun onUnload() {
      AppModule.ttsObserver.shutdown()
    }

  fun initTTs(context:PluginContext)
  {
      AppModule.init(context.application)
      AppModule.ttsObserver.start()
      AppModule.ttsObserver.statusObserver = this
      AppModule.ferrostarCore.spokenInstructionObserver = AppModule.ttsObserver

      // Setup the global Ferrostar logger
      createFerrostarLogger()
  }

  // TTS listener methods
  override fun onTtsInitialized(tts: TextToSpeech?, status: Int) {
    // 先检查初始化状态
    if (status != TextToSpeech.SUCCESS) {
      Timber.e("TTS 引擎初始化失败，状态码: $status")
      return
    }
    // 再检查对象是否为空（理论上 status == SUCCESS 时不会为 null，但做防御性判断）
    val engine = tts ?: run {
      Timber.e("TTS 初始化成功但对象为空，这是不应该出现的情况")
      return
    }
    // 设置语言
    val result = engine.setLanguage(Locale.SIMPLIFIED_CHINESE)
    when (result) {
      TextToSpeech.LANG_MISSING_DATA -> {
        Timber.w("TTS 缺少中文语音数据，请引导用户下载")
      }
      TextToSpeech.LANG_NOT_SUPPORTED -> {
        Timber.w("当前 TTS 引擎不支持中文")
      }
      else -> {
        val voiceName = engine.voice?.name ?: "default"
        Timber.i("TTS 中文设置成功，当前语音: $voiceName")
      }
    }
  }

  override fun onTtsSpeakError(utteranceId: String, errorCode: Int) {
    // 建议将参数名从 status 改为 errorCode，避免与初始化 status 混淆
    val errorMsg = when (errorCode) {
      TextToSpeech.ERROR_SYNTHESIS -> "语音合成失败"
      TextToSpeech.ERROR_SERVICE -> "TTS 服务错误"
      TextToSpeech.ERROR_OUTPUT -> "音频输出错误"
      TextToSpeech.ERROR_NETWORK -> "网络错误（在线 TTS）"
      TextToSpeech.ERROR_NETWORK_TIMEOUT -> "网络超时"
      TextToSpeech.ERROR_INVALID_REQUEST -> "无效请求"
      TextToSpeech.ERROR_NOT_INSTALLED_YET -> "语音数据尚未安装完成"
      else -> "未知错误"
    }
    Timber.e("TTS 合成失败 [$utteranceId]: $errorMsg (code: $errorCode)")
  }

  override fun onTtsShutdownAndRelease() {
    // 正常生命周期事件，不应使用 error 级别
    Timber.i("TTS 已关闭并释放，如需使用请重新调用 start()")
  }
}
