package org.cwcc.open.plugin.geokori.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.cwcc.open.geokori.ui.material3.bottomsheet.FlexibleBottomSheet
import org.cwcc.open.geokori.ui.material3.bottomsheet.core.FlexibleSheetSize
import org.cwcc.open.geokori.ui.material3.bottomsheet.core.FlexibleSheetState
import org.cwcc.open.geokori.ui.material3.bottomsheet.core.FlexibleSheetValue
import org.cwcc.open.geokori.ui.material3.bottomsheet.core.rememberFlexibleBottomSheetState


@Composable
fun rememberGeoKoriModalSheetState(
    initialValue: FlexibleSheetValue = FlexibleSheetValue.SlightlyExpanded,
) = rememberFlexibleBottomSheetState(
    flexibleSheetSize = FlexibleSheetSize(
        fullyExpanded = 0.85f,
        intermediatelyExpanded = 0.4f,
        slightlyExpanded = 0.12f,
    ),
    isModal = false,  //非模态
    skipHiddenState = false,
    allowNestedScroll = true,
    initialValue = initialValue,
    skipSlightlyExpanded = false,
)
/**
 * GeoKoriCenter底部 Sheet + 详情弹窗一体化组件。
 *
 * @param modifier 外部修饰器
 * @param sheetState BottomSheet 状态，可由外部传入以精细控制；默认提供非模态三档配置
 * @param quickActions 快捷操作按钮列表（内部自动处理折叠态/展开态展示数量）
 * @param poiList POI 数据列表
 * @param onPoiSelected POI 被点击选中时回调（此时内部已自动收起 Sheet）
 * @param onPoiNavigate 详情卡片内点击"路线"时回调
 * @param onQuickActionClick 快捷操作被点击时回调
 * @param onPoiDetailClose 详情卡片关闭时回调（含点击外部、返回键、关闭按钮）
 */
@Composable
fun GeoKoriCenterSheet(
    modifier: Modifier = Modifier,
    sheetState: FlexibleSheetState = rememberGeoKoriModalSheetState(),
    quickActions: List<QuickAction> = defaultQuickActions(),
    poiList: List<PoiItem> = defaultPoiList(),
    // 新增：目的地相关参数，替代内部硬编码的 sceneState
    destination: Any? = null,  // 你的 Destination 类型，这里用 Any? 示例
    isDestinationSheetVisible: Boolean = false,
    destinationContent: @Composable () -> Unit = {},
    onPoiSelected: (PoiItem) -> Unit = {},
    onPoiNavigate: (PoiItem) -> Unit = {},
    onQuickActionClick: (QuickAction) -> Unit = {},
    onPoiDetailClose: () -> Unit = {},
    onSheetDismiss: () -> Unit = {},  // 点击遮罩/返回时
) {
  // ========== 内部自治状态 ==========
  var selectedPoi by remember { mutableStateOf<PoiItem?>(null) }
  //前一页面状态
  var previousSheetValue by remember { mutableStateOf<FlexibleSheetValue?>(null) }

  // ========== Sheet 与弹窗状态联动 ==========
  LaunchedEffect(selectedPoi) {
    if (selectedPoi != null) {
      if (previousSheetValue == null) {
        previousSheetValue = sheetState.currentValue
      }
      if (sheetState.currentValue != FlexibleSheetValue.SlightlyExpanded) {
        sheetState.slightlyExpand()
      }
    } else {
      previousSheetValue?.let { prev ->
        if (sheetState.currentValue != prev) {
          sheetState.animateTo(prev)
        }
        previousSheetValue = null
      }
    }
  }

  // ========== BottomSheet 主体 ==========
  FlexibleBottomSheet(
      sheetState = sheetState,
      containerColor = Color.White,
      onTargetChanges = { /* ... */ },
      onDismissRequest = onSheetDismiss,  // 使用传入的回调
      dragHandle = null,
      windowInsets = WindowInsets.systemBars,
      modifier = modifier.fillMaxSize()
  ) {
    if (isDestinationSheetVisible && destination != null) {
      // 调用方提供的目的地内容
      destinationContent()
    } else {
      BottomSheetContent(
          sheetState = sheetState,
          quickActions = quickActions,
          poiList = poiList,
          onPoiClick = { poi ->
            selectedPoi = poi
            onPoiSelected(poi)
          },
          onQuickActionClick = onQuickActionClick,
      )
    }
  }

  // ========== 非模态 POI 详情弹窗（屏幕正中央） ==========
  if (selectedPoi != null) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = (screenWidth * 0.85f).coerceAtMost(360.dp)

    Popup(
        alignment = Alignment.Center,
        properties = PopupProperties(
            focusable = false,              // 非模态：背后地图/BottomSheet 仍可交互
            dismissOnClickOutside = true,   // 点击卡片外部自动关闭
            dismissOnBackPress = true,
        ),
        onDismissRequest = {
          selectedPoi = null
          onPoiDetailClose()
        }
    ) {
      // 进入/退出动画
      var visible by remember { mutableStateOf(false) }
      LaunchedEffect(Unit) { visible = true }

      val density = androidx.compose.ui.platform.LocalDensity.current
      val offsetY by androidx.compose.animation.core.animateIntOffsetAsState(
          targetValue = if (visible) {
            androidx.compose.ui.unit.IntOffset(0, 0)
          } else {
            androidx.compose.ui.unit.IntOffset(0, with(density) { 80.dp.toPx().toInt() })
          },
          animationSpec = androidx.compose.animation.core.spring(
              dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
          ),
          label = "poi_card_slide"
      )

      Box(
          modifier = Modifier
              .width(cardWidth)
              .offset { offsetY }
      ) {
        PoiDetailCardV2(
            poi = selectedPoi!!,
            onClose = {
              selectedPoi = null
              onPoiDetailClose()
            },
            onNavigate = {
              selectedPoi?.let { onPoiNavigate(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

// ==================== 内部实现 ====================

@Composable
fun BottomSheetContent(
    sheetState: FlexibleSheetState,
    quickActions: List<QuickAction>,
    poiList: List<PoiItem>,
    onPoiClick: (PoiItem) -> Unit,
    onQuickActionClick: (QuickAction) -> Unit,
) {
  val isExpanded by remember {
    derivedStateOf { sheetState.currentValue != FlexibleSheetValue.SlightlyExpanded }
  }

  Column(
      modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight()
  ) {
    SearchHeaderV2()

    // 拖拽指示条
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
      Box(
          modifier = Modifier
              .width(40.dp)
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(Color(0xFFDADCE0))
      )
    }

    // 快捷操作（折叠/展开自动切换）
    AnimatedContent(
        targetState = isExpanded,
        label = "quick_actions"
    ) { expanded ->
      if (expanded) {
        ExpandedQuickActions(
            actions = quickActions,
            onActionClick = onQuickActionClick
        )
      } else {
        CollapsedQuickActions(
            actions = quickActions,
            onActionClick = onQuickActionClick
        )
      }
    }

    // 分隔线（随 Sheet 展开进度渐变）
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(1.dp)
            .alpha(0.3f + sheetState.visibilityProgress * 0.7f)
            .background(Color(0xFFDADCE0))
    )

    // 列表头部
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
          text = "附近推荐",
          fontSize = if (isExpanded) 18.sp else 16.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF202124)
      )
      TextButton(onClick = { }) {
        Text("查看更多", fontSize = 13.sp)
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // POI 列表
    LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
    ) {
      items(poiList, key = { it.id }) { poi ->
        PoiListItemV2(
            poi = poi,
            onClick = { onPoiClick(poi) }
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
// ==================== 默认数据（可替换） ====================
//QuickAction(Icons.Default.Home, "动物", Color(0xFFE3F2FD), Color(0xFF1565C0)),
fun defaultQuickActions(): List<QuickAction> = listOf(
    QuickAction(null, "动物", Color(0xFFE3F2FD), Color(0xFF1565C0)),
    QuickAction(null, "植物", Color(0xFFF3E5F5), Color(0xFF6A1B9A)),
    QuickAction(null, "鸟类", Color(0xFFFFF3E0), Color(0xFFEF6C00)),
    QuickAction(null, "致害", Color(0xFFFFFDE7), Color(0xFFF9A825)),
    QuickAction(null, "收容", Color(0xFFE8F5E9), Color(0xFF2E7D32)),
    QuickAction(null, "谱系", Color(0xFFFFEBEE), Color(0xFFC62828)),
    QuickAction(Icons.Default.LocalPolice, "执法", Color(0xFFFFF3E0), Color(0xFFEF6C00)),
    QuickAction(Icons.Default.Flag, "履约", Color(0xFFE0F2F1), Color(0xFF00695C)),
    QuickAction(Icons.Default.BugReport, "名录-动物", Color(0xFFE0F2F1), Color(0xFF00695C)),
    QuickAction(Icons.Filled.AcUnit, "名录-植物", Color(0xFFFFFDE7), Color(0xFF2E7D32)),
    QuickAction(Icons.Filled.ArtTrack, "名录-三有", Color(0xFFE0F2F1), Color(0xFF00695C)),
    QuickAction(null, "CITES附录", Color(0xFFE0F2F1), Color(0xFF00695C)),
)

fun defaultPoiList(): List<PoiItem> = listOf(
    PoiItem("1", "星巴克咖啡", "餐饮", 4.8f, "120m", "建国路88号SOHO现代城", listOf("咖啡", "WiFi", "安静")),
    PoiItem("2", "万达广场", "购物", 4.6f, "350m", "长安街1号", listOf("商场", "IMAX", "餐饮")),
    PoiItem("3", "建国门地铁站", "交通", 4.9f, "80m", "建国门站B口", listOf("1号线", "2号线", "换乘")),
    PoiItem("4", "海底捞火锅", "餐饮", 4.7f, "500m", "朝阳路66号", listOf("火锅", "24小时", "排队")),
    PoiItem("5", "北京协和医院", "医疗", 4.9f, "1.2km", "东单北大街53号", listOf("三甲", "急诊", "专家")),
    PoiItem("6", "全聚德烤鸭店", "餐饮", 4.5f, "800m", "前门大街30号", listOf("烤鸭", "老字号", "宴请")),
)
