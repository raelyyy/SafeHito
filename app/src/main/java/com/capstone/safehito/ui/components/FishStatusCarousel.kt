package com.capstone.safehito.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight

import kotlinx.coroutines.delay

data class CardData(val title: String, val status: String, val message: String)

@Composable
fun FishStatusCarousel(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
    onItemSelected: (String) -> Unit
) {
    val cardItems = listOf(
        CardData("Latest Scan", "Healthy", "🟢 Last scan indicates healthy fish."),
        CardData("Scan Streak", "5 Days", "No infection detected recently."),
        CardData("Common Status", "Healthy", "85% of recent scans are healthy."),
        CardData("Tips", "Check Filter", "Clean filters weekly to avoid issues."),
        CardData("Manual Scan", "Available", "You can upload an image manually.")
    )

    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        itemsIndexed(cardItems) { index, item ->
            val isFocused = listState.firstVisibleItemIndex == index

            val scale by animateFloatAsState(
                targetValue = if (isFocused) 1.0f else 0.94f,
                animationSpec = tween(300),
                label = "cardScale"
            )

            FishStatusCarouselCard(
                title = item.title,
                status = item.status,
                message = item.message,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                darkTheme = darkTheme,
                onClick = { onItemSelected("records") }
            )
        }
    }
}

@Composable
fun FishStatusCarouselCard(
    title: String,
    status: String,
    message: String,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
    onClick: () -> Unit
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF5DCCFC), Color(0xFF007EF2)),
        start = Offset.Zero,
        end = Offset.Infinite
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .width(260.dp)
            .height(160.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradientBrush)
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = status,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = message,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}


@Composable
fun FishTipsCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .shadow(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("🐟 Tips for Healthier Fish", style = MaterialTheme.typography.titleMedium)
            Text("✅ Clean filters weekly\n✅ Monitor pH and oxygen\n✅ Avoid overfeeding")
            Button(onClick = {}) {
                Text("View All Tips")
            }
        }
    }
}

@Composable
fun FishSummaryCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .shadow(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("📈 Scan Summary", style = MaterialTheme.typography.titleMedium)
            Text("Healthy Scans: 12\nInfected Scans: 2\nNo Fish: 1")
            Button(onClick = {}) {
                Text("View History")
            }
        }
    }
}

@Composable
fun ManualScanCard(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .shadow(6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("📷 Manual Scan", style = MaterialTheme.typography.titleMedium)
            Text("Didn’t scan yet? You can upload a photo manually.")
            Button(onClick = onClick) {
                Text("Scan Now")
            }
        }
    }
}
