package com.capstone.safehito.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.capstone.safehito.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.capstone.safehito.util.uriToBase64
import com.capstone.safehito.util.base64ToBitmap
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import java.io.ByteArrayOutputStream

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    navController: NavHostController,
    onLogout: () -> Unit,
    darkTheme: Boolean
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    val db = FirebaseDatabase.getInstance().reference

    var fullName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var imageBase64 by remember { mutableStateOf<String?>(null) }

    var isEditing by remember { mutableStateOf(false) }
    var showImageOptions by remember { mutableStateOf(false) }
    var showRemoveConfirmation by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showManageAccountDialog by remember { mutableStateOf(false) }
    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showPasswordForEmailDialog by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var currentPasswordForEmail by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }


    val focusManager = LocalFocusManager.current

    val backgroundColor = if (darkTheme) Color(0xFF121212) else Color(0xFFF4F8FB)

    fun saveProfile() {
        uid?.let { id ->
            db.child("users").child(id).updateChildren(
                mapOf("fullName" to fullName, "contactNumber" to contactNumber)
            ).addOnSuccessListener {
                Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                isEditing = false
                focusManager.clearFocus()
            }
        }
    }

    val scrollState = rememberScrollState()

    // Set status bar color to match background
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = backgroundColor,
            darkIcons = !darkTheme // true for light background, false for dark
        )
    }

    var totalScans by remember { mutableStateOf(0) }
    var lastScanDate by remember { mutableStateOf("No scans yet") }
    var isLoading by remember { mutableStateOf(true) }
    var totalUsers by remember { mutableStateOf(0) }
    var userRole by remember { mutableStateOf("") }

    fun refreshProfileData() {
        uid?.let {
            db.child("users").child(it).get().addOnSuccessListener { snapshot ->
                fullName = snapshot.child("fullName").getValue<String>() ?: ""
                contactNumber = snapshot.child("contactNumber").getValue<String>() ?: ""
                email = auth.currentUser?.email ?: ""
                imageBase64 = snapshot.child("profileImageBase64").getValue<String>()
                userRole = snapshot.child("role").getValue<String>() ?: ""
            }
            
            // Fetch total users count and all scans from all users
            db.child("users").get().addOnSuccessListener { usersSnapshot ->
                totalUsers = usersSnapshot.childrenCount.toInt()
                
                var totalScansCount = 0
                var latestTimestamp = 0L
                
                // Iterate through all users to get their scans
                for (userSnapshot in usersSnapshot.children) {
                    val scansSnapshot = userSnapshot.child("scans")
                    totalScansCount += scansSnapshot.childrenCount.toInt()
                    
                    // Find the latest scan from this user
                    for (scanSnapshot in scansSnapshot.children) {
                        val timestamp = scanSnapshot.child("timestamp").getValue<Long>() ?: 0L
                        if (timestamp > latestTimestamp) {
                            latestTimestamp = timestamp
                        }
                    }
                }
                
                // Update the total scans count
                totalScans = totalScansCount
                
                // Update the last scan date
                if (latestTimestamp != 0L) {
                    val date = java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault())
                        .format(java.util.Date(latestTimestamp * 1000))
                    lastScanDate = date
                } else {
                    lastScanDate = "No scans yet"
                }
                
                isLoading = false
            }
        }
    }

    LaunchedEffect(uid) {
        refreshProfileData()
    }

    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
            if (uriContent != null) {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uriContent)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uriContent)
                    ImageDecoder.decodeBitmap(source)
                }
                Log.d("Cropper", "Bitmap size: ${bitmap.width} x ${bitmap.height}")
                if (bitmap.width == 0 || bitmap.height == 0) {
                    Toast.makeText(context, "Failed to load cropped image!", Toast.LENGTH_SHORT).show()
                    return@rememberLauncherForActivityResult
                }
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                val byteArray = outputStream.toByteArray()
                val base64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
                imageBase64 = base64
                // Save to Firebase
                if (base64 != null && uid != null) {
                    db.child("users").child(uid).child("profileImageBase64")
                        .setValue(base64)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_LONG).show()
                            Log.e("FirebaseUpload", "Failed to upload image: ${it.message}")
                        }
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            cropImageLauncher.launch(
                com.canhub.cropper.CropImageContractOptions(
                    uri,
                    CropImageOptions().apply {
                        guidelines = com.canhub.cropper.CropImageView.Guidelines.ON
                        aspectRatioX = 1
                        aspectRatioY = 1
                        fixAspectRatio = true
                    }
                )
            )
        }
    }

    @Composable
    fun AdminProfileInfoRow(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        isEditing: Boolean,
        onSave: (() -> Unit)? = null
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onSave?.invoke()
                        }
                    )
                )
            } else {
                if (label == "Full Name") {
                    // Show name with verified badge for admin/superadmin
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = if (userRole == "superadmin") "Verified Superadmin" else "Verified Admin",
                            tint = if (userRole == "superadmin") Color(0xFFFFD700) else Color(0xFF1DA1F2), // Gold for superadmin, Twitter blue for admin
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text("SafeHito Admin", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            if (isEditing) "Edit Profile" else "Profile",
                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (darkTheme) Color(0xFF1E1E1E) else Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = "Edit Profile",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // 🔧 Settings Button
                    IconButton(onClick = { navController.navigate("settings") }, modifier = Modifier.padding(end = 18.dp)) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (darkTheme) Color(0xFF1E1E1E) else Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_settings),
                                    contentDescription = "Settings",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
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
                        // Refresh profile data
                        refreshProfileData()
                        // Delay to show refresh indicator
                        delay(1000)
                    } catch (e: Exception) {
                        Log.e("AdminProfileRefresh", "Error during refresh: ${e.message}")
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    }
                    .padding(bottom = 75.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (darkTheme) Color(0xFF1A1A1A) else Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // --- Discord-Style Profile Image ---
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clickable { showImageOptions = true },
                                contentAlignment = Alignment.Center
                            ) {
                                // Discord-style animated border (outer ring)
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .background(
                                            brush = Brush.sweepGradient(
                                                colors = if (userRole == "superadmin") {
                                                    listOf(
                                                        Color(0xFFFFD700), // Gold
                                                        Color(0xFFFF8C00), // Darker gold
                                                        Color(0xFFFF6B00), // Even darker gold
                                                        Color(0xFFFF4500), // Darkest gold
                                                        Color(0xFFFFD700)  // Back to start
                                                    )
                                                } else {
                                                    listOf(
                                                        Color(0xFF5DCCFC), // Primary blue
                                                        Color(0xFF4FC3F7), // Light blue
                                                        Color(0xFF29B6F6), // Medium blue
                                                        Color(0xFF1E88E5), // Dark blue
                                                        Color(0xFF1565C0), // Deeper blue
                                                        Color(0xFF0D47A1), // Darkest blue
                                                        Color(0xFF5DCCFC)  // Back to start
                                                    )
                                                }
                                            ),
                                            shape = CircleShape
                                        )
                                        .padding(3.dp)
                                        .background(
                                            color = if (darkTheme) Color(0xFF2C2F33) else Color(0xFFF6F6F7),
                                            shape = CircleShape
                                        )
                                ) {
                                    // Profile image
                                    if (imageBase64 != null) {
                                        val bitmap = base64ToBitmap(imageBase64!!)
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Profile Picture",
                                                modifier = Modifier
                                                    .size(114.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.default_profile),
                                                contentDescription = "Profile Picture",
                                                modifier = Modifier
                                                    .size(114.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.default_profile),
                                            contentDescription = "Profile Picture",
                                            modifier = Modifier
                                                .size(114.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // --- Profile Details ---
                            AdminProfileInfoRow("Full Name", fullName, { fullName = it }, isEditing, onSave = { saveProfile() })
                            AdminProfileInfoRow("Contact Number", contactNumber, { contactNumber = it }, isEditing, onSave = { saveProfile() })
                            AdminProfileInfoRow("Email", email, {}, false)
                            
                            // Email verification status
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Email Status", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (auth.currentUser?.isEmailVerified == true) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Email Verified",
                                            tint = Color.Green,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Verified", style = MaterialTheme.typography.bodyMedium, color = Color.Green)
                                    } else {
                                        Text("Email Not Yet Verified", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                    }
                                }
                            }
                            


                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Users", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("$totalUsers", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Scans", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("$totalScans", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Last Scan", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(lastScanDate, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // --- Admin Buttons ---
                            OutlinedButton(
                                onClick = { showManageAccountDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Manage Account")
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            if (isEditing) {
                                Button(
                                    onClick = { saveProfile() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFF5DCCFC), Color(0xFF007EF2)),
                                                start = Offset(0f, 0f),
                                                end = Offset.Infinite
                                            ),
                                            RoundedCornerShape(50)
                                        ),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Save")
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { showLogoutConfirmation = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFB71C1C), // dark red
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Log Out")
                            }
                        }
                    }
                }
            }
        }

        // Add this state
        var showFullImage by remember { mutableStateOf(false) }

        if (showImageOptions) {
            AlertDialog(
                onDismissRequest = { showImageOptions = false },
                confirmButton = {
                    // Provide an empty confirm button to avoid errors
                },
                title = { Text("Profile Picture") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .clickable {
                                    showFullImage = true // open full image on click
                                }
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageBase64 != null) {
                                val bitmap = base64ToBitmap(imageBase64!!)
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Current Profile Picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.default_profile),
                                        contentDescription = "Default Profile",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.default_profile),
                                    contentDescription = "Default Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Change Profile Picture",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    imagePickerLauncher.launch("image/*")
                                    showImageOptions = false
                                }
                                .padding(vertical = 8.dp)
                        )
                        Divider()
                        Text(
                            "Remove Profile Picture",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showImageOptions = false
                                    showRemoveConfirmation = true
                                }
                                .padding(vertical = 8.dp)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImageOptions = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }

        if (showRemoveConfirmation) {
            AlertDialog(
                onDismissRequest = { showRemoveConfirmation = false },
                title = { Text("Confirm Removal") },
                text = { Text("Are you sure you want to remove your profile picture?") },
                confirmButton = {
                    TextButton(onClick = {
                        uid?.let {
                            db.child("users").child(it).child("profileImageBase64").removeValue()
                                .addOnSuccessListener {
                                    imageBase64 = null
                                    Toast.makeText(context, "Profile picture removed.", Toast.LENGTH_SHORT).show()
                                }
                        }
                        showRemoveConfirmation = false
                    }) {
                        Text("REMOVE", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveConfirmation = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }

        if (showLogoutConfirmation) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmation = false },
                title = { Text("Confirm Logout") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    TextButton(onClick = {
                        auth.signOut()
                        onLogout()
                        showLogoutConfirmation = false
                    }) {
                        Text("LOG OUT", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirmation = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }

        // Manage Account Dialog
        if (showManageAccountDialog) {
            AlertDialog(
                onDismissRequest = { showManageAccountDialog = false },
                title = { Text("Manage Account") },
                text = {
                    Column {
                        Text(
                            "Change Email",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showManageAccountDialog = false
                                    showPasswordForEmailDialog = true
                                }
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Divider()
                        Text(
                            "Change Password",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showManageAccountDialog = false
                                    showChangePasswordDialog = true
                                }
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showManageAccountDialog = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }

        // Password for Email Dialog
        if (showPasswordForEmailDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showPasswordForEmailDialog = false
                    currentPasswordForEmail = ""
                    passwordVisible = false
                },
                title = { Text("Enter Password") },
                text = {
                    Column {
                        Text(
                            "Please enter your current password to change your email address.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        OutlinedTextField(
                            value = currentPasswordForEmail,
                            onValueChange = { currentPasswordForEmail = it },
                            label = { Text("Current Password") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            visualTransformation = if (passwordVisible) {
                                androidx.compose.ui.text.input.VisualTransformation.None
                            } else {
                                androidx.compose.ui.text.input.PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible }
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) {
                                            Icons.Default.VisibilityOff
                                        } else {
                                            Icons.Default.Visibility
                                        },
                                        contentDescription = if (passwordVisible) {
                                            "Hide password"
                                        } else {
                                            "Show password"
                                        }
                                    )
                                }
                            },
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (currentPasswordForEmail.isNotEmpty()) {
                                        // Verify password first
                                        val credential = com.google.firebase.auth.EmailAuthProvider
                                            .getCredential(auth.currentUser?.email ?: "", currentPasswordForEmail)
                                        
                                        auth.currentUser?.reauthenticate(credential)
                                            ?.addOnSuccessListener {
                                                Log.d("EmailChange", "Password verification successful")
                                                showPasswordForEmailDialog = false
                                                showChangeEmailDialog = true
                                                passwordVisible = false
                                            }
                                            ?.addOnFailureListener { e ->
                                                Log.e("EmailChange", "Password verification failed: ${e.message}")
                                                Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                                                currentPasswordForEmail = ""
                                            }
                                    } else {
                                        Toast.makeText(context, "Please enter your current password", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (currentPasswordForEmail.isEmpty()) {
                                Toast.makeText(context, "Please enter your current password", Toast.LENGTH_SHORT).show()
                            } else {
                                // Verify password first
                                val credential = com.google.firebase.auth.EmailAuthProvider
                                    .getCredential(auth.currentUser?.email ?: "", currentPasswordForEmail)
                                
                                auth.currentUser?.reauthenticate(credential)
                                    ?.addOnSuccessListener {
                                        showPasswordForEmailDialog = false
                                        showChangeEmailDialog = true
                                        passwordVisible = false
                                    }
                                    ?.addOnFailureListener { e ->
                                        Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                                        currentPasswordForEmail = ""
                                    }
                            }
                        }
                    ) {
                        Text("CONTINUE")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showPasswordForEmailDialog = false
                            currentPasswordForEmail = ""
                            passwordVisible = false
                        }
                    ) {
                        Text("CANCEL")
                    }
                }
            )
        }

        // Change Email Dialog
        if (showChangeEmailDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showChangeEmailDialog = false
                    newEmail = ""
                },
                title = { Text("Change Email") },
                text = {
                    Column {
                        Text(
                            "Enter your new email address:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("New Email") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                                imeAction = ImeAction.Done
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            when {
                                newEmail.isEmpty() -> {
                                    Toast.makeText(context, "Please enter a new email address", Toast.LENGTH_SHORT).show()
                                }
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches() -> {
                                    Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    // Check if current email is verified first
                                    if (auth.currentUser?.isEmailVerified == true) {
                                        // Re-authenticate again before changing email to ensure session is valid
                                        val credential = com.google.firebase.auth.EmailAuthProvider
                                            .getCredential(auth.currentUser?.email ?: "", currentPasswordForEmail)
                                        
                                        auth.currentUser?.reauthenticate(credential)
                                            ?.addOnSuccessListener {
                                                // Now change the email in Firebase Auth
                                                auth.currentUser?.updateEmail(newEmail)
                                                    ?.addOnSuccessListener {
                                                        // Also update email in Realtime Database
                                                        uid?.let { userId ->
                                                            db.child("users").child(userId).child("email").setValue(newEmail)
                                                                .addOnSuccessListener {
                                                                    Toast.makeText(context, "Email updated successfully!", Toast.LENGTH_SHORT).show()
                                                                    email = newEmail
                                                                    showChangeEmailDialog = false
                                                                    newEmail = ""
                                                                    currentPasswordForEmail = ""
                                                                }
                                                                .addOnFailureListener { dbError ->
                                                                    Log.e("EmailChange", "Failed to update email in database: ${dbError.message}")
                                                                    Toast.makeText(context, "Email updated in Auth but failed to update in database. Please try again.", Toast.LENGTH_LONG).show()
                                                                }
                                                        }
                                                    }
                                                    ?.addOnFailureListener { e ->
                                                        Log.e("EmailChange", "Failed to update email: ${e.message}")
                                                        when {
                                                            e.message?.contains("requires recent authentication") == true -> {
                                                                Toast.makeText(context, "Session expired. Please try again.", Toast.LENGTH_LONG).show()
                                                                showChangeEmailDialog = false
                                                                showPasswordForEmailDialog = true
                                                            }
                                                            e.message?.contains("not allowed") == true -> {
                                                                Toast.makeText(context, "Email change not allowed. Please contact support.", Toast.LENGTH_LONG).show()
                                                            }
                                                            else -> {
                                                                Toast.makeText(context, "Failed to update email: ${e.message}", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                    }
                                            }
                                            ?.addOnFailureListener { e ->
                                                Log.e("EmailChange", "Re-authentication failed: ${e.message}")
                                                Toast.makeText(context, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show()
                                                showChangeEmailDialog = false
                                                showPasswordForEmailDialog = true
                                            }
                                    } else {
                                        // Email not verified - send verification email first
                                        auth.currentUser?.sendEmailVerification()
                                            ?.addOnSuccessListener {
                                                Toast.makeText(context, "Please verify your current email first. Verification email sent.", Toast.LENGTH_LONG).show()
                                                showChangeEmailDialog = false
                                            }
                                            ?.addOnFailureListener { e ->
                                                Log.e("EmailChange", "Failed to send verification email: ${e.message}")
                                                Toast.makeText(context, "Failed to send verification email. Please try again.", Toast.LENGTH_LONG).show()
                                            }
                                    }
                                }
                            }
                        }
                    ) {
                        Text("UPDATE")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showChangeEmailDialog = false
                            newEmail = ""
                        }
                    ) {
                        Text("CANCEL")
                    }
                }
            )
        }

        // Change Password Dialog
        if (showChangePasswordDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showChangePasswordDialog = false
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                    currentPasswordVisible = false
                    newPasswordVisible = false
                    confirmPasswordVisible = false
                },
                title = { Text("Change Password") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Current Password") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            visualTransformation = if (currentPasswordVisible) {
                                androidx.compose.ui.text.input.VisualTransformation.None
                            } else {
                                androidx.compose.ui.text.input.PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { currentPasswordVisible = !currentPasswordVisible }
                                ) {
                                    Icon(
                                        imageVector = if (currentPasswordVisible) {
                                            Icons.Default.VisibilityOff
                                        } else {
                                            Icons.Default.Visibility
                                        },
                                        contentDescription = if (currentPasswordVisible) {
                                            "Hide password"
                                        } else {
                                            "Show password"
                                        }
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            visualTransformation = if (newPasswordVisible) {
                                androidx.compose.ui.text.input.VisualTransformation.None
                            } else {
                                androidx.compose.ui.text.input.PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { newPasswordVisible = !newPasswordVisible }
                                ) {
                                    Icon(
                                        imageVector = if (newPasswordVisible) {
                                            Icons.Default.VisibilityOff
                                        } else {
                                            Icons.Default.Visibility
                                        },
                                        contentDescription = if (newPasswordVisible) {
                                            "Hide password"
                                        } else {
                                            "Show password"
                                        }
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            visualTransformation = if (confirmPasswordVisible) {
                                androidx.compose.ui.text.input.VisualTransformation.None
                            } else {
                                androidx.compose.ui.text.input.PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                                ) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) {
                                            Icons.Default.VisibilityOff
                                        } else {
                                            Icons.Default.Visibility
                                        },
                                        contentDescription = if (confirmPasswordVisible) {
                                            "Hide password"
                                        } else {
                                            "Show password"
                                        }
                                    )
                                }
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            when {
                                currentPassword.isEmpty() -> {
                                    Toast.makeText(context, "Please enter your current password", Toast.LENGTH_SHORT).show()
                                }
                                newPassword.isEmpty() -> {
                                    Toast.makeText(context, "Please enter a new password", Toast.LENGTH_SHORT).show()
                                }
                                newPassword.length < 6 -> {
                                    Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                }
                                newPassword != confirmPassword -> {
                                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    // Re-authenticate user before changing password
                                    val credential = com.google.firebase.auth.EmailAuthProvider
                                        .getCredential(auth.currentUser?.email ?: "", currentPassword)
                                    
                                    auth.currentUser?.reauthenticate(credential)
                                        ?.addOnSuccessListener {
                                            // Now change the password
                                            auth.currentUser?.updatePassword(newPassword)
                                                ?.addOnSuccessListener {
                                                    Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                                    showChangePasswordDialog = false
                                                    currentPassword = ""
                                                    newPassword = ""
                                                    confirmPassword = ""
                                                    currentPasswordVisible = false
                                                    newPasswordVisible = false
                                                    confirmPasswordVisible = false
                                                }
                                                ?.addOnFailureListener { e ->
                                                    Toast.makeText(context, "Failed to update password: ${e.message}", Toast.LENGTH_LONG).show()
                                                }
                                        }
                                        ?.addOnFailureListener { e ->
                                            Toast.makeText(context, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }
                    ) {
                        Text("UPDATE")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showChangePasswordDialog = false
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                            currentPasswordVisible = false
                            newPasswordVisible = false
                            confirmPasswordVisible = false
                        }
                    ) {
                        Text("CANCEL")
                    }
                }
            )
        }

        if (showFullImage) {
            Dialog(onDismissRequest = { showFullImage = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showFullImage = false },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageBase64 != null) {
                            val bitmap = base64ToBitmap(imageBase64!!)
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Full Profile Picture",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.default_profile),
                                    contentDescription = "Default Profile Picture",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp)
                                )
                            }
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.default_profile),
                                contentDescription = "Default Profile Picture",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            )
                        }
                    }
                }
            }
        }


    }
}

@Composable
fun AdminProfileTextField(label: String, value: String, onValueChange: (String) -> Unit, enabled: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
    }
} 