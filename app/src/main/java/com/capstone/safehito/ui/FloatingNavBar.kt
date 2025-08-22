package com.capstone.safehito.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit

// New data class for nav items
data class NavBarItem(
    val label: String,
    val route: String,
    val iconResId: Int
)

@Composable
fun FloatingNavBar(
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    items: List<NavBarItem>,
    modifier: Modifier = Modifier,
    darkTheme: Boolean
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val responsiveBoxHeight = when {
        screenWidthDp < 340 -> 64.dp
        screenWidthDp < 380 -> 70.dp
        screenWidthDp < 420 -> 78.dp
        else -> 86.dp
    }
    // Responsive icon and label sizes
    val baseIconAndLabelSize: Pair<Dp, TextUnit> = when {
        screenWidthDp < 340 -> Pair(18.dp, 10.sp)
        screenWidthDp < 380 -> Pair(22.dp, 11.sp)
        screenWidthDp < 420 -> Pair(26.dp, 12.sp)
        else -> Pair(30.dp, 14.sp)
    }
    val baseIconSize = baseIconAndLabelSize.first
    val baseLabelFontSize = baseIconAndLabelSize.second
    var size by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val gradient = if (darkTheme) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF3B3B3B),
                Color(0xFF262626)
            ),
            start = Offset(0f, 0f),
            end = Offset(
                x = size.width * 0.866f,
                y = size.height * 0.5f
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFE0E0E0),
                Color(0xFFD9D9D9)
            ),
            start = Offset(0f, 0f),
            end = Offset(
                x = size.width * 0.866f,
                y = size.height * 0.5f
            )
        )
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(responsiveBoxHeight)
            .padding(horizontal = 25.dp)
            .shadow(16.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .onSizeChanged { size = androidx.compose.ui.geometry.Size(it.width.toFloat(), it.height.toFloat()) }
            .background(gradient)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.route == selectedRoute
                AnimatedColumn(
                    isSelected = isSelected,
                    darkTheme = darkTheme,
                    item = item,
                    onItemSelected = onItemSelected,
                    baseIconSize = baseIconSize,
                    baseLabelFontSize = baseLabelFontSize
                )
            }
        }
    }
}

@Composable
fun AnimatedColumn(
    isSelected: Boolean,
    darkTheme: Boolean,
    item: NavBarItem,
    onItemSelected: (String) -> Unit,
    baseIconSize: Dp,
    baseLabelFontSize: TextUnit
) {
    // Animate shadow elevation
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 0.dp,
        animationSpec = if (isSelected) tween(durationMillis = 400) else tween(durationMillis = 50)
    )
    // Animate shape radius (optional, for a "pop" effect)
    val shapeRadius by animateDpAsState(
        targetValue = if (isSelected) 240.dp else 24.dp,
        animationSpec = tween(durationMillis = 400)
    )
    // Animate horizontal padding
    val horizontalPadding by animateDpAsState(
        targetValue = when {
            isSelected && item.route == "scan" -> 22.dp
            isSelected && item.route == "dashboard" -> 16.dp
            isSelected -> 16.dp
            else -> 12.dp
        },
        animationSpec = tween(durationMillis = 400)
    )
    // Animate background color stops (for gradient)
    val startColor = when {
        isSelected && darkTheme -> Color(0xFF464646)
        isSelected && !darkTheme -> Color(0xFFE0E0E0)
        else -> Color.Transparent
    }
    val endColor = when {
        isSelected && darkTheme -> Color(0xFF343434)
        isSelected && !darkTheme -> Color(0xFFF5F5F5)
        else -> Color.Transparent
    }
    // Animate icon size
    val iconSize by animateDpAsState(
        targetValue = if (isSelected) baseIconSize + 4.dp else baseIconSize,
        animationSpec = tween(durationMillis = 400)
    )
    // Animate label alpha and scale
    val labelAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.7f,
        animationSpec = tween(durationMillis = 400)
    )
    val labelScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(durationMillis = 400)
    )
    // Use static colors for label and icon
    val labelColor = if (isSelected) Color(0xFF007EF2) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val labelWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    val gradientBrush = if (darkTheme) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF36CBFF), Color(0xFF2BBAFF)),
            start = Offset(0f, 0f),
            end = Offset(60f, 60f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF1976D2), Color(0xFF1565C0)),
            start = Offset(0f, 0f),
            end = Offset(60f, 60f)
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .shadow(elevation, RoundedCornerShape(shapeRadius))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(startColor, endColor)
                )
            )
            .clickable { onItemSelected(item.route) }
            .padding(
                horizontal = horizontalPadding,
                vertical = 8.dp
            )
    ) {
        // Icon with gradient if selected, else tinted
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithCache {
                        onDrawWithContent {
                            drawContent()
                            drawRect(gradientBrush, blendMode = androidx.compose.ui.graphics.BlendMode.SrcAtop)
                        }
                    }
            ) {
                Image(
                    painter = painterResource(id = item.iconResId),
                    contentDescription = item.label,
                    modifier = Modifier.fillMaxSize(),
                    colorFilter = null
                )
            }
        } else {
            Image(
                painter = painterResource(id = item.iconResId),
                contentDescription = item.label,
                modifier = Modifier.size(iconSize),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        // Label with animated alpha and scale, static color
        Text(
            text = item.label,
            color = labelColor,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = baseLabelFontSize),
            fontWeight = labelWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.graphicsLayer(
                alpha = labelAlpha,
                scaleX = labelScale,
                scaleY = labelScale
            )
        )
    }
}
