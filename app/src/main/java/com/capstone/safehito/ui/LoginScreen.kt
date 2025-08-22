package com.capstone.safehito.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstone.safehito.viewmodel.AuthViewModel
import com.capstone.safehito.R
import com.capstone.safehito.ui.components.TermsOfServiceModal
import com.capstone.safehito.ui.components.PrivacyPolicyModal
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush


@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onForgotPassword: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showTermsModal by remember { mutableStateOf(false) }
    var showPrivacyModal by remember { mutableStateOf(false) }
    val authResult by viewModel.authResult.collectAsState()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDarkTheme = isSystemInDarkTheme()
    val systemUiController = rememberSystemUiController()
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF7F9FB)

    // Set system bars color
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = backgroundColor,
            darkIcons = !isDarkTheme
        )
    }

    // Handle login result
    LaunchedEffect(authResult) {
        if (authResult.isNotEmpty()) {
            Toast.makeText(context, authResult, Toast.LENGTH_SHORT).show()
            if (authResult == "Login successful") onLoginSuccess()
        }
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
                        Spacer(Modifier.height(12.dp))

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
                            text = "Welcome to SafeHito",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = "Login to continue monitoring your fish.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { viewModel.login(email.trim(), password.trim()) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors()
                        )


                        // Forgot Password
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = onForgotPassword,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else Color(0xFF03A9F4)
                                )
                            ) {
                                Text(
                                    "Forgot Password?",
                                    fontSize = 12.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                    color = if (isDarkTheme) MaterialTheme.colorScheme.primary else Color(0xFF03A9F4)
                                )
                            }
                        }


                        // Login Button
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    viewModel.setError("Email and password cannot be empty")
                                } else {
                                    viewModel.login(email.trim(), password.trim())
                                }
                            },
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
                            Text(
                                "Log In",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }

                        // Error Message
                        if (authResult.isNotEmpty() && authResult != "Login successful") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = authResult,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sign Up + Terms
                        TextButton(
                            onClick = onNavigateToSignUp,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else Color(0xFF03A9F4)
                            )
                        ) {
                            Text(
                                "Don't have an account? Sign up",
                                fontSize = 14.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                color = if (isDarkTheme) MaterialTheme.colorScheme.primary else Color(0xFF03A9F4)
                            )
                        }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "By logging in, you agree to our ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                            Text(
                                text = "Terms of Service",
                                fontSize = 11.sp,
                                color = if (isDarkTheme) MaterialTheme.colorScheme.primary else Color(0xFF03A9F4),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                lineHeight = 16.sp,
                                modifier = Modifier
                                    .clickable { showTermsModal = true }
                                    .padding(horizontal = 1.dp)
                            )

                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "and ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                            Text(
                                text = "Privacy Policy",
                                fontSize = 11.sp,
                                color = if (isDarkTheme) MaterialTheme.colorScheme.primary else Color(
                                    0xFF03A9F4
                                ),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp,
                                modifier = Modifier
                                    .clickable { showPrivacyModal = true }
                                    .padding(horizontal = 1.dp)
                            )
                        }
                    }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // Terms of Service Modal
        TermsOfServiceModal(
            isVisible = showTermsModal,
            onDismiss = { showTermsModal = false }
        )
        
        // Privacy Policy Modal
        PrivacyPolicyModal(
            isVisible = showPrivacyModal,
            onDismiss = { showPrivacyModal = false }
        )
    }
}