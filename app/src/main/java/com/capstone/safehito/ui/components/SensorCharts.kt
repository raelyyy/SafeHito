package com.capstone.safehito.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt


@Composable
fun GradientSensorChart(
    currentValue: Float,
    minValue: Float,
    maxValue: Float,
    gradient: Brush,
    thresholds: List<Float>,
    labelFormatter: (Float) -> String = { it.toString() },
    zoneLabels: List<String> = emptyList()
) {
    val density = LocalDensity.current
    var barWidthPx by remember { mutableStateOf(0f) }

    val rawFraction = ((currentValue - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = rawFraction,
        animationSpec = tween(400),
        label = "AnimatedFraction"
    )

    val offsetDp = remember(barWidthPx, animatedFraction) {
        with(density) { (animatedFraction * barWidthPx).toDp() }
    }

    Column {

        Spacer(Modifier.height(8.dp))

        // ✅ Zone Labels - now ABOVE the gradient bar
        if (zoneLabels.size >= 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                zoneLabels.forEach {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        // ✅ Bar + Arrow
        Box(modifier = Modifier.fillMaxWidth().height(40.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush = gradient)
                    .onGloballyPositioned {
                        barWidthPx = it.size.width.toFloat()
                    }
            )

            // ▼ Arrow
            if (barWidthPx > 0f) {
                val usableWidthPx = barWidthPx - with(density) { 24.dp.toPx() }
                val clampedFraction = animatedFraction.coerceIn(0f, 1f)
                val offset = with(density) { (clampedFraction * usableWidthPx).toDp() + 12.dp }

                Text(
                    text = "▼",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .offset(x = offset - 10.dp)
                        .align(Alignment.TopStart)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // ✅ Threshold Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            thresholds.forEach {
                Text(labelFormatter(it), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}


@Composable
fun SensorRangeChart(sensorLabel: String, value: Float) {
    val normalizedLabel = sensorLabel.trim().lowercase()

    when {
        // ✅ pH Chart
        normalizedLabel.contains("ph") -> {
            //Text("pH Level (Acidity)", style = MaterialTheme.typography.labelLarge)
            GradientSensorChart(
                currentValue = value,
                minValue = 4f,
                maxValue = 10f,
                gradient = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFD32F2F), // Strongly acidic
                        Color(0xFFFFA000), // Slightly acidic
                        Color(0xFF8BC34A),
                        Color(0xFF2E7D32), // Neutral
                        Color(0xFF8BC34A),
                        Color(0xFFFFA000), // Slightly alkaline
                        Color(0xFFD32F2F)  // Alkaline
                    )
                ),
                thresholds = listOf(4f, 5.3f, 7f, 8.3f, 10f),
                labelFormatter = {
                    when (it) {
                        4f -> "≤4"
                        5.3f -> "5.3"
                        7f -> "7"
                        8.3f -> "8.3"
                        10f -> "≥10"
                        else -> "pH %.1f".format(it)
                    }
                },
                zoneLabels = listOf("Acidic", "Balanced", "Alkaline")
            )
        }

        // ✅ Dissolved Oxygen Chart
        normalizedLabel.contains("oxygen") -> {
            //Text("Dissolved Oxygen (mg/L)", style = MaterialTheme.typography.labelLarge)
            GradientSensorChart(
                currentValue = value,
                minValue = 0f,
                maxValue = 10f,
                gradient = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFD32F2F), // Critical
                        Color(0xFFFF9800), // Low
                        Color(0xFF8BC34A), // Sufficient
                        Color(0xFF4CAF50), // Good
                        Color(0xFF2E7D32)  // Excellent
                    )
                ),
                thresholds = listOf(0f, 3.5f, 5f, 6.5f, 10f),
                labelFormatter = {
                    when (it) {
                        0f -> "0"
                        3.5f -> "3.5"
                        5f -> "5.0"
                        6.5f -> "6.5"
                        10f -> "10"
                        else -> "%.1f".format(it)
                    }
                },
                zoneLabels = listOf("Low", "Adequate", "High")
            )
        }

        // ✅ Turbidity Chart
        normalizedLabel.contains("turbidity") -> {
            //Text("Water Clarity (NTU)", style = MaterialTheme.typography.labelLarge)
            GradientSensorChart(
                currentValue = value,
                minValue = 0f,
                maxValue = 300f,
                gradient = Brush.horizontalGradient(
                    listOf(

                        Color(0xFF2E7D32),  // Clear ✅ center
                        Color(0xFF8BC34A),
                        Color(0xFFFFA000),  // Slightly Murky
                        Color(0xFFD32F2F)   // Very Murky
                    )
                ),
                thresholds = listOf(0f, 50f, 100f, 200f, 300f),
                labelFormatter = {
                    when (it) {
                        0f -> "0"
                        50f -> "50"
                        100f -> "100"
                        200f -> "200"
                        300f -> "≥300"
                        else -> "%.0f".format(it)
                    }
                },
                zoneLabels = listOf("Clear", "Slightly Murky", "Murky")
            )
        }

        // ✅ Temperature Chart
        normalizedLabel.contains("temperature") -> {
            //Text("Temperature (°C)", style = MaterialTheme.typography.labelLarge)
            GradientSensorChart(
                currentValue = value,
                minValue = 18f,
                maxValue = 34f,
                gradient = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFD32F2F), // Too Cold
                        Color(0xFFFFA000),
                        Color(0xFF8BC34A),
                        Color(0xFF2E7D32), // Optimal
                        Color(0xFF8BC34A),
                        Color(0xFFFFA000),
                        Color(0xFFD32F2F)  // Too Hot
                    )
                ),
                thresholds = listOf(18f, 22f, 26f, 30f, 34f),
                labelFormatter = {
                    when (it.roundToInt()) {
                        18 -> "≤18"
                        22 -> "22"
                        26 -> "26"
                        30 -> "30"
                        34 -> "≥34"
                        else -> "%.0f".format(it)
                    }
                },
                zoneLabels = listOf("Too Cold", "Optimal", "Too Hot")
            )
        }

        // ✅ Water Level Chart
        normalizedLabel.contains("water level") -> {
            //Text("Water Level (cm)", style = MaterialTheme.typography.labelLarge)
            GradientSensorChart(
                currentValue = value,
                minValue = 0f,
                maxValue = 60f, // allows space for overflow
                gradient = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFD32F2F), // Critical
                        Color(0xFFFF9800), // Low
                        Color(0xFF8BC34A), // Sufficient
                        Color(0xFF4CAF50), // Good
                        Color(0xFF2E7D32), // Excellent
                        Color(0xFF4CAF50),
                        Color(0xFF8BC34A),
                        Color(0xFFFF9800),
                        Color(0xFFD32F2F)  // Overflow
                    )
                ),
                thresholds = listOf(0f, 10f, 15f, 30f, 45f, 60f),
                labelFormatter = {
                    when (it) {
                        0f -> "0"
                        10f -> "10"
                        15f -> "15"
                        30f -> "30"
                        45f -> "45"
                        60f -> ">45"
                        else -> "%.0f".format(it)
                    }
                },
                zoneLabels = listOf("Low", "Sufficient", "Overflow")
            )
        }
    }

    Spacer(Modifier.height(12.dp))
}

