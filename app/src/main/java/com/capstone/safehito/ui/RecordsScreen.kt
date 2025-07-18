package com.capstone.safehito.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

import androidx.compose.animation.core.LinearEasing
import kotlin.random.Random
import androidx.compose.ui.graphics.Path
import okhttp3.Callback
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale

import java.util.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.capstone.safehito.util.generateMedicalPDF
import com.capstone.safehito.viewmodel.NotificationViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat


data class Record(
    val image_url: String = "",
    val result: String = "",
    val confidence: Float = 0f,
    val timestamp: Long = 0
)


@Composable
fun ScanCard(record: Record, recordKey: String, darkTheme: Boolean) {
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
        record.result.contains("reddish", ignoreCase = true) -> DiagnosisStyle(
            bgColor = if (darkTheme) Color(0xFF424242) else Color(0xFFF5F5F5),
            textColor = if (darkTheme) Color(0xFFFF8A80) else Color(0xFFD32F2F),
            statusText = "Infected – Fusariosis",
            dotColor = Color.Red
        )
        record.result.contains("whitepatch", ignoreCase = true) -> DiagnosisStyle(
            bgColor = if (darkTheme) Color(0xFF424242) else Color(0xFFF5F5F5),
            textColor = if (darkTheme) Color(0xFFFF8A80) else Color(0xFFD32F2F),
            statusText = "Infected – Candidiasis",
            dotColor = Color.Red
        )
        record.result.contains("ulcer", ignoreCase = true) -> DiagnosisStyle(
            bgColor = if (darkTheme) Color(0xFF424242) else Color(0xFFF5F5F5),
            textColor = if (darkTheme) Color(0xFFFF8A80) else Color(0xFFD32F2F),
            statusText = "Infected – Achlyosis",
            dotColor = Color.Red
        )
        record.result.contains("Fungal", ignoreCase = true) -> DiagnosisStyle(
            bgColor = if (darkTheme) Color(0xFF424242) else Color(0xFFF5F5F5),
            textColor = if (darkTheme) Color(0xFFFF9890) else Color(0xFFD32F2F),
            statusText = "General Fungal Infection",
            dotColor = Color.Red
        )
        record.result.contains("Healthy", ignoreCase = true) -> DiagnosisStyle(
            bgColor = if (darkTheme) Color(0xFF424242) else Color(0xFFF5F5F5),
            textColor = if (darkTheme) Color(0xFF81C784) else Color(0xFF2E7D32),
            statusText = "Healthy",
            dotColor = Color(0xFF4CAF50)
        )
        record.result.contains("No Fish", ignoreCase = true) -> DiagnosisStyle(
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
            scientificName = "Saprolegnia parasitica",
            shortDescription = "Cotton-like growth on skin or fins"
        )
        record.result.contains("reddish", ignoreCase = true) -> DiagnosisDetails(
            scientificName = "Fusarium solani",
            shortDescription = "Reddish ulcers or skin wounds"
        )
        record.result.contains("whitepatch", ignoreCase = true) -> DiagnosisDetails(
            scientificName = "Candida albicans",
            shortDescription = "Milky white smooth skin patches"
        )
        record.result.contains("ulcer", ignoreCase = true) -> DiagnosisDetails(
            scientificName = "Achlya americana",
            shortDescription = "Open ulcers with fungal edge"
        )
        record.result.contains("fungal", ignoreCase = true) -> DiagnosisDetails(
            scientificName = "Various fungal species",
            shortDescription = "Signs of fungal infection detected"
        )
        record.result.contains("healthy", ignoreCase = true) -> DiagnosisDetails(
            scientificName = "No pathogens detected",
            shortDescription = "Fish appear to be in good condition"
        )
        record.result.contains("no fish", ignoreCase = true) -> DiagnosisDetails(
            scientificName = "No fish detected",
            shortDescription = "There's no visible fish in the frame"
        )
        else -> DiagnosisDetails(
            scientificName = "Unknown",
            shortDescription = "No additional details"
        )
    }


    val context = LocalContext.current
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showDownloadDialog = remember { mutableStateOf(false) }
    val showFullImageDialog = remember { mutableStateOf(false) }
    val expandedMenu = remember { mutableStateOf(false) }
    val pendingAction = remember { mutableStateOf<(() -> Unit)?>(null) }

    val showConfirmationDialog = remember { mutableStateOf(false) }

    val waveColor1 = if (darkTheme) Color(0xFF4BA3C7).copy(alpha = 0.3f) else Color(0xFF4BA3C7).copy(alpha = 0.5f)
    val waveColor2 = if (darkTheme) Color(0xFF5DCCFC).copy(alpha = 0.2f) else Color(0xFF5DCCFC).copy(alpha = 0.4f)
    val waveColor3 = if (darkTheme) Color(0xFFB3E5FC).copy(alpha = 0.15f) else Color(0xFFB3E5FC).copy(alpha = 0.3f)
    val waveColor4 = if (darkTheme) Color(0xFFE1F5FE).copy(alpha = 0.1f) else Color(0xFFE1F5FE).copy(alpha = 0.25f)

    val infiniteTransition = rememberInfiniteTransition()
    val wavePhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing))
    )
    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing))
    )
    val wavePhase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing))
    )
    val wavePhase4 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing))
    )

    val isInfected = listOf(
        "cotton",
        "reddish",
        "whitepatch",
        "ulcer",
        "Fungal"
    ).any { keyword ->
        record.result.contains(keyword, ignoreCase = true)
    }


    val baseYTargets = if (isInfected) {
        listOf(0.60f, 0.58f, 0.56f, 0.54f)  // lower for infected
    } else {
        listOf(0.68f, 0.66f, 0.64f, 0.62f)  // higher for healthy/no fish/unknown
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


    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            targets = List(4) {
                val amp = listOf(18f, 16f, 14f, 12f)[it] + Random.nextFloat() * 4f
                val freq = listOf(1.5f, 1.3f, 1.1f, 0.9f)[it] + Random.nextFloat() * 0.4f
                Triple(amp, baseYTargets[it], freq) // ✅ baseY stays the same
            }
        }
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
                    Box {
                        IconButton(onClick = { expandedMenu.value = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "More Options"
                            )
                        }
                        DropdownMenu(
                            expanded = expandedMenu.value,
                            onDismissRequest = { expandedMenu.value = false },
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(16.dp) // Softer, more modern corner radius
                                )
                        ) {
                            DropdownMenuItem(
                                text = { Text("Download Report") },
                                onClick = {
                                    expandedMenu.value = false
                                    pendingAction.value = { showDownloadDialog.value = true }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDownward, // Modern icon
                                        contentDescription = "Download"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = {
                                    expandedMenu.value = false
                                    val intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Fish scan report:\nDiagnosis: ${record.result}\nConfidence: ${(record.confidence * 100).toInt()}%"
                                        )
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Report"))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.IosShare, // Modern icon
                                        contentDescription = "Share"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Record") },
                                onClick = {
                                    expandedMenu.value = false
                                    pendingAction.value = { showDeleteDialog.value = true }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Delete, // Modern icon
                                        contentDescription = "Delete",
                                        tint = if (darkTheme) Color(0xFFE57373) else Color(0xFFD32F2F)
                                    )
                                }
                            )
                        }

                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = record.image_url,
                    contentDescription = "Fish Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showFullImageDialog.value = true }
                )




                Spacer(modifier = Modifier.height(25.dp))
                Text(
                    "🕓 $formattedTime",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Diagnosis: ${diagnosisDetails.scientificName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = style.textColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${diagnosisDetails.shortDescription}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )



                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Confidence: ${(record.confidence * 100).toInt()}%",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )


                if (isInfected) {
                    Spacer(modifier = Modifier.height(8.dp))

                    val gradientBrush = Brush.linearGradient(
                        colors = listOf(Color(0xFF5DCCFC), Color(0xFF007EF2)),
                        start = Offset(0f, 0f),
                        end = Offset.Infinite
                    )

                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    var cooldownMinutes by remember { mutableStateOf(720L) }
                    var lastActivationTimestamp by remember { mutableStateOf(0L) }
                    var isCooldownActive by remember { mutableStateOf(false) }
                    var remainingMillis by remember { mutableStateOf(0L) }

                    // Firebase fetch
                    LaunchedEffect(uid) {
                        uid?.let {
                            val dbRef = FirebaseDatabase.getInstance().getReference("users/$uid")

                            dbRef.child("settings/saltBathCooldownMinutes")
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        cooldownMinutes = snapshot.getValue(Long::class.java) ?: 720L

                                        // Recalculate cooldown immediately
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

                                    override fun onCancelled(error: DatabaseError) {}
                                })


                            dbRef.child("saltbath_history").orderByChild("timestamp").limitToLast(1)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val lastEntry = snapshot.children.firstOrNull()
                                        lastActivationTimestamp =
                                            lastEntry?.child("timestamp")?.getValue(Long::class.java) ?: 0L

                                        val now = System.currentTimeMillis()
                                        val cooldownMillis = cooldownMinutes * 60 * 1000
                                        val passed = now - lastActivationTimestamp
                                        val remaining = cooldownMillis - passed

                                        if (remaining > 0) {
                                            isCooldownActive = true
                                            remainingMillis = remaining
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(gradientBrush)
                    ) {
                        Button(
                            onClick = { if (!isCooldownActive) showConfirmationDialog.value = true },
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
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    LaunchedEffect(expandedMenu.value) {
        if (!expandedMenu.value) {
            pendingAction.value?.invoke()
            pendingAction.value = null
        }
    }

    val client = remember { OkHttpClient() }

    if (showConfirmationDialog.value) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog.value = false },
            title = {
                Text("Confirm Activation")
            },
            text = {
                Text("Are you sure you want to activate the salt bath treatment?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Dismiss dialog immediately
                        showConfirmationDialog.value = false

                        // Show initial toast
                        Toast.makeText(context, "Fetching ESP32 IP...", Toast.LENGTH_SHORT).show()

                        // Fetch IP from Firebase
                        val firebaseUrl = "https://safehito-ebd48-default-rtdb.firebaseio.com/deviceIp.json"

                        val requestFetchIp = Request.Builder()
                            .url(firebaseUrl)
                            .get()
                            .build()

                        client.newCall(requestFetchIp).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                (context as Activity).runOnUiThread {
                                    Toast.makeText(
                                        context,
                                        "❌ Failed to get IP: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful) {
                                    val ip = response.body?.string()?.replace("\"", "")?.trim()

                                    if (ip.isNullOrEmpty()) {
                                        (context as Activity).runOnUiThread {
                                            Toast.makeText(
                                                context,
                                                "❌ No IP found in Firebase.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        return
                                    }

                                    // Send GET request to ESP32
                                    val activateUrl = "http://$ip/activate_salt_bath"
                                    val activateRequest = Request.Builder()
                                        .url(activateUrl)
                                        .get()
                                        .build()

                                    client.newCall(activateRequest).enqueue(object : Callback {
                                        override fun onFailure(call: Call, e: IOException) {
                                            (context as Activity).runOnUiThread {
                                                Toast.makeText(
                                                    context,
                                                    "❌ Failed to command ESP32: ${e.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }

                                        override fun onResponse(call: Call, response: Response) {
                                            (context as Activity).runOnUiThread {
                                                if (response.isSuccessful) {
                                                    // Toast
                                                    Toast.makeText(
                                                        context,
                                                        "✅ Salt bath activated!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    // Log treatment in Firebase
                                                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                                                    if (uid != null) {
                                                        val database = FirebaseDatabase.getInstance()

                                                        // Log to saltbath_history
                                                        val historyRef = database.getReference("users/$uid/saltbath_history").push()
                                                        val historyData = mapOf(
                                                            "activated" to true,
                                                            "timestamp" to System.currentTimeMillis()
                                                        )
                                                        historyRef.setValue(historyData)

                                                        // Create notification
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
                                                    Toast.makeText(
                                                        context,
                                                        "❌ ESP32 error: ${response.code}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        }

                                    })
                                } else {
                                    (context as Activity).runOnUiThread {
                                        Toast.makeText(
                                            context,
                                            "❌ Firebase error: ${response.code}",
                                            Toast.LENGTH_LONG
                                        ).show()
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
                TextButton(
                    onClick = { showConfirmationDialog.value = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }



    if (showDownloadDialog.value) {
        AlertDialog(
            onDismissRequest = { showDownloadDialog.value = false },
            title = { Text("Download Report") },
            text = { Text("Do you want to download this scan report as a PDF?") },
            confirmButton = {
                TextButton(onClick = {
                    GlobalScope.launch {
                        generateMedicalPDF(context, record)
                    }
                    showDownloadDialog.value = false
                }) {
                    Text("Download")
                }

            },
            dismissButton = {
                TextButton(onClick = { showDownloadDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Delete Record") },
            text = { Text("Are you sure you want to delete this scan record?") },
            confirmButton = {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                TextButton(onClick = {
                    FirebaseDatabase.getInstance().getReference("users/$uid/scans")
                        .child(recordKey).removeValue()
                    showDeleteDialog.value = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFullImageDialog.value) {
        Dialog(
            onDismissRequest = { showFullImageDialog.value = false },
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

                    IconButton(
                        onClick = { showFullImageDialog.value = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
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




@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    navController: NavHostController,
    notificationViewModel: NotificationViewModel,
    darkTheme: Boolean
) {
    val scroll = rememberScrollState()
    val systemUiController = rememberSystemUiController()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "admin"
    var hasUnread by remember { mutableStateOf(false) }
    var records by remember { mutableStateOf(mapOf<String, Record>()) }

    val db = FirebaseDatabase.getInstance()

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = if (darkTheme) Color(0xFF121212) else Color(0xFFF4F8FB),
            darkIcons = !darkTheme
        )
    }

    LaunchedEffect(uid) {
        db.getReference("notifications/$uid").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hasUnread = snapshot.children.any {
                    it.child("read").getValue(Boolean::class.java) == false
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    LaunchedEffect(true) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        db.getReference("users/$uid/scans").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = snapshot.children.associate { snap ->
                    val status = snap.child("status").getValue(String::class.java) ?: ""
                    val type = snap.child("type").getValue(String::class.java) ?: "-"
                    val confidence =
                        snap.child("confidence").getValue(Double::class.java)?.toFloat() ?: 0f
                    val timestamp = snap.child("timestamp").getValue(Long::class.java) ?: 0L
                    val image = snap.child("image").getValue(String::class.java) ?: ""

                    val result = when {
                        status.equals("Healthy", true) -> "Healthy"
                        status.equals("No Fish", true) -> "No Fish"
                        status.equals("Infected", true) -> "Fungal - $type"
                        status.equals("Error", true) -> "Error"
                        else -> "Unknown"
                    }

                    snap.key!! to Record(
                        image_url = image,
                        result = result,
                        confidence = confidence,
                        timestamp = timestamp
                    )
                }
                records = map.toList().sortedByDescending { it.second.timestamp }.toMap()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val filterOptions = listOf("All", "Healthy", "Infected", "No Fish", "Unknown")
    var selectedFilter by remember { mutableStateOf("All") }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text("SafeHito", fontSize = 12.sp, color = Color.Gray)
                        Text("Diagnosis History", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("notifications") },
                        modifier = Modifier.padding(end = 18.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp)
                                .shadow(6.dp, shape = CircleShape, clip = false),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (darkTheme) Color(0xFF1E1E1E) else Color.White,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notification",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                if (hasUnread) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-4).dp, y = 4.dp)
                                            .background(
                                                MaterialTheme.colorScheme.error,
                                                CircleShape
                                            )
                                            .border(
                                                1.dp,
                                                if (darkTheme) Color(0xFF1E1E1E) else Color.White,
                                                CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        /*bottomBar = {
            FloatingNavBar(
                selectedRoute = selectedRoute,
                onItemSelected = onItemSelected,
                modifier = Modifier.padding(bottom = 25.dp)
            )
        }*/
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {


            Spacer(modifier = Modifier.height(16.dp))


            if (records.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
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
            } else {
                val listState = rememberLazyListState()
                val scope = rememberCoroutineScope()
                val showScrollToTop by remember {
                    derivedStateOf { listState.firstVisibleItemIndex > 0 }
                }

                val filterOptions = listOf("All", "Healthy", "Infected", "No Fish", "Unknown")
                val sortOptions = listOf("Newest", "Oldest", "Confidence")

                var selectedFilter by remember { mutableStateOf("All") }
                var selectedSort by remember { mutableStateOf("Newest") }

                var filterExpanded by remember { mutableStateOf(false) }
                var sortExpanded by remember { mutableStateOf(false) }

                val filteredRecords = records
                    .filter { (_, record) ->
                        when (selectedFilter) {
                            "All" -> true
                            "Healthy" -> record.result.contains("Healthy", ignoreCase = true)
                            "Infected" -> listOf(
                                "Fungal",
                                "cotton",
                                "ulcer",
                                "whitepatch",
                                "reddish"
                            )
                                .any { keyword ->
                                    record.result.contains(
                                        keyword,
                                        ignoreCase = true
                                    )
                                }

                            "No Fish" -> record.result.contains("No Fish", ignoreCase = true)
                            "Unknown" -> record.result.contains("Unknown", ignoreCase = true)
                            else -> true
                        }
                    }
                    .let {
                        when (selectedSort) {
                            "Newest" -> it.toList().sortedByDescending { it.second.timestamp }
                            "Oldest" -> it.toList().sortedBy { it.second.timestamp }
                            "Confidence" -> it.toList().sortedByDescending { it.second.confidence }
                            else -> it.toList()
                        }
                    }

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        stickyHeader {
                            Surface(
                                tonalElevation = 4.dp,
                                color = MaterialTheme.colorScheme.background
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
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
                                                    containerColor = if (darkTheme) Color(0xFF2C2C2C) else Color(
                                                        0xFFEFEFEF
                                                    ),
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
                                                    text = if (selectedFilter == "Infected") "🚨 $selectedFilter" else selectedFilter,
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
                                                    containerColor = if (darkTheme) Color(0xFF2C2C2C) else Color(
                                                        0xFFEFEFEF
                                                    ),
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
                                                sortOptions.forEach { option ->
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
                            }
                        }

                        items(filteredRecords.size) { index ->
                            val (key, record) = filteredRecords[index]
                            ScanCard(record = record, recordKey = key, darkTheme = darkTheme)
                        }

                        item { Spacer(modifier = Modifier.height(70.dp)) }
                    }





                    AnimatedVisibility(
                        visible = showScrollToTop,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 45.dp, bottom = 120.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            shadowElevation = 6.dp,
                            color = MaterialTheme.colorScheme.surface,
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
    }
}



/*
val titlePaint = android.graphics.Paint().apply {
    isAntiAlias = true
    color = android.graphics.Color.BLACK
    textSize = 24f
    textAlign = android.graphics.Paint.Align.CENTER
    typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
}

val subtitlePaint = android.graphics.Paint().apply {
    isAntiAlias = true
    color = android.graphics.Color.DKGRAY
    textSize = 16f
    textAlign = android.graphics.Paint.Align.CENTER
    typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.ITALIC)
}

val bodyPaint = android.graphics.Paint().apply {
    isAntiAlias = true
    color = android.graphics.Color.BLACK
    textSize = 14f
    textAlign = android.graphics.Paint.Align.LEFT
}

val borderPaint = android.graphics.Paint().apply {
    color = android.graphics.Color.DKGRAY
    style = android.graphics.Paint.Style.STROKE
    strokeWidth = 4f
}
*/

