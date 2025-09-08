package com.capstone.safehito.ui

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.capstone.safehito.util.generateMedicalPDF
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.database.*
import androidx.navigation.NavHostController
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response
import java.io.IOException
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import android.util.Log
import androidx.compose.ui.res.painterResource
import com.capstone.safehito.R

// Data class for an admin record (to avoid redeclaration)
data class AdminRecord(
    val image_url: String = "",
    val result: String = "",
    val confidence: Float = 0f,
    val timestamp: Long = 0,
    val userEmail: String = ""
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdminRecordsScreen(darkTheme: Boolean, navController: NavHostController? = null) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = if (darkTheme) Color(0xFF121212) else Color(0xFFF4F8FB),
            darkIcons = !darkTheme
        )
    }

    var allRecords by remember { mutableStateOf(listOf<Pair<String, AdminRecord>>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun refreshRecordsData() {
        isLoading = true
        errorMessage = null
        val db = FirebaseDatabase.getInstance()
        db.getReference("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val records = mutableListOf<Pair<String, AdminRecord>>()
                for (userSnap in snapshot.children) {
                    val userEmail = userSnap.child("email").getValue(String::class.java) ?: "Unknown User"
                    val scansSnap = userSnap.child("scans")
                    for (scanSnap in scansSnap.children) {
                        val imageUrl = scanSnap.child("image").getValue(String::class.java) ?: ""
                        val status = scanSnap.child("status").getValue(String::class.java) ?: ""
                        val type = scanSnap.child("type").getValue(String::class.java) ?: "-"
                        val confidence = scanSnap.child("confidence").getValue(Double::class.java)?.toFloat() ?: 0f
                        val timestamp = scanSnap.child("timestamp").getValue(Long::class.java) ?: 0L
                        
                        val result = when {
                            status.equals("Healthy", true) -> "Healthy"
                            status.equals("No Fish", true) -> "No Fish"
                            status.equals("Infected", true) -> "Fungal - $type"
                            status.equals("Error", true) -> "Error"
                            else -> "Unknown"
                        }
                        
                        val record = AdminRecord(
                            image_url = imageUrl,
                            result = result,
                            confidence = confidence,
                            timestamp = timestamp,
                            userEmail = userEmail
                        )
                        records.add(scanSnap.key!! to record)
                    }
                }
                allRecords = records.sortedByDescending { it.second.timestamp }
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) {
                errorMessage = "Failed to load records: ${error.message}"
                isLoading = false
            }
        })
    }

    // Fetch all users' records from Firebase
    LaunchedEffect(Unit) {
        refreshRecordsData()
    }

    val filterOptions = listOf("All", "Healthy", "Infected", "No Fish", "Unknown")
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("Newest") }
    var filterExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }

    val filteredRecords = allRecords
        .filter { (_, record) ->
            when (selectedFilter) {
                "All" -> true
                "Healthy" -> record.result.contains("Healthy", ignoreCase = true)
                "Infected" -> listOf("Fungal", "cotton", "ulcer", "whitepatch", "reddish")
                    .any { keyword -> record.result.contains(keyword, ignoreCase = true) }
                "No Fish" -> record.result.contains("No Fish", ignoreCase = true)
                "Unknown" -> record.result.contains("Unknown", ignoreCase = true)
                else -> true
            }
        }
        .let {
            when (selectedSort) {
                "Newest" -> it.sortedByDescending { it.second.timestamp }
                "Oldest" -> it.sortedBy { it.second.timestamp }
                "Confidence" -> it.sortedByDescending { it.second.confidence }
                else -> it
            }
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

    // Per-admin notification unread indicator
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text("SafeHito Admin", fontSize = 12.sp, color = Color.Gray)
                        Text("All Users' Diagnosis Records", fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                                Image(
                                    painter = painterResource(id = R.drawable.ic_notifbell),
                                    contentDescription = "Notification",
                                    modifier = Modifier.size(22.dp)
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        var isRefreshing by remember { mutableStateOf(false) }
        val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)
        val coroutineScope = rememberCoroutineScope()

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    try {
                        // Refresh records data
                        refreshRecordsData()
                        // Delay to show refresh indicator
                        delay(1000)
                    } catch (e: Exception) {
                        Log.e("AdminRecordsRefresh", "Error during refresh: ${e.message}")
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
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $errorMessage", color = Color.Red)
                }
            } else if (filteredRecords.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "No Records Icon",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No scan records found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                val listState = rememberLazyListState()
                val scope = rememberCoroutineScope()
                val showScrollToTop by remember {
                    derivedStateOf { listState.firstVisibleItemIndex > 0 }
                }
                // Main content in a Column
                Column(Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredRecords.size} Records",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.zIndex(1f)) {
                                TextButton(
                                    onClick = { filterExpanded = true },
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.textButtonColors(
                                        containerColor = if (darkTheme) Color(0xFF2C2C2C) else Color(0xFFEFEFEF),
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Tune,
                                        contentDescription = "Filter",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (selectedFilter == "Infected") "\uD83D\uDEA8 $selectedFilter" else selectedFilter,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                                DropdownMenu(
                                    expanded = filterExpanded,
                                    onDismissRequest = { filterExpanded = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    filterOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                selectedFilter = option
                                                filterExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.zIndex(0f)) {
                                TextButton(
                                    onClick = { sortExpanded = true },
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.textButtonColors(
                                        containerColor = if (darkTheme) Color(0xFF2C2C2C) else Color(0xFFEFEFEF),
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.SwapVert,
                                        contentDescription = "Sort",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = selectedSort,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                                DropdownMenu(
                                    expanded = sortExpanded,
                                    onDismissRequest = { sortExpanded = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    listOf("Newest", "Oldest", "Confidence").forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                selectedSort = option
                                                sortExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(filteredRecords.size) { index ->
                            val (key, record) = filteredRecords[index]
                            AdminScanCard(record = record, recordKey = key, darkTheme = darkTheme)
                        }
                        item { Spacer(modifier = Modifier.height(70.dp)) }
                    }
                }
                // Move scroll-to-top button here, as a sibling to the Column
                Box(Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = showScrollToTop,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 45.dp, bottom = 100.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            shadowElevation = 6.dp,
                            color = if (darkTheme) Color(0xFF2C2C2C) else Color.White,
                            modifier = Modifier
                                .size(56.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    scope.launch {
                                        listState.scrollToItem(0)
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Scroll to top",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun AdminScanCard(record: AdminRecord, recordKey: String, darkTheme: Boolean) {
    var showFullImageDialog by remember { mutableStateOf(false) }
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    val formattedTime = try {
        if (record.timestamp < 1000000000000L) sdf.format(Date(record.timestamp * 1000))
        else sdf.format(Date(record.timestamp))
    } catch (e: Exception) {
        "Invalid time"
    }

    data class DiagnosisStyle(
        val bgColor: Color,
        val textColor: Color,
        val statusText: String,
        val dotColor: Color
    )

    val style = when {
        record.result.contains("cotton", ignoreCase = true) -> DiagnosisStyle(
            bgColor = if (darkTheme) Color(0xFF424242) else Color(0xFFF5F5F5),
            textColor = if (darkTheme) Color(0xFFFF8A80) else Color(0xFFD32F2F),
            statusText = "Infected – Saprolegniasis",
            dotColor = Color.Red
        )
        record.result.contains("whitepatch", ignoreCase = true) -> DiagnosisStyle(
            bgColor = if (darkTheme) Color(0xFF424242) else Color(0xFFF5F5F5),
            textColor = if (darkTheme) Color(0xFFFF8A80) else Color(0xFFD32F2F),
            statusText = "Infected – White Patch",
            dotColor = Color.Red
        )
        record.result.contains("reddish", ignoreCase = true) -> DiagnosisStyle(
            bgColor = if (darkTheme) Color(0xFF424242) else Color(0xFFF5F5F5),
            textColor = if (darkTheme) Color(0xFFFF8A80) else Color(0xFFD32F2F),
            statusText = "Infected – Reddish Patch",
            dotColor = Color.Red
        )
        record.result.contains("ulcer", ignoreCase = true) -> DiagnosisStyle(
            bgColor = if (darkTheme) Color(0xFF424242) else Color(0xFFF5F5F5),
            textColor = if (darkTheme) Color(0xFFFF8A80) else Color(0xFFD32F2F),
            statusText = "Infected – Ulcerative Lesion",
            dotColor = Color.Red
        )
        record.result.contains("fungal", ignoreCase = true) -> DiagnosisStyle(
            bgColor = if (darkTheme) Color(0xFF424242) else Color(0xFFF5F5F5),
            textColor = if (darkTheme) Color(0xFFFF9890) else Color(0xFFD32F2F),
            statusText = "General Fungal Infection",
            dotColor = Color.Red
        )
        record.result.contains("healthy", ignoreCase = true) -> DiagnosisStyle(
            bgColor = if (darkTheme) Color(0xFF424242) else Color(0xFFF5F5F5),
            textColor = if (darkTheme) Color(0xFF81C784) else Color(0xFF2E7D32),
            statusText = "Healthy",
            dotColor = Color(0xFF4CAF50)
        )
        record.result.contains("no fish", ignoreCase = true) -> DiagnosisStyle(
            bgColor = if (darkTheme) Color(0xFF424242) else Color(0xFFF5F5F5),
            textColor = if (darkTheme) Color(0xFFEEEEEE) else Color(0xFF616161),
            statusText = "No Fish Detected",
            dotColor = Color.Gray
        )
        else -> DiagnosisStyle(
            bgColor = if (darkTheme) Color(0xFF424242) else Color(0xFFF5F5F5),
            textColor = if (darkTheme) Color(0xFFFFF176) else Color(0xFFF9A825),
            statusText = "Unknown Diagnosis",
            dotColor = Color.Yellow
        )
    }


    data class DiagnosisDetails(
        val scientificName: String,
        val shortDescription: String
    )

    val diagnosisDetails = when {
        record.result.contains("cotton", ignoreCase = true) -> DiagnosisDetails(
            scientificName = "Saprolegnia spp.",
            shortDescription = "Cotton-like fungal growth on skin or fins."
        )
        record.result.contains("whitepatch", ignoreCase = true) -> DiagnosisDetails(
            scientificName = "Possible early fungal infection",
            shortDescription = "Flat white patches that may indicate early fungal infection."
        )
        record.result.contains("reddish", ignoreCase = true) -> DiagnosisDetails(
            scientificName = "Possible secondary infection",
            shortDescription = "Reddish areas from irritation or bacterial co-infection."
        )
        record.result.contains("ulcer", ignoreCase = true) -> DiagnosisDetails(
            scientificName = "Skin ulcer / necrotic lesion",
            shortDescription = "Open sores often linked to advanced fungal or bacterial infection."
        )
        record.result.contains("fungal", ignoreCase = true) -> DiagnosisDetails(
            scientificName = "Various fungal species",
            shortDescription = "Signs of fungal infection detected."
        )
        record.result.contains("healthy", ignoreCase = true) -> DiagnosisDetails(
            scientificName = "No pathogens detected",
            shortDescription = "Fish appear to be in good condition."
        )
        record.result.contains("no fish", ignoreCase = true) -> DiagnosisDetails(
            scientificName = "No fish detected",
            shortDescription = "No visible fish in the frame."
        )
        else -> DiagnosisDetails(
            scientificName = "Unknown",
            shortDescription = "No additional details available."
        )
    }

    // Animated water background setup (copied from RecordsScreen)
    val waveColor1 = if (darkTheme) Color(0xFF4BA3C7).copy(alpha = 0.3f) else Color(0xFF4BA3C7).copy(alpha = 0.5f)
    val waveColor2 = if (darkTheme) Color(0xFF5DCCFC).copy(alpha = 0.2f) else Color(0xFF5DCCFC).copy(alpha = 0.4f)
    val waveColor3 = if (darkTheme) Color(0xFFB3E5FC).copy(alpha = 0.15f) else Color(0xFFB3E5FC).copy(alpha = 0.3f)
    val waveColor4 = if (darkTheme) Color(0xFFE1F5FE).copy(alpha = 0.1f) else Color(0xFFE1F5FE).copy(alpha = 0.25f)
    val infiniteTransition = rememberInfiniteTransition(label = "wavePhase")
    val wavePhase1: Float by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "wavePhase1"
    )
    val wavePhase2: Float by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "wavePhase2"
    )
    val wavePhase3: Float by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)),
        label = "wavePhase3"
    )
    val wavePhase4: Float by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "wavePhase4"
    )

    // Determine if infected
    val isInfected = record.result.contains("cotton", true) || record.result.contains("reddish", true) || record.result.contains("whitepatch", true) || record.result.contains("ulcer", true) || record.result.contains("fungal", true)

    // Set wave baseYTargets higher for infected
    val baseYTargets = if (isInfected) {
        listOf(0.60f, 0.58f, 0.56f, 0.54f) // higher waves for infected
    } else {
        listOf(0.68f, 0.66f, 0.64f, 0.62f) // lower for healthy/no fish/unknown
    }

    var targets by remember {
        mutableStateOf(
            listOf(
                Triple(18f, baseYTargets[0], 1.5f),
                Triple(16f, baseYTargets[1], 1.3f),
                Triple(14f, baseYTargets[2], 1.1f),
                Triple(12f, baseYTargets[3], 0.9f)
            )
        )
    }

    val anims = targets.mapIndexed { i, t ->
        Triple(
            animateFloatAsState(t.first, tween(2000)).value,
            animateFloatAsState(t.second, tween(2000)).value,
            animateFloatAsState(t.third, tween(2000)).value
        )
    }

    LaunchedEffect(isInfected) {
        // Update targets if infection status changes
        targets = listOf(
            Triple(18f, baseYTargets[0], 1.5f),
            Triple(16f, baseYTargets[1], 1.3f),
            Triple(14f, baseYTargets[2], 1.1f),
            Triple(12f, baseYTargets[3], 0.9f)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = style.bgColor)
    ) {
        Box(Modifier.fillMaxWidth()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                fun drawWave(phase: Float, amplitude: Float, baseYRatio: Float, frequency: Float, color: Color) {
                    val path = Path()
                    val baseY = height * baseYRatio
                    val step = 1f
                    path.moveTo(0f, baseY)
                    var x = 0f
                    while (x <= width) {
                        val angle = (x / width) * 2 * Math.PI.toFloat() * frequency + phase
                        val y = baseY + amplitude * kotlin.math.sin(angle)
                        path.lineTo(x, y)
                        x += step
                    }
                    path.lineTo(width, height)
                    path.lineTo(0f, height)
                    path.close()
                    drawPath(path, color)
                }
                drawWave(wavePhase1, anims[0].first, anims[0].second, anims[0].third, waveColor1)
                drawWave(wavePhase2, anims[1].first, anims[1].second, anims[1].third, waveColor2)
                drawWave(wavePhase3, anims[2].first, anims[2].second, anims[2].third, waveColor3)
                drawWave(wavePhase4, anims[3].first, anims[3].second, anims[3].third, waveColor4)
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.size(10.dp)) {
                            drawCircle(style.dotColor)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = style.statusText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = style.textColor
                        )
                    }
                    // More (three dots) menu
                    var expanded by remember { mutableStateOf(false) }
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    val context = LocalContext.current
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "More Options"
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp)
                            )
                        ) {
                            DropdownMenuItem(
                                text = { Text("Download Report") },
                                onClick = {
                                    expanded = false
                                    // Download logic: generate PDF and save (in coroutine, map AdminRecord to Record)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val pdfRecord = Record(
                                                image_url = record.image_url,
                                                result = record.result,
                                                confidence = record.confidence,
                                                timestamp = record.timestamp
                                            )
                                            generateMedicalPDF(context, pdfRecord)
                                            launch(Dispatchers.Main) {
                                                Toast.makeText(context, "PDF downloaded!", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            launch(Dispatchers.Main) {
                                                Toast.makeText(context, "Failed to download PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDownward,
                                        contentDescription = "Download"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = {
                                    expanded = false
                                    // Share logic: share diagnosis text and image
                                    val shareIntent = Intent(Intent.ACTION_SEND)
                                    shareIntent.type = "text/plain"
                                    val text = "Fish scan report:\nDiagnosis: ${record.result}\nConfidence: ${(record.confidence * 100).toInt()}%\nUser: ${record.userEmail}"
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, text)
                                    // Try to attach image if possible
                                    if (record.image_url.isNotBlank()) {
                                        try {
                                            val uri = Uri.parse(record.image_url)
                                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                                            shareIntent.type = "image/*"
                                        } catch (_: Exception) {}
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Report"))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.IosShare,
                                        contentDescription = "Share"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Record") },
                                onClick = {
                                    expanded = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        tint = if (darkTheme) Color(0xFFE57373) else Color(0xFFD32F2F)
                                    )
                                }
                            )
                        }
                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Delete Record") },
                                text = { Text("Are you sure you want to delete this scan record?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        // Remove from Firebase
                                        val db = FirebaseDatabase.getInstance()
                                        db.getReference("users")
                                            .orderByChild("email")
                                            .equalTo(record.userEmail)
                                        db.getReference("users").addValueEventListener(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                    for (userSnap in snapshot.children) {
                                                        val scansRef = userSnap.ref.child("scans")
                                                        scansRef.child(recordKey).removeValue()
                                                    }
                                                }
                                                override fun onCancelled(error: DatabaseError) {}
                                            })
                                        showDeleteDialog = false
                                    }) {
                                        Text("Delete", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
                AsyncImage(
                    model = record.image_url,
                    contentDescription = "Fish Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showFullImageDialog = true }
                )
                Spacer(modifier = Modifier.height(25.dp))
                Text(
                    text = "User: ${record.userEmail}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "🕓 $formattedTime",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                // ✅ Diagnosis details
                Text(
                    text = "Diagnosis: ${diagnosisDetails.scientificName}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = style.textColor
                )
                Text(
                    text = diagnosisDetails.shortDescription,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Confidence: ${(record.confidence * 100).toInt()}%",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Salt bath activation for infected records (admins)
                val user = FirebaseAuth.getInstance().currentUser
                val isAdmin = user != null // Only admins can see this screen
                val isInfected = record.result.contains("cotton", true) || record.result.contains("reddish", true) || record.result.contains("whitepatch", true) || record.result.contains("ulcer", true) || record.result.contains("fungal", true)
                val context = LocalContext.current
                var showConfirmationDialog by remember { mutableStateOf(false) }
                var cooldownMinutes by remember { mutableStateOf(720L) }
                var lastActivationTimestamp by remember { mutableStateOf(0L) }
                var isCooldownActive by remember { mutableStateOf(false) }
                var remainingMillis by remember { mutableStateOf(0L) }
                val client = remember { OkHttpClient() }

                if (isInfected && isAdmin) {
                    // Fetch cooldown and last activation for this admin
                    LaunchedEffect(Unit) {
                        val dbRef = FirebaseDatabase.getInstance().getReference("settings")
                        dbRef.child("saltBathCooldownMinutes")
                            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                                    cooldownMinutes = snapshot.getValue(Long::class.java) ?: 720L
                                    val now = System.currentTimeMillis()
                                    val cooldownMillis = cooldownMinutes * 60 * 1000
                                    val passed = now - lastActivationTimestamp
                                    val remaining = cooldownMillis - passed
                                    if (remaining > 0) {
                                        isCooldownActive = true
                                        remainingMillis = remaining
                                    } else {
                                        isCooldownActive = false
                                        remainingMillis = 0L
                                    }
                                }
                                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                            })
                        val userUid = user?.uid
                        if (userUid != null) {
                            val userDbRef = FirebaseDatabase.getInstance().getReference("users/$userUid")
                            userDbRef.child("saltbath_history").orderByChild("timestamp").limitToLast(1)
                                .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                                        val lastEntry = snapshot.children.firstOrNull()
                                        lastActivationTimestamp = lastEntry?.child("timestamp")?.getValue(Long::class.java) ?: 0L
                                        val now = System.currentTimeMillis()
                                        val cooldownMillis = cooldownMinutes * 60 * 1000
                                        val passed = now - lastActivationTimestamp
                                        val remaining = cooldownMillis - passed
                                        if (remaining > 0) {
                                            isCooldownActive = true
                                            remainingMillis = remaining
                                        }
                                    }
                                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                                })
                        }
                    }
                    // Countdown tick
                    LaunchedEffect(remainingMillis) {
                        if (remainingMillis > 0) {
                            while (remainingMillis > 0) {
                                delay(1000L)
                                remainingMillis -= 1000L
                            }
                            isCooldownActive = false
                        }
                    }
                    val hours = (remainingMillis / 1000 / 60 / 60).toInt()
                    val minutes = ((remainingMillis / 1000 / 60) % 60).toInt()
                    val timerText = when {
                        hours > 0 -> "⏳ ${hours}h ${minutes}m remaining"
                        minutes > 0 -> "⏳ ${minutes}m remaining"
                        else -> ""
                    }
                    val gradientBrush = Brush.linearGradient(
                        colors = listOf(Color(0xFF5DCCFC), Color(0xFF007EF2)),
                        start = Offset(0f, 0f),
                        end = Offset.Infinite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(gradientBrush)
                    ) {
                        Button(
                            onClick = { if (!isCooldownActive) showConfirmationDialog = true },
                            enabled = !isCooldownActive,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.WaterDrop,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Activate Salt Bath",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (isCooldownActive && timerText.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(0.dp))
                                    Text(
                                        text = timerText,
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                    if (showConfirmationDialog) {
                        AlertDialog(
                            onDismissRequest = { showConfirmationDialog = false },
                            title = { Text("Confirm Activation") },
                            text = { Text("Are you sure you want to activate the salt bath treatment?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showConfirmationDialog = false
                                        Toast.makeText(context, "Fetching ESP32 IP...", Toast.LENGTH_SHORT).show()
                                        val firebaseUrl = "https://safehito-ebd48-default-rtdb.firebaseio.com/deviceIp.json"
                                        val requestFetchIp = Request.Builder().url(firebaseUrl).get().build()
                                        client.newCall(requestFetchIp).enqueue(object : Callback {
                                            override fun onFailure(call: Call, e: IOException) {
                                                (context as? android.app.Activity)?.runOnUiThread {
                                                    Toast.makeText(context, "❌ Failed to get IP: ${e.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                            override fun onResponse(call: Call, response: Response) {
                                                if (response.isSuccessful) {
                                                    val ip = response.body?.string()?.replace("\"", "")?.trim()
                                                    if (ip.isNullOrEmpty()) {
                                                        (context as? android.app.Activity)?.runOnUiThread {
                                                            Toast.makeText(context, "❌ No IP found in Firebase.", Toast.LENGTH_LONG).show()
                                                        }
                                                        return
                                                    }
                                                    val activateUrl = "http://$ip/activate_salt_bath"
                                                    val activateRequest = Request.Builder().url(activateUrl).get().build()
                                                    client.newCall(activateRequest).enqueue(object : Callback {
                                                        override fun onFailure(call: Call, e: IOException) {
                                                            (context as? android.app.Activity)?.runOnUiThread {
                                                                Toast.makeText(context, "❌ Failed to command ESP32: ${e.message}", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                        override fun onResponse(call: Call, response: Response) {
                                                            (context as? android.app.Activity)?.runOnUiThread {
                                                                if (response.isSuccessful) {
                                                                    Toast.makeText(context, "✅ Salt bath activated!", Toast.LENGTH_SHORT).show()
                                                                    val uid = user?.uid
                                                                    if (uid != null) {
                                                                        val database = FirebaseDatabase.getInstance()
                                                                        val historyRef = database.getReference("users/$uid/saltbath_history").push()
                                                                        val historyData = mapOf(
                                                                            "activated" to true,
                                                                            "timestamp" to System.currentTimeMillis()
                                                                        )
                                                                        historyRef.setValue(historyData)
                                                                        val notifRef = database.getReference("notifications/$uid").push()
                                                                        val notifData = mapOf(
                                                                            "id" to notifRef.key,
                                                                            "message" to "✅ Salt bath treatment has been activated.",
                                                                            "time" to System.currentTimeMillis(),
                                                                            "read" to false
                                                                        )
                                                                        notifRef.setValue(notifData)
                                                                    }
                                                                } else {
                                                                    Toast.makeText(context, "❌ ESP32 error: ${response.code}", Toast.LENGTH_LONG).show()
                                                                }
                                                            }
                                                        }
                                                    })
                                                } else {
                                                    (context as? android.app.Activity)?.runOnUiThread {
                                                        Toast.makeText(context, "❌ Firebase error: ${response.code}", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        })
                                    }
                                ) {
                                    Text("Activate")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirmationDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showFullImageDialog) {
        Dialog(
            onDismissRequest = { showFullImageDialog = false },
            properties = DialogProperties(
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                var scale by remember { mutableStateOf(1f) }
                val maxScale = 5f
                val minScale = 1f
                val zoomModifier = Modifier.pointerInput(Unit) {
                    detectTransformGestures { _, _, zoomChange, _ ->
                        scale = (scale * zoomChange).coerceIn(minScale, maxScale)
                    }
                }.pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            scale = if (scale > 1f) 1f else 2f
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .then(zoomModifier)
                ) {
                    AsyncImage(
                        model = record.image_url,
                        contentDescription = null,
                        modifier = Modifier
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        IconButton(
                            onClick = { showFullImageDialog = false }
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
    }
}

