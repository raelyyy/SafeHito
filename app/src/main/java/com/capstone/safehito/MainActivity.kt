package com.capstone.safehito

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.capstone.safehito.navigation.AppNavigation
import com.capstone.safehito.ui.LoginScreen
import com.capstone.safehito.ui.SignUpScreen
import com.capstone.safehito.ui.theme.SafeHitoTheme
import com.capstone.safehito.util.WaterStatusManager
import com.capstone.safehito.util.PiStatusManager
import com.capstone.safehito.util.rememberOfflineBannerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {

    override fun onResume() {
        super.onResume()
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val ref = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users/$uid/lastActive")
            ref.setValue(System.currentTimeMillis())
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Match system bar colors
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Prevent flash of white before Compose loads
        window.setBackgroundDrawableResource(android.R.color.black)

        // Disable contrast enforcement for dark mode nav bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // Initialize notification channel
        com.capstone.safehito.service.MyFirebaseMessagingService.createNotificationChannel(this)

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId != null) {
            WaterStatusManager(applicationContext, userId).startMonitoring()
        }

        // Initialize Pi Status Manager
        val piStatusManager = PiStatusManager()
        piStatusManager.startMonitoring()

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isUserOverride by remember { mutableStateOf(false) }
            var isDarkMode by remember { mutableStateOf(systemDark) }

            // Sync with system unless overridden
            LaunchedEffect(systemDark) {
                if (!isUserOverride) {
                    isDarkMode = systemDark
                }
            }

            val systemUiController = rememberSystemUiController()
            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = !isDarkMode
                )
            }


            SafeHitoTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val showOfflineBanner by rememberOfflineBannerState()

                val backgroundColor = if (isDarkMode) Color.Black else Color.White

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    color = backgroundColor
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavigation(
                            navController = navController,
                            isDarkMode = isDarkMode,
                            onToggleTheme = {
                                isUserOverride = true
                                isDarkMode = !isDarkMode
                            },
                            resetOverride = {
                                isUserOverride = false
                                isDarkMode = systemDark
                            }
                        )

                        AnimatedVisibility(
                            visible = showOfflineBanner,
                            enter = slideInVertically { -it } + fadeIn(),
                            exit = slideOutVertically { -it } + fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 60.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color.Black.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudOff,
                                            contentDescription = "Offline Icon",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "You're offline",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelLarge
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
}
