package com.capstone.safehito.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.capstone.safehito.R
import com.capstone.safehito.ui.components.TermsOfServiceModal
import com.capstone.safehito.ui.components.PrivacyPolicyModal
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showTermsModal by remember { mutableStateOf(false) }
    var showPrivacyModal by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDarkTheme = isSystemInDarkTheme()
    val systemUiController = rememberSystemUiController()
    val backgroundColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF7F9FB)

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
                        .widthIn(max = 400.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()) // ✅ scroll if small screen/keyboard open
                            .fillMaxWidth()
                            .widthIn(max = 400.dp) // ✅ prevent stretching on tablets
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

                        Spacer(modifier = Modifier.heightIn(min = 12.dp, max = 24.dp)) // ✅ adjusts better


                        Text("Create Account", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Sign up to continue using SafeHito.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = {
                            if (it.length <= 11 && it.all(Char::isDigit)) {
                                contactNumber = it
                            }
                        },
                        label = { Text("Contact Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                            onDone = {
                                handleSignUp(
                                    fullName, contactNumber, email, password, context,
                                    onSignUpSuccess, onError = { errorMessage = it }
                                )
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            handleSignUp(
                                fullName, contactNumber, email, password, context,
                                onSignUpSuccess, onError = { errorMessage = it }
                            )
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
                        Text("Sign Up", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(
                        onClick = onNavigateToLogin,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else Color(0xFF03A9F4)
                        )
                    ) {
                        Text(
                            "Already have an account? Log in",
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


private fun handleSignUp(
    fullName: String,
    contactNumber: String,
    email: String,
    password: String,
    context: android.content.Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    when {
        fullName.isBlank() -> onError("Please enter your full name.")
        contactNumber.isBlank() -> onError("Please enter your contact number.")
        email.isBlank() -> onError("Please enter your email.")
        password.isBlank() -> onError("Please enter your password.")
        password.length < 6 -> onError("Password must be at least 6 characters.")
        else -> {
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        val user = mapOf(
                            "id" to uid,
                            "fullName" to fullName,
                            "email" to email,
                            "role" to "user",
                            "contactNumber" to contactNumber,
                            "profilePictureUrl" to "",
                            "profileImageBase64" to ""
                        )
                        FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .setValue(user)
                        Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    } else {
                        onError(task.exception?.localizedMessage ?: "Sign up failed")
                    }
                }
        }
    }
}
