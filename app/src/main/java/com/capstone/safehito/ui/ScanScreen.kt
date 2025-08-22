package com.capstone.safehito.ui

import android.R.attr.alpha
import android.app.AlertDialog
import android.content.Context
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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material.icons.filled.ControlPointDuplicate
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.FitScreen
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior.getTag
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior.setTag
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
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

    // Start Pi Status monitoring
    LaunchedEffect(Unit) {
        piStatusManager.startMonitoring()
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
                    hasUnread = snapshot.children.any {
                        it.child("read").getValue(Boolean::class.java) == false
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
    var usePiCamera by remember { mutableStateOf(false) }
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

    LaunchedEffect(Unit) {
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("raspberry_pi/ngrok_url")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val url = snapshot.getValue(String::class.java)
                if (!url.isNullOrEmpty()) {
                    serverUrl = url  // ✅ update state automatically
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Firebase error: ${error.message}")
            }
        })
    }


    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
        }
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
                    val currentPiStatus = piStatusManager.piStatus.value
                    if (currentPiStatus.ipAddress == null || uidBody == null) {
                        withContext(Dispatchers.Main) {
                            scanResult = "❌ Missing UID or Raspberry Pi IP."
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.javaScriptEnabled = true
                                        webViewClient = WebViewClient()

                                        // ✅ Always send skip-warning header
                                        val headers = mapOf("ngrok-skip-browser-warning" to "true")

                                        if (serverUrl.isNotEmpty()) {
                                            loadUrl("$serverUrl/live?key=$reloadKey", headers)
                                        }

                                        rotation = 270f // keep your rotation
                                    }
                                },
                                modifier = Modifier
                                    .size(480.dp)
                                    .graphicsLayer {
                                        rotationZ = 90f
                                    }
                            )
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
                                                imageUrl = responseBody.image_url
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

            if (scanResult != null) {
                val diagnosisText = remember(scanResult) {
                    val text = scanResult!!.trim()
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
                            "⚠️ Possible infection detected:\n$text\nPlease consult your veterinarian."
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 205.dp
                        )
                        .background(
                            Color(0xAA000000),
                            shape = RoundedCornerShape(12.dp)
                        )
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

