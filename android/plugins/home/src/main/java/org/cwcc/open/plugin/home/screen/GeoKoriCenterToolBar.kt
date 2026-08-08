package org.cwcc.open.plugin.home.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 底部工具栏数据项
 *
 * @param icon 图标
 * @param label 标签文字
 * @param isFloating 是否为凸起按钮（居中突出显示）
 */
data class BottomToolbarItem(
    val icon: ImageVector,
    val label: String,
    val isFloating: Boolean = false
)

/**
 * 默认底部工具栏按钮列表
 */
fun defaultToolbarItems(): List<BottomToolbarItem> = listOf(
    BottomToolbarItem(Icons.Default.Home, "首页"),
    BottomToolbarItem(Icons.Default.Map, "地图"),
    BottomToolbarItem(Icons.Default.Add, "发布", isFloating = true),
    BottomToolbarItem(Icons.Default.ChatBubble, "聊天"),
    BottomToolbarItem(Icons.Default.Person, "我的")
)

/**
 * GeoKori 底部工具栏
 *
 * 支持自适应宽屏显示，凸起按钮效果
 *
 * @param modifier 外部修饰器
 * @param items 工具栏按钮列表，默认使用 defaultToolbarItems()
 * @param selectedIndex 当前选中的按钮索引
 * @param onItemSelected 按钮点击回调
 * @param isExpanded 是否展开状态（控制按钮文字显示）
 * @param toolbarWidth 工具栏宽度，null 则根据屏幕自适应
 * @param toolbarHorizontalAlignment 水平对齐方式，null 则根据屏幕自适应
 */
@Composable
fun GeoKoriCenterToolBar(
    modifier: Modifier = Modifier,
    items: List<BottomToolbarItem> = defaultToolbarItems(),
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit = {},
    isExpanded: Boolean = true,
    toolbarWidth: Dp? = null,
    toolbarHorizontalAlignment: Alignment.Horizontal? = null,
) {
  // ========== 获取屏幕/容器宽度 ==========
  val windowInfo = LocalWindowInfo.current
  val containerWidthPx = windowInfo.containerSize.width
  val density = LocalDensity.current
  val containerWidthDp = with(density) { containerWidthPx.toDp() }

  // 判断是否为宽屏（折叠屏展开/平板）
  val isWideScreen by remember(containerWidthDp) {
    derivedStateOf { containerWidthDp >= 600.dp }
  }

  // 计算工具栏宽度
  val adaptiveWidth = toolbarWidth ?: when {
    isWideScreen -> containerWidthDp / 2
    else -> null
  }

  // 计算水平对齐方式
  val adaptiveAlignment = toolbarHorizontalAlignment ?: when {
    isWideScreen -> Alignment.Start
    else -> Alignment.CenterHorizontally
  }

  val toolbarHeight by animateDpAsState(
      targetValue = if (isExpanded) 72.dp else 56.dp,
      animationSpec = tween(durationMillis = 300),
      label = "toolbar_height"
  )

  val shadowElevation by animateDpAsState(
      targetValue = if (isExpanded) 8.dp else 4.dp,
      animationSpec = tween(durationMillis = 300),
      label = "toolbar_shadow"
  )

  // 计算工具栏实际宽度
  val actualWidthModifier = if (adaptiveWidth != null) {
    Modifier.width(adaptiveWidth)
  } else {
    Modifier.fillMaxWidth()
  }

  // 检查是否有凸起按钮（只能有一个）
  val floatingItemCount = items.count { it.isFloating }
  require(floatingItemCount <= 1) { "只能有一个凸起按钮 (isFloating = true)" }

  // 使用 Box 包裹，让凸起按钮可以超出边界
  Box(
      modifier = modifier
          .then(actualWidthModifier)
          .height(toolbarHeight + 20.dp)
          .padding(top = 20.dp),
      contentAlignment = when (adaptiveAlignment) {
        Alignment.Start -> Alignment.BottomStart
        Alignment.End -> Alignment.BottomEnd
        else -> Alignment.BottomCenter
      }
  ) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(toolbarHeight)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .shadow(elevation = shadowElevation, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = Color.White,
        shadowElevation = shadowElevation
    ) {
      Row(
          modifier = Modifier.fillMaxSize(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
      ) {
        items.forEachIndexed { index, item ->
          if (item.isFloating) {
            // 凸起按钮
            val floatingOffset by animateDpAsState(
                targetValue = if (isExpanded) (-28).dp else (-16).dp,
                animationSpec = tween(durationMillis = 300),
                label = "floating_offset"
            )
            val floatingSize by animateDpAsState(
                targetValue = if (isExpanded) 56.dp else 44.dp,
                animationSpec = tween(durationMillis = 300),
                label = "floating_size"
            )
            val floatingColor by animateColorAsState(
                targetValue = if (selectedIndex == index) Color(0xFF1A73E8) else Color(0xFF4285F4),
                animationSpec = tween(durationMillis = 300),
                label = "floating_color"
            )

            Box(
                modifier = Modifier
                    .size(floatingSize)
                    .offset(y = floatingOffset)
                    .clip(CircleShape)
                    .background(floatingColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White),
                        onClick = { onItemSelected(index) }
                    ),
                contentAlignment = Alignment.Center
            ) {
              Icon(
                  imageVector = item.icon,
                  contentDescription = item.label,
                  tint = Color.White,
                  modifier = Modifier.size(floatingSize / 2)
              )
            }
          } else {
            // 普通按钮
            BottomToolbarButton(
                item = item,
                isSelected = index == selectedIndex,
                isExpanded = isExpanded,
                onClick = { onItemSelected(index) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BottomToolbarButton(
    item: BottomToolbarItem,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }

  val textAlpha by animateFloatAsState(
      targetValue = if (isExpanded) 1f else 0f,
      animationSpec = tween(durationMillis = 200),
      label = "text_alpha"
  )

  val iconSize by animateDpAsState(
      targetValue = if (isExpanded) 28.dp else 24.dp,
      animationSpec = tween(durationMillis = 200),
      label = "icon_size"
  )

  val iconColor by animateColorAsState(
      targetValue = if (isSelected) Color(0xFF1A73E8) else Color(0xFF5F6368),
      animationSpec = tween(durationMillis = 200),
      label = "icon_color"
  )

  val textColor by animateColorAsState(
      targetValue = if (isSelected) Color(0xFF1A73E8) else Color(0xFF5F6368),
      animationSpec = tween(durationMillis = 200),
      label = "text_color"
  )

  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
          .clickable(
              interactionSource = interactionSource,
              indication = ripple(bounded = false),
              onClick = onClick
          )
          .padding(
              horizontal = if (isExpanded) 16.dp else 12.dp,
              vertical = if (isExpanded) 8.dp else 4.dp
          )
  ) {
    Icon(
        imageVector = item.icon,
        contentDescription = item.label,
        tint = iconColor,
        modifier = Modifier.size(iconSize)
    )
    if (isExpanded) {
      Spacer(modifier = Modifier.height(2.dp))
      Text(
          text = item.label,
          fontSize = 11.sp,
          color = textColor,
          fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
          modifier = Modifier.alpha(textAlpha)
      )
    }
  }
}
