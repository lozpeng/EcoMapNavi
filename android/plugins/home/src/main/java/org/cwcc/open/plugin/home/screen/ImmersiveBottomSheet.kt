package org.cwcc.open.plugin.home.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

// ==================== 状态定义 ====================
enum class SheetAnchor {
  COLLAPSED,   // 完全折叠（只显示搜索条）
  PEEK,        // 窥视态（快捷功能 + 列表顶部）
  HALF,        // 半展开（列表占屏幕40%）
  EXPANDED     // 全展开（列表占屏幕85%）
}

data class SheetConfig(
    val collapsedHeight: Float = 80f,      // dp
    val peekHeight: Float = 220f,          // dp
    val halfHeightRatio: Float = 0.45f,    // 屏幕比例
    val expandedHeightRatio: Float = 0.88f // 屏幕比例
)

// ==================== 沉浸式底部面板 ====================
@Composable
fun ImmersiveBottomSheet(
    modifier: Modifier = Modifier,
    config: SheetConfig = SheetConfig(),
    sheetState: ImmersiveSheetState = rememberImmersiveSheetState(),
    onSheetProgress: (Float) -> Unit = {},
    onAnchorChanged: (SheetAnchor) -> Unit = {},
    content: @Composable () -> Unit
) {
  val density = LocalDensity.current
  val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

  val collapsedPx = with(density) { config.collapsedHeight.dp.toPx() }
  val peekPx = with(density) { config.peekHeight.dp.toPx() }
  val halfPx = screenHeightPx * config.halfHeightRatio
  val expandedPx = screenHeightPx * config.expandedHeightRatio

  val anchors = remember {
    mapOf(
        SheetAnchor.COLLAPSED to collapsedPx,
        SheetAnchor.PEEK to peekPx,
        SheetAnchor.HALF to halfPx,
        SheetAnchor.EXPANDED to expandedPx
    )
  }

  val scope = rememberCoroutineScope()
  val offsetY = sheetState.offset

  // progress: 0=折叠, 1=展开
  val progress by remember {
    derivedStateOf {
      val current = offsetY.value
      val range = expandedPx - collapsedPx
      if (range <= 0) 0f else ((current - collapsedPx) / range).coerceIn(0f, 1f)
    }
  }

  // 面板高度 = 可见高度（从底部向上展开的高度）
  val sheetHeightDp by remember {
    derivedStateOf { with(density) { offsetY.value.toDp() } }
  }

  LaunchedEffect(progress) {
    onSheetProgress(progress)
  }

  fun settleToNearestAnchor() {
    val current = offsetY.value
    val target = anchors.minByOrNull { (_, value) -> (value - current).absoluteValue }?.key ?: SheetAnchor.PEEK
    sheetState.currentAnchor = target
    onAnchorChanged(target)
    scope.launch {
      val targetPx = anchors[target] ?: peekPx
      offsetY.animateTo(targetPx, animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f))
    }
  }

  val dragModifier = Modifier.pointerInput(Unit) {
    val velocityTracker = VelocityTracker()
    detectVerticalDragGestures(
        onDragStart = {
          velocityTracker.resetTracking()
          scope.launch { offsetY.stop() }
        },
        onDragEnd = {
          val velocity = velocityTracker.calculateVelocity().y
          if (velocity > 1500) {
            val current = sheetState.currentAnchor
            val next = when (current) {
              SheetAnchor.EXPANDED -> SheetAnchor.HALF
              SheetAnchor.HALF -> SheetAnchor.PEEK
              SheetAnchor.PEEK -> SheetAnchor.COLLAPSED
              SheetAnchor.COLLAPSED -> SheetAnchor.COLLAPSED
            }
            sheetState.currentAnchor = next
            onAnchorChanged(next)
            scope.launch {
              offsetY.animateTo(anchors[next] ?: peekPx, spring(0.8f, 500f))
            }
          } else if (velocity < -1500) {
            val current = sheetState.currentAnchor
            val next = when (current) {
              SheetAnchor.COLLAPSED -> SheetAnchor.PEEK
              SheetAnchor.PEEK -> SheetAnchor.HALF
              SheetAnchor.HALF -> SheetAnchor.EXPANDED
              SheetAnchor.EXPANDED -> SheetAnchor.EXPANDED
            }
            sheetState.currentAnchor = next
            onAnchorChanged(next)
            scope.launch {
              offsetY.animateTo(anchors[next] ?: halfPx, spring(0.8f, 500f))
            }
          } else {
            settleToNearestAnchor()
          }
        },
        onVerticalDrag = { change, dragAmount ->
          change.consume()
          velocityTracker.addPointerInputChange(change)
          val newValue = (offsetY.value - dragAmount).coerceIn(collapsedPx, expandedPx)
          scope.launch { offsetY.snapTo(newValue) }
        }
    )
  }

  val scrimAlpha by remember { derivedStateOf { progress * 0.35f } }

  Box(modifier = modifier.fillMaxSize()) {
    // 遮罩层：只在展开时渲染，避免拦截地图事件
    if (scrimAlpha > 0.01f) {
      Box(
          modifier = Modifier
              .fillMaxSize()
              .alpha(scrimAlpha)
              .background(Color.Black)
              .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null
              ) {
                sheetState.currentAnchor = SheetAnchor.PEEK
                onAnchorChanged(SheetAnchor.PEEK)
                scope.launch {
                  offsetY.animateTo(peekPx, spring(0.85f, 400f))
                }
              }
      )
    }

    // ✅ 关键修复：使用 align(Alignment.BottomCenter) 替代 graphicsLayer
    // 面板真正布局在屏幕底部，高度变化实现展开/收起
    // 布局位置与视觉位置完全一致，点击事件正常传递
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(sheetHeightDp)
            .align(Alignment.BottomCenter)      // ← 真正贴在父 Box 底部
            .then(dragModifier)
            .shadow(
                elevation = (8 + progress * 8).dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color(0xFFF8F9FA))
    ) {
      content()
    }
  }
}

// ==================== Sheet 状态管理 ====================
@Composable
fun rememberImmersiveSheetState(
    initialAnchor: SheetAnchor = SheetAnchor.PEEK
): ImmersiveSheetState {
  val density = LocalDensity.current
  val peekPx = with(density) { 220.dp.toPx() }
  return remember {
    ImmersiveSheetState(
        initialOffset = peekPx,
        initialAnchor = initialAnchor
    )
  }
}

class ImmersiveSheetState(
    initialOffset: Float,
    initialAnchor: SheetAnchor
) {
  val offset = Animatable(initialOffset)
  var currentAnchor by mutableStateOf(initialAnchor)

  suspend fun animateTo(anchor: SheetAnchor, targetPx: Float) {
    currentAnchor = anchor
    offset.animateTo(targetPx, spring(dampingRatio = 0.85f, stiffness = 400f))
  }

  suspend fun snapTo(px: Float) {
    offset.snapTo(px)
  }
}

// ==================== 搜索栏 V2 ====================
@Composable
fun SearchHeaderV2() {
  Column(modifier = Modifier.fillMaxWidth()) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
      Row(
          modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF4285F4)),
            contentAlignment = Alignment.Center
        ) {
          Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "个人中心",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "搜索地点、公交、地铁",
            color = Color(0xFF9AA0A6),
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { }) {
          Icon(
              imageVector = Icons.Default.Mic,
              contentDescription = "语音搜索",
              tint = Color(0xFF4285F4),
              modifier = Modifier.size(24.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      WeatherChip(icon = Icons.Default.WbSunny, text = "26°C 晴", bg = Color(0xFFFFF3E0))
      Spacer(modifier = Modifier.width(8.dp))
      WeatherChip(icon = Icons.Default.Air, text = "AQI 45 优", bg = Color(0xFFE8F5E9))
      Spacer(modifier = Modifier.width(8.dp))
      WeatherChip(icon = Icons.Default.Traffic, text = "路况畅通", bg = Color(0xFFE3F2FD))
    }
  }
}
@Composable
fun WeatherChip(icon: ImageVector, text: String, bg: Color) {
  Surface(
      shape = RoundedCornerShape(16.dp),
      color = bg.copy(alpha = 0.9f),
      modifier = Modifier.height(28.dp)
  ) {
    Row(
        modifier = Modifier.padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(icon, null, modifier = Modifier.size(14.dp), tint = Color.DarkGray)
      Spacer(modifier = Modifier.width(4.dp))
      Text(text, fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
    }
  }
}

// ==================== 底部面板内容 V2（联动版）====================
@Composable
fun BottomSheetContentV2(
    progress: Float,
    sheetState: ImmersiveSheetState,
    onPoiClick: (PoiItem) -> Unit
) {
  val scope = rememberCoroutineScope()

  // ✅ Column 填满整个面板高度，LazyColumn 用 weight 自适应
  Column(
      modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight()
  ) {
    SearchHeaderV2()   // ← 加在这里
    // 拖拽指示条
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        //.pointerInput(Unit) { /* 把 ImmersiveBottomSheet 里的 detectVerticalDragGestures 移到这里 */ },
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

    // 快捷操作（固定高度区域）
    AnimatedContent(
        targetState = progress > 0.3f,
        label = "quick_actions"
    ) { isExpanded ->
      if (isExpanded) {
        ExpandedQuickActions()
      } else {
        CollapsedQuickActions()
      }
    }

    // 分割线
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(1.dp)
            .alpha(0.3f + progress * 0.7f)
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
          fontSize = if (progress > 0.5f) 18.sp else 16.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF202124)
      )
      TextButton(onClick = { }) {
        Text("查看更多", fontSize = 13.sp)
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // ✅ 推荐列表：用 weight 占据剩余空间，不再用 heightIn
    val samplePois = remember {
      listOf(
          PoiItem("1", "星巴克咖啡", "餐饮", 4.8f, "120m", "建国路88号SOHO现代城", listOf("咖啡", "WiFi", "安静")),
          PoiItem("2", "万达广场", "购物", 4.6f, "350m", "长安街1号", listOf("商场", "IMAX", "餐饮")),
          PoiItem("3", "建国门地铁站", "交通", 4.9f, "80m", "建国门站B口", listOf("1号线", "2号线", "换乘")),
          PoiItem("4", "海底捞火锅", "餐饮", 4.7f, "500m", "朝阳路66号", listOf("火锅", "24小时", "排队")),
          PoiItem("5", "北京协和医院", "医疗", 4.9f, "1.2km", "东单北大街53号", listOf("三甲", "急诊", "专家")),
          PoiItem("6", "全聚德烤鸭店", "餐饮", 4.5f, "800m", "前门大街30号", listOf("烤鸭", "老字号", "宴请")),
      )
    }

    LazyColumn(
        modifier = Modifier.weight(1f),   // ← 关键：自适应剩余高度
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
    ) {
      items(samplePois) { poi ->
        PoiListItemV2(poi = poi, onClick = { onPoiClick(poi) })
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

// 折叠态快捷操作（单行紧凑布局）
@Composable
fun CollapsedQuickActions() {
  val actions = remember {
    listOf(
        QuickAction(Icons.Default.Home, "回家", Color(0xFFE3F2FD), Color(0xFF1565C0)),
        QuickAction(Icons.Default.Business, "去公司", Color(0xFFF3E5F5), Color(0xFF6A1B9A)),
        QuickAction(Icons.Default.Place, "附近", Color(0xFFFFF3E0), Color(0xFFEF6C00)),
        QuickAction(Icons.Default.Star, "收藏", Color(0xFFFFFDE7), Color(0xFFF9A825)),
    )
  }
  Row(
      modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceEvenly
  ) {
    actions.forEach { QuickActionItem(it) }
  }
}

// 展开态快捷操作（双行网格 + 更多功能）
@Composable
fun ExpandedQuickActions() {
  val actions = remember {
    listOf(
        QuickAction(Icons.Default.Home, "回家", Color(0xFFE3F2FD), Color(0xFF1565C0)),
        QuickAction(Icons.Default.Business, "去公司", Color(0xFFF3E5F5), Color(0xFF6A1B9A)),
        QuickAction(Icons.Default.Place, "附近", Color(0xFFFFF3E0), Color(0xFFEF6C00)),
        QuickAction(Icons.Default.Star, "收藏", Color(0xFFFFFDE7), Color(0xFFF9A825)),
        QuickAction(Icons.Default.LocalParking, "停车", Color(0xFFE8F5E9), Color(0xFF2E7D32)),
        QuickAction(Icons.Default.LocalGasStation, "加油", Color(0xFFFFEBEE), Color(0xFFC62828)),
        QuickAction(Icons.Default.Restaurant, "美食", Color(0xFFFFF3E0), Color(0xFFEF6C00)),
        QuickAction(Icons.Default.Hotel, "酒店", Color(0xFFE0F2F1), Color(0xFF00695C)),
    )
  }
  Column {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      actions.take(4).forEach { QuickActionItem(it) }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      actions.drop(4).forEach { QuickActionItem(it) }
    }
  }
}

@Composable
fun QuickActionItem(action: QuickAction) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.clickable { }
  ) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(action.containerColor),
        contentAlignment = Alignment.Center
    ) {
      Icon(action.icon, null, tint = action.contentColor, modifier = Modifier.size(24.dp))
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(action.label, fontSize = 12.sp, color = Color(0xFF5F6368))
  }
}

// ==================== POI 列表项 V2 ====================
@Composable
fun PoiListItemV2(poi: PoiItem, onClick: () -> Unit) {
  Card(
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onClick)
  ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
          modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFFE3F2FD)),
          contentAlignment = Alignment.Center
      ) {
        Icon(
            imageVector = when (poi.category) {
              "餐饮" -> Icons.Default.Restaurant
              "购物" -> Icons.Default.ShoppingCart
              "交通" -> Icons.Default.DirectionsTransit
              "医疗" -> Icons.Default.LocalHospital
              else -> Icons.Default.Place
            },
            contentDescription = null,
            tint = Color(0xFF4285F4),
            modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = poi.name,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF202124)
          )
          Spacer(modifier = Modifier.width(6.dp))
          if (poi.rating > 0) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFFF9800)
            ) {
              Text(
                  text = "${poi.rating}",
                  fontSize = 11.sp,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                  fontWeight = FontWeight.Bold
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = poi.address,
            fontSize = 13.sp,
            color = Color(0xFF5F6368),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row {
          poi.tags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFF1F3F4),
                modifier = Modifier.padding(end = 4.dp)
            ) {
              Text(
                  text = tag,
                  fontSize = 11.sp,
                  color = Color(0xFF5F6368),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
            text = poi.distance,
            fontSize = 13.sp,
            color = Color(0xFF5F6368)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Icon(
            imageVector = Icons.Default.Directions,
            contentDescription = "导航",
            tint = Color(0xFF4285F4),
            modifier = Modifier.size(28.dp)
        )
      }
    }
  }
}

// ==================== POI 详情卡片 V2 ====================
@Composable
fun PoiDetailCardV2(
    poi: PoiItem,
    onClose: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
  Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = modifier
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = poi.name,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF202124)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
              text = "${poi.category} · ${poi.rating}分 · ${poi.distance}",
              fontSize = 14.sp,
              color = Color(0xFF5F6368)
          )
        }
        IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Close, null, tint = Color(0xFF5F6368))
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
          text = poi.address,
          fontSize = 14.sp,
          color = Color(0xFF5F6368)
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onNavigate,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Directions, null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("路线", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        OutlinedButton(
            onClick = { },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("电话", fontSize = 15.sp)
        }
      }
    }
  }
}

// ==================== 数据模型（复用）====================
data class QuickAction(
    val icon: ImageVector,
    val label: String,
    val containerColor: Color,
    val contentColor: Color
)

data class PoiItem(
    val id: String,
    val name: String,
    val category: String,
    val rating: Float,
    val distance: String,
    val address: String,
    val tags: List<String>
)
