package com.capstone.safehito.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.capstone.safehito.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.capstone.safehito.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel = viewModel(),
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val systemUiController = rememberSystemUiController()
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF4F8FB)

    var email by remember { mutableStateOf("") }
    val authResult by viewModel.authResult.collectAsState()
    var cooldownSeconds by remember { mutableStateOf(0) }
    val cooldownActive = cooldownSeconds > 0

    // Start cooldown timer when cooldownSeconds is set
    LaunchedEffect(cooldownSeconds) {
        if (cooldownSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            cooldownSeconds--
        }
    }

    LaunchedEffect(authResult) {
        if (authResult.isNotEmpty()) {
            Toast.makeText(context, authResult, Toast.LENGTH_SHORT).show()
            if (authResult.contains("reset", ignoreCase = true)) {
                cooldownSeconds = 30 // 30 second cooldown
            }
            if (authResult == "Reset email sent") {
                onBackToLogin()
            }
        }
    }

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = backgroundColor,
            darkIcons = !isDarkTheme
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { focusManager.clearFocus() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.3f),
                        spotColor = Color.Black.copy(alpha = 1f)
                    )
                    .background(
                        Brush.linearGradient(
                            colors = if (isDarkTheme) {
                                listOf(
                                    Color(0xFF2A2A2A),  // Slightly lighter dark gray
                                    Color(0xFF1A1A1A)   // Dark gray
                                )
                            } else {
                                listOf(
                                    Color(0xFFFFFFFF),   // Slightly darker light gray
                                    Color(0xFFF1F1F1)  // Light gray
                                )
                            },
                            start = Offset(0f, 0f),
                            end = Offset.Infinite
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .padding(1.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.safehito_logoo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .scale(1.3f)
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "Forgot Password",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = "Enter your email to receive reset instructions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email
                        ),
                        colors = OutlinedTextFieldDefaults.colors()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (email.isBlank()) {
                                viewModel.setError("Email cannot be empty")
                            } else {
                                viewModel.resetPassword(email.trim())
                            }
                        },
                        enabled = !cooldownActive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF5DCCFC), Color(0xFF007EF2)),
                                    start = Offset(0f, 0f),
                                    end = Offset.Infinite
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        if (cooldownActive) {
                            Text("Wait ${cooldownSeconds}s", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        } else {
                            Text("Send Reset Link", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = onBackToLogin,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else Color(0xFF03A9F4)
                        )
                    ) {
                        Text(
                            "Back to Login",
                            color = if (isDarkTheme) MaterialTheme.colorScheme.primary else Color(0xFF03A9F4)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
                }
            }
        }
    }
}
