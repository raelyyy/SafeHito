package com.capstone.safehito.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Verified

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstone.safehito.R
import com.capstone.safehito.model.User
import com.capstone.safehito.viewmodel.AdminViewModel
import com.google.firebase.auth.FirebaseAuth
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.clickable
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.launch

import androidx.navigation.NavHostController
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity

import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import kotlinx.coroutines.delay
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.unit.TextUnit
import androidx.compose.material.icons.filled.MoreHoriz

@Composable
fun AdaptiveText(
    text: String,
    maxWidth: Dp,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.Black,
    defaultFontSize: TextUnit = 16.sp,
    modifier: Modifier = Modifier
) {
    var fontSize by remember { mutableStateOf(defaultFontSize) }
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    
    LaunchedEffect(text, maxWidth) {
        var currentSize = defaultFontSize
        val maxWidthPx = with(density) { maxWidth.toPx() }
        
        while (currentSize > 6.sp) {
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(text),
                style = TextStyle(
                    fontSize = currentSize,
                    fontWeight = fontWeight
                )
            )
            if (textLayoutResult.size.width <= maxWidthPx) {
                fontSize = currentSize
                break
            }
            currentSize = (currentSize.value - 2).sp
        }
    }
    
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdminUserListScreen(
    viewModel: AdminViewModel = viewModel(),
    darkTheme: Boolean,
    navController: NavHostController? = null
) {
    val users by viewModel.users.collectAsState()
    val context = LocalContext.current
    var userToRemove by remember { mutableStateOf<User?>(null) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showImagePreview by remember { mutableStateOf<Pair<Boolean, String?>>(false to null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var currentUserRole by remember { mutableStateOf("") }

    var tankStatus by remember { mutableStateOf("Loading...") }

    var roleFilter by remember { mutableStateOf("All") }
    val roleOptions = listOf("All", "user", "admin", "superadmin")
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val sortOptions = listOf("A-Z", "Z-A", "Newest", "Oldest")
    var selectedSort by remember { mutableStateOf("A-Z") }
    var sortDropdownExpanded by remember { mutableStateOf(false) }

    val filteredUsers = users.filter { user ->
        (roleFilter == "All" || user.role == roleFilter) &&
        (searchQuery.isBlank() || user.fullName.contains(searchQuery, ignoreCase = true) || user.email.contains(searchQuery, ignoreCase = true))
    }.let {
        when (selectedSort) {
            "A-Z" -> it.sortedBy { it.fullName.lowercase() }
            "Z-A" -> it.sortedByDescending { it.fullName.lowercase() }
            "Newest" -> it.sortedByDescending { it.id } // Assuming id is based on creation order
            "Oldest" -> it.sortedBy { it.id }
            else -> it
        }
    }

    fun refreshUserList() {
        viewModel.refreshUsers()
        // Update current user's lastActive on refresh
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val ref = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users/$uid/lastActive")
            ref.setValue(System.currentTimeMillis())
        }
    }

    val listState = rememberLazyListState()
    val backgroundColor = if (darkTheme) Color(0xFF121212) else Color(0xFFF4F8FB)
    val cardBg = if (darkTheme) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surface
    val cardText = if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    val dropdownBg = if (darkTheme) Color(0xFF23272A) else Color(0xFFEFEFEF)
    val dropdownSurface = if (darkTheme) Color(0xFF23272A) else MaterialTheme.colorScheme.surface
    val primaryTextColor = if (darkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("waterData/waterStatus")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tankStatus = snapshot.getValue(String::class.java) ?: "Unknown"
            }
            override fun onCancelled(error: DatabaseError) {
                tankStatus = "Error"
            }
        })
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
    
    // Fetch current user's role
    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentUserRole = snapshot.child("role").getValue(String::class.java) ?: ""
                }
                override fun onCancelled(error: DatabaseError) {
                    currentUserRole = ""
                }
            })
        }
    }
    

    
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
                        Text(text = "Safehito Admin", fontSize = 12.sp, color = secondaryTextColor)
                        Text(text = "Manage Users", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = primaryTextColor)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }, modifier = Modifier.padding(end = 0.dp)) {
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
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Add User",
                                    tint = Color(0xFF5DCCFC),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
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
        val coroutineScope = rememberCoroutineScope()

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    try {
                        // Refresh user list data
                        refreshUserList()
                        // Delay to show refresh indicator
                        delay(1000)
                    } catch (e: Exception) {
                        Log.e("AdminUserListRefresh", "Error during refresh: ${e.message}")
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
            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .padding(horizontal = 25.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    stickyHeader {
                        Surface(
                            tonalElevation = 4.dp,
                            color = backgroundColor,
                            modifier = Modifier.zIndex(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${filteredUsers.size} Users",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = primaryTextColor
                                    )
                                    if (currentUserRole.isNotEmpty()) {
                                        Text(
                                            text = "Logged in as: ${currentUserRole.capitalize()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = when (currentUserRole) {
                                                "superadmin" -> if (darkTheme) Color(0xFFFFD700) else Color(0xFFFF9800) // Gold for dark mode, Orange for light mode
                                                "admin" -> Color(0xFF2196F3) // Blue for admin
                                                else -> Color.Gray
                                            }
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.zIndex(1f)) {
                                        TextButton(
                                            onClick = { roleDropdownExpanded = true },
                                            shape = RoundedCornerShape(50),
                                            colors = ButtonDefaults.textButtonColors(
                                                containerColor = dropdownBg,
                                                contentColor = primaryTextColor
                                            )
                                        ) {
                                            Icon(Icons.Default.Tune, contentDescription = "Filter", tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(roleFilter)
                                        }
                                        DropdownMenu(
                                            expanded = roleDropdownExpanded,
                                            onDismissRequest = { roleDropdownExpanded = false },
                                            modifier = Modifier.background(dropdownSurface)
                                        ) {
                                            roleOptions.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option) },
                                                    onClick = {
                                                        roleFilter = option
                                                        roleDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Sort dropdown
                                    Box(modifier = Modifier.zIndex(0f)) {
                                        TextButton(
                                            onClick = { sortDropdownExpanded = true },
                                            shape = RoundedCornerShape(50),
                                            colors = ButtonDefaults.textButtonColors(
                                                containerColor = dropdownBg,
                                                contentColor = primaryTextColor
                                            )
                                        ) {
                                            Icon(Icons.Default.SwapVert, contentDescription = "Sort", tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(selectedSort)
                                        }
                                        DropdownMenu(
                                            expanded = sortDropdownExpanded,
                                            onDismissRequest = { sortDropdownExpanded = false },
                                            modifier = Modifier.background(dropdownSurface)
                                        ) {
                                            sortOptions.forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option) },
                                                    onClick = {
                                                        selectedSort = option
                                                        sortDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    items(filteredUsers) { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = cardBg,
                                contentColor = cardText
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Profile image with privilege-based border
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clickable { showImagePreview = true to user.profileImageBase64 },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Discord-style animated border based on privilege
                                    val borderColors = when (user.role) {
                                        "admin" -> listOf(
                                            Color(0xFF5DCCFC), // Primary blue
                                            Color(0xFF4FC3F7), // Light blue
                                            Color(0xFF29B6F6), // Medium blue
                                            Color(0xFF1E88E5), // Dark blue
                                            Color(0xFF1565C0), // Deeper blue
                                            Color(0xFF0D47A1), // Darkest blue
                                            Color(0xFF5DCCFC)  // Back to start
                                        )
                                        "user" -> listOf(
                                            Color(0xFF43B581), // Discord online green
                                            Color(0xFF3CA374), // Darker green
                                            Color(0xFF2D7D4A), // Even darker green
                                            Color(0xFF1F5B35), // Darkest green
                                            Color(0xFF43B581)  // Back to start
                                        )
                                        else -> listOf(
                                            Color(0xFFFFD700), // Gold
                                            Color(0xFFFF8C00), // Darker gold
                                            Color(0xFFFF6B00), // Even darker gold
                                            Color(0xFFFF4500), // Darkest gold
                                            Color(0xFFFFD700)  // Back to start
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .background(
                                                brush = Brush.sweepGradient(colors = borderColors),
                                                shape = CircleShape
                                            )
                                            .padding(3.dp)
                                            .background(
                                                color = if (darkTheme) Color(0xFF2C2F33) else Color(0xFFF6F6F7),
                                                shape = CircleShape
                                            )
                                    ) {
                                        // Profile image
                                        val base64 = user.profileImageBase64
                                        val bitmap = if (!base64.isNullOrBlank() && base64 != "null" && base64 != "undefined") {
                                            try {
                                                val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
                                                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                            } catch (e: Exception) { null }
                                        } else null

                                        if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Profile Picture",
                                                modifier = Modifier
                                                    .size(62.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.default_profile),
                                                contentDescription = "Profile",
                                                modifier = Modifier
                                                    .size(62.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        // Green dot overlay for active users (lastActive within 1 minute)
                                        val isActive = user.lastActive != null && (System.currentTimeMillis() - user.lastActive!!) < 3 * 60 * 1000
                                        if (isActive) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .align(Alignment.BottomEnd)
                                                    .background(Color(0xFF43B581), CircleShape)
                                                    .border(2.dp, Color.White, CircleShape)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AdaptiveText(
                                            text = user.fullName,
                                            maxWidth = 280.dp,
                                            fontWeight = FontWeight.Bold,
                                            color = cardText
                                        )
                                        if (user.role == "admin" || user.role == "superadmin") {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = if (user.role == "superadmin") "Verified Superadmin" else "Verified Admin",
                                                tint = if (user.role == "superadmin") Color(0xFFFFD700) else Color(0xFF1DA1F2), // Gold for superadmin, Twitter blue for admin
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    AdaptiveText(
                                        text = user.email,
                                        maxWidth = 280.dp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (darkTheme) Color(0xFFB0B0B0) else Color.Gray,
                                        defaultFontSize = 12.sp
                                    )
                                    AdaptiveText(
                                        text = "Contact: ${user.contactNumber}",
                                        maxWidth = 280.dp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (darkTheme) Color(0xFFB0B0B0) else Color.Gray,
                                        defaultFontSize = 11.sp
                                    )
                                    Text(
                                        text = "Role: ${user.role}",
                                        color = when (user.role) {
                                            "admin" -> Color(0xFF5DCCFC) // Primary blue
                                            "user" -> Color(0xFF43B581)  // Discord online green
                                            else -> if (darkTheme) Color(0xFFFFD700) else Color(0xFFFF9800) // Gold for dark mode, Orange for light mode
                                        },
                                        fontSize = 13.sp
                                    )
                                }
                                // Check if current user can edit this user
                                val canEditThisUser = when (currentUserRole) {
                                    "superadmin" -> true // Superadmin can edit anyone
                                    "admin" -> user.role == "user" // Admin can only edit users
                                    else -> false
                                }
                                
                                // Check if current user can delete this user
                                val canDeleteThisUser = when (currentUserRole) {
                                    "superadmin" -> user.id != FirebaseAuth.getInstance().currentUser?.uid // Superadmin can delete anyone except themselves
                                    "admin" -> user.role == "user" // Admin can only delete users
                                    else -> false
                                }
                                
                                // MoreHoriz menu for edit and delete
                                var expandedMenu by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(
                                        onClick = { expandedMenu = true },
                                        modifier = Modifier.offset(x = (8).dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreHoriz,
                                            contentDescription = "More actions",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expandedMenu,
                                        onDismissRequest = { expandedMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit") },
                                            onClick = {
                                                expandedMenu = false
                                                userToEdit = user
                                                showEditDialog = true
                                            },
                                            enabled = canEditThisUser,
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = null,
                                                    tint = if (canEditThisUser) MaterialTheme.colorScheme.primary else Color.Gray
                                                )
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            onClick = {
                                                expandedMenu = false
                                                userToRemove = user
                                                showRemoveDialog = true
                                            },
                                            enabled = canDeleteThisUser,
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = if (canDeleteThisUser) Color(0xFFB71C1C) else Color.Gray
                                                )
                                            }
                                        )
                                    }
                                }
                                

                            }
                        }
                    }
                }
                val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
                AnimatedVisibility(
                    visible = showScrollToTop,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 45.dp, bottom = 130.dp)
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
                                coroutineScope.launch {
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
    // Remove confirmation dialog
    if (showRemoveDialog && userToRemove != null) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove User") },
            text = { Text("Are you sure you want to remove ${userToRemove?.fullName}?") },
            confirmButton = {
                TextButton(onClick = {
                    val adminEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown Admin"
                    viewModel.removeUser(userToRemove!!, adminEmail) { success ->
                        showRemoveDialog = false
                    }
                }) { Text("REMOVE", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) { Text("CANCEL") }
            }
        )
    }

    // Edit user dialog (all fields editable)
    if (showEditDialog && userToEdit != null) {
        var newName by remember { mutableStateOf(userToEdit!!.fullName) }
        var newEmail by remember { mutableStateOf(userToEdit!!.email) }
        var newContact by remember { mutableStateOf(userToEdit!!.contactNumber) }
                var newRole by remember { mutableStateOf(userToEdit!!.role) }

 
        val roles = when (currentUserRole) {
            "superadmin" -> listOf("user", "admin", "superadmin")
            "admin" -> listOf("user")
            else -> listOf("user")
        }
        var expanded by remember { mutableStateOf(false) }
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val isSelf = userToEdit!!.id == currentUid
        
        // Check if current user can edit this user
        val canEditUser = when (currentUserRole) {
            "superadmin" -> true // Superadmin can edit anyone
            "admin" -> userToEdit!!.role == "user" // Admin can only edit users
            else -> false
        }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit User") },
            text = {
                Column {
                    if (!canEditUser) {
                        Text(
                            text = "⚠️ You don't have permission to edit this user",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        enabled = canEditUser
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { },
                        label = { Text("Email") },
                        singleLine = true,
                        readOnly = true,
                        enabled = false
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newContact,
                        onValueChange = { newContact = it },
                        label = { Text("Contact Number") },
                        singleLine = true,
                        enabled = canEditUser
                    )
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { if (!isSelf && canEditUser) expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = newRole,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Role") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier.menuAnchor(),
                            enabled = !isSelf && canEditUser
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            roles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role) },
                                    onClick = {
                                        newRole = role
                                        expanded = false
                                    },
                                    enabled = !isSelf && canEditUser
                                )
                            }
                        }
                            }



    }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updated = userToEdit!!.copy(
                            fullName = newName,
                            email = newEmail,
                            contactNumber = newContact,
                            role = newRole
                        )
                        viewModel.editUser(updated) { success ->
                            showEditDialog = false
                        }
                    },
                    enabled = canEditUser
                ) { Text("SAVE") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("CANCEL") }
            }
        )


    }

    // Add User Dialog
    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        var newEmail by remember { mutableStateOf("") }
        var newContact by remember { mutableStateOf("") }
        var newRole by remember { mutableStateOf("user") }
        val roles = when (currentUserRole) {
            "superadmin" -> listOf("user", "admin", "superadmin")
            "admin" -> listOf("user")
            else -> listOf("user")
        }
        var expanded by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add User") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newContact,
                        onValueChange = { newContact = it },
                        label = { Text("Contact Number") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = newRole,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Role") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            roles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role) },
                                    onClick = {
                                        newRole = role
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Call a function to add the user (implement in your ViewModel or here)
                    viewModel.addUser(newName, newEmail, newContact, newRole) { success ->
                        showAddDialog = false
                    }
                }) { Text("ADD") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("CANCEL") }
            }
        )
    }

    // Full image preview dialog
    if (showImagePreview.first) {
        AlertDialog(
            onDismissRequest = { showImagePreview = false to null },
            confirmButton = {},
            title = { Text("Profile Picture Preview") },
            text = {
                val base64 = showImagePreview.second
                val bitmap = if (!base64.isNullOrBlank() && base64 != "null" && base64 != "undefined") {
                    try {
                        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    } catch (e: Exception) { null }
                } else null
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Full Profile Picture",
                            modifier = Modifier.size(220.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.default_profile),
                            contentDescription = "Default Profile Picture",
                            modifier = Modifier.size(220.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImagePreview = false to null }) { Text("CLOSE") }
            }
        )
    }
} 