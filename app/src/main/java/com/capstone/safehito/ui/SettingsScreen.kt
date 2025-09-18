package com.capstone.safehito.ui

import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.messaging.FirebaseMessaging
import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    resetOverride: () -> Unit
) {
    val iconColor = Color(0xFF5DCCFC)
    val context = LocalContext.current

    val supportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val privacySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val termsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val aboutSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val devsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf<ModalSheetType?>(null) }
    var isPushEnabled by remember { mutableStateOf(true) }
    
    // Load push notification preference from SharedPreferences
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("settings_prefs", android.content.Context.MODE_PRIVATE)
        isPushEnabled = prefs.getBoolean("push_enabled", true)
    }

    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
    } catch (e: PackageManager.NameNotFoundException) {
        "1.0.0"
    }

    // Moved cooldown state and options here for global scope
    val user = FirebaseAuth.getInstance().currentUser
    // Check if user is admin or superadmin based on their role in Firebase
    var isAdmin by remember { mutableStateOf(false) }
    LaunchedEffect(user?.uid) {
        if (user?.uid != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users/${user.uid}")
            userRef.child("role").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val role = snapshot.getValue(String::class.java)
                    isAdmin = role == "admin" || role == "superadmin"
                }
                override fun onCancelled(error: DatabaseError) {
                    isAdmin = false
                }
            })
        }
    }
    val cooldownOptions = listOf(
        1 to "1 min",
        30 to "30 min",
        60 to "1 hr",
        180 to "3 hrs",
        360 to "6 hrs",
        720 to "12 hrs",
        1440 to "24 hrs"
    )
    var selectedCooldown by remember { mutableStateOf(720) }
    var expanded by remember { mutableStateOf(false) }
    var pendingCooldown by remember { mutableStateOf<Int?>(null) }
    var showCooldownDialog by remember { mutableStateOf(false) }
    var isCooldownLoading by remember { mutableStateOf(true) }
    var isCooldownSaving by remember { mutableStateOf(false) }

    // Always read from global/admin location (now with real-time updates)
    DisposableEffect(Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("settings/saltBathCooldownMinutes")
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val value = snapshot.getValue(Int::class.java)
                if (value != null) {
                    selectedCooldown = value
                }
                isCooldownLoading = false
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                isCooldownLoading = false
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }


    var connectionMode by remember { mutableStateOf("auto") } // ✅ default auto


    LaunchedEffect(user?.uid) {
        if (user?.uid != null) {
            val ref = FirebaseDatabase.getInstance().getReference("users/${user.uid}/connectionMode")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Default to cloudflare if not set
                    connectionMode = snapshot.getValue(String::class.java) ?: "auto"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text("Preferences", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onToggleTheme() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface, // ✅ adaptive color
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Dark Mode", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleTheme() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Push Notifications", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(
                        checked = isPushEnabled,
                        onCheckedChange = { enabled ->
                            isPushEnabled = enabled
                            // Save preference to SharedPreferences
                            val prefs = context.getSharedPreferences("settings_prefs", android.content.Context.MODE_PRIVATE)
                            prefs.edit().putBoolean("push_enabled", enabled).apply()

                            // Subscribe/unsubscribe from Firebase topics
                            if (enabled) {
                                FirebaseMessaging.getInstance().subscribeToTopic("all")
                                Toast.makeText(context, "Push notifications enabled", Toast.LENGTH_SHORT).show()
                            } else {
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("all")
                                Toast.makeText(context, "Push notifications disabled", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 0.dp, top = 16.dp, bottom = 16.dp), // ✅ reduced end padding
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface, // ✅ adaptive color
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Pi Connection", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    ConnectionModeDropdown(
                        selected = connectionMode,
                        onSelected = { mode ->
                            connectionMode = mode
                            if (user?.uid != null) {
                                FirebaseDatabase.getInstance()
                                    .getReference("users/${user.uid}/connectionMode")
                                    .setValue(mode)
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to save preference", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    )
                }
            }




            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 0.dp, top = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Salt Bath Cooldown",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )

                    if (isAdmin) {
                        Box {
                            // 🔹 Styled like Pi Connection dropdown (no ripple, primary text, arrow)
                            Row(
                                modifier = Modifier
                                    .clickable(
                                        indication = null, // ✅ hides ripple/click indicator
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { if (!isCooldownLoading) expanded = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isCooldownLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    cooldownOptions.find { it.first == selectedCooldown }?.second ?: "",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                cooldownOptions.forEach { (value, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            pendingCooldown = value
                                            expanded = false
                                            showCooldownDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = cooldownOptions.find { it.first == selectedCooldown }?.second ?: "",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(24.dp))
            Text("Support", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            ActionCard("Support / Contact Us", Icons.Default.SupportAgent, iconColor, isDarkTheme) {
                showSheet = ModalSheetType.Support
            }
            ActionCard("Privacy Policy", Icons.Default.PrivacyTip, iconColor, isDarkTheme) {
                showSheet = ModalSheetType.Privacy
            }
            ActionCard("Terms & Conditions", Icons.Default.Description, iconColor, isDarkTheme) {
                showSheet = ModalSheetType.Terms
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("About", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            ActionCard("Check for updates", Icons.Default.SystemUpdate, iconColor, isDarkTheme) {
                val url = "https://github.com/raelyyy/SafeHito/releases/latest"
                try {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                } catch (_: Exception) {
                    Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
                }
            }

            ActionCard("About SafeHito", Icons.Default.Info, iconColor, isDarkTheme) {
                showSheet = ModalSheetType.About
            }
            ActionCard("Developers", Icons.Default.People, iconColor, isDarkTheme) {
                showSheet = ModalSheetType.Developers
            }

            Spacer(modifier = Modifier.height(32.dp))

            ActionCard("Reset Settings", Icons.Default.Restore, MaterialTheme.colorScheme.error, isDarkTheme) {
                resetOverride()
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "App version $versionName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Place the dialog INSIDE the Column so all variables are in scope
            if (showCooldownDialog && pendingCooldown != null) {
                AlertDialog(
                    onDismissRequest = { if (!isCooldownSaving) showCooldownDialog = false },
                    title = { Text("Change Salt Bath Cooldown") },
                    text = { Text("Are you sure you want to set the salt bath cooldown to " +
                        (cooldownOptions.find { it.first == pendingCooldown }?.second ?: "") + "?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                isCooldownSaving = true
                                FirebaseDatabase.getInstance()
                                    .getReference("settings/saltBathCooldownMinutes")
                                    .setValue(pendingCooldown!!)
                                    .addOnSuccessListener {
                                        isCooldownSaving = false
                                        showCooldownDialog = false
                                        // No need to set selectedCooldown here; listener will update it
                                    }
                                    .addOnFailureListener { e ->
                                        isCooldownSaving = false
                                        Toast.makeText(context, "Failed to save cooldown: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            },
                            enabled = !isCooldownSaving
                        ) {
                            if (isCooldownSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { if (!isCooldownSaving) showCooldownDialog = false }, enabled = !isCooldownSaving) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

    }

    when (showSheet) {
        ModalSheetType.Support -> ModalSheet(supportSheetState, { showSheet = null }, "Support / Contact") {
            Text("📧 Email us at:")
            Text("safehito.help@gmail.com", color = MaterialTheme.colorScheme.primary)
        }
        ModalSheetType.Privacy -> ModalSheet(privacySheetState, { showSheet = null }, "Privacy Policy") {
            Text("We value your privacy. SafeHito only collects data to help monitor water quality and fish health. No data is shared without your consent.")
        }
        ModalSheetType.Terms -> ModalSheet(termsSheetState, { showSheet = null }, "Terms & Conditions") {
            Text("By using SafeHito, you agree to use it responsibly. While we help monitor water quality and fish health, we do not guarantee results and recommend professional guidance when needed.")
        }
        ModalSheetType.About -> ModalSheet(aboutSheetState, { showSheet = null }, "About SafeHito") {
            Text("SafeHito monitors water quality and fungal health in African catfish using AI and real-time sensors to ensure a healthy aquatic environment.")
        }
        ModalSheetType.Developers -> ModalSheet(devsSheetState, { showSheet = null }, "Developers") {
            Text("Capstone Devs:\n\nRaely Ivan Reyes\nJesie Reyes\nDavid Monzon\nJomari Ramos\nFelicia May Sagum")
        }
        null -> {}
    }
}

@Composable
fun ActionCard(title: String, icon: ImageVector, iconTint: Color, isDarkTheme: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}


@Composable
fun ConnectionModeDropdown(selected: String, onSelected: (String) -> Unit) {
    val labels = mapOf(
        "auto" to "Auto",
        "lan" to "LAN",
        "cloudflare" to "Cloudflare"
    )
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { expanded = true }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(labels[selected] ?: selected)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            labels.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelected(value) // stores "auto", "lan", or "cloudflare"
                        expanded = false
                    }
                )
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            content()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

enum class ModalSheetType {
    Support, Privacy, Terms, About, Developers
}
