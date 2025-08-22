package com.capstone.safehito.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstone.safehito.viewmodel.AdminViewModel
import com.capstone.safehito.viewmodel.WaterDataViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Receipt
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.res.painterResource
import com.capstone.safehito.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh

import androidx.compose.ui.unit.TextUnit
import com.capstone.safehito.ui.components.SensorRangeChart
import com.capstone.safehito.api.WeatherApi
import com.capstone.safehito.model.ForecastResponse
import androidx.compose.ui.unit.Dp
import com.google.firebase.database.*
import androidx.navigation.NavHostController
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import com.google.firebase.auth.FirebaseAuth
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.toProperCase(): String =
    split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }

@Composable
fun AdminFishStatusCard(
    status: String,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val responsiveBoxHeight = when {
        screenWidth < 340 -> 170.dp
        screenWidth < 380 -> 190.dp
        screenWidth < 420 -> 210.dp
        else -> 230.dp
    }
    val scope = rememberCoroutineScope()
    val tapRipples = remember { mutableStateListOf<Animatable<Float, AnimationVector1D>>() }
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    val waveColor1 = if (isDarkTheme) Color(0xFF4BA3C7).copy(alpha = 0.3f) else Color(0xFF4BA3C7).copy(alpha = 0.5f)
    val waveColor2 = if (isDarkTheme) Color(0xFF5DCCFC).copy(alpha = 0.2f) else Color(0xFF5DCCFC).copy(alpha = 0.4f)
    val waveColor3 = if (isDarkTheme) Color(0xFFB3E5FC).copy(alpha = 0.15f) else Color(0xFFB3E5FC).copy(alpha = 0.3f)
    val waveColor4 = if (isDarkTheme) Color(0xFFE1F5FE).copy(alpha = 0.1f) else Color(0xFFE1F5FE).copy(alpha = 0.25f)
    var targets by remember {
        mutableStateOf(
            listOf(
                Triple(24f, 0.45f, 1.5f),
                Triple(20f, 0.42f, 1.3f),
                Triple(16f, 0.38f, 1.1f),
                Triple(12f, 0.34f, 0.9f)
            )
        )
    }
    var foamParticles by remember {
        mutableStateOf(List(30) {
            FoamParticle(
                x = Random.nextFloat() * 2000f,
                y = Random.nextFloat() * 600f,
                radius = Random.nextFloat() * 5f + 3f,
                alpha = Random.nextFloat() * 0.5f + 0.3f,
                dx = Random.nextFloat() * 0.4f - 0.2f,
                dy = Random.nextFloat() * -0.2f - 0.1f
            )
        })
    }
    val anims = targets.mapIndexed { i, t ->
        Triple(
            animateFloatAsState(t.first, tween(2000), label = "amp$i").value,
            animateFloatAsState(t.second, tween(2000), label = "baseY$i").value,
            animateFloatAsState(t.third, tween(2000), label = "freq$i").value
        )
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            targets = List(4) {
                val amp = listOf(24f, 20f, 16f, 12f)[it] + Random.nextFloat() * 6f
                val baseY = listOf(0.45f, 0.42f, 0.38f, 0.34f)[it] + Random.nextFloat() * 0.02f
                val freq = listOf(1.5f, 1.3f, 1.1f, 0.9f)[it] + Random.nextFloat() * 0.5f
                Triple(amp, baseY, freq)
            }
        }
    }
    val density = LocalDensity.current
    val configuration2 = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration2.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration2.screenHeightDp.dp.toPx() }
    val smoothTiltX = 0f
    val smoothTiltY = 0f
    val clampedTiltX = smoothTiltX.coerceIn(-8f, 8f)
    val clampedTiltY = smoothTiltY.coerceIn(-8f, 8f)
    LaunchedEffect(Unit) {
        while (true) {
            delay(40L)
            foamParticles = foamParticles.map {
                var newX = it.x + it.dx + clampedTiltX * 0.3f
                var newY = it.y + it.dy + clampedTiltY * 0.2f
                val newAlpha = (it.alpha - 0.005f).coerceAtLeast(0f)
                if (newAlpha <= 0.05f || newY < 0f || newX < 0f || newX > 2000f) {
                    FoamParticle(
                        x = Random.nextFloat() * screenWidthPx,
                        y = Random.nextFloat() * screenHeightPx * 0.4f,
                        radius = Random.nextFloat() * 4f + 2f,
                        alpha = 0.6f,
                        dx = Random.nextFloat() * 0.4f - 0.2f,
                        dy = Random.nextFloat() * -0.3f - 0.05f
                    )
                } else {
                    it.copy(x = newX, y = newY, alpha = newAlpha)
                }
            }
        }
    }
    Box(
        modifier = modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
            .height(responsiveBoxHeight)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    tapOffset = offset
                    repeat(3) { i ->
                        val ripple = Animatable(0f)
                        tapRipples.add(ripple)
                        scope.launch {
                            delay(i * 120L)
                            ripple.animateTo(300f + i * 40f, tween(1200, easing = FastOutSlowInEasing))
                            tapRipples.remove(ripple)
                        }
                    }
                }
            }
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "wavePhase")
        val wavePhase1 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
            label = "wavePhase1"
        )
        val wavePhase2 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
            label = "wavePhase2"
        )
        val wavePhase3 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)),
            label = "wavePhase3"
        )
        val wavePhase4 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
            label = "wavePhase4"
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = clampedTiltX * 1.8f
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    clip = false
                }
        ) {
            val extendedWidth = size.width * 1.5f
            val extendedHeight = size.height + 600f
            val waveVerticalShift = clampedTiltY * 10f
            tapRipples.forEachIndexed { i: Int, ripple: Animatable<Float, AnimationVector1D> ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f - i * 0.05f),
                    radius = ripple.value,
                    center = tapOffset,
                    style = Stroke(width = 2f + i)
                )
            }
            fun drawWave(
                phase: Float,
                amplitude: Float,
                baseYRatio: Float,
                frequency: Float,
                color: Color
            ) {
                val path = Path()
                val step = 2f
                val baseY = size.height * baseYRatio + waveVerticalShift
                path.moveTo(-extendedWidth / 4f, baseY)
                var x = -extendedWidth / 4f
                while (x <= extendedWidth) {
                    val angle = (x / extendedWidth) * 2 * Math.PI.toFloat() * frequency + phase
                    val y = baseY + amplitude * kotlin.math.sin(angle)
                    path.lineTo(x, y)
                    x += step
                }
                path.lineTo(extendedWidth, extendedHeight)
                path.lineTo(-extendedWidth / 4f, extendedHeight)
                path.close()
                drawPath(path = path, color = color)
            }
            drawWave(wavePhase1, anims[0].first, anims[0].second, anims[0].third, waveColor1)
            drawWave(wavePhase2, anims[1].first, anims[1].second, anims[1].third, waveColor2)
            drawWave(wavePhase3, anims[2].first, anims[2].second, anims[2].third, waveColor3)
            drawWave(wavePhase4, anims[3].first, anims[3].second, anims[3].third, waveColor4)
            foamParticles.forEach {
                drawCircle(
                    color = Color(0xFF5DCCFC).copy(alpha = it.alpha),
                    radius = it.radius,
                    center = Offset(it.x, it.y)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "SafeHito",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    "Fish Status:",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                val statusClean = status.trim().lowercase()
                val (emoji, statusColor) = when (statusClean) {
                    "healthy" -> "🟢" to Color(0xFF43A047)
                    "infected" -> "🔴" to Color(0xFFE53935)
                    "no fish" -> "⚪" to MaterialTheme.colorScheme.onSurface
                    "unknown" -> "❓" to Color(0xFFFB8C00)
                    else -> "🐟" to MaterialTheme.colorScheme.onSurface
                }
                val responsiveFontSize = when {
                    screenWidth < 380 -> 16.sp
                    screenWidth < 400 -> 18.sp
                    else -> 22.sp
                }
                val statusFontSize = responsiveFontSize
                Text(
                    text = "$emoji ${status.trim().replaceFirstChar { it.uppercase() }}",
                    fontSize = statusFontSize,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFishStatusCardContainer(
    modifier: Modifier = Modifier,
    boxHeight: Dp = 230.dp, // new parameter with default
    content: @Composable RowScope.() -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val responsiveBoxHeight = when {
        screenWidth < 340 -> 170.dp
        screenWidth < 380 -> 190.dp
        screenWidth < 420 -> 210.dp
        else -> 230.dp
    }
    val scope = rememberCoroutineScope()
    val tapRipples = remember { mutableStateListOf<Animatable<Float, AnimationVector1D>>() }
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    val waveColor1 = if (isDarkTheme) Color(0xFF4BA3C7).copy(alpha = 0.3f) else Color(0xFF4BA3C7).copy(alpha = 0.5f)
    val waveColor2 = if (isDarkTheme) Color(0xFF5DCCFC).copy(alpha = 0.2f) else Color(0xFF5DCCFC).copy(alpha = 0.4f)
    val waveColor3 = if (isDarkTheme) Color(0xFFB3E5FC).copy(alpha = 0.15f) else Color(0xFFB3E5FC).copy(alpha = 0.3f)
    val waveColor4 = if (isDarkTheme) Color(0xFFE1F5FE).copy(alpha = 0.1f) else Color(0xFFE1F5FE).copy(alpha = 0.25f)
    var targets by remember {
        mutableStateOf(
            listOf(
                Triple(24f, 0.45f, 1.5f),
                Triple(20f, 0.42f, 1.3f),
                Triple(16f, 0.38f, 1.1f),
                Triple(12f, 0.34f, 0.9f)
            )
        )
    }
    var foamParticles by remember {
        mutableStateOf(List(30) {
            FoamParticle(
                x = Random.nextFloat() * 2000f,
                y = Random.nextFloat() * 600f,
                radius = Random.nextFloat() * 5f + 3f,
                alpha = Random.nextFloat() * 0.5f + 0.3f,
                dx = Random.nextFloat() * 0.4f - 0.2f,
                dy = Random.nextFloat() * -0.2f - 0.1f
            )
        })
    }
    val anims = targets.mapIndexed { i, t ->
        Triple(
            animateFloatAsState(t.first, tween(2000), label = "amp$i").value,
            animateFloatAsState(t.second, tween(2000), label = "baseY$i").value,
            animateFloatAsState(t.third, tween(2000), label = "freq$i").value
        )
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            targets = List(4) {
                val amp = listOf(24f, 20f, 16f, 12f)[it] + Random.nextFloat() * 6f
                val baseY = listOf(0.45f, 0.42f, 0.38f, 0.34f)[it] + Random.nextFloat() * 0.02f
                val freq = listOf(1.5f, 1.3f, 1.1f, 0.9f)[it] + Random.nextFloat() * 0.5f
                Triple(amp, baseY, freq)
            }
        }
    }
    val density = LocalDensity.current
    val configuration2 = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration2.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration2.screenHeightDp.dp.toPx() }
    val smoothTiltX = 0f
    val smoothTiltY = 0f
    val clampedTiltX = smoothTiltX.coerceIn(-8f, 8f)
    val clampedTiltY = smoothTiltY.coerceIn(-8f, 8f)
    LaunchedEffect(Unit) {
        while (true) {
            delay(40L)
            foamParticles = foamParticles.map {
                var newX = it.x + it.dx + clampedTiltX * 0.3f
                var newY = it.y + it.dy + clampedTiltY * 0.2f
                val newAlpha = (it.alpha - 0.005f).coerceAtLeast(0f)
                if (newAlpha <= 0.05f || newY < 0f || newX < 0f || newX > 2000f) {
                    FoamParticle(
                        x = Random.nextFloat() * screenWidthPx,
                        y = Random.nextFloat() * screenHeightPx * 0.4f,
                        radius = Random.nextFloat() * 4f + 2f,
                        alpha = 0.6f,
                        dx = Random.nextFloat() * 0.4f - 0.2f,
                        dy = Random.nextFloat() * -0.3f - 0.05f
                    )
                } else {
                    it.copy(x = newX, y = newY, alpha = newAlpha)
                }
            }
        }
    }
    Box(
        modifier = modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
            .height(boxHeight) // use the responsive height
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    tapOffset = offset
                    repeat(3) { i ->
                        val ripple = Animatable(0f)
                        tapRipples.add(ripple)
                        scope.launch {
                            delay(i * 120L)
                            ripple.animateTo(300f + i * 40f, tween(1200, easing = FastOutSlowInEasing))
                            tapRipples.remove(ripple)
                        }
                    }
                }
            }
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "wavePhase")
        val wavePhase1 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
            label = "wavePhase1"
        )
        val wavePhase2 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
            label = "wavePhase2"
        )
        val wavePhase3 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)),
            label = "wavePhase3"
        )
        val wavePhase4 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
            label = "wavePhase4"
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = clampedTiltX * 1.8f
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    clip = false
                }
        ) {
            val extendedWidth = size.width * 1.5f
            val extendedHeight = size.height + 600f
            val waveVerticalShift = clampedTiltY * 10f
            tapRipples.forEachIndexed { i: Int, ripple: Animatable<Float, AnimationVector1D> ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f - i * 0.05f),
                    radius = ripple.value,
                    center = tapOffset,
                    style = Stroke(width = 2f + i)
                )
            }
            fun drawWave(
                phase: Float,
                amplitude: Float,
                baseYRatio: Float,
                frequency: Float,
                color: Color
            ) {
                val path = Path()
                val step = 2f
                val baseY = size.height * baseYRatio + waveVerticalShift
                path.moveTo(-extendedWidth / 4f, baseY)
                var x = -extendedWidth / 4f
                while (x <= extendedWidth) {
                    val angle = (x / extendedWidth) * 2 * Math.PI.toFloat() * frequency + phase
                    val y = baseY + amplitude * kotlin.math.sin(angle)
                    path.lineTo(x, y)
                    x += step
                }
                path.lineTo(extendedWidth, extendedHeight)
                path.lineTo(-extendedWidth / 4f, extendedHeight)
                path.close()
                drawPath(path = path, color = color)
            }
            drawWave(wavePhase1, anims[0].first, anims[0].second, anims[0].third, waveColor1)
            drawWave(wavePhase2, anims[1].first, anims[1].second, anims[1].third, waveColor2)
            drawWave(wavePhase3, anims[2].first, anims[2].second, anims[2].third, waveColor3)
            drawWave(wavePhase4, anims[3].first, anims[3].second, anims[3].third, waveColor4)
            foamParticles.forEach {
                drawCircle(
                    color = Color(0xFF5DCCFC).copy(alpha = it.alpha),
                    radius = it.radius,
                    center = Offset(it.x, it.y)
                )
            }
        }
        // Content slot for quick stats
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            content = content
        )
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    darkTheme: Boolean,
    adminViewModel: AdminViewModel = viewModel(),
    waterDataViewModel: WaterDataViewModel = viewModel(),
    onViewUsers: (() -> Unit)? = null,
    onItemSelected: (String) -> Unit = {}, // Add missing onItemSelected parameter
    navController: NavHostController? = null // Add navController for navigation to notifications if needed
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val responsiveBoxHeight = when {
        screenWidth < 340 -> 170.dp
        screenWidth < 380 -> 190.dp
        screenWidth < 420 -> 210.dp
        else -> 230.dp
    }
    val users by adminViewModel.users.collectAsState()
    val auditLogs by adminViewModel.auditLogs.collectAsState()
    val userCount = users.size
    val totalScans by adminViewModel.totalScans.collectAsState()
    val reportsCount = auditLogs.size // Example: use audit logs as 'Reports'
    val ph by waterDataViewModel.ph.collectAsState()
    val temp by waterDataViewModel.temperature.collectAsState()
    val turbidity by waterDataViewModel.turbidity.collectAsState()
    val oxygen by waterDataViewModel.dissolvedOxygen.collectAsState()
    val waterLevel by waterDataViewModel.waterLevel.collectAsState()
    val waterStatus by waterDataViewModel.waterStatus.collectAsState()
    val fishStatus by waterDataViewModel.fishStatus.collectAsState()
    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val notifications = remember { listOf("No recent notifications.") }
    val totalInfected by adminViewModel.totalInfected.collectAsState()

    // Add these for tank status logic
    val temperature = temp
    val dissolvedOxygen = oxygen

    val triggeredParams = remember(ph, temp, turbidity, oxygen, waterLevel) {
        buildList {
            ph.toDoubleOrNull()?.takeIf { it !in 6.5..8.5 }?.let { add("pH") }
            temp.toDoubleOrNull()?.takeIf { it !in 24.0..30.0 }?.let { add("Temperature") }
            turbidity.toDoubleOrNull()?.takeIf { it > 125.0 }?.let { add("Turbidity") }
            oxygen.toDoubleOrNull()?.takeIf { it < 3.5 }?.let { add("Oxygen") }
            waterLevel.toDoubleOrNull()?.takeIf { it < 20.0 }?.let { add("Water Level") }
        }
    }
    val evaluatedStatus = when (triggeredParams.size) {
        0 -> "Normal"
        1, 2 -> "Caution"
        else -> "Warning"
    }
    val statusTextColor = when (evaluatedStatus.lowercase()) {
        "normal", "good" -> Color(0xFF2E7D32)
        "caution" -> Color(0xFF8D6E63)
        "warning", "infected", "critical" -> Color(0xFFC62828)
        else -> Color.DarkGray
    }
    data class SensorData(
        val label: String,
        val value: String,
        val status: String,
        val description: String
    )

    val sensorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeSensor by remember { mutableStateOf<SensorData?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(activeSensor) {
        if (activeSensor != null) {
            coroutineScope.launch { sensorSheetState.show() }
        } else {
            coroutineScope.launch { sensorSheetState.hide() }
        }
    }

    LaunchedEffect(Unit) {
        adminViewModel.fetchTotalScans()
        adminViewModel.fetchTotalInfected()
    }

    var forecastData by remember { mutableStateOf<ForecastResponse?>(null) }
    var forecastError by remember { mutableStateOf<String?>(null) }
    var isLoadingForecast by remember { mutableStateOf(true) }

    fun refreshWeatherData() {
        isLoadingForecast = true
        forecastError = null
        coroutineScope.launch {
            try {
                forecastData = WeatherApi.retrofitService.getForecastByCity(
                    cityName = "Candaba,PH",
                    apiKey = "679a23f4f66196b14b59b8cc5bfca900"
                )
            } catch (e: Exception) {
                forecastError = "Failed to load forecast: ${e.localizedMessage}"
                Log.e("AdminWeatherRefresh", "Error refreshing weather: ${e.message}")
            } finally {
                isLoadingForecast = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshWeatherData()
    }

    // Notification logic
    var globalNotifications by remember { mutableStateOf<List<Map<String, Any?>>>(emptyList()) }
    var hasUnreadGlobal by remember { mutableStateOf(false) }
    val db = FirebaseDatabase.getInstance()
    LaunchedEffect(Unit) {
        db.getReference("notifications/global").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notifList = snapshot.children.mapNotNull { child ->
                    val message = child.child("message").getValue(String::class.java) ?: return@mapNotNull null
                    val time = child.child("time").getValue(Long::class.java) ?: 0L
                    val read = child.child("read").getValue(Boolean::class.java) ?: false
                    mapOf(
                        "id" to (child.child("id").getValue(String::class.java) ?: child.key),
                        "message" to message,
                        "time" to time,
                        "read" to read
                    )
                }.sortedByDescending { it["time"] as Long }
                globalNotifications = notifList
                hasUnreadGlobal = notifList.any { it["read"] == false }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Fetch admin full name from Firebase
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var fullName by remember { mutableStateOf("Admin") }
    LaunchedEffect(uid) {
        uid.let {
            val ref = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users/$uid/fullName")
            ref.get().addOnSuccessListener {
                fullName = it.value as? String ?: "Admin"
            }
        }
    }

    // Per-admin notification unread indicator
    var hasUnread by remember { mutableStateOf(false) }
    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            val notifRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("notifications").child(uid)
            notifRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val notifList = snapshot.children.mapNotNull { child ->
                        val read = child.child("read").getValue(Boolean::class.java) ?: false
                        read
                    }
                    hasUnread = notifList.any { !it }
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })
        }
    }

    var profileImageBase64 by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        uid?.let {
            val ref = FirebaseDatabase.getInstance().getReference("users/$it")
            ref.child("profileImageBase64").get().addOnSuccessListener {
                profileImageBase64 = it.value as? String
            }
        }
    }

    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning,"
            in 12..17 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { onItemSelected("admin_profile") }
                                .background(Color.LightGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val bitmap = profileImageBase64?.let { base64 ->
                                try {
                                    val decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                                    android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                } catch (e: Exception) { null }
                            }
                            if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
                                androidx.compose.foundation.Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.size(48.dp).clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = R.drawable.default_profile),
                                    contentDescription = "Default Profile",
                                    modifier = Modifier.size(48.dp).clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray
                                ),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = fullName.toProperCase(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController?.navigate("admin_notifications") }, modifier = Modifier.padding(end = 18.dp)) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (darkTheme) Color(0xFF1E1E1E) else Color.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notification",
                                    tint = Color(0xFF5DCCFC),
                                    modifier = Modifier.size(24.dp)
                                )
                                if (hasUnread) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-4).dp, y = 4.dp)
                                            .background(MaterialTheme.colorScheme.error, shape = CircleShape)
                                            .border(1.dp, if (darkTheme) Color(0xFF1E1E1E) else Color.White, shape = CircleShape)
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        var isRefreshing by remember { mutableStateOf(false) }
        val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    try {
                        // Refresh admin data
                        adminViewModel.fetchTotalScans()
                        adminViewModel.fetchTotalInfected()
                        
                        // Refresh weather data
                        refreshWeatherData()
                        
                        // Delay to show refresh indicator
                        delay(1000)
                    } catch (e: Exception) {
                        Log.e("AdminDashboardRefresh", "Error during refresh: ${e.message}")
                    } finally {
                        isRefreshing = false
                    }
                }
            },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    backgroundColor = if (darkTheme) Color(0xFF2C2C2C) else Color.White,
                    contentColor = Color(0xFF5DCCFC), // Your app's primary blue color
                    scale = true,
                    arrowEnabled = true,
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 25.dp, vertical = 0.dp)
                    .padding(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Stats inside animated container with info and catfish image
                AdminFishStatusCardContainer(boxHeight = responsiveBoxHeight) {
                    // Responsive sizes for icon and stats
                    val iconSize: Dp
                    val valueFontSize: TextUnit
                    val labelFontSize: TextUnit
                    val statPadding: Dp
                    val fishIconSize: Dp

                    when {
                        screenWidth < 340 -> {
                            iconSize = 18.dp; valueFontSize = 12.sp; labelFontSize = 8.sp; statPadding = 2.dp; fishIconSize = 85.dp
                        }
                        screenWidth < 380 -> {
                            iconSize = 22.dp; valueFontSize = 16.sp; labelFontSize = 10.sp; statPadding = 4.dp; fishIconSize = 105.dp
                        }
                        screenWidth < 420 -> {
                            iconSize = 26.dp; valueFontSize = 20.sp; labelFontSize = 12.sp; statPadding = 6.dp; fishIconSize = 125.dp
                        }
                        else -> {
                            iconSize = 30.dp; valueFontSize = 24.sp; labelFontSize = 14.sp; statPadding = 8.dp; fishIconSize = 145.dp
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left side: SafeHito, System Overview, stats, button
                        Column(
                            modifier = Modifier.weight(1f).padding(start = 0.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "SafeHito",
                                fontSize = 13.sp,
                                color = primaryTextColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = "System Overview",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = primaryTextColor,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatItemReadable(Icons.Default.People, "Users", userCount, primaryTextColor, iconSize, valueFontSize, labelFontSize, statPadding)
                                StatItemReadable(Icons.Default.Receipt, "Scans", totalScans, primaryTextColor, iconSize, valueFontSize, labelFontSize, statPadding)
                                StatItemReadable(Icons.Default.BugReport, "Infected", totalInfected, primaryTextColor, iconSize, valueFontSize, labelFontSize, statPadding)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            // Button removed as requested
                        }
                        // Right side: Catfish image with floating animation and sound on tap
                        val context = LocalContext.current
                        val soundPlayer = remember { SoundPlayer(context) }
                        var rotation by remember { mutableStateOf(0f) }
                        val animatedRotation by animateFloatAsState(
                            targetValue = rotation,
                            animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
                            label = "fishSpin"
                        )
                        val infiniteTransition = rememberInfiniteTransition()
                        val fishOffsetY by infiniteTransition.animateFloat(
                            initialValue = -10f,
                            targetValue = 10f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 3000, easing = LinearEasing),
                                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                            )
                        )
                        Image(
                            painter = painterResource(id = R.drawable.hito_icon),
                            contentDescription = "Fish Status Image",
                            modifier = Modifier
                                .size(fishIconSize)
                                .offset(y = fishOffsetY.dp)
                                .graphicsLayer {
                                    rotationZ = animatedRotation
                                }
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    soundPlayer.play()
                                    rotation += 360f
                                }
                        )
                    }
                }

                // Water Status Overview (modern, colored, icon cards)
                Text("Water Status Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryTextColor, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
                Column(Modifier.fillMaxWidth()) {

                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        FlippableSensorCardWithModal(
                            label = "Temperature",
                            value = "$temp °C",
                            status = getTempStatusLabel(temp),
                            modifier = Modifier.weight(1f),
                            onLongPress = {
                                activeSensor = SensorData(
                                    label = "Temperature",
                                    value = "$temp °C",
                                    status = getTempStatusLabel(temp),
                                    description = "Water temperature affects fish activity and oxygen levels."
                                )
                            },
                            trendContent = { Text("28.1  →  28.3  →  28.0", fontSize = 14.sp) }
                        )
                        FlippableSensorCardWithModal(
                            label = "pH Level",
                            value = ph,
                            status = getPhStatusLabel(ph),
                            modifier = Modifier.weight(1f),
                            onLongPress = {
                                activeSensor = SensorData(
                                    label = "pH Level",
                                    value = ph,
                                    status = getPhStatusLabel(ph),
                                    description = "pH level indicates how acidic or alkaline the water is."
                                )
                            },
                            trendContent = { Text("📊 pH Trend", style = MaterialTheme.typography.bodyMedium) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        FlippableSensorCardWithModal(
                            label = "Dissolved Oxygen",
                            value = "$oxygen mg/L",
                            status = getOxygenStatusLabel(oxygen),
                            modifier = Modifier.weight(1f),
                            onLongPress = {
                                activeSensor = SensorData(
                                    label = "Dissolved Oxygen",
                                    value = "$oxygen mg/L",
                                    status = getOxygenStatusLabel(oxygen),
                                    description = "Dissolved oxygen is vital for fish survival."
                                )
                            },
                            trendContent = { Text("💨 Oxygen Trend", style = MaterialTheme.typography.bodyMedium) }
                        )
                        FlippableSensorCardWithModal(
                            label = "Turbidity",
                            value = "$turbidity NTU",
                            status = getTurbidityStatusLabel(turbidity),
                            modifier = Modifier.weight(1f),
                            onLongPress = {
                                activeSensor = SensorData(
                                    label = "Turbidity",
                                    value = "$turbidity NTU",
                                    status = getTurbidityStatusLabel(turbidity),
                                    description = "Turbidity measures how clear or cloudy the water is."
                                )
                            },
                            trendContent = { Text("🌫️ Turbidity Trend", style = MaterialTheme.typography.bodyMedium) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        FlippableSensorCardWithModal(
                            label = "Water Level",
                            value = "$waterLevel cm",
                            status = getWaterLevelStatusLabel(waterLevel),
                            modifier = Modifier.weight(1f),
                            onLongPress = {
                                activeSensor = SensorData(
                                    label = "Water Level",
                                    value = "$waterLevel cm",
                                    status = getWaterLevelStatusLabel(waterLevel),
                                    description = "Indicates how much water is in the tank."
                                )
                            },
                            trendContent = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Water Level Range Reference",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val waterLevelValue = waterLevel.toFloatOrNull() ?: 0f
                                    SensorRangeChart(sensorLabel = "Water Level", value = waterLevelValue)
                                }
                            }
                        )
                        FlippableSensorCardWithModal(
                            label = "Tank Status",
                            value = evaluatedStatus,
                            status = if (triggeredParams.isEmpty()) "All good" else "Check water",
                            statusColor = statusTextColor,
                            modifier = Modifier.weight(1f),
                            onLongPress = {
                                activeSensor = SensorData(
                                    label = "Tank Status",
                                    value = evaluatedStatus,
                                    status = if (triggeredParams.isEmpty()) "All good" else "Check water",
                                    description = "Overall tank status based on sensor readings."
                                )
                            },
                            trendContent = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val issues = listOfNotNull(
                                        temp.toDoubleOrNull()?.let {
                                            if (it < 24.0) "Temp: Low" else if (it > 30.0) "Temp: High" else null
                                        },
                                        ph.toDoubleOrNull()?.let {
                                            if (it < 6.5) "pH: Low" else if (it > 8.5) "pH: High" else null
                                        },
                                        oxygen.toDoubleOrNull()?.let {
                                            if (it < 3.5) "O₂: Low" else null
                                        },
                                        turbidity.toDoubleOrNull()?.let {
                                            if (it > 125.0) "Turb: High" else null
                                        },
                                        waterLevel.toDoubleOrNull()?.let {
                                            if (it < 20.0) "Lvl: Low" else null
                                        }
                                    )
                                    if (issues.isEmpty()) {
                                        Text(
                                            text = "All parameters normal",
                                            fontSize = 10.sp,
                                            color = Color.White
                                        )
                                    } else {
                                        val firstLine = issues.take(2).joinToString(" , ")
                                        val secondLine = issues.drop(2).joinToString(" , ")
                                        Text(
                                            text = firstLine,
                                            fontSize = 9.sp,
                                            color = Color.Black
                                        )
                                        if (secondLine.isNotEmpty()) {
                                            Text(
                                                text = secondLine,
                                                fontSize = 9.sp,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                // Weather section
                if (isLoadingForecast) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else if (forecastError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = forecastError ?: "",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else if (forecastData != null) {
                    ForecastResult(forecastData!!)
                }

                // ✅ Recent Logs Section
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header with title and button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Logs",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = primaryTextColor
                        )

                        TextButton(onClick = { navController?.navigate("all_logs") }) {
                            Text(
                                "See All Logs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFA0A7BA)
                            )
                        }
                    }
                }

                // ✅ Timestamp formatter
                fun formatTimestamp(timestamp: Long): String {
                    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                    return sdf.format(Date(timestamp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (auditLogs.isNotEmpty()) {
                        auditLogs
                            .sortedByDescending { it.timestamp }
                            .take(5)
                            .forEach { log ->

                                // Severity color
                                val severityColor = when (log.severity.uppercase(Locale.getDefault())) {
                                    "CRITICAL", "ERROR" -> Color.Red
                                    "WARN", "WARNING" -> Color(0xFFFFA726) // orange
                                    "INFO" -> Color(0xFF66BB6A) // green
                                    else -> Color.Gray
                                }

                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant // ✅ dark/light adaptive
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Severity indicator
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(severityColor, CircleShape)
                                        )

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(4f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = log.action,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )

                                                Spacer(modifier = Modifier.width(6.dp))

                                                // Severity label
                                                Text(
                                                    text = log.severity.uppercase(Locale.getDefault()),
                                                    fontSize = 11.sp,
                                                    color = severityColor,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            // User + Time
                                            Text(
                                                text = "By ${log.performedBy} • ${formatTimestamp(log.timestamp)}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            // Description
                                            if (log.details.isNotEmpty()) {
                                                Text(
                                                    text = log.details,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No logs available",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                }



                // Show global notifications below stats
                if (globalNotifications.isNotEmpty()) {
                    Text("Recent Notifications", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryTextColor, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
                    Column(Modifier.fillMaxWidth()) {
                        globalNotifications.take(5).forEach { notif ->
                            NotificationCardStyled(
                                message = notif["message"] as? String ?: "",
                                icon = Icons.Default.Notifications,
                                color = if (notif["read"] == false) MaterialTheme.colorScheme.primary else Color.Gray,
                                darkTheme = darkTheme
                            )
                        }
                    }
                }

            }

        }

    }

    if (activeSensor != null) {
        ModalBottomSheet(
            onDismissRequest = { activeSensor = null },
            sheetState = sensorSheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            val sensor = activeSensor!!
            val label = sensor.label.lowercase()
            val statusColor = when (sensor.status.lowercase()) {
                "optimal", "balanced", "adequate", "clear", "sufficient" -> Color(0xFF2E7D32)
                "too low", "too high", "acidic", "alkaline", "low oxygen", "murky", "low water level" -> Color(0xFFD32F2F)
                "unknown" -> Color(0xFF616161)
                else -> Color(0xFFEF6C00)
            }
            data class SensorStatus(val label: String, val color: Color)
            fun getSensorStatus(label: String, value: Float): SensorStatus {
                return when (label.lowercase()) {
                    "ph", "ph level" -> when {
                        value < 6.5f -> SensorStatus("Acidic", Color(0xFFD32F2F))
                        value > 8.5f -> SensorStatus("Alkaline", Color(0xFFD32F2F))
                        else -> SensorStatus("Balanced", Color(0xFF2E7D32))
                    }
                    "temperature" -> when {
                        value < 24f -> SensorStatus("Too Low", Color(0xFFD32F2F))
                        value > 30f -> SensorStatus("Too High", Color(0xFFD32F2F))
                        else -> SensorStatus("Optimal", Color(0xFF2E7D32))
                    }
                    "dissolved oxygen" -> when {
                        value < 3.5f -> SensorStatus("Low Oxygen", Color(0xFFD32F2F))
                        value < 5.0f -> SensorStatus("Slightly Low", Color(0xFFFFA000))
                        value <= 6.5f -> SensorStatus("Adequate", Color(0xFF2E7D32))
                        else -> SensorStatus("High Oxygen", Color(0xFFFFA000))
                    }
                    "turbidity" -> when {
                        value <= 50f -> SensorStatus("Clear", Color(0xFF2E7D32))
                        value <= 125f -> SensorStatus("Slightly Murky", Color(0xFFFFA000))
                        else -> SensorStatus("Murky", Color(0xFFD32F2F))
                    }
                    "water level" -> when {
                        value < 20f -> SensorStatus("Low Water Level", Color(0xFFD32F2F))
                        value in 20f..50f -> SensorStatus("Sufficient", Color(0xFF2E7D32))
                        else -> SensorStatus("Unknown", Color(0xFF616161))
                    }
                    else -> SensorStatus("Unknown", Color(0xFF616161))
                }
            }
            val (currentValue, status) = when {
                label.contains("ph", ignoreCase = true) -> {
                    val v = ph.toFloatOrNull() ?: 0f
                    v to getSensorStatus("ph", v)
                }
                label.contains("temperature", ignoreCase = true) -> {
                    val v = temp.toFloatOrNull() ?: 0f
                    v to getSensorStatus("temperature", v)
                }
                label.contains("oxygen", ignoreCase = true) -> {
                    val v = oxygen.toFloatOrNull() ?: 0f
                    v to getSensorStatus("dissolved oxygen", v)
                }
                label.contains("turbidity", ignoreCase = true) -> {
                    val v = turbidity.toFloatOrNull() ?: 0f
                    v to getSensorStatus("turbidity", v)
                }
                label.contains("water level", ignoreCase = true) -> {
                    val v = sensor.value.replace("[^0-9.]".toRegex(), "").toFloatOrNull() ?: 0f
                    v to getSensorStatus("water level", v)
                }
                label.contains("tank status", ignoreCase = true) -> {
                    0f to SensorStatus(
                        evaluatedStatus.replaceFirstChar { it.uppercaseChar() },
                        when (evaluatedStatus.lowercase()) {
                            "normal", "good" -> Color(0xFF2E7D32) // green
                            "caution" -> Color(0xFF8D6E63)        // brown
                            "warning", "critical" -> Color(0xFFC62828) // red
                            else -> Color(0xFF616161)
                        }
                    )
                }
                else -> {
                    val v = sensor.value.replace("[^0-9.]".toRegex(), "").toFloatOrNull() ?: 0f
                    v to SensorStatus("Unknown", Color(0xFF616161))
                }
            }
            val icon = when {
                label.contains("ph", ignoreCase = true) -> Icons.Default.Science
                label.contains("temperature", ignoreCase = true) -> Icons.Default.Thermostat
                label.contains("oxygen", ignoreCase = true) -> Icons.Default.BubbleChart
                label.contains("turbidity", ignoreCase = true) -> Icons.Default.BlurOn
                label.contains("water level", ignoreCase = true) -> Icons.Default.Water
                else -> Icons.Default.Info
            }
            fun getTankStatusDescription(status: String, triggeredParams: List<String>): String {
                return when (status.lowercase()) {
                    "normal" -> "All water parameters are within safe range. No immediate action is required."
                    "caution" -> "Some parameters are approaching unsafe levels. Monitor the tank closely."
                    "warning", "critical" -> {
                        val issues = triggeredParams.joinToString(", ")
                        "Critical alert. The following parameters need attention: $issues."
                    }
                    else -> "Unable to determine tank condition at the moment."
                }
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Sensor Icon",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${sensor.label} Overview",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Spacer(Modifier.height(12.dp))
                if (!label.contains("tank status", ignoreCase = true)) {
                    Text("Current Value: $currentValue", style = MaterialTheme.typography.bodyLarge)
                }
                Text("Status: ${status.label}", color = status.color, style = MaterialTheme.typography.bodyLarge)
                if (!label.contains("tank status", ignoreCase = true)) {
                    Spacer(Modifier.height(30.dp))
                    val referenceTitle = when {
                        label.contains("ph", ignoreCase = true) -> "pH Level Range Reference (Acidity)"
                        label.contains("temperature", ignoreCase = true) -> "Temperature Range Reference (°C)"
                        label.contains("oxygen", ignoreCase = true) -> "Dissolved Oxygen Reference (mg/L)"
                        label.contains("turbidity", ignoreCase = true) -> "Water Clarity Reference (NTU)"
                        label.contains("water level", ignoreCase = true) -> "Water Level Reference (cm)"
                        else -> "Safe Range Reference"
                    }
                    Text(referenceTitle, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    SensorRangeChart(sensorLabel = sensor.label, value = currentValue)
                }
                if (label.contains("tank status", ignoreCase = true)) {
                    val description = getTankStatusDescription(evaluatedStatus, triggeredParams)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(description, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(16.dp))
                val gradientBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF5DCCFC), // bright cyan
                        Color(0xFF007EF2)  // deep aqua blue – creates a real gradient feel
                    ),
                    start = Offset(0f, 0f),
                    end = Offset.Infinite
                )
                Button(
                    onClick = { activeSensor = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(brush = gradientBrush, shape = RoundedCornerShape(8.dp))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
        Text(label, color = color, fontSize = 13.sp)
        Text("$value", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
    }
}

@Composable
fun StatItemReadable(
    icon: ImageVector,
    label: String,
    value: Int,
    color: Color,
    iconSize: Dp = 26.dp,
    valueFontSize: TextUnit = 20.sp,
    labelFontSize: TextUnit = 12.sp,
    horizontalPadding: Dp = 6.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = horizontalPadding)
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(iconSize))
        Text("$value", fontWeight = FontWeight.Bold, fontSize = valueFontSize, color = color)
        Text(label, color = color, fontSize = labelFontSize, fontWeight = FontWeight.Medium)
    }
}


@Composable
fun WaterParamCard(
    icon: ImageVector,
    label: String,
    value: String,
    statusColor: Color,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = if (darkTheme) Color.White else Color.Black, modifier = Modifier.size(28.dp))
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (darkTheme) Color.White else Color.Black)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (darkTheme) Color.White else Color.Black)
        }
    }
}

@Composable
fun NotificationCardStyled(message: String, icon: ImageVector, color: Color, darkTheme: Boolean) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = if (darkTheme) 0.18f else 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(end = 8.dp))
            Text(message, color = if (darkTheme) Color.White else Color.Black, fontSize = 15.sp)
        }
    }
}

// Status color helpers (match user UI logic)
fun getPhStatus(ph: String): Color {
    val value = ph.toFloatOrNull() ?: return Color.LightGray
    return when {
        value in 6.5..8.5 -> Color(0xFFD7FCD4)
        value < 6.5 -> Color(0xFFFFF9C4)
        value > 8.5 -> Color(0xFFFFCDD2)
        else -> Color.LightGray
    }
}
fun getTempStatus(temp: String): Color {
    val value = temp.toFloatOrNull() ?: return Color.LightGray
    return when {
        value in 24.0..30.0 -> Color(0xFFD7FCD4)
        value < 24.0 -> Color(0xFFFFF9C4)
        value > 30.0 -> Color(0xFFFFCDD2)
        else -> Color.LightGray
    }
}
fun getTurbidityStatus(turbidity: String): Color {
    val value = turbidity.toFloatOrNull() ?: return Color.LightGray
    return when {
        value <= 5.0 -> Color(0xFFD7FCD4)
        value <= 10.0 -> Color(0xFFFFF9C4)
        value > 10.0 -> Color(0xFFFFCDD2)
        else -> Color.LightGray
    }
}
fun getOxygenStatus(oxygen: String): Color {
    val value = oxygen.toFloatOrNull() ?: return Color.LightGray
    return when {
        value >= 4.0 -> Color(0xFFD7FCD4)
        value in 2.0..3.9 -> Color(0xFFFFF9C4)
        value < 2.0 -> Color(0xFFFFCDD2)
        else -> Color.LightGray
    }
}
fun getWaterLevelStatus(level: String): Color {
    val value = level.toFloatOrNull() ?: return Color.LightGray
    return when {
        value >= 20.0 -> Color(0xFFD7FCD4)
        value in 10.0..19.9 -> Color(0xFFFFF9C4)
        value < 10.0 -> Color(0xFFFFCDD2)
        else -> Color.LightGray
    }
}
fun getOverallStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "normal", "good" -> Color(0xFFD7FCD4)
        "caution" -> Color(0xFFFFF9C4)
        "warning", "infected", "critical" -> Color(0xFFFFCDD2)
        else -> Color.LightGray
    }
}
fun getFishStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "healthy" -> Color(0xFFD7FCD4)
        "infected" -> Color(0xFFFFCDD2)
        "no fish" -> Color.LightGray
        else -> Color(0xFFFFF9C4)
    }
}

// Helper functions for status labels (copy logic from DashboardScreen)
fun getTempStatusLabel(temp: String): String {
    val value = temp.toDoubleOrNull() ?: return "Unknown"
    return when {
        value < 24.0 -> "Too Low"
        value > 30.0 -> "Too High"
        else -> "Optimal"
    }
}
fun getPhStatusLabel(ph: String): String {
    val value = ph.toDoubleOrNull() ?: return "Unknown"
    return when {
        value < 6.5 -> "Acidic"
        value > 8.5 -> "Alkaline"
        else -> "Balanced"
    }
}
fun getOxygenStatusLabel(oxygen: String): String {
    val value = oxygen.toDoubleOrNull() ?: return "Unknown"
    return when {
        value < 3.5 -> "Low Oxygen"
        value < 5.0 -> "Slightly Low"
        value <= 6.5 -> "Adequate"
        else -> "High Oxygen"
    }
}
fun getTurbidityStatusLabel(turbidity: String): String {
    val value = turbidity.toDoubleOrNull() ?: return "Unknown"
    return when {
        value <= 50.0 -> "Clear"
        value <= 125.0 -> "Slightly Murky"
        else -> "Murky"
    }
}
fun getWaterLevelStatusLabel(level: String): String {
    val value = level.toDoubleOrNull() ?: return "Unknown"
    return when {
        value < 20.0 -> "Low Water Level"
        value in 20.0..50.0 -> "Sufficient"
        else -> "Unknown"
    }
}
fun getTankStatusLabel(status: String): String {
    return when (status.lowercase()) {
        "normal", "good" -> "All good"
        "caution" -> "Caution"
        "warning", "infected", "critical" -> "Check water"
        else -> "Unknown"
    }
}



