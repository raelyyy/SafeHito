package com.capstone.safehito.navigation

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.capstone.safehito.ui.*
import com.capstone.safehito.ui.ForgotPasswordScreen
import com.capstone.safehito.viewmodel.NotificationViewModel
import com.capstone.safehito.viewmodel.NotificationViewModelFactory
import com.google.firebase.auth.FirebaseAuth
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
import androidx.compose.ui.graphics.Brush


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
    val startDestination = if (auth.currentUser != null) "dashboard" else "login"

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination

    val context = LocalContext.current
    val activity = context as? Activity

    val coroutineScope = rememberCoroutineScope()
    val navDebouncer = remember { NavigationDebouncer() }

    val uid = auth.currentUser?.uid ?: "admin"
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(uid)
    )

    LaunchedEffect(Unit) {
        notificationViewModel.watchFishStatusAndNotify()
    }

    val navItems = listOf("dashboard", "scan", "records", "profile")

    var previousRoute by remember { mutableStateOf(startDestination) }

    val isForward = remember(previousRoute, currentRoute) {
        val order = navItems
        order.indexOf(currentRoute).takeIf { it >= 0 } ?: 0 >=
                order.indexOf(previousRoute).takeIf { it >= 0 } ?: 0
    }

    LaunchedEffect(currentRoute) {
        previousRoute = currentRoute
    }

    val backgroundColor = MaterialTheme.colorScheme.background

    fun navigateToTab(route: String) {
        if (currentRoute != route) {
            navDebouncer.navigate(coroutineScope) {
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
            AnimatedNavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                enterTransition = {
                    val from = initialState.destination.route
                    val to = targetState.destination.route

                    if ((from in navItems) && (to in navItems)) {
                        slideInHorizontally(
                            initialOffsetX = { if (navItems.indexOf(to) > navItems.indexOf(from)) it else -it },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(300))
                    } else if (to == "settings" || to == "notifications") {
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(tween(300))
                    } else {
                        EnterTransition.None
                    }
                },
                exitTransition = {
                    val from = initialState.destination.route
                    val to = targetState.destination.route

                    if ((from in navItems) && (to in navItems)) {
                        slideOutHorizontally(
                            targetOffsetX = { if (navItems.indexOf(to) > navItems.indexOf(from)) -it else it },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)

                        ) + fadeOut(tween(300))
                    } else if (from == "settings" || from == "notifications") {
                        ExitTransition.None
                    } else {
                        ExitTransition.None
                    }
                },
                popEnterTransition = {
                    val from = initialState.destination.route
                    val to = targetState.destination.route

                    if ((from in navItems) && (to in navItems)) {
                        slideInHorizontally(
                            initialOffsetX = { if (navItems.indexOf(to) < navItems.indexOf(from)) -it else it },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)

                        ) + fadeIn(tween(300))
                    } else if (to == "settings" || to == "notifications") {
                        EnterTransition.None
                    } else {
                        EnterTransition.None
                    }
                },
                popExitTransition = {
                    val from = initialState.destination.route
                    val to = targetState.destination.route

                    if ((from in navItems) && (to in navItems)) {
                        slideOutHorizontally(
                            targetOffsetX = { if (navItems.indexOf(to) < navItems.indexOf(from)) it else -it },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)

                        ) + fadeOut(tween(300))
                    } else if (from == "settings" || from == "notifications") {
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
                            navController.navigate("signup")
                        },
                        onForgotPassword = { navController.navigate("forgot_password") }
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




            }

            // FloatingNavBar overlay
            if (currentRoute in navItems) {
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
                    )
                    FloatingNavBar(
                        selectedRoute = currentRoute,
                        onItemSelected = { route ->
                            if (route != currentRoute) {
                                navDebouncer.navigate(coroutineScope) {
                                    navController.navigate(route) {
                                        launchSingleTop = true
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        restoreState = true
                                    }
                                }
                            }
                        },
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
