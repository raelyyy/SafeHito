package com.capstone.safehito.ui

import android.R.attr.alpha
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.ScaleGestureDetector
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ControlPointDuplicate
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.FitScreen
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.viewinterop.AndroidView
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior.getTag
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior.setTag
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.room.vo.Warning

import coil.compose.AsyncImage
import com.capstone.safehito.R
import com.capstone.safehito.viewmodel.NotificationViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.capstone.safehito.util.PiStatusManager
import com.capstone.safehito.ui.components.PiStatusIndicator
import com.capstone.safehito.ui.components.PiStatusDialog
import com.google.android.gms.common.config.GservicesValue.value
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.Response
import java.io.File
import java.lang.reflect.Array.set
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext.get
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlin.io.path.moveTo


interface PiApi {
    @GET("scan")
    suspend fun scanFromPi(
        @Query("uid") uid: String
    ): Response<ScanResponse>


    @Multipart
    @POST("scan-image")
    suspend fun scanPhoneImage(
        @Part image: MultipartBody.Part,
        @Part("uid") uid: RequestBody
    ): Response<ScanResponse>
}

data class ScanResponse(
    val status: String,
    val result: String?,
    val confidence: Float?,         // ✅ Needed
    val image_url: String?,         // ✅ Needed
    val timestamp: Long?,           // Optional, useful for logs
    val message: String? = null     // ✅ Optional error message (from backend)
)






fun createRetrofit(serverUrl: String): PiApi {
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/") // ✅ use ngrok URL
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(PiApi::class.java)
}






@Composable
fun CameraPreviewBox(
    context: Context,
    modifier: Modifier = Modifier,
    onCameraReady: (ImageCapture, ProcessCameraProvider, CameraControl, CameraInfo) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val scaleGestureDetector = ScaleGestureDetector(ctx, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                var currentZoomRatio = 1f

                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val scaleFactor = detector.scaleFactor
                    val currentZoom = previewView.camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
                    val minZoom = previewView.camera?.cameraInfo?.zoomState?.value?.minZoomRatio ?: 1f
                    val maxZoom = previewView.camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f

                    val delta = (scaleFactor - 1f) * 1.0f
                    val newZoom = (currentZoom + delta).coerceIn(minZoom, maxZoom)

                    previewView.camera?.cameraControl?.setZoomRatio(newZoom)
                    return true
                }


            })

            previewView.setOnTouchListener { _, event ->
                scaleGestureDetector.onTouchEvent(event)
                true
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                val selector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner, selector, preview, imageCapture
                    )
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    previewView.camera = camera // attach camera to PreviewView
                    onCameraReady(imageCapture, cameraProvider, camera.cameraControl, camera.cameraInfo)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
    )
}

var PreviewView.camera: Camera?
    get() = getTag(R.id.camera_tag) as? Camera
    set(value) {
        setTag(R.id.camera_tag, value)
    }



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    navController: NavHostController, // ✅ Add this
    notificationViewModel: NotificationViewModel,
    darkTheme: Boolean
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseDatabase.getInstance()
    var hasUnread by remember { mutableStateOf(false) }
    var confidence by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    // Pi Status Manager
    val piStatusManager = remember { PiStatusManager() }
    var showPiStatusDialog by remember { mutableStateOf(false) }


    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = if (darkTheme) Color(0xFF121212) else Color(0xFFF4F8FB),
            darkIcons = !darkTheme
        )
    }



    // Clean up when component is destroyed
    DisposableEffect(Unit) {
        onDispose {
            piStatusManager.stopMonitoring()
        }
    }



    LaunchedEffect(uid) {
        uid?.let {
            val notifRef = db.getReference("notifications/$uid")
            notifRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    hasUnread = snapshot.children.any { child ->
                        val message = child.child("message").getValue(String::class.java)
                        val time = child.child("time").getValue(Long::class.java)
                        val isRead = child.child("read").getValue(Boolean::class.java)

                        // Only valid notifications with a message + time can count
                        !message.isNullOrBlank() && time != null && isRead == false
                    }

                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }


    val context = LocalContext.current

    var imageUrl by remember { mutableStateOf<String?>(null) }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var showLivePreview by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var usePiCamera by remember { mutableStateOf(true) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraActive by remember { mutableStateOf(true) }
    var localImagePath by remember { mutableStateOf<String?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var scanJob by remember { mutableStateOf<Job?>(null) }
    var reloadKey by remember { mutableStateOf(0) }
    var showCancelConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var flashEnabled by remember { mutableStateOf(false) }

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    //var zoomRatio by remember { mutableFloatStateOf(1f) } // default zoom
    var maxZoom by remember { mutableFloatStateOf(1f) }
    var minZoom by remember { mutableFloatStateOf(1f) }
    var cameraInfo: CameraInfo? by remember { mutableStateOf(null) }

    var serverUrl by remember { mutableStateOf("") }

    var connectionMode by remember { mutableStateOf("auto") } // default

    LaunchedEffect(uid) {
        if (uid != null) {
            val ref = FirebaseDatabase.getInstance().getReference("users/$uid/connectionMode")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    connectionMode = snapshot.getValue(String::class.java) ?: "auto"
                    piStatusManager.startMonitoring(connectionMode.lowercase())
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    // Keep a live copy of Pi status for both camera modes
    val piStatus by piStatusManager.piStatus.collectAsState()

    // Always compute active serverUrl based on connectionMode + Pi status
    LaunchedEffect(connectionMode, piStatus) {
        serverUrl = when (connectionMode.lowercase()) {
            "lan" -> if (!piStatus.ipAddress.isNullOrEmpty()) {
                "http://${piStatus.ipAddress}:5000"
            } else ""
            "cloudflare" -> piStatus.cloudflareUrl ?: ""
            else -> { // auto
                if (!piStatus.ipAddress.isNullOrEmpty()) {
                    "http://${piStatus.ipAddress}:5000"
                } else {
                    piStatus.cloudflareUrl ?: ""
                }
            }
        }
        Log.d("ScanScreen", "✅ Active serverUrl: $serverUrl (mode=$connectionMode)")
    }

    val cameraPermission = android.Manifest.permission.CAMERA
    val cameraPermissionGranted = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, cameraPermission) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        cameraPermissionGranted.value = it
    }


    LaunchedEffect(Unit) {
        if (!cameraPermissionGranted.value) {
            launcher.launch(cameraPermission)
        } else {
            Toast.makeText(context, "Camera permission granted", Toast.LENGTH_SHORT).show()
        }
    }


    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val file = File(context.cacheDir, "selected_${UUID.randomUUID()}.jpg")
            val inputStream = context.contentResolver.openInputStream(uri)
            file.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }

            localImagePath = file.absolutePath
            showLivePreview = false
            cameraActive = false
            imageUrl = null
            scanResult = null
            reloadKey++
            isLoading = true

            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestBody)
            val uidBody = uid?.toRequestBody("text/plain".toMediaTypeOrNull())

            scanJob = coroutineScope.launch(Dispatchers.IO) {
                try {
                    // Require an active serverUrl (LAN or Cloudflare) and UID
                    if (serverUrl.isBlank() || uidBody == null) {
                        withContext(Dispatchers.Main) {
                            scanResult = "❌ Missing UID or Pi URL."
                            isLoading = false
                        }
                        return@launch
                    }

                    val api = createRetrofit(serverUrl)  // ✅ comes from Firebase listener
                    val response = api.scanPhoneImage(body, uidBody)

                    withContext(Dispatchers.Main) {
                        val res = response.body()
                        if (response.isSuccessful && res?.status == "success") {
                            scanResult = res.result ?: "No result"
                            confidence = res.confidence ?: 0f
                            imageUrl = res.image_url ?: ""
                            cameraProvider?.unbindAll()

                            Toast.makeText(
                                context,
                                "✅ Scan complete: ${res.result}",
                                Toast.LENGTH_LONG
                            ).show()

                            reloadKey++
                        } else {
                            val errorMsg = res?.message ?: response.errorBody()?.string() ?: "Unknown error"
                            scanResult = "❌ Scan failed: $errorMsg"
                            confidence = 0f
                            imageUrl = null

                            Toast.makeText(
                                context,
                                "❌ $errorMsg",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        isLoading = false
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        scanResult = "❌ Error: ${e.localizedMessage ?: e.message}"
                        confidence = 0f
                        imageUrl = null
                        isLoading = false
                    }
                }
            }
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text("SafeHito", fontSize = 12.sp, color = Color.Gray)
                        Text("Fungal Infection Scan", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("notifications") },
                        modifier = Modifier.padding(end = 18.dp)) {
                        Box(
                            modifier = Modifier.size(48.dp).shadow(6.dp, shape = CircleShape, clip = false),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (darkTheme) Color(0xFF1E1E1E) else Color.White, CircleShape),
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
                                            .background(MaterialTheme.colorScheme.error, CircleShape)
                                            .border(1.dp, if (darkTheme) Color(0xFF1E1E1E) else Color.White, CircleShape)
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
        val isRetakeState = remember(cameraActive, scanResult, imageUrl, localImagePath) {
            !cameraActive && (scanResult != null || imageUrl != null || localImagePath != null)
        }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 27.dp) // Add space for floating navbar
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 75.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                when {
                    localImagePath != null -> {
                        AsyncImage(
                            model = File(localImagePath!!),
                            contentDescription = "Captured Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    imageUrl != null -> {
                        AsyncImage(
                            model = "${imageUrl}?key=$reloadKey",
                            contentDescription = "Scanned Image",
                            modifier = Modifier.fillMaxSize()
                        )
                    }



                    showLivePreview && usePiCamera -> {
                        var isRefreshing by remember { mutableStateOf(false) }
                        val piStatus by piStatusManager.piStatus.collectAsState()

                        // 🔧 Keep serverUrl in sync with connectionMode + Pi status
                        LaunchedEffect(connectionMode, piStatus) {
                            serverUrl = when (connectionMode.lowercase()) {
                                "lan" -> if (!piStatus.ipAddress.isNullOrEmpty()) {
                                    "http://${piStatus.ipAddress}:5000"
                                } else ""
                                "cloudflare" -> piStatus.cloudflareUrl ?: ""
                                else -> { // auto
                                    if (!piStatus.ipAddress.isNullOrEmpty()) {
                                        "http://${piStatus.ipAddress}:5000"
                                    } else {
                                        piStatus.cloudflareUrl ?: ""
                                    }
                                }
                            }
                            Log.d("ScanScreen", "✅ Active serverUrl: $serverUrl (mode=$connectionMode)")
                        }

                        Box(
                            modifier = Modifier
                                .then(
                                    if (!piStatus.isOnline || serverUrl.isEmpty()) {
                                        Modifier
                                            .background(
                                                if (darkTheme) Color(0xFF2A2A2A)
                                                else Color(0xFFF5F5F5)
                                            )
                                            .padding(16.dp)
                                    } else {
                                        Modifier // ✅ No padding or background when online
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (piStatus.isOnline) {
                                // ✅ Prefer LAN if available
                                val activeUrl = when (connectionMode) {
                                    "lan" -> if (!piStatus.ipAddress.isNullOrEmpty()) {
                                        "http://${piStatus.ipAddress}:5000"
                                    } else null

                                    "cloudflare" -> piStatus.serverUrl

                                    else -> { // auto (default)
                                        if (!piStatus.ipAddress.isNullOrEmpty()) {
                                            "http://${piStatus.ipAddress}:5000"
                                        } else {
                                            piStatus.serverUrl
                                        }
                                    }
                                }

                                var showUrl by remember { mutableStateOf(false) }
                                val context = LocalContext.current

                                if (!activeUrl.isNullOrEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // WebView (Pi Preview)
                                        AndroidView(
                                            factory = { ctx ->
                                                WebView(ctx).apply {
                                                    settings.javaScriptEnabled = true
                                                    webViewClient = WebViewClient()
                                                    val headers = mapOf("ngrok-skip-browser-warning" to "true")
                                                    this.loadUrl("$activeUrl/live-tracking?key=$reloadKey", headers)
                                                    rotation = 270f
                                                }
                                            },
                                            modifier = Modifier
                                                .size(480.dp)
                                                .graphicsLayer { rotationZ = 90f }
                                        )

                                        // ✅ Reset refreshing state once WebView loads
                                        LaunchedEffect(Unit) { isRefreshing = false }

                                        // "More" toggle button (top-right)
                                        IconButton(
                                            onClick = { showUrl = !showUrl },
                                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "More",
                                                tint = Color.White
                                            )
                                        }

                                        // Show link + copy icon if toggled
                                        if (showUrl) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(top = 48.dp, end = 12.dp) // below the More button
                                                    .background(Color(0x66000000), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = activeUrl,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(end = 6.dp)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                        val clip = android.content.ClipData.newPlainText("Pi Address", activeUrl)
                                                        clipboard.setPrimaryClip(clip)
                                                        Toast.makeText(context, "Address copied!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(18.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copy",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                            }

                            /*
                            else {
                                FlappyCatfish()
                            }*/


                            else {
                                // ❌ Friendly Offline Screen (Centered & Responsive)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp)
                                        .padding(bottom = 42.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth(0.95f) // responsive max width
                                            .wrapContentHeight()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudOff,
                                            contentDescription = "Pi Offline",
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(90.dp)
                                        )

                                        Text(
                                            text = "Unable to connect to Raspberry Pi",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 18.sp
                                            ),
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )

                                        Text(
                                            text = "Your Pi might be offline or server is not available.\n" +
                                                    "Make sure it's running and try again.",
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = {
                                                isRefreshing = true
                                                reloadKey++   // reload WebView
                                                piStatusManager.startMonitoring(connectionMode)

                                                scope.launch {
                                                    delay(3000)
                                                    isRefreshing = false
                                                }
                                            },
                                            contentPadding = PaddingValues(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                            shape = RoundedCornerShape(50),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        brush = Brush.linearGradient(
                                                            colors = listOf(Color(0xFF5DCCFC), Color(0xFF007EF2))
                                                        ),
                                                        shape = RoundedCornerShape(50)
                                                    )
                                                    .fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isRefreshing) {
                                                    CircularProgressIndicator(
                                                        color = Color.White,
                                                        strokeWidth = 2.dp,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                } else {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Replay,
                                                            contentDescription = "Retry",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "Reconnect Pi",
                                                            color = Color.White,
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }





                    showLivePreview && !usePiCamera && cameraPermissionGranted.value && cameraActive -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()

                        ) {
                            CameraPreviewBox(context = context) { capture, provider, control, info ->
                                imageCapture = capture
                                cameraProvider = provider
                                cameraControl = control
                                maxZoom = info.zoomState.value?.maxZoomRatio ?: 1f
                                minZoom = info.zoomState.value?.minZoomRatio ?: 1f
                            }
                        }
                    }
                    else -> {
                        Image(
                            painter = painterResource(id = R.drawable.ic_scan_placeholder),
                            contentDescription = "Placeholder",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Pi Status Indicator (TOP LEFT)
                PiStatusIndicator(
                    piStatusManager = piStatusManager,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    showDetailedInfo = true,
                    onStatusClick = { showPiStatusDialog = true }
                )

                // Flash toggle (TOP RIGHT)
                if (!usePiCamera) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .graphicsLayer {
                                alpha = if (isLoading) 0.5f else 1f
                            }
                            .background(Color(0xAA000000), shape = CircleShape)
                            .clickable(
                                enabled = !isLoading,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    flashEnabled = !flashEnabled
                                    cameraControl?.enableTorch(flashEnabled)
                                }
                            )
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Toggle Flash",
                            tint = Color.White
                        )
                    }
                }

                val interactionSource = remember { MutableInteractionSource() }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Upload (left)
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .graphicsLayer {
                                alpha = if (isLoading) 0.5f else 1f
                            }
                            .background(Color(0xAA000000), shape = CircleShape)
                            .clickable(enabled = !isLoading,
                                       indication = null,
                                       interactionSource = remember { MutableInteractionSource() }
                            ) {
                                galleryLauncher.launch("image/*")
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Upload",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(40.dp))

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    // Animated Capture Button with scan logic

                    val innerSize by animateDpAsState(
                        targetValue = if (isPressed) 60.dp else 72.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    fun startScan() {
                        isLoading = true

                        if (usePiCamera) {
                            scanJob = CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    if (serverUrl.isBlank()) {
                                        withContext(Dispatchers.Main) {
                                            scanResult = "No Raspberry Pi URL found. Please check Firebase."
                                            cameraActive = false
                                            isLoading = false
                                        }
                                        return@launch
                                    }

                                    // ✅ normalize URL
                                    val fixedUrl = if (serverUrl.startsWith("http://") || serverUrl.startsWith("https://")) {
                                        serverUrl
                                    } else {
                                        "http://$serverUrl"
                                    }

                                    val api = createRetrofit(fixedUrl)
                                    val response = withTimeoutOrNull(7000) { api.scanFromPi(uid ?: "admin") }

                                    withContext(Dispatchers.Main) {
                                        if (response == null) {
                                            scanResult = "Request timeout. Make sure your Pi is online."
                                            cameraActive = false
                                        } else {
                                            val responseBody = response.body()
                                            if (response.isSuccessful && responseBody?.status == "success") {
                                                imageUrl = "$serverUrl/${responseBody.image_url?.trimStart('/')}"
                                                scanResult = responseBody.result ?: "No result"
                                                showLivePreview = false
                                                cameraActive = false
                                                reloadKey++
                                            } else {
                                                val errMsg = responseBody?.message ?: response.errorBody()?.string() ?: "Unknown error"
                                                scanResult = "Scan failed: $errMsg"
                                                cameraActive = false
                                            }
                                        }
                                        isLoading = false
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        scanResult = "Error: ${e.message}"
                                        cameraActive = false
                                        isLoading = false
                                    }
                                }
                            }
                        } else {
                            // phone capture branch — also use fixedUrl instead of ipAddress
                            val file = File(context.cacheDir, "capture_${UUID.randomUUID()}.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                            if (imageCapture == null) {
                                scanResult = "Camera not ready"
                                cameraActive = false
                                isLoading = false
                                return
                            }

                            imageCapture?.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        localImagePath = file.absolutePath
                                        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                                        val body = MultipartBody.Part.createFormData("image", file.name, requestBody)
                                        val uidBody = uid?.toRequestBody("text/plain".toMediaTypeOrNull())

                                        scanJob = CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                if (serverUrl.isBlank() || uidBody == null) {
                                                    withContext(Dispatchers.Main) {
                                                        scanResult = "Missing UID or Pi URL"
                                                        cameraActive = false
                                                        isLoading = false
                                                    }
                                                    return@launch
                                                }

                                                val fixedUrl = if (serverUrl.startsWith("http")) serverUrl else "http://$serverUrl"
                                                val api = createRetrofit(fixedUrl)
                                                val response = api.scanPhoneImage(body, uidBody)

                                                withContext(Dispatchers.Main) {
                                                    val responseBody = response.body()
                                                    if (response.isSuccessful && responseBody?.status == "success") {
                                                        imageUrl = responseBody.image_url
                                                        scanResult = responseBody.result ?: "No result"
                                                        showLivePreview = false
                                                        cameraActive = false
                                                        reloadKey++
                                                        cameraProvider?.unbindAll()
                                                    } else {
                                                        val errMsg = responseBody?.message ?: response.errorBody()?.string() ?: "Unknown error"
                                                        scanResult = "Scan failed: $errMsg"
                                                        cameraActive = false
                                                    }
                                                    isLoading = false
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    scanResult = "Error: ${e.message}"
                                                    cameraActive = false
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    }

                                    override fun onError(exc: ImageCaptureException) {
                                        scanResult = "Capture failed"
                                        cameraActive = false
                                        isLoading = false
                                    }
                                }
                            )
                        }
                    }


                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .graphicsLayer {
                                alpha = if (isLoading) 0.4f else 1f
                            }
                            .background(Color(0xAA000000), shape = CircleShape)
                            .clickable(
                                enabled = !isLoading,
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                if (isRetakeState) {
                                    scanResult = null
                                    imageUrl = null
                                    localImagePath = null
                                    showLivePreview = true
                                    cameraActive = true
                                    reloadKey++
                                    isLoading = false
                                } else {
                                    startScan()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Ring
                        if (isLoading) {
                            // Spinner when loading
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(36.dp)
                            )
                        } else {
                            // Ring
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .border(
                                        width = 2.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Inner bouncing circle with optional retake icon
                                Box(
                                    modifier = Modifier
                                        .size(innerSize)
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isRetakeState) {
                                        Icon(
                                            imageVector = Icons.Default.Replay,
                                            contentDescription = "Retake",
                                            tint = Color.DarkGray,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.BugReport, // or any icon you like
                                            contentDescription = "Scan",
                                            tint = Color.DarkGray,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }

                                }

                            }
                        }

                    }


                    Spacer(modifier = Modifier.width(40.dp))

                    // Switch (right)
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .graphicsLayer {
                                alpha = if (isLoading) 0.5f else 1f
                            }
                            .background(Color(0xAA000000), shape = CircleShape)
                            .clickable(enabled = !isLoading,
                                       indication = null,
                                       interactionSource = remember { MutableInteractionSource() }
                            ) {
                                showLivePreview = true
                                usePiCamera = !usePiCamera
                                cameraActive = true
                                scanResult = null
                                imageUrl = null
                                localImagePath = null
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cached,
                            contentDescription = "Switch Camera",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }


            }

            val currentScanResult = scanResult // ✅ copy to local variable

            if (currentScanResult != null && currentScanResult.isNotBlank()) {
                val diagnosisText = remember(currentScanResult) {
                    val text = currentScanResult.trim()
                    when {
                        text.contains("no infection", ignoreCase = true) -> {
                            "✅ No infection detected.\nYour fish appear healthy."
                        }
                        text.contains("error", ignoreCase = true)
                                || text.contains("failed", ignoreCase = true)
                                || text.contains("timeout", ignoreCase = true)
                                || text.contains("No Pi IP", ignoreCase = true) -> {
                            "❌ Error:\n$text"
                        }
                        else -> {
                            "⚠️ Possible infection detected:\n$text\n"
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 20.dp, end = 20.dp, bottom = 205.dp)
                        .background(Color(0xAA000000), shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = diagnosisText,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }




        }
        
        // Pi Status Dialog
        if (showPiStatusDialog) {
            PiStatusDialog(
                piStatusManager = piStatusManager,
                onDismiss = { showPiStatusDialog = false }
            )
        }
    }
}



/*
@Composable
fun WaterBackground(isDarkMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    // Wave animation
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // Bubble animations (just a few for simplicity)
    val bubble1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble1"
    )
    val bubble2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, delayMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // 🌊 Waves
        val waveColor = if (isDarkMode) {
            Color(0xFF4BA3C7).copy(alpha = 0.3f)
        } else {
            Color(0xFF5DCCFC).copy(alpha = 0.4f)
        }

        fun drawWave(phase: Float, amplitude: Float, baseY: Float, frequency: Float) {
            val path = Path()
            val step = 2f
            path.moveTo(0f, baseY)
            var x = 0f
            while (x <= size.width) {
                val angle = (x / size.width) * 2f * Math.PI.toFloat() * frequency + phase
                val y = baseY + amplitude * kotlin.math.sin(angle).toFloat()
                path.lineTo(x, y)
                x += step
            }
            path.lineTo(size.width, size.height)
            path.lineTo(0f, size.height)
            path.close()
            drawPath(path, color = waveColor)
        }

        drawWave(wavePhase, 30f, size.height * 0.7f, 1.2f)
        drawWave(wavePhase * 0.7f, 20f, size.height * 0.8f, 0.9f)

        // 🫧 Bubbles
        val bubbleColor = if (isDarkMode) {
            Color(0xFF4BA3C7).copy(alpha = 0.6f)
        } else {
            Color(0xFF5DCCFC).copy(alpha = 0.7f)
        }

        val bubble1Y = size.height * (1f + bubble1Offset)
        if (bubble1Y < size.height) {
            drawCircle(bubbleColor, radius = 8f, center = Offset(size.width * 0.25f, bubble1Y))
        }
        val bubble2Y = size.height * (1f + bubble2Offset)
        if (bubble2Y < size.height) {
            drawCircle(bubbleColor, radius = 10f, center = Offset(size.width * 0.7f, bubble2Y))
        }
    }
}



@Composable
fun FlappyCatfish(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()

    // Fish Y position (0f = top, 1f = bottom)
    val fishX = 0.5f // center of screen
    val fishY = remember { Animatable(0.5f) }
    var velocity by remember { mutableStateOf(0f) }

    // Obstacles
    data class Obstacle(val x: Float, val gapCenter: Float, val passed: Boolean = false)
    var obstacles by remember { mutableStateOf(listOf<Obstacle>()) }

    // State
    var score by remember { mutableStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }


    // Physics
    val gravity = 0.0008f
    val flapPower = -0.015f
    val obstacleSpeed = 0.0035f
    val gapSize = 0.28f

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
            .background(Color(0xFFE0F7FA))
    ) {

        val density = LocalDensity.current
        val playWidth = maxWidth   // already Dp
        val playHeight = maxHeight // already Dp


        // 🎮 Game loop
        LaunchedEffect(started, isGameOver) {
            if (started && !isGameOver) {
                fishY.snapTo(0.5f)
                velocity = 0f
                obstacles = listOf(Obstacle(1f, 0.5f))
                score = 0

                while (true) {
                    velocity += gravity
                    fishY.snapTo(fishY.value + velocity)

                    // Move obstacles
                    obstacles = obstacles.map { it.copy(x = it.x - obstacleSpeed) }

// Add new obstacle when last one is far enough
                    if ((obstacles.lastOrNull()?.x ?: 0f) < 0.6f) {
                        obstacles = obstacles + Obstacle(
                            x = 1f,
                            gapCenter = (0.3f + Math.random().toFloat() * 0.4f)
                        )
                    }

// Score when fish passes obstacle
                    val fishXPos = fishX
                    obstacles.forEachIndexed { index, obs ->
                        if (!obs.passed && obs.x + 0.05f < fishXPos) {
                            score++
                            obstacles = obstacles.toMutableList().also {
                                it[index] = it[index].copy(passed = true)
                            }
                        }
                    }

// Remove obstacles that are fully off-screen
                    if (obstacles.firstOrNull()?.x ?: 0f < -0.3f) {
                        obstacles = obstacles.drop(1)
                    }


                    // Collision detection
                    val fishTop = fishY.value - 0.04f
                    val fishBottom = fishY.value + 0.04f



                    obstacles.forEach { obs ->
                        if (obs.x < fishX + 0.05f && obs.x > fishX - 0.05f) {
                            val gapTop = obs.gapCenter - gapSize / 2
                            val gapBottom = obs.gapCenter + gapSize / 2
                            if (fishTop < gapTop || fishBottom > gapBottom) {
                                isGameOver = true
                            }
                        }
                    }

                    // Bounds
                    if (fishY.value <= 0f || fishY.value >= 1f) {
                        isGameOver = true
                    }

                    delay(16)
                }
            }
        }



        if (isGameOver) {
            // ☠️ Game Over UI
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Game Over", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                Text("Score: $score", fontSize = 16.sp, color = Color.DarkGray)
                Button(onClick = {
                    isGameOver = false
                    started = false
                    obstacles = listOf()
                    scope.launch { fishY.snapTo(0.5f) }
                }) { Text("Restart") }
            }
        } else {
            // 🐟 Gameplay Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null, // ✅ no ripple
                        interactionSource = interactionSource
                    ) {
                        if (!started) started = true
                        if (!isGameOver) velocity = flapPower
                    }
            ) {
                // 🐟 Fish
                Box(
                    modifier = Modifier
                        .offset(
                            x = fishX * playWidth - 20.dp,   // 20.dp = half of fish width, centers it
                            y = fishY.value * playHeight
                        )
                        .size(40.dp, 28.dp)
                        .background(Color(0xFF1976D2), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐟", fontSize = 20.sp)
                }


// 🍄 Obstacles
                obstacles.forEach { obs ->
                    val gapTop = obs.gapCenter - gapSize / 2
                    val gapBottom = obs.gapCenter + gapSize / 2

                    // Top fungus
                    Box(
                        modifier = Modifier
                            .offset(x = obs.x * playWidth, y = 0.dp)
                            .width(50.dp)
                            .height(gapTop * playHeight)
                            .background(Color(0xFF8BC34A)),
                        contentAlignment = Alignment.Center
                    ) { Text("🍄", fontSize = 20.sp) }

                    // Bottom fungus
                    Box(
                        modifier = Modifier
                            .offset(x = obs.x * playWidth, y = gapBottom * playHeight)
                            .width(50.dp)
                            .height((1f - gapBottom) * playHeight)
                            .background(Color(0xFF8BC34A)),
                        contentAlignment = Alignment.Center
                    ) { Text("🍄", fontSize = 20.sp) }
                }

                // HUD
                if (started) {
                    Text(
                        "Score: $score",
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 30.dp),
                        fontSize = 18.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                } else if (!started) {
                    Text(
                        "Tap to Start",
                        modifier = Modifier
                            .offset(
                                x = (fishX * playWidth) - 55.dp, // centered under fish
                                y = (fishY.value * playHeight) + 50.dp
                            ),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }


            }
        }
    }
}
*/