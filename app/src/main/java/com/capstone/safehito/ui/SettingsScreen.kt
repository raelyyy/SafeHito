package com.capstone.safehito.ui

import android.content.pm.PackageManager
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
import com.google.firebase.messaging.FirebaseMessaging

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

    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
    } catch (e: PackageManager.NameNotFoundException) {
        "1.0.0"
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
                    Icon(Icons.Default.DarkMode, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Dark Mode", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = isDarkTheme, onCheckedChange = { onToggleTheme() })
                }
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid
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

            LaunchedEffect(uid) {
                uid?.let {
                    FirebaseDatabase.getInstance()
                        .getReference("users/$uid/settings/saltBathCooldownMinutes")
                        .get()
                        .addOnSuccessListener { snap ->
                            selectedCooldown = snap.getValue(Int::class.java) ?: 720
                        }
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
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF5DCCFC), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Salt Bath Cooldown",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )

                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(
                                cooldownOptions.find { it.first == selectedCooldown }?.second ?: "",
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
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
                                        selectedCooldown = value
                                        expanded = false
                                        uid?.let {
                                            FirebaseDatabase.getInstance()
                                                .getReference("users/$uid/settings/saltBathCooldownMinutes")
                                                .setValue(value)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            /*Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        isPushEnabled = !isPushEnabled
                        if (isPushEnabled) FirebaseMessaging.getInstance().subscribeToTopic("all")
                        else FirebaseMessaging.getInstance().unsubscribeFromTopic("all")
                    },
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
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Push Notifications", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = isPushEnabled, onCheckedChange = {
                        isPushEnabled = it
                        if (it) FirebaseMessaging.getInstance().subscribeToTopic("all")
                        else FirebaseMessaging.getInstance().unsubscribeFromTopic("all")
                    })
                }
            }*/

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
            Text("Capstone Devs:\n\nRaely Ivan Reyes\nJomari Ramos\nJesie Reyes\nDavid Monzon\nMay Sagum")
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
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge)
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
