package com.capstone.safehito.navigation

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.capstone.safehito.ui.*
import com.capstone.safehito.ui.ForgotPasswordScreen
import com.capstone.safehito.ui.SignUpScreen
import com.capstone.safehito.viewmodel.NotificationViewModel
import com.capstone.safehito.viewmodel.NotificationViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DrawerDefaults.backgroundColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.capstone.safehito.util.NavigationDebouncer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Brush
import com.capstone.safehito.ui.NavBarItem
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.vector.ImageVector
import com.capstone.safehito.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.filled.Water
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay


@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    resetOverride: () -> Unit,
) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    var userRole by remember { mutableStateOf<String?>(null) }
    var usedFallbackRole by remember { mutableStateOf(false) }

    // Fetch user role from Firebase with real-time listener
    DisposableEffect(uid) {
        val listener = if (uid != null) {
            val ref = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users/$uid/role")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userRole = snapshot.value as? String
                    usedFallbackRole = false
                }
                
                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            }
            ref.addValueEventListener(listener)
            listener
        } else null
        
        onDispose {
            if (uid != null && listener != null) {
                val ref = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users/$uid/role")
                ref.removeEventListener(listener)
            }
        }
    }

    // Fallback: If userRole is not loaded after 2 seconds, set to 'user' and mark fallback
    LaunchedEffect(uid) {
        if (uid != null) {
            kotlinx.coroutines.delay(2000)
            if (userRole == null) {
                userRole = "user"
                usedFallbackRole = true
            }
        }
    }

    val isAdmin = userRole == "admin" || userRole == "superadmin"
    
    // Debug logging
    LaunchedEffect(userRole) {
        println("DEBUG: userRole = $userRole, isAdmin = $isAdmin")
    }
    val adminNavItems = listOf("admin_dashboard", "admin_user_list", "admin_records")
    val adminStartDestination = "admin_dashboard"
    val startDestination = if (auth.currentUser != null) {
        if (userRole != null && isAdmin) adminStartDestination else "dashboard"
    } else "login"

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var currentRoute by remember { mutableStateOf(startDestination) }
    
    // Update currentRoute safely when role changes - will be moved after navRoutes is defined

    val context = LocalContext.current
    val activity = context as? Activity

    val coroutineScope = rememberCoroutineScope()
    val navDebouncer = remember { NavigationDebouncer() }

    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(uid ?: "")
    )

    LaunchedEffect(uid) {
        if (uid != null && uid.isNotEmpty()) {
            notificationViewModel.watchFishStatusAndNotify()
        }
    }

    val adminNavBarItems = listOf(
        NavBarItem("Dashboard", "admin_dashboard", R.drawable.dashboard),
        NavBarItem("User List", "admin_user_list", R.drawable.people), // Replace with actual icon if available
        NavBarItem("Records", "admin_records", R.drawable.records),
        NavBarItem("Profile", "admin_profile", R.drawable.profile)
    )
    val userNavBarItems = listOf(
        NavBarItem("Dashboard", "dashboard", R.drawable.dashboard),
        NavBarItem("Scan", "scan", R.drawable.scan),
        NavBarItem("Records", "records", R.drawable.records),
        NavBarItem("Profile", "profile", R.drawable.profile)
    )
    val navBarItems = if (userRole != null && isAdmin) adminNavBarItems else userNavBarItems

    val navRoutes = navBarItems.map { it.route }
    val authRoutes = listOf("login", "signup", "forgot_password")
    val additionalRoutes = listOf("settings", "notifications", "admin_notifications", "all_logs")

    // Debug logging for routes
    LaunchedEffect(navRoutes, isAdmin) {
        println("DEBUG: navRoutes = $navRoutes, isAdmin = $isAdmin")
    }

    // For navigation logic, use navRoutes + authRoutes + additionalRoutes
    val allRoutes = navRoutes + authRoutes + additionalRoutes

    // Update currentRoute safely when role changes
    LaunchedEffect(navBackStackEntry?.destination?.route, isAdmin, navRoutes) {
        val route = navBackStackEntry?.destination?.route
        if (route != null && route in allRoutes) {
            currentRoute = route
        } else if (route != null && route !in allRoutes) {
            // If current route is not valid for current role, navigate to appropriate start destination
            println("DEBUG: Invalid route '$route' for current role, navigating to start destination")
            navController.navigate(startDestination) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    var previousRoute by remember { mutableStateOf(startDestination) }

    val isForward = remember(previousRoute, currentRoute) {
        val order = navRoutes
        ((order.indexOf(currentRoute).takeIf { it >= 0 } ?: 0) >=
                (order.indexOf(previousRoute).takeIf { it >= 0 } ?: 0))
    }

    LaunchedEffect(currentRoute) {
        previousRoute = currentRoute
    }

    val backgroundColor = MaterialTheme.colorScheme.background

    fun navigateToTab(route: String) {
        println("DEBUG: navigateToTab called with route: $route, currentRoute: $currentRoute, isAdmin: $isAdmin")
        if (currentRoute != route) {
            navDebouncer.navigate(coroutineScope) {
                println("DEBUG: Navigating to: $route")
                navController.navigate(route) {
                    launchSingleTop = true
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    restoreState = true
                }
            }
        }
    }


    Scaffold(
        // Remove bottomBar
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main content
            var showLoading by remember { mutableStateOf(true) }
            
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(3500) // Show loading for exactly 5 seconds
                showLoading = false
            }
            
            if (auth.currentUser != null && (userRole == null || showLoading)) {
                // Show loading screen for 3 seconds
                LoadingScreen(isDarkMode = isDarkMode)
            } else {
                if (usedFallbackRole) {
                    // Show offline/fallback warning
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFD32F2F))
                            .padding(12.dp)
                            .align(Alignment.TopCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Offline: Showing limited access. Some features may not work.",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                AnimatedNavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                enterTransition = {
                    val from = initialState.destination.route
                    val to = targetState.destination.route

                    if ((from in navRoutes) && (to in navRoutes)) {
                        slideInHorizontally(
                            initialOffsetX = { if (navRoutes.indexOf(to) > navRoutes.indexOf(from)) it else -it },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(300))
                    } else if (
                        (from in listOf("login", "signup", "forgot_password")) &&
                        (to in listOf("login", "signup", "forgot_password"))
                    ) {
                        slideInHorizontally(
                            initialOffsetX = { it / 2 },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(300))
                    } else if (to == "settings" || to == "notifications" || to == "admin_notifications") {
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300))
                    } else {
                        EnterTransition.None
                    }
                },
                exitTransition = {
                    val from = initialState.destination.route
                    val to = targetState.destination.route

                    if ((from in navRoutes) && (to in navRoutes)) {
                        slideOutHorizontally(
                            targetOffsetX = { if (navRoutes.indexOf(to) > navRoutes.indexOf(from)) -it else it },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(tween(300))
                    } else if (
                        (from in listOf("login", "signup", "forgot_password")) &&
                        (to in listOf("login", "signup", "forgot_password"))
                    ) {
                        slideOutHorizontally(
                            targetOffsetX = { -it / 2 },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(tween(300))
                    } else if (from == "settings" || from == "notifications" || from == "admin_notifications") {
                        ExitTransition.None
                    } else {
                        ExitTransition.None
                    }
                },
                popEnterTransition = {
                    val from = initialState.destination.route
                    val to = targetState.destination.route

                    if ((from in navRoutes) && (to in navRoutes)) {
                        slideInHorizontally(
                            initialOffsetX = { if (navRoutes.indexOf(to) < navRoutes.indexOf(from)) -it else it },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(300))
                    } else if (
                        (from in listOf("login", "signup", "forgot_password")) &&
                        (to in listOf("login", "signup", "forgot_password"))
                    ) {
                        slideInHorizontally(
                            initialOffsetX = { -it / 2 },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(300))
                    } else if (to == "settings" || to == "notifications" || to == "admin_notifications") {
                        EnterTransition.None
                    } else {
                        EnterTransition.None
                    }
                },
                popExitTransition = {
                    val from = initialState.destination.route
                    val to = targetState.destination.route

                    if ((from in navRoutes) && (to in navRoutes)) {
                        slideOutHorizontally(
                            targetOffsetX = { if (navRoutes.indexOf(to) < navRoutes.indexOf(from)) it else -it },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(tween(300))
                    } else if (
                        (from in listOf("login", "signup", "forgot_password")) &&
                        (to in listOf("login", "signup", "forgot_password"))
                    ) {
                        slideOutHorizontally(
                            targetOffsetX = { it / 2 },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(tween(300))
                    } else if (from == "settings" || from == "notifications" || from == "admin_notifications") {
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(tween(300))
                    } else {
                        ExitTransition.None
                    }
                }
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate("dashboard") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSignUp = {
                            println("DEBUG: Navigating to signup")
                            navController.navigate("signup") {
                                launchSingleTop = true
                            }
                        },
                        onForgotPassword = { 
                            println("DEBUG: Navigating to forgot password")
                            navController.navigate("forgot_password") {
                                launchSingleTop = true
                            }
                        }
                    )
                }


                composable("signup") {
                    SignUpScreen(
                        onSignUpSuccess = {
                            navController.navigate("dashboard") {
                                popUpTo("signup") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                composable("forgot_password") {
                    ForgotPasswordScreen(onBackToLogin = { navController.popBackStack() })
                }

                composable("dashboard") {
                    DashboardScreen(
                        selectedRoute = currentRoute,
                        onItemSelected = { route -> navigateToTab(route) }
                        ,
                        onLogout = {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        navController = navController,
                        notificationViewModel = notificationViewModel,
                        darkTheme = isDarkMode
                    )
                }

                composable("scan") {
                    ScanScreen(
                        selectedRoute = currentRoute,
                        onItemSelected = { route -> navigateToTab(route) },
                        navController = navController,
                        notificationViewModel = notificationViewModel,
                        darkTheme = isDarkMode
                    )
                }

                composable("records") {
                    RecordsScreen(
                        selectedRoute = currentRoute,
                        onItemSelected = { route -> navigateToTab(route) },
                        navController = navController,
                        notificationViewModel = notificationViewModel,
                        darkTheme = isDarkMode
                    )
                }

                composable("profile") {
                    ProfileScreen(
                        selectedRoute = currentRoute,
                        onItemSelected = { route -> navigateToTab(route) },
                        navController = navController,
                        onLogout = {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        darkTheme = isDarkMode
                    )
                }

                composable("notifications") {
                    NotificationScreen(
                        notificationViewModel = notificationViewModel,
                        onBack = {
                            if (!navController.popBackStack()) {
                                activity?.finish()
                            }
                        },
                        darkTheme = isDarkMode
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        navController = navController,
                        onBack = { navController.popBackStack() },
                        isDarkTheme = isDarkMode,
                        onToggleTheme = onToggleTheme,
                        resetOverride = resetOverride
                    )
                }

                // --- ADMIN ROUTES ---
                composable("admin_dashboard") {
                    AdminDashboardScreen(
                        darkTheme = isDarkMode,
                        onItemSelected = { route -> navigateToTab(route) },
                        navController = navController // Pass navController so notification icon works
                    )
                }
                composable("admin_user_list") {
                    AdminUserListScreen(
                        darkTheme = isDarkMode,
                        navController = navController // Pass navController for notification icon
                    )
                }
                composable("admin_records") {
                    AdminRecordsScreen(
                        darkTheme = isDarkMode,
                        navController = navController // Pass navController for notification icon
                    )
                }
                composable("admin_profile") {
                    AdminProfileScreen(
                        selectedRoute = currentRoute,
                        onItemSelected = { route -> navigateToTab(route) },
                        navController = navController,
                        onLogout = {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        darkTheme = isDarkMode
                    )
                }
                composable("admin_notifications") {
                    AdminNotificationScreen(
                        uid = uid ?: "",
                        onBack = {
                            if (!navController.popBackStack()) {
                                activity?.finish()
                            }
                        },
                        darkTheme = isDarkMode
                    )
                }
                
                composable("all_logs") {
                    AllLogsScreen(
                        darkTheme = isDarkMode,
                        navController = navController
                    )
                }


            }
            }

            // FloatingNavBar overlay
            if (currentRoute in navRoutes && userRole != null && !showLoading) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                ) {
                    // Gradient background from bottom of screen upwards
                    val gradientBottomColor = if (isDarkMode) Color.Black.copy(alpha = 0.75f) else Color.Black.copy(alpha = 0.35f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp) // Adjust height as needed
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent, // Transparent at the top
                                        gradientBottomColor
                                    )
                                )
                            )
                            .blur(25.dp) // Blur is applied in all directions, but only visible where the gradient is not transparent
                    )
                    FloatingNavBar(
                        selectedRoute = currentRoute,
                        onItemSelected = { route -> navigateToTab(route) },
                        items = navBarItems,
                        darkTheme = isDarkMode,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(isDarkMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    // Floating animation for the fish - 3 seconds
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fishFloat"
    )
    
    // Wave animation - 3 seconds
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * kotlin.math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )
    
    // Bubble animations
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
            animation = tween(3500, delayMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble2"
    )
    
    val bubble3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, delayMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble3"
    )
    
    val bubble4Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, delayMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble4"
    )
    
    val bubble5Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, delayMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble5"
    )

    // Additional bubbles for more variety
    val bubble6Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3300, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble6"
    )

    val bubble7Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4700, delayMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble7"
    )

    val bubble8Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, delayMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble8"
    )

    val bubble9Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4100, delayMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble9"
    )

    val bubble10Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble10"
    )

    val bubble11Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4400, delayMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble11"
    )

    val bubble12Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3700, delayMillis = 1900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble12"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors =
                        listOf(
                            Color(0xFF90CAF9),
                            Color(0xFFE3F2FD),
                            Color(0xFFBBDEFB),

                        )

                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // Canvas wave animations on top
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val waveColor = if (isDarkMode) {
                Color(0xFF4BA3C7).copy(alpha = 0.3f)
            } else {
                Color(0xFF5DCCFC).copy(alpha = 0.4f)
            }

            fun drawWave(phase: Float, amplitude: Float, baseY: Float, frequency: Float) {
                val path = Path()
                val step = 2f
                path.moveTo(-size.width / 4f, baseY)
                var x = -size.width / 4f
                while (x <= size.width * 1.5f) {
                    val angle = (x / size.width) * 2f * kotlin.math.PI.toFloat() * frequency + phase
                    val y = baseY + amplitude * kotlin.math.sin(angle.toDouble()).toFloat()
                    path.lineTo(x, y)
                    x += step
                }
                path.lineTo(size.width * 1.5f, size.height)
                path.lineTo(-size.width / 4f, size.height)
                path.close()
                drawPath(path = path, color = waveColor)
            }

            drawWave(wavePhase, 30f, size.height * 0.7f, 1.2f)
            drawWave(wavePhase * 0.7f, 25f, size.height * 0.75f, 0.8f)
            drawWave(wavePhase * 1.3f, 20f, size.height * 0.8f, 1.5f)

            // Draw bubbles
            val bubbleColor = if (isDarkMode) {
                Color(0xFF4BA3C7).copy(alpha = 0.6f)
            } else {
                Color(0xFF5DCCFC).copy(alpha = 0.7f)
            }

            // Bubble 1
            val bubble1Y = size.height * (1f + bubble1Offset)
            if (bubble1Y < size.height) {
                drawCircle(
                    color = bubbleColor,
                    radius = 8f,
                    center = Offset(size.width * 0.15f, bubble1Y)
                )
            }

            // Bubble 2
            val bubble2Y = size.height * (1f + bubble2Offset)
            if (bubble2Y < size.height) {
                drawCircle(
                    color = bubbleColor,
                    radius = 12f,
                    center = Offset(size.width * 0.35f, bubble2Y)
                )
            }

            // Bubble 3
            val bubble3Y = size.height * (1f + bubble3Offset)
            if (bubble3Y < size.height) {
                drawCircle(
                    color = bubbleColor,
                    radius = 6f,
                    center = Offset(size.width * 0.65f, bubble3Y)
                )
            }

            // Bubble 4
            val bubble4Y = size.height * (1f + bubble4Offset)
            if (bubble4Y < size.height) {
                drawCircle(
                    color = bubbleColor,
                    radius = 10f,
                    center = Offset(size.width * 0.85f, bubble4Y)
                )
            }

            // Bubble 5
            val bubble5Y = size.height * (1f + bubble5Offset)
            if (bubble5Y < size.height) {
                drawCircle(
                    color = bubbleColor,
                    radius = 7f,
                    center = Offset(size.width * 0.5f, bubble5Y)
                )
            }

            // Bubble 6
            val bubble6Y = size.height * (1f + bubble6Offset)
            if (bubble6Y < size.height) {
                drawCircle(
                    color = bubbleColor,
                    radius = 5f,
                    center = Offset(size.width * 0.25f, bubble6Y)
                )
            }

            // Bubble 7
            val bubble7Y = size.height * (1f + bubble7Offset)
            if (bubble7Y < size.height) {
                drawCircle(
                    color = bubbleColor,
                    radius = 9f,
                    center = Offset(size.width * 0.75f, bubble7Y)
                )
            }

            // Bubble 8
            val bubble8Y = size.height * (1f + bubble8Offset)
            if (bubble8Y < size.height) {
                drawCircle(
                    color = bubbleColor,
                    radius = 4f,
                    center = Offset(size.width * 0.1f, bubble8Y)
                )
            }

            // Bubble 9
            val bubble9Y = size.height * (1f + bubble9Offset)
            if (bubble9Y < size.height) {
                drawCircle(
                    color = bubbleColor,
                    radius = 11f,
                    center = Offset(size.width * 0.9f, bubble9Y)
                )
            }

            // Bubble 10
            val bubble10Y = size.height * (1f + bubble10Offset)
            if (bubble10Y < size.height) {
                drawCircle(
                    color = bubbleColor,
                    radius = 6f,
                    center = Offset(size.width * 0.45f, bubble10Y)
                )
            }

            // Bubble 11
            val bubble11Y = size.height * (1f + bubble11Offset)
            if (bubble11Y < size.height) {
                drawCircle(
                    color = bubbleColor,
                    radius = 8f,
                    center = Offset(size.width * 0.55f, bubble11Y)
                )
            }

            // Bubble 12
            val bubble12Y = size.height * (1f + bubble12Offset)
            if (bubble12Y < size.height) {
                drawCircle(
                    color = bubbleColor,
                    radius = 5f,
                    center = Offset(size.width * 0.3f, bubble12Y)
                )
            }
        }
        
        // Multiple fish animations jumping out of water
        val fishComposition by rememberLottieComposition(
            LottieCompositionSpec.Asset("fish.json")
        )
        
        // Fish 1 - Random position at specific height
        var fish1Playing by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(0)
            fish1Playing = true
        }
        val fish1Progress by animateLottieCompositionAsState(
            composition = fishComposition,
            iterations = LottieConstants.IterateForever,
            speed = 0.8f,
            isPlaying = fish1Playing
        )
        LottieAnimation(
            composition = fishComposition,
            progress = { fish1Progress },
            modifier = Modifier
                .size(80.dp)
                .offset(x = (-120).dp, y = 150.dp)
        )
        
        // Fish 2 - Random position at specific height
        var fish2Playing by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(800)
            fish2Playing = true
        }
        val fish2Progress by animateLottieCompositionAsState(
            composition = fishComposition,
            iterations = LottieConstants.IterateForever,
            speed = 1.2f,
            isPlaying = fish2Playing
        )
        LottieAnimation(
            composition = fishComposition,
            progress = { fish2Progress },
            modifier = Modifier
                .size(60.dp)
                .offset(x = 80.dp, y = 150.dp)
        )
        
        // Fish 3 - Random position at specific height
        var fish3Playing by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(1200)
            fish3Playing = true
        }
        val fish3Progress by animateLottieCompositionAsState(
            composition = fishComposition,
            iterations = LottieConstants.IterateForever,
            speed = 0.6f,
            isPlaying = fish3Playing
        )
        LottieAnimation(
            composition = fishComposition,
            progress = { fish3Progress },
            modifier = Modifier
                .size(70.dp)
                .offset(x = (-60).dp, y = 150.dp)
        )
        
        // Fish 4 - Random position at specific height
        var fish4Playing by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(1800)
            fish4Playing = true
        }
        val fish4Progress by animateLottieCompositionAsState(
            composition = fishComposition,
            iterations = LottieConstants.IterateForever,
            speed = 1.0f,
            isPlaying = fish4Playing
        )
        LottieAnimation(
            composition = fishComposition,
            progress = { fish4Progress },
            modifier = Modifier
                .size(90.dp)
                .offset(x = 120.dp, y = 150.dp)
        )
        
        // Fish 5 - Random position at specific height
        var fish5Playing by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(2000)
            fish5Playing = true
        }
        val fish5Progress by animateLottieCompositionAsState(
            composition = fishComposition,
            iterations = LottieConstants.IterateForever,
            speed = 0.9f,
            isPlaying = fish5Playing
        )
        LottieAnimation(
            composition = fishComposition,
            progress = { fish5Progress },
            modifier = Modifier
                .size(65.dp)
                .offset(x = (-180).dp, y = 150.dp)
        )
        
        // Fish 6 - Random position at specific height
        var fish6Playing by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(2400)
            fish6Playing = true
        }
        val fish6Progress by animateLottieCompositionAsState(
            composition = fishComposition,
            iterations = LottieConstants.IterateForever,
            speed = 1.1f,
            isPlaying = fish6Playing
        )
        LottieAnimation(
            composition = fishComposition,
            progress = { fish6Progress },
            modifier = Modifier
                .size(75.dp)
                .offset(x = 180.dp, y = 150.dp)
        )
        
        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated fish icon with circular border
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(y = floatOffset.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circular border using sirkol.json animation
                val borderComposition by rememberLottieComposition(
                    LottieCompositionSpec.Asset("sirkol.json")
                )
                val borderProgress by animateLottieCompositionAsState(
                    composition = borderComposition,
                    iterations = LottieConstants.IterateForever,
                    speed = 0.8f
                )
                
                LottieAnimation(
                    composition = borderComposition,
                    progress = { borderProgress },
                    modifier = Modifier.size(180.dp)
                )
                
                // Circular fish logo
                Image(
                    painter = painterResource(id = R.drawable.safehito_logoo),
                    contentDescription = "Loading Fish",
                    modifier = Modifier
                        .size(125.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            
            // Lottie loading animation
            val loadingComposition by rememberLottieComposition(
                LottieCompositionSpec.Asset("loading.json")
            )
            val loadingProgress by animateLottieCompositionAsState(
                composition = loadingComposition,
                iterations = LottieConstants.IterateForever,
                speed = 1f
            )
            
            LottieAnimation(
                composition = loadingComposition,
                progress = { loadingProgress },
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
