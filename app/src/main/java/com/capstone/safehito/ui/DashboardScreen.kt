package com.capstone.safehito.ui

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstone.safehito.R
import com.capstone.safehito.viewmodel.NotificationViewModel
import com.capstone.safehito.viewmodel.NotificationViewModelFactory
import com.capstone.safehito.viewmodel.WaterDataViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import androidx.compose.ui.graphics.drawscope.Stroke

import com.google.accompanist.pager.*
import java.util.*
import androidx.compose.foundation.layout.FlowRow
import android.webkit.WebView
import android.webkit.WebSettings
import androidx.compose.ui.viewinterop.AndroidView

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PageSize.Fill

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.graphics.Path

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import kotlin.math.*

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter

import androidx.core.net.toUri

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.capstone.safehito.api.WeatherApi
import com.capstone.safehito.model.ForecastResponse
import com.capstone.safehito.ui.components.SensorRangeChart
import kotlin.compareTo


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.StrokeCap


import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.android.gms.common.config.GservicesValue.value
import java.time.LocalDate

import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.window.DialogProperties
import com.capstone.safehito.ui.toProperCase

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: WaterDataViewModel = viewModel(),
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    onLogout: () -> Unit,
    navController: NavHostController,
    notificationViewModel: NotificationViewModel,
    darkTheme: Boolean
) {
    var selectedTab by remember { mutableStateOf("dashboard") }
    BackHandler(enabled = true) {}

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    val ph by viewModel.ph.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val turbidity by viewModel.turbidity.collectAsState()
    val dissolvedOxygen by viewModel.dissolvedOxygen.collectAsState()
    val waterLevel by viewModel.waterLevel.collectAsState()
    val waterStatus by viewModel.waterStatus.collectAsState()
    val fishStatus by viewModel.fishStatus.collectAsState()

    val triggeredParams = remember(ph, temperature, turbidity, dissolvedOxygen, waterLevel) {
        buildList {
            ph.toDoubleOrNull()?.takeIf { it !in 6.5..8.5 }?.let { add("pH") }
            temperature.toDoubleOrNull()?.takeIf { it !in 24.0..30.0 }?.let { add("Temperature") }
            turbidity.toDoubleOrNull()?.takeIf { it > 125.0 }?.let { add("Turbidity") }
            dissolvedOxygen.toDoubleOrNull()?.takeIf { it < 3.5 }?.let { add("Oxygen") }
            waterLevel.toDoubleOrNull()?.takeIf { it < 20.0 }?.let { add("Water Level") }
        }
    }

    fun evaluateWaterStatusWithDetails(
        ph: Double,
        temperature: Double,
        oxygen: Double,
        turbidity: Double,
        waterLevel: Double
    ): Triple<String, List<String>, Map<String, Boolean>> {
        val triggeredParams = mutableListOf<String>()
        val paramStates = mutableMapOf<String, Boolean>()

        // pH
        val phNormal = ph in 6.5..8.5
        paramStates["pH"] = phNormal
        if (!phNormal) triggeredParams.add("pH ($ph)")

        // Temperature
        val tempNormal = temperature in 24.0..30.0
        paramStates["Temperature"] = tempNormal
        if (!tempNormal) triggeredParams.add("Temperature (${temperature}°C)")

        // Oxygen
        val oxygenNormal = oxygen >= 3.5
        paramStates["Oxygen"] = oxygenNormal
        if (!oxygenNormal) triggeredParams.add("Oxygen (${oxygen} mg/L)")

        // Turbidity
        val turbidityNormal = turbidity <= 125.0
        paramStates["Turbidity"] = turbidityNormal
        if (!turbidityNormal) triggeredParams.add("Turbidity (${turbidity} NTU)")

        // Water Level
        val waterLevelNormal = waterLevel >= 20.0
        paramStates["Water Level"] = waterLevelNormal
        if (!waterLevelNormal) triggeredParams.add("Water Level (${waterLevel} cm)")

        // Determine status by number of issues
        val numIssues = triggeredParams.size
        val status = when {
            numIssues == 0 -> "Normal"
            numIssues >= 3 -> "Warning"
            numIssues >= 2 -> "Caution"
            else -> "Caution"
        }

        return Triple(status, triggeredParams, paramStates)
    }

    val (evaluatedStatus, evaluatedIssues, _) = evaluateWaterStatusWithDetails(
        ph = ph.toDoubleOrNull() ?: 0.0,
        temperature = temperature.toDoubleOrNull() ?: 0.0,
        oxygen = dissolvedOxygen.toDoubleOrNull() ?: 0.0,
        turbidity = turbidity.toDoubleOrNull() ?: 0.0,
        waterLevel = waterLevel.toDoubleOrNull() ?: 0.0
    )


    if (uid == null) {
        // No UID = no user logged in; show message and return early
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("You are not logged in.")
        }
        return
    }
    LaunchedEffect(uid) {
        viewModel.loadLatestFishStatus(uid)
    }

    // Sensor status logic
    val tempStatus = temperature.toDoubleOrNull()?.let {
        when {
            it < 24.0 -> "Too Low"
            it > 30.0 -> "Too High"
            else -> "Optimal"
        }
    } ?: "Unknown"

    val phStatus = ph.toDoubleOrNull()?.let {
        when {
            it < 6.5 -> "Acidic"
            it > 8.5 -> "Alkaline"
            else -> "Balanced"
        }
    } ?: "Unknown"

    val oxygenStatus = dissolvedOxygen.toDoubleOrNull()?.let {
        when {
            it < 3.5 -> "Low Oxygen"         // 🔴
            it < 5.0 -> "Slightly Low"       // 🟠
            it <= 6.5 -> "Adequate"          // 🟢
            else -> "High Oxygen"            // 🟡
        }
    } ?: "Unknown"

    val turbidityStatus = turbidity.toDoubleOrNull()?.let {
        when {
            it <= 50.0 -> "Clear"
            it <= 125.0 -> "Slightly Murky"
            else -> "Murky"
        }
    } ?: "Unknown"

    val waterLevelStatus = waterLevel.toDoubleOrNull()?.let {
        if (it >= 20.0) "Sufficient" else "Low Water Level"
    } ?: "Unknown"

// Status card colors
    val statusColor = when (evaluatedStatus.lowercase()) {
        "normal", "good" -> Color(0xFFD7FCD4)
        "caution" -> Color(0xFFFFF9C4)
        "warning", "infected", "critical" -> Color(0xFFFFCDD2)
        else -> Color.LightGray
    }

    val statusTextColor = when (evaluatedStatus.lowercase()) {
        "normal", "good" -> Color(0xFF2E7D32)
        "caution" -> Color(0xFF8D6E63)
        "warning", "infected", "critical" -> Color(0xFFC62828)
        else -> Color.DarkGray
    }

    val modalStatusTextColor = when (evaluatedStatus.lowercase()) {
        "normal", "good" -> Color(0xFF2E7D32)
        "caution" -> Color(0xFFFFC107)
        "warning", "infected", "critical" -> Color(0xFFEF5350)
        else -> Color.DarkGray
    }

    // Modal Sheet Controller
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }


    // Animation: bounce (scale effect)
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120)
    )


    // Colors based on status
    // Status background color pair
    val (baseColor, deepColor) = when (evaluatedStatus.lowercase()) {
        "sufficient", "optimal", "good", "normal" -> Pair(Color(0xFFB2DFDB), Color(0xFF4DB6AC))
        "warning", "infected", "low", "too low", "critical" -> Pair(Color(0xFFFFCDD2), Color(0xFFEF5350))
        "high", "too high", "overflow" -> Pair(Color(0xFFD1C4E9), Color(0xFF7E57C2))
        "unknown" -> Pair(Color(0xFFCFD8DC), Color(0xFF90A4AE))
        else -> Pair(Color(0xFFFFECB3), Color(0xFFFFC107))
    }




    val currentTime = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }

    val auth = FirebaseAuth.getInstance()
    var fullName by remember { mutableStateOf("User") }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning,"
            in 12..17 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
    }

    fun String.toProperCase(): String =
        split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }

    LaunchedEffect(uid) {
        uid?.let {
            val ref = FirebaseDatabase.getInstance().getReference("users/$it")
            ref.child("fullName").get().addOnSuccessListener {
                fullName = it.value as? String ?: "User"
            }
        }
    }

    val backgroundColor = if (darkTheme) Color(0xFF121212) else Color(0xFFF4F8FB)
    val primaryTextColor = if (darkTheme) Color.White else Color.Black

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = backgroundColor,
            darkIcons = !darkTheme
        )
    }


    var hasUnread by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        uid?.let {
            val notifRef = FirebaseDatabase.getInstance().getReference("notifications/$uid")
            notifRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    hasUnread = snapshot.children.any {
                        it.child("read").getValue(Boolean::class.java) == false
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
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

    // Trigger modal sheet show/hide
    LaunchedEffect(activeSensor) {
        if (activeSensor != null) {
            coroutineScope.launch {
                sheetState.show()
            }
        } else {
            coroutineScope.launch {
                sheetState.hide()
            }
        }
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
                Log.e("WeatherRefresh", "Error refreshing weather: ${e.message}")
            } finally {
                isLoadingForecast = false
            }
        }
    }

    fun refreshWeatherDataAsync(): kotlinx.coroutines.Job {
        isLoadingForecast = true
        forecastError = null
        return coroutineScope.launch {
            try {
                forecastData = WeatherApi.retrofitService.getForecastByCity(
                    cityName = "Candaba,PH",
                    apiKey = "679a23f4f66196b14b59b8cc5bfca900"
                )
            } catch (e: Exception) {
                forecastError = "Failed to load forecast: ${e.localizedMessage}"
                Log.e("WeatherRefresh", "Error refreshing weather: ${e.message}")
            } finally {
                isLoadingForecast = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshWeatherData()
    }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF5DCCFC), // bright cyan
            Color(0xFF007EF2)  // deep aqua blue – creates a real gradient feel
        ),
        start = Offset(0f, 0f),
        end = Offset.Infinite
    )

    var profileImageBase64 by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uid) {
        uid?.let {
            val ref = FirebaseDatabase.getInstance().getReference("users/$it")
            ref.child("profileImageBase64").get().addOnSuccessListener {
                profileImageBase64 = it.value as? String
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(12.dp))
                        // Profile picture
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { onItemSelected("profile") }
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
                    IconButton(onClick = { navController.navigate("notifications") },
                        modifier = Modifier.padding(end = 18.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .shadow(6.dp, shape = CircleShape),
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
                    containerColor = backgroundColor
                )
            )
        },
        /*bottomBar = {
            FloatingNavBar(
                selectedRoute = selectedRoute,
                onItemSelected = onItemSelected,
                modifier = Modifier.padding(bottom = 25.dp)
            )
        }*/
        containerColor = backgroundColor
    ) { innerPadding ->
        var isRefreshing by remember { mutableStateOf(false) }
        val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)
        val coroutineScope = rememberCoroutineScope()

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                isRefreshing = true
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                coroutineScope.launch {
                    try {
                        // Refresh water data
                        viewModel.refresh()
                        
                        // Force reload fish status
                        uid?.let { viewModel.loadLatestFishStatus(it) }
                        
                        // Refresh weather data asynchronously
                        val weatherJob = refreshWeatherDataAsync()
                        
                        // Wait for weather data to complete with timeout
                        var timeout = 0
                        while (isLoadingForecast && timeout < 50) { // 5 second timeout
                            delay(100)
                            timeout++
                        }
                        
                        // Wait for weather job to complete
                        weatherJob.join()
                        
                        // Additional delay to ensure all data is loaded and UI updates
                        delay(300)
                    } catch (e: Exception) {
                        // Handle any errors during refresh
                        Log.e("DashboardRefresh", "Error during refresh: ${e.message}")
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 25.dp, vertical = 0.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                item {
                    FishStatusCarouselPager(
                        darkTheme = darkTheme,
                        onItemSelected = onItemSelected
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Water Quality Readings", style = MaterialTheme.typography.titleMedium, color = primaryTextColor)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    // FIRST ROW: Temperature & pH Level
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        FlippableSensorCardWithModal(
                            label = "Temperature",
                            value = "$temperature °C",
                            status = tempStatus,
                            modifier = Modifier.weight(1f),
                            onLongPress = {
                                activeSensor = SensorData(
                                    label = "Temperature",
                                    value = "$temperature °C",
                                    status = tempStatus,
                                    description = "Water temperature affects fish activity and oxygen levels."
                                )
                            },
                            trendContent = {
                                Text("28.1  →  28.3  →  28.0", fontSize = 14.sp)
                            }
                        )

                        FlippableSensorCardWithModal(
                            label = "pH Level",
                            value = ph,
                            status = phStatus,
                            modifier = Modifier.weight(1f),
                            onLongPress = {
                                activeSensor = SensorData(
                                    label = "pH Level",
                                    value = ph,
                                    status = phStatus,
                                    description = "pH level indicates how acidic or alkaline the water is."
                                )
                            },
                            trendContent = {
                                Text("📊 pH Trend", style = MaterialTheme.typography.bodyMedium)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    // SECOND ROW: Dissolved Oxygen & Turbidity
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        FlippableSensorCardWithModal(
                            label = "Dissolved Oxygen",
                            value = "$dissolvedOxygen mg/L",
                            status = oxygenStatus,
                            modifier = Modifier.weight(1f),
                            onLongPress = {
                                activeSensor = SensorData(
                                    label = "Dissolved Oxygen",
                                    value = "$dissolvedOxygen mg/L",
                                    status = oxygenStatus,
                                    description = "Dissolved oxygen is vital for fish survival."
                                )
                            },
                            trendContent = {
                                Text("💨 Oxygen Trend", style = MaterialTheme.typography.bodyMedium)
                            }
                        )

                        FlippableSensorCardWithModal(
                            label = "Turbidity",
                            value = "$turbidity NTU",
                            status = turbidityStatus,
                            modifier = Modifier.weight(1f),
                            onLongPress = {
                                activeSensor = SensorData(
                                    label = "Turbidity",
                                    value = "$turbidity NTU",
                                    status = turbidityStatus,
                                    description = "Turbidity measures how clear or cloudy the water is."
                                )
                            },
                            trendContent = {
                                Text("🌫️ Turbidity Trend", style = MaterialTheme.typography.bodyMedium)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
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

                            data class SensorStatus(
                                val label: String,
                                val color: Color
                            )

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
                                    val v = temperature.toFloatOrNull() ?: 0f
                                    v to getSensorStatus("temperature", v)
                                }
                                label.contains("oxygen", ignoreCase = true) -> {
                                    val v = dissolvedOxygen.toFloatOrNull() ?: 0f
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


                                // Optional: Show description if you want
                                // Text(sensor.description, style = MaterialTheme.typography.bodySmall)


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
                                    contentPadding = PaddingValues(), // remove default padding
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp) // optional: consistent height
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(brush = gradientBrush, shape = RoundedCornerShape(8.dp))
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Close",
                                            color = Color.White
                                        )
                                    }
                                }

                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))





                    Text(
                        "Water Tank Status",
                        style = MaterialTheme.typography.titleMedium,
                        color = primaryTextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val context = LocalContext.current
                    val mediaPlayer = remember { MediaPlayer() }
                    val statusTextColor = when (evaluatedStatus.lowercase()) {
                        "normal", "good" -> Color(0xFF2E7D32)        // ✅ Green for "All good"
                        "caution" -> Color(0xFFFFA000)               // ✅ Yellow for "Caution"
                        "warning", "infected", "critical" -> Color(0xFFC62828) // ✅ Red for "Check water"
                        else -> Color.DarkGray
                    }


                    val waterLevelFloat = waterLevel.toFloatOrNull() ?: 0f

                    val waterLevelStatus = when {
                        waterLevelFloat < 20f -> "Low Water Level"
                        waterLevelFloat in 20f..50f -> "Sufficient"
                        else -> "Unknown"
                    }


                    DisposableEffect(Unit) {
                        onDispose {
                            mediaPlayer.release()
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 🔹 Water Level Card (new unique color)
                        FlippableSensorCardWithModal(
                            label = "Water Level",
                            value = "$waterLevel cm",
                            status = waterLevelStatus,
                            modifier = Modifier.weight(1f),
                            onLongPress = {
                                activeSensor = SensorData(
                                    label = "Water Level",
                                    value = "$waterLevel cm",
                                    status = waterLevelStatus,
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

                                    SensorRangeChart(sensorLabel = "Water Level", value = waterLevelFloat)
                                }
                            }
                        )


                        // 🔁 Tank Status Card (now flippable)
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
                                    horizontalAlignment = Alignment.CenterHorizontally // ✅ this centers the text
                                ) {
                                    val issues = listOfNotNull(
                                        temperature.toDoubleOrNull()?.let {
                                            if (it < 24.0) "Temp: Low" else if (it > 30.0) "Temp: High" else null
                                        },
                                        ph.toDoubleOrNull()?.let {
                                            if (it < 6.5) "pH: Low" else if (it > 8.5) "pH: High" else null
                                        },
                                        dissolvedOxygen.toDoubleOrNull()?.let {
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

                item {
                    // 🧾 Bottom Sheet: Detailed Tank Status
                    if (showSheet) {
                        ModalBottomSheet(
                            sheetState = sheetState,
                            onDismissRequest = { showSheet = false },
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {

                                // 🧊 Header
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.WaterDrop,
                                        contentDescription = "Water Tank Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Water Tank Details",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // 🟥 Status with accurate color
                                val tankStatusColor = when (evaluatedStatus.lowercase()) {
                                    "normal", "good" -> Color(0xFF2E7D32)        // Green
                                    "caution" -> Color(0xFF8D6E63)               // Brown
                                    "warning", "infected", "critical" -> Color(0xFFC62828) // Red
                                    else -> Color.DarkGray
                                }

                                Text(
                                    text = "Status: ${evaluatedStatus.replaceFirstChar { it.uppercaseChar() }}",
                                    color = tankStatusColor,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                // 🟨 Triggered issues in two lines if needed
                                if (triggeredParams.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Issues:", style = MaterialTheme.typography.bodySmall)

                                    val firstLine = triggeredParams.take(2).joinToString(", ")
                                    val secondLine = triggeredParams.drop(2).joinToString(", ")

                                    Text(text = firstLine, style = MaterialTheme.typography.bodySmall)
                                    if (secondLine.isNotEmpty()) {
                                        Text(text = secondLine, style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                // 💧 Water level
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Water Level: $waterLevel Liters",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                // ⏱️ Last updated
                                Text(
                                    text = "Last Updated: $currentTime",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // 📊 Water Level Chart
                                Text(
                                    text = "Water Level Range Reference",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val waterLevelValue = waterLevel.toFloatOrNull() ?: 0f
                                SensorRangeChart(sensorLabel = "Water Level", value = waterLevelValue)

                                Spacer(modifier = Modifier.height(24.dp))

                                // ✅ Button
                                Button(
                                    onClick = { showSheet = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(brush = gradientBrush, shape = RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Got it!",
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    if (isLoadingForecast) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp), // spacing above/below
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    else if (forecastError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = forecastError ?: "",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    else if (forecastData != null) {
                        ForecastResult(forecastData!!)
                    }
                }
            }
        }
    }
}




@Composable
fun StatusTag(label: String, status: String) {
    val color = when (status.lowercase()) {
        "too low", "too high" -> Color(0xFFFFA726) // orange
        "normal" -> Color(0xFF66BB6A)              // green
        else -> Color.LightGray
    }

    Box(
        modifier = Modifier
            .padding(2.dp)
            .background(color.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp) // tighter padding
    ) {
        Text(
            text = "$label: $status",
            color = Color.White,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}





fun getWeatherIconResource(iconCode: String?): Int {
    // If the iconCode is null or empty, return a default icon
    if (iconCode.isNullOrEmpty()) {
        return R.drawable.ic_default // Return a default icon in case of invalid iconCode
    }

    return when (iconCode) {
        "01d" -> R.drawable.ic_sunny // Sunny icon for day
        "02d" -> R.drawable.ic_sun_cloud // Sun with cloud icon for day
        "03d", "03n" -> R.drawable.ic_sun_cloud // Cloudy icon
        "04d", "04n" -> R.drawable.ic_sun_cloud // More overcast conditions
        "09d", "09n" -> R.drawable.ic_sun_rain // Sun with rain icon
        "10d", "10n" -> R.drawable.ic_rain // Rain icon
        "11d", "11n" -> R.drawable.ic_thunderstorm // Thunderstorm icon
        "13d", "13n" -> R.drawable.ic_snow // Snow icon
        else -> R.drawable.ic_default // Default or fallback icon for unexpected values
    }
}

@Composable
fun rememberClickSoundPlayer(): () -> Unit {
    val context = LocalContext.current
    return remember {
        {
            val player = MediaPlayer.create(context, R.raw.tap)
            player.setOnCompletionListener {
                it.release()
            }
            player.start()
        }
    }
}



@Composable
fun ForecastResult(data: ForecastResponse) {
    var isFiveDayForecast by remember { mutableStateOf(true) }

    val primaryTextColor = MaterialTheme.colorScheme.onBackground

    val playClickSound = rememberClickSoundPlayer()

    var expanded by remember { mutableStateOf(false) }

// Determine target width
    val targetWidth = if (expanded) 220.dp else 80.dp

// Animate the width smoothly
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "cardWidth"
    )

    val groupedForecast = if (isFiveDayForecast) {
        data.list
            .groupBy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.dt * 1000)) }
            .map { it.value.first() }
    } else {
        data.list.take(24)
    }

    fun convertTemperature(celsius: Float): String {
        return "${celsius.roundToInt()}°C"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(15.dp))

        // Header Row (title + toggle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isFiveDayForecast) "5-Day Weather Forecast" else "Hourly Weather Forecast",
                style = MaterialTheme.typography.titleMedium,
                color = primaryTextColor
            )

            Text(
                text = if (isFiveDayForecast) "See Hourly" else "See 5-Day",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFA0A7BA),
                modifier = Modifier.clickable {
                    isFiveDayForecast = !isFiveDayForecast
                }
            )
        }


        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal forecast cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            groupedForecast.forEachIndexed { index, forecast ->

                val date = Date(forecast.dt * 1000)
                val calendarForecast = Calendar.getInstance().apply { time = date }
                val calendarToday = Calendar.getInstance()

                val isToday =
                    calendarForecast.get(Calendar.YEAR) == calendarToday.get(Calendar.YEAR) &&
                            calendarForecast.get(Calendar.DAY_OF_YEAR) == calendarToday.get(Calendar.DAY_OF_YEAR)

                val forecastHour =
                    Calendar.getInstance().apply { timeInMillis = forecast.dt * 1000 }
                        .get(Calendar.HOUR_OF_DAY)
                val forecastDay =
                    Calendar.getInstance().apply { timeInMillis = forecast.dt * 1000 }
                        .get(Calendar.DAY_OF_YEAR)

                val now = Calendar.getInstance()
                val currentHour = now.get(Calendar.HOUR_OF_DAY)
                val currentDay = now.get(Calendar.DAY_OF_YEAR)

                val highlightToday = if (isFiveDayForecast) {
                    isToday || (index == 0)
                } else {
                    forecastHour == currentHour && forecastDay == currentDay
                }

                val isDarkTheme = isSystemInDarkTheme()
                val backgroundColor = if (highlightToday) {
                    Brush.linearGradient(listOf(Color(0xFF67E1D2), Color(0xFF54A8FF)))
                } else {
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.LightGray.copy(alpha = 0.85f)
                        )
                    )
                }

                val borderStroke = if (!highlightToday) BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.5f)
                ) else null

                val textColor = if (highlightToday) Color.White else Color.Black

                var expanded by remember { mutableStateOf(false) }

                val targetWidth = if (expanded) 220.dp else 80.dp
                val animatedWidth by animateDpAsState(
                    targetValue = targetWidth,
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    ),
                    label = "cardWidth"
                )

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(animatedWidth)
                        .height(225.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(40.dp),
                            ambientColor = Color.Black.copy(alpha = 0.25f),
                            spotColor = Color.Black.copy(alpha = 0.3f)
                        )
                        .background(
                            brush = backgroundColor,
                            shape = RoundedCornerShape(40.dp)
                        )
                        .clip(RoundedCornerShape(40.dp))
                        .then(
                            if (borderStroke != null) Modifier.border(
                                borderStroke,
                                RoundedCornerShape(40.dp)
                            )
                            else Modifier
                        )
                        .clickable {
                            expanded = !expanded
                        }
                ) {
                    Card(
                        shape = RoundedCornerShape(40.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = FastOutSlowInEasing
                                )
                            )
                    ) {
                        if (expanded) {
                            // Expanded View
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isFiveDayForecast) {
                                            SimpleDateFormat("EEE", Locale.getDefault()).format(date)
                                        } else {
                                            SimpleDateFormat("hh a", Locale.getDefault()).format(date)
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = textColor
                                    )
                                    Text(
                                        text = SimpleDateFormat("MM/dd", Locale.getDefault()).format(date),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textColor
                                    )
                                    if (forecast.weather.isNotEmpty()) {
                                        val iconCode = forecast.weather[0].icon
                                        val painter = painterResource(id = getWeatherIconResource(iconCode))
                                        Image(
                                            painter = painter,
                                            contentDescription = "Weather Icon",
                                            modifier = Modifier
                                                .size(50.dp)
                                                .padding(top = 4.dp)
                                        )
                                        Text(
                                            text = forecast.weather[0].description.replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textColor,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🌡️ Temp", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = textColor)
                                        Text(convertTemperature(forecast.main.temp), style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp), color = textColor)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("💧 Humidity", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = textColor)
                                        Text("${forecast.main.humidity}%", style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp), color = textColor)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🌬️ Wind", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = textColor)
                                        Text("${forecast.wind.speed} m/s", style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp), color = textColor)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🔽 Pressure", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = textColor)
                                        Text("${forecast.main.pressure} hPa", style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp), color = textColor)
                                    }
                                }
                            }
                        } else {
                            // Collapsed View
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text(
                                    text = if (isFiveDayForecast)
                                        SimpleDateFormat("EEE", Locale.getDefault()).format(date)
                                    else
                                        SimpleDateFormat("hh a", Locale.getDefault()).format(date),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textColor
                                )
                                Text(
                                    text = SimpleDateFormat("MM/dd", Locale.getDefault()).format(date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor
                                )
                                if (forecast.weather.isNotEmpty()) {
                                    val iconCode = forecast.weather[0].icon
                                    val painter = painterResource(id = getWeatherIconResource(iconCode))
                                    Image(
                                        painter = painter,
                                        contentDescription = "Weather Icon",
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Text(
                                    text = convertTemperature(forecast.main.temp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor
                                )
                                Text(
                                    text = forecast.weather.firstOrNull()?.description ?: "N/A",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun SensorCard(
    label: String,
    value: String,
    status: String,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val labelFontSize = when {
        screenWidth < 340 -> 12.sp
        screenWidth < 400 -> 14.sp
        else -> 16.sp
    }

    val valueFontSize = when {
        screenWidth < 340 -> 16.sp
        screenWidth < 400 -> 20.sp
        else -> 24.sp
    }

    val statusFontSize = when {
        screenWidth < 340 -> 10.sp
        screenWidth < 400 -> 12.sp
        else -> 14.sp
    }

    val statusColor = when (status.lowercase()) {
        "optimal", "balanced", "adequate", "clear", "sufficient", "all good" -> Color(0xFF2E7D32)
        "too low", "too high", "acidic", "alkaline", "low oxygen", "murky", "low water level", "check water" -> Color(0xFFD32F2F)
        "unknown" -> Color(0xFF616161)
        else -> Color(0xFFEF6C00)
    }


    val (icon, baseColor, deepColor) = when (label.lowercase()) {
        "temperature" -> Triple(Icons.Default.Thermostat, Color(0xFFB3E5FC), Color(0xFF4FC3F7))
        "ph level" -> Triple(Icons.Default.Science, Color(0xFFFFE0B2), Color(0xFFFFB74D))
        "dissolved oxygen" -> Triple(Icons.Default.BubbleChart, Color(0xFFC8E6C9), Color(0xFF81C784))
        "turbidity" -> Triple(Icons.Default.BlurOn, Color(0xFFE1BEE7), Color(0xFFCE93D8))
        "water level" -> Triple(Icons.Default.Water, Color(0xFFB2EBF2), Color(0xFF00ACC1)) // Aqua Mint

        "tank status" -> {
            val (statusBase, statusDeep) = when (value.lowercase()) {
                "sufficient", "optimal", "good", "normal" -> Pair(Color(0xFFB2DFDB), Color(0xFF4DB6AC))
                "warning", "infected", "low", "too low", "critical" -> Pair(Color(0xFFFFCDD2), Color(0xFFEF5350))
                "high", "too high", "overflow" -> Pair(Color(0xFFD1C4E9), Color(0xFF7E57C2))
                "unknown" -> Pair(Color(0xFFCFD8DC), Color(0xFF90A4AE))
                else -> Pair(Color(0xFFFFECB3), Color(0xFFFFC107))
            }
            Triple(Icons.Default.Warning, statusBase, statusDeep)
        }
        else -> Triple(Icons.Default.Info, Color(0xFFE0E0E0), Color(0xFFBDBDBD))
    }


    val iconTint = baseColor.copy(alpha = 1f).compositeOver(Color.Black.copy(alpha = 0.5f))

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(100.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(baseColor, deepColor),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.Black
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = statusColor
                    )
                }

                Icon(
                    imageVector = icon,
                    contentDescription = "$label icon",
                    tint = iconTint,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SensorCardBack(
    label: String,
    sensorValue: String,
    modifier: Modifier = Modifier,
    viewModel: WaterDataViewModel = viewModel(),
    trendContent: @Composable (() -> Unit)? = null
) {
    val (icon, baseColor, deepColor) = when (label.lowercase()) {
        "temperature" -> Triple(Icons.Default.Thermostat, Color(0xFFB3E5FC), Color(0xFF4FC3F7))
        "ph level" -> Triple(Icons.Default.Science, Color(0xFFFFE0B2), Color(0xFFFFB74D))
        "dissolved oxygen" -> Triple(Icons.Default.BubbleChart, Color(0xFFC8E6C9), Color(0xFF81C784))
        "turbidity" -> Triple(Icons.Default.BlurOn, Color(0xFFE1BEE7), Color(0xFFCE93D8))
        "water level" -> Triple(Icons.Default.Water, Color(0xFFB2EBF2), Color(0xFF00ACC1)) // Aqua Mint

        "tank status" -> {
            val (statusBase, statusDeep) = when (sensorValue.lowercase()) {
                "sufficient", "optimal", "good", "normal" -> Pair(Color(0xFFB2DFDB), Color(0xFF4DB6AC))
                "warning", "infected", "low", "too low", "critical" -> Pair(Color(0xFFFFCDD2), Color(0xFFEF5350))
                "high", "too high", "overflow" -> Pair(Color(0xFFD1C4E9), Color(0xFF7E57C2))
                "unknown" -> Pair(Color(0xFFCFD8DC), Color(0xFF90A4AE))
                else -> Pair(Color(0xFFFFECB3), Color(0xFFFFC107))
            }
            Triple(Icons.Default.Warning, statusBase, statusDeep)
        }
        else -> Triple(Icons.Default.Info, Color(0xFFE0E0E0), Color(0xFFBDBDBD))
    }


    val cardHeight = 100.dp

    val dataPoints by when (label.lowercase()) {
        "temperature" -> viewModel.temperatureHistory.collectAsState()
        "ph level" -> viewModel.phHistory.collectAsState()
        "dissolved oxygen" -> viewModel.oxygenHistory.collectAsState()
        "turbidity" -> viewModel.turbidityHistory.collectAsState()
        else -> remember { mutableStateOf(emptyList()) }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(cardHeight)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(baseColor, deepColor),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // 🟡 Watermark icon (centered background)
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .padding(top = 20.dp)
            )

            // 🔵 Title stays pinned to the top
            Text(
                text = if (label.lowercase() == "tank status") "Tank Status" else "$label Trend",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp
                ),
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            )



            // 🟣 Conditional content: either chart or fallback text (centered)
            if (label.lowercase() == "tank status" && trendContent != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp, start = 8.dp, end = 8.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    trendContent()
                }
            } else if (dataPoints.size > 1) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp, start = 8.dp, end = 8.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    SensorTrendLineChart(dataPoints = dataPoints)
                }
            } else {
                Text(
                    text = "No trend data available",
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 20.dp)
                )
            }


        }
    }
}






@Composable
fun SensorTrendLineChart(dataPoints: List<Float>) {
    val maxVal = dataPoints.maxOrNull() ?: 1f
    val minVal = dataPoints.minOrNull() ?: 0f

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(800))
    }

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .padding(horizontal = 8.dp)) {

        val chartWidth = size.width
        val chartHeight = size.height
        val step = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

        val points = dataPoints.mapIndexed { index, value ->
            val x = index * step
            val y = chartHeight - ((value - minVal) / (maxVal - minVal + 0.0001f)) * chartHeight
            Offset(x, y)
        }

        val animatedPoints = points.mapIndexed { i, pt ->
            if (i == 0) pt
            else {
                val prev = points[i - 1]
                Offset(
                    x = prev.x + (pt.x - prev.x) * animatedProgress.value,
                    y = prev.y + (pt.y - prev.y) * animatedProgress.value
                )
            }
        }

        val path = Path().apply {
            animatedPoints.forEachIndexed { i, point ->
                if (i == 0) moveTo(point.x, point.y)
                else lineTo(point.x, point.y)
            }
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo(points.last().x, chartHeight)
            lineTo(points.first().x, chartHeight)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent))
        )

        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlippableSensorCardWithModal(
    label: String,
    value: String,
    status: String,
    statusColor: Color = Color.Black,
    modifier: Modifier = Modifier,
    onLongPress: () -> Unit,
    trendContent: @Composable () -> Unit
) {
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer.release() }
    }

    var flipped by remember { mutableStateOf(false) }

    val rotationAnim by animateFloatAsState(
        targetValue = if (flipped) -180f else 0f, // Flip clockwise
        animationSpec = tween(durationMillis = 500),
        label = "rotationAnim"
    )

    val cameraDistance = with(LocalDensity.current) { 8.dp.toPx() }
    val frontVisible = rotationAnim >= -90f

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotationAnim
                this.cameraDistance = cameraDistance
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        try {
                            mediaPlayer.reset()
                            val afd = context.resources.openRawResourceFd(R.raw.drop)
                            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                            afd.close()
                            mediaPlayer.prepare()
                            mediaPlayer.start()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        flipped = !flipped
                    },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        if (frontVisible) {
            SensorCard(
                label = label,
                value = value,
                status = status,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            SensorCardBack(
                label = label,
                sensorValue = value,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = -180f
                        this.cameraDistance = cameraDistance
                        alpha = if (!frontVisible) 1f else 0f
                    },
                trendContent = trendContent // ✅ Now passed through
            )
        }
    }
}


class TiltSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

    private val _tiltX = MutableStateFlow(0f)
    private val _tiltY = MutableStateFlow(0f)

    val tiltX = _tiltX.asStateFlow()
    val tiltY = _tiltY.asStateFlow()

    fun startListening() {
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_UI)
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GRAVITY) {
            _tiltX.value = event.values[0] // left/right
            _tiltY.value = event.values[1] // forward/back
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}


@Composable
fun rememberTiltSensor(): TiltSensorManager {
    val context = LocalContext.current
    val sensorManager = remember { TiltSensorManager(context) }

    DisposableEffect(Unit) {
        sensorManager.startListening()
        onDispose {
            sensorManager.stopListening()
        }
    }
    return sensorManager
}




class SoundPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun play() {
        // If a sound is already playing, stop and release it
        mediaPlayer?.release()

        // Load and play the bubble sound
        mediaPlayer = MediaPlayer.create(context, R.raw.blop)
        mediaPlayer?.start()
    }
}


data class FoamParticle(
    var x: Float,
    var y: Float,
    var radius: Float,
    var alpha: Float,
    var dx: Float,
    var dy: Float
)


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BouncyButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "scaleAnim"
    )

    BoxWithConstraints {
        val screenWidth = maxWidth.value
        val fontSize = when {
            screenWidth < 360f -> 12.sp
            screenWidth < 420f -> 14.sp
            else -> 16.sp
        }

        val gradientBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF5DCCFC), // bright cyan
                Color(0xFF007EF2)  // deep aqua blue – creates a real gradient feel
            ),
            start = Offset(0f, 0f),
            end = Offset.Infinite
        )




        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .defaultMinSize(minWidth = 120.dp),
            contentPadding = PaddingValues() // remove inner default spacing
        ) {
            Box(
                modifier = Modifier
                    .background(brush = gradientBrush, shape = RoundedCornerShape(25.dp))
                    .padding(horizontal = 25.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "View Details",
                    fontSize = fontSize,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishStatusCard(darkTheme: Boolean,onItemSelected: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()


    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val dbRef = remember(uid) {
        FirebaseDatabase.getInstance().getReference("users/$uid/scans")
    }

    var latestStatus by remember { mutableStateOf("Loading...") }

    LaunchedEffect(uid) {
        if (uid != null) {
            dbRef.orderByChild("timestamp").limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val latest = snapshot.children.firstOrNull()
                        val status = latest?.child("status")?.getValue(String::class.java)
                        latestStatus = status ?: "Unknown"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        latestStatus = "Unknown"
                    }
                })
        } else {
            latestStatus = "Unknown"
        }
    }

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

    // Adaptive wave colors based on theme
    val waveColor1 = if (isDarkTheme) Color(0xFF4BA3C7).copy(alpha = 0.3f) else Color(0xFF4BA3C7).copy(alpha = 0.5f)
    val waveColor2 = if (isDarkTheme) Color(0xFF5DCCFC).copy(alpha = 0.2f) else Color(0xFF5DCCFC).copy(alpha = 0.4f)
    val waveColor3 = if (isDarkTheme) Color(0xFFB3E5FC).copy(alpha = 0.15f) else Color(0xFFB3E5FC).copy(alpha = 0.3f)
    val waveColor4 = if (isDarkTheme) Color(0xFFE1F5FE).copy(alpha = 0.1f) else Color(0xFFE1F5FE).copy(alpha = 0.25f)

    Box(
        modifier = Modifier
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
    )
    {
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
        val configuration = LocalConfiguration.current
        val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
        val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
        val screenWidth = configuration.screenWidthDp


        val context = LocalContext.current
        val soundPlayer = remember { SoundPlayer(context) }


        val tiltSensor = rememberTiltSensor()
        val rawTiltX by tiltSensor.tiltX.collectAsState()
        val rawTiltY by tiltSensor.tiltY.collectAsState()


        val smoothTiltX by animateFloatAsState(targetValue = rawTiltX, label = "smoothTiltX")
        val smoothTiltY by animateFloatAsState(targetValue = rawTiltY, label = "smoothTiltY")

        val clampedTiltX = smoothTiltX.coerceIn(-8f, 8f)
        val clampedTiltY = smoothTiltY.coerceIn(-8f, 8f)

        LaunchedEffect(Unit) {
            while (true) {
                delay(40L)
                foamParticles = foamParticles.map {
                    var newX = it.x + it.dx + clampedTiltX * 0.3f
                    var newY = it.y + it.dy + clampedTiltY * 0.2f
                    val newAlpha = (it.alpha - 0.005f).coerceAtLeast(0f)

                    // Respawn particle if it fades
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


        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // ✅ Stronger opposite tilt like real water
                    rotationZ = clampedTiltX * 1.8f  // Amplified effect
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    clip = false
                }
        ) {
            val extendedWidth = size.width * 1.5f  // ✅ Draw beyond screen left/right
            val extendedHeight = size.height + 600f  // ✅ Extend downward to avoid clipping

            val waveVerticalShift = clampedTiltY * 10f  // ✅ Stronger up/down motion

            tapRipples.forEachIndexed { i, ripple ->
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

                path.moveTo(-extendedWidth / 4f, baseY)  // ✅ Start before 0 for extended left

                var x = -extendedWidth / 4f
                while (x <= extendedWidth) {
                    val angle = (x / extendedWidth) * 2 * Math.PI.toFloat() * frequency + phase
                    val y = baseY + amplitude * kotlin.math.sin(angle)
                    path.lineTo(x, y)
                    x += step
                }

                // ✅ Fill extended bottom
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

                // Determine emoji and color based on status
                val (emoji, statusColor) = when {
                    latestStatus.trim().equals("Healthy", true) -> "🟢" to Color(0xFF43A047)
                    latestStatus.trim().startsWith("Infected", true) -> "🔴" to Color(0xFFE53935)
                    latestStatus.trim().equals("No Fish", true) -> "⚪" to MaterialTheme.colorScheme.onSurface
                    latestStatus.trim().equals("Unknown", true) -> "❓" to Color(0xFFFB8C00)
                    else -> "🐟" to MaterialTheme.colorScheme.onSurface
                }

                val isLoading = latestStatus.trim().lowercase() == "loading..." || latestStatus.trim().lowercase() == "loading" || latestStatus.trim().lowercase() == "no data" || latestStatus.trim().lowercase() == "unknown"


                val responsiveFontSize = when {
                    screenWidth < 380 -> 16.sp
                    screenWidth < 400 -> 18.sp
                    else -> 22.sp
                }

                val statusFontSize = if (isLoading) {
                    responsiveFontSize
                } else {
                    (responsiveFontSize.value + 4).sp
                }

                Text(
                    text = "$emoji ${latestStatus.trim().replaceFirstChar { it.uppercase() }}",
                    fontSize = statusFontSize,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    maxLines = 1
                )






                Spacer(modifier = Modifier.height(5.dp))




                BouncyButton(onClick = { showDialog = true })



            }

            val fishOffsetY by rememberInfiniteTransition(label = "fishFloat").animateFloat(
                initialValue = -10f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "fishOffsetY"
            )

            var rotation by remember { mutableStateOf(0f) }
            val animatedRotation by animateFloatAsState(
                targetValue = rotation,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
                label = "fishSpin"
            )

            val fishImageRes = when {
                latestStatus.trim().startsWith("Infected", true) -> R.drawable.hito_icon_infected1
                latestStatus.trim().equals("Healthy", true) -> R.drawable.hito_icon
                else -> R.drawable.hito_icon
            }

            val fishSize = when {
                screenWidth < 340 -> 100.dp
                screenWidth < 380 -> 120.dp
                else -> 150.dp
            }

            Image(
                painter = painterResource(id = fishImageRes),
                contentDescription = "Fish Status Image",
                modifier = Modifier
                    .size(fishSize)
                    .offset(y = fishOffsetY.dp)
                    .graphicsLayer {
                        rotationZ = animatedRotation
                        translationY = clampedTiltY * 2f
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        soundPlayer.play()
                        rotation += 360f  // spin once
                    }

            )




        }
    }

    @Composable
    fun ModalSheet(
        sheetState: SheetState,
        onDismiss: () -> Unit,
        content: @Composable ColumnScope.() -> Unit
    ) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                content()
            }
        }
    }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF5DCCFC), // bright cyan
            Color(0xFF007EF2)  // deep aqua blue – creates a real gradient feel
        ),
        start = Offset(0f, 0f),
        end = Offset.Infinite
    )







    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showDialog) {
        val detailsText = when {
            latestStatus.trim().equals("Healthy", true) -> "The fish are healthy. No signs of infection detected in the latest scan. Continue regular monitoring."
            latestStatus.trim().startsWith("Infected", true) -> "Signs of fungal infection have been detected. Immediate action is recommended — isolate affected fish and treat the aquaculture environment."
            latestStatus.trim().equals("No Fish", true) -> "No fish were detected in the scan. Please try scanning again with a clearer image or check your diagnosis history."
            latestStatus.trim().equals("Unknown", true) -> "The fish status could not be determined. Please perform a scan or review your diagnosis history for more information."
            else -> "Status information is unavailable or not recognized."
        }

        val (emoji, statusColor) = when {
            latestStatus.trim().equals("Healthy", true) -> "🟢" to Color(0xFF43A047)
            latestStatus.trim().startsWith("Infected", true) -> "🔴" to Color(0xFFE53935)
            latestStatus.trim().equals("No Fish", true) -> "⚪" to MaterialTheme.colorScheme.onSurface
            latestStatus.trim().equals("Unknown", true) -> "❓" to MaterialTheme.colorScheme.onSurface
            else -> "❓" to MaterialTheme.colorScheme.onSurface
        }

        ModalSheet(
            sheetState = sheetState,
            onDismiss = { showDialog = false }
        ) {
            Text(
                text = "$emoji ${latestStatus.trim().replaceFirstChar { it.uppercase() }}",
                color = statusColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = detailsText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    showDialog = false
                    onItemSelected("records")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(), // removes default padding
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = gradientBrush, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "View Diagnosis History",
                        color = Color.White
                    )
                }
            }
        }
    }
}








@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalPagerApi::class)
@Composable
fun FishStatusCarouselPager(
    darkTheme: Boolean,
    onItemSelected: (String) -> Unit
) {
    val pagerState = rememberPagerState()
    val cards = listOf("Main", "Summary", /*"One",*/ "Two", /*"Three"*/)
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenWidthDp = configuration.screenWidthDp
    val responsiveBoxHeight = when {
        screenWidthDp < 340 -> 170.dp
        screenWidthDp < 380 -> 190.dp
        screenWidthDp < 420 -> 210.dp
        else -> 230.dp
    }

    var scanHistory by remember { mutableStateOf<List<FishScanRecord>>(emptyList()) }
    var lastScanDate by remember { mutableStateOf("N/A") }

    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "admin"
    val db = FirebaseDatabase.getInstance()

    LaunchedEffect(uid) {
        db.getReference("users/$uid/scans")
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val history = mutableListOf<FishScanRecord>()
                    var latestTimestamp = 0L

                    snapshot.children.forEach { scan ->
                        val status = scan.child("status").getValue(String::class.java) ?: ""
                        val rawTimestamp = scan.child("timestamp").getValue(Long::class.java) ?: 0L
                        val correctedTimestamp = if (rawTimestamp < 1000000000000L) rawTimestamp * 1000 else rawTimestamp

                        val healthy = if (status.equals("Healthy", true)) 1 else 0
                        val infected = if (status.startsWith("Infected", true)) 1 else 0
                        val noFish = if (status.equals("No Fish", true)) 1 else 0

                        history.add(FishScanRecord(healthy, infected, noFish))

                        if (correctedTimestamp > latestTimestamp) {
                            latestTimestamp = correctedTimestamp
                        }
                    }

                    scanHistory = history
                    lastScanDate = if (latestTimestamp > 0) {
                        SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(latestTimestamp))
                    } else {
                        "N/A"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FishSummary", "Failed to load history", error.toException())
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            count = cards.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(responsiveBoxHeight)
        ) { page ->
            Box(
                modifier = Modifier
                    .width(screenWidth)
                    .height(responsiveBoxHeight)
            ) {
                when (cards[page]) {
                    "Main" -> FishStatusCard(darkTheme = darkTheme, onItemSelected = onItemSelected)
                    "Summary" -> FishSummaryCard(
                        scanHistory = scanHistory
                    )
                    //"One" -> FillerCardOne()
                    "Two" -> FishDiseaseGuideCard()
                    "Three" -> FillerCardThree()
                }
            }
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            activeColor = MaterialTheme.colorScheme.primary,
            inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            indicatorWidth = 4.dp,
            indicatorHeight = 4.dp,
            spacing = 4.dp
        )
    }
}



// Simple scan record model
data class FishScanRecord(val healthy: Int, val infected: Int, val noFish: Int)
@Composable
fun FishSummaryCard(scanHistory: List<FishScanRecord>) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenWidth = screenWidthDp.dp
    val responsiveBoxHeight = when {
        screenWidthDp < 340 -> 170.dp
        screenWidthDp < 380 -> 190.dp
        screenWidthDp < 420 -> 210.dp
        else -> 230.dp
    }


    val scope = rememberCoroutineScope()
    val tapRipples = remember { mutableStateListOf<Animatable<Float, AnimationVector1D>>() }
    var tapOffset by remember { mutableStateOf(Offset.Zero) }

    val total = scanHistory.fold(FishScanRecord(0, 0, 0)) { acc, record ->
        FishScanRecord(
            healthy = acc.healthy + record.healthy,
            infected = acc.infected + record.infected,
            noFish = acc.noFish + record.noFish
        )
    }

    val totalScanned = total.healthy + total.infected + total.noFish
    val infectionRate = if (totalScanned > 0) (total.infected * 100 / totalScanned) else 0

    val colors = listOf(Color(0xFF66BB6A), Color(0xFFEF5350), Color.LightGray)

    Box(
        modifier = Modifier
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
        // Background GIF
        Image(
            painter = AnimatedGifPainter(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Blurred overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(16.dp)
                .background(Color(0xAA222222))
        )

        // Ripple effect
        Canvas(modifier = Modifier.matchParentSize()) {
            tapRipples.forEachIndexed { i, ripple ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f - i * 0.05f),
                    radius = ripple.value,
                    center = tapOffset,
                    style = Stroke(width = 2f + i)
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Fish Health Summary",
                color = Color.White,
                fontSize = when {
                    screenWidthDp < 360 -> 12.sp
                    screenWidthDp < 400 -> 13.sp
                    else -> 14.sp
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DonutChart(
                    total,
                    modifier = Modifier
                        .size(if (screenWidthDp < 360) 50.dp else 65.dp),
                    colors = colors
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SummaryBar("Healthy", total.healthy, totalScanned, colors[0])
                    SummaryBar("Infected", total.infected, totalScanned, colors[1])
                    SummaryBar("No Fish", total.noFish, totalScanned, colors[2])
                    Text(
                        text = "Infection Rate: $infectionRate%",
                        fontSize = if (screenWidthDp < 360) 10.sp else 12.sp,
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Last Scan: ${LocalDate.now()}",
                    color = Color.Gray,
                    fontSize = 8.sp
                )
                Text(
                    text = "Total Fish Scanned: $totalScanned",
                    color = Color.Gray,
                    fontSize = 8.sp
                )
            }
        }
    }
}


@Composable
fun SummaryBar(label: String, count: Int, total: Int, color: Color) {
    val percent = if (total > 0) count.toFloat() / total else 0f
    val barWidth = percent.coerceIn(0f, 1f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            modifier = Modifier.width(60.dp)
        )

        Box(
            modifier = Modifier
                .height(8.dp)
                .weight(1f)
                .background(Color.DarkGray, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(barWidth)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "$count",
            color = Color.White,
            fontSize = 12.sp
        )
    }
}


@Composable
fun DonutChart(data: FishScanRecord, modifier: Modifier = Modifier, colors: List<Color>) {
    val sweepAngles = listOf(
        data.healthy.toFloat(),
        data.infected.toFloat(),
        data.noFish.toFloat()
    )
    val total = sweepAngles.sum().takeIf { it > 0 } ?: 1f

    Canvas(
        modifier = modifier.size(80.dp),
        onDraw = {
            var startAngle = -90f
            val strokeWidth = 16.dp.toPx()
            val radius = size.minDimension / 2 - strokeWidth / 2
            val center = Offset(size.width / 2, size.height / 2)

            sweepAngles.forEachIndexed { i, value ->
                val angle = (value / total) * 360f
                drawArc(
                    color = colors[i],
                    startAngle = startAngle,
                    sweepAngle = angle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += angle
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun AnimatedGifPainter(): Painter {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components { add(ImageDecoderDecoder.Factory()) }
        .build()

    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data("file:///android_asset/ponyo.gif")
            .build(),
        imageLoader = imageLoader
    )
}



@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FillerCardOne() {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val scope = rememberCoroutineScope()
    val tapRipples = remember { mutableStateListOf<Animatable<Float, AnimationVector1D>>() }
    var tapOffset by remember { mutableStateOf(Offset.Zero) }

    val responsiveBoxHeight = when {
        screenWidthDp < 340 -> 170.dp
        screenWidthDp < 380 -> 190.dp
        screenWidthDp < 420 -> 210.dp
        else -> 230.dp
    }

    Box(
        modifier = Modifier
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
        // Background GIF
        Image(
            painter = AnimatedGifPainterOne(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Blur overlay
        Box(
            modifier = Modifier
                .matchParentSize()
//                .blur(16.dp)
//                .background(Color(0xAA222222))
        )

        // Ripple animation
        Canvas(modifier = Modifier.matchParentSize()) {
            tapRipples.forEachIndexed { i, ripple ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f - i * 0.05f),
                    radius = ripple.value,
                    center = tapOffset,
                    style = Stroke(width = 2f + i)
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

        }
    }
}


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun AnimatedGifPainterOne(): Painter {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components { add(ImageDecoderDecoder.Factory()) }
        .build()

    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data("file:///android_asset/ilog.gif")
            .build(),
        imageLoader = imageLoader
    )
}







// Disease information data class
data class DiseaseInfo(
    val name: String,
    val emoji: String,      // keep this for the card row
    val imageRes: Int,      // use this in the modal
    val scientificName: String,
    val description: String,
    val symptoms: String,
    val treatment: String
)


@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FishDiseaseGuideCard() {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val scope = rememberCoroutineScope()
    val tapRipples: MutableList<Animatable<Float, AnimationVector1D>> = remember { mutableStateListOf() }
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    var showDiseaseModal by remember { mutableStateOf(false) }
    var selectedDisease by remember { mutableStateOf<DiseaseInfo?>(null) }

    val responsiveBoxHeight = when {
        screenWidthDp < 340 -> 170.dp
        screenWidthDp < 380 -> 190.dp
        screenWidthDp < 420 -> 210.dp
        else -> 230.dp
    }

    val diseases = listOf(
        DiseaseInfo(
            name = "Cotton",
            emoji = "🦠",
            imageRes = R.drawable.ic_cotton,
            scientificName = "Saprolegnia spp.",
            description = "Fungal infection causing cotton-like growth",
            symptoms = "Cotton-like growth on skin or fins",
            treatment = "Antifungal medication, improve water quality"
        ),
        DiseaseInfo(
            name = "White Patch",
            emoji = "⚪",
            imageRes = R.drawable.ic_whitepatch,
            scientificName = "White spot disease",
            description = "White spot disease is a common parasitic infection in fish",
            symptoms = "Small white spots on body and fins, fish may scratch against objects",
            treatment = "Increase water temperature, use anti-parasitic medication"
        ),
        DiseaseInfo(
            name = "Reddish",
            emoji = "🔴",
            imageRes = R.drawable.ic_reddish,
            scientificName = "Bacterial Infection",
            description = "Bacterial infection causing red patches on fish skin",
            symptoms = "Red patches or sores on body, lethargic behavior",
            treatment = "Antibiotic treatment, improve water quality"
        ),
        DiseaseInfo(
            name = "Ulcer",
            emoji = "🟤",
            imageRes = R.drawable.ic_ulcer,
            scientificName = "Bacterial Ulcer",
            description = "Open wounds with bacterial infection",
            symptoms = "Open wounds or ulcers, weight loss and poor appetite",
            treatment = "Antibiotic treatment, wound care, isolate affected fish"
        )
    )



    Box(
        modifier = Modifier
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
        // Background GIF
        Image(
            painter = AnimatedGifPainterTwo(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Blurred overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(16.dp)
                .background(Color(0xAA222222))
        )

        // Ripple animation
        Canvas(modifier = Modifier.matchParentSize()) {
            tapRipples.forEachIndexed { i, ripple ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f - i * 0.05f),
                    radius = ripple.value,
                    center = tapOffset,
                    style = Stroke(width = 2f + i)
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Top section with title - matching FishSummaryCard style
            Text(
                "Fish Disease Guide",
                color = Color.White,
                fontSize = when {
                    screenWidthDp < 360 -> 12.sp
                    screenWidthDp < 400 -> 13.sp
                    else -> 14.sp
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )


            Text(
                "Quick tips on spotting and preventing common fish diseases.",
                color = Color.LightGray,
                fontSize = if (screenWidthDp < 360) 8.sp else 10.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Individual disease cards - centered with spacing, positioned higher
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    items(diseases) { disease ->
                        Card(
                            modifier = Modifier
                                .width(80.dp)
                                .height(70.dp)
                                .clickable {
                                    selectedDisease = disease
                                    showDiseaseModal = true
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = disease.emoji,
                                    fontSize = 18.sp
                                )

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = disease.name,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Disease Information Bottom Modal
    if (showDiseaseModal && selectedDisease != null) {
        val disease = selectedDisease!!
        ModalBottomSheet(
            onDismissRequest = { showDiseaseModal = false },
            sheetState = rememberModalBottomSheetState(),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Disease Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = disease.emoji,
                        fontSize = 48.sp
                    )

                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = disease.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = disease.scientificName,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }


                Spacer(modifier = Modifier.height(24.dp))


                val showFullImageDialog = remember { mutableStateOf(false) }

// Disease image preview
                Image(
                    painter = painterResource(id = disease.imageRes),
                    contentDescription = "${disease.name} image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showFullImageDialog.value = true },
                    contentScale = ContentScale.Crop
                )


                if (showFullImageDialog.value) {
                    Dialog(
                        onDismissRequest = { showFullImageDialog.value = false },
                        properties = DialogProperties(
                            dismissOnClickOutside = true,
                            usePlatformDefaultWidth = false
                        )
                    ) {
                        // Fullscreen overlay that dismisses on outside click
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.05f)) // dim background (optional)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    showFullImageDialog.value = false // dismiss on outside click
                                }
                                .padding(horizontal = 25.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            var scale by remember { mutableStateOf(1f) }
                            val maxScale = 5f
                            val minScale = 1f

                            val zoomModifier = Modifier
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, _, zoomChange, _ ->
                                        scale = (scale * zoomChange).coerceIn(minScale, maxScale)
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            scale = if (scale > 1f) 1f else 2f
                                        }
                                    )
                                }

                            // Prevent clicks on the image box from dismissing
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Black)
                                    .then(zoomModifier)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { /* block outside dismiss when image clicked */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = disease.imageRes),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale
                                        ),
                                    contentScale = ContentScale.Crop
                                )

                                IconButton(
                                    onClick = { showFullImageDialog.value = false },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }



                Spacer(modifier = Modifier.height(16.dp))

                // Disease Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Description",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = disease.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Symptoms
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🚨 Symptoms",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = disease.symptoms,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Treatment
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "💊 Treatment",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = disease.treatment,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Close Button
                Button(
                    onClick = { showDiseaseModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF5DCCFC), // bright cyan
                                        Color(0xFF007EF2)  // deep aqua blue
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Close",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun AnimatedGifPainterTwo(): Painter {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components { add(ImageDecoderDecoder.Factory()) }
        .build()

    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data("file:///android_asset/swim.gif")
            .build(),
        imageLoader = imageLoader
    )
}







@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FillerCardThree() {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val scope = rememberCoroutineScope()
    val tapRipples = remember { mutableStateListOf<Animatable<Float, AnimationVector1D>>() }
    var tapOffset by remember { mutableStateOf(Offset.Zero) }

    val responsiveBoxHeight = when {
        screenWidthDp < 340 -> 170.dp
        screenWidthDp < 380 -> 190.dp
        screenWidthDp < 420 -> 210.dp
        else -> 230.dp
    }

    Box(
        modifier = Modifier
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
        // Background GIF
        Image(
            painter = AnimatedGifPainterThree(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Blur overlay
        Box(
            modifier = Modifier
                .matchParentSize()
//                .blur(16.dp)
//                .background(Color(0xAA222222))
        )

        // Ripple animation
        Canvas(modifier = Modifier.matchParentSize()) {
            tapRipples.forEachIndexed { i, ripple ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f - i * 0.05f),
                    radius = ripple.value,
                    center = tapOffset,
                    style = Stroke(width = 2f + i)
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Top section with title and status
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "🐟 Fish Health Tips",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Daily Monitoring",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Middle section with tips
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "• Check water quality daily",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "• Monitor fish behavior",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "• Maintain proper feeding",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            // Bottom section with action
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Scan Now",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Tap to start monitoring",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun AnimatedGifPainterThree(): Painter {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components { add(ImageDecoderDecoder.Factory()) }
        .build()

    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data("file:///android_asset/kopal.gif")
            .build(),
        imageLoader = imageLoader
    )
}








@Composable
fun ThreeDModelCard() {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val responsiveBoxHeight = when {
        screenWidthDp < 340 -> 170.dp
        screenWidthDp < 380 -> 190.dp
        screenWidthDp < 420 -> 210.dp
        else -> 230.dp
    }

    val htmlContent = """
        <!DOCTYPE html>
        <html>
          <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <script type="module" src="https://unpkg.com/@google/model-viewer/dist/model-viewer.min.js"></script>
            <style>
              body { margin: 0; background: transparent; }
              model-viewer {
                width: 100vw;
                height: 100vh;
              }
            </style>
          </head>
          <body>
            <model-viewer
              src="file:///android_asset/fish_model.glb"
              alt="A 3D fish model"
              auto-rotate
              camera-controls
              background-color="#FFFFFF">
            </model-viewer>
          </body>
        </html>
    """.trimIndent()

    Box(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
            .height(responsiveBoxHeight)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.allowFileAccess = true
                    settings.domStorageEnabled = true
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    loadDataWithBaseURL(
                        null,
                        htmlContent,
                        "text/html",
                        "utf-8",
                        null
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

