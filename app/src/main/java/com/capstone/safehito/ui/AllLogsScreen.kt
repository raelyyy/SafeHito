package com.capstone.safehito.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.capstone.safehito.model.AuditLog
import com.capstone.safehito.viewmodel.AdminViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import kotlinx.coroutines.launch
import android.content.ContentValues
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.*
import androidx.compose.foundation.interaction.MutableInteractionSource

@Composable
fun LogCard(
    action: String,
    timestamp: Long,
    performedBy: String,
    details: String,
    category: String,
    severity: String,
    sessionId: String? = null,
    userId: String? = null,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(timestamp) {
        if (timestamp > 0) dateFormat.format(Date(timestamp)) else "Unknown time"
    }

    val severityColor = when (severity.uppercase()) {
        "CRITICAL", "ERROR" -> Color.Red
        "WARNING" -> Color(0xFFFFA726)
        "INFO" -> Color(0xFF66BB6A)
        else -> Color.Gray
    }

    var showDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showDialog = true } // ✅ make card clickable
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(severityColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "By $performedBy • $formattedDate",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (details.isNotEmpty()) {
                    Text(
                        text = details,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // ✅ Dialog with full log details
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text("Log Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Action: $action", fontWeight = FontWeight.SemiBold)
                    Text("Performed By: $performedBy")
                    Text("Timestamp: $formattedDate")
                    Text("Severity: $severity")
                    Text("Category: $category")
                    if (!details.isNullOrEmpty()) Text("Details: $details")
                    if (!userId.isNullOrEmpty()) Text("User ID: $userId")
                    if (!sessionId.isNullOrEmpty()) Text("Session ID: $sessionId")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AllLogsScreen(
    darkTheme: Boolean,
    adminViewModel: AdminViewModel = viewModel(),
    navController: NavHostController
) {
    val auditLogs by adminViewModel.auditLogs.collectAsState()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)
    val context = LocalContext.current

    // Scroll state for push-to-top
    val listState = rememberLazyListState()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    // Filter states
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedSeverity by remember { mutableStateOf("ALL") }

    // ✅ Filtered logs
    val filteredLogs = remember(auditLogs, selectedCategory, selectedSeverity) {
        auditLogs
            .filter { log ->
                val categoryMatch = selectedCategory == "ALL" || log.category.equals(selectedCategory, ignoreCase = true)
                val severityMatch = selectedSeverity == "ALL" || log.severity.equals(selectedSeverity, ignoreCase = true)
                categoryMatch && severityMatch
            }
            .sortedByDescending { it.timestamp }
    }

    LaunchedEffect(Unit) {
        adminViewModel.refreshAuditLogs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "All System Logs",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    var showDownloadDialog by remember { mutableStateOf(false) }

                    // Delete all logs
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete All Logs",
                            tint = Color.Red
                        )
                    }

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Delete All Logs?") },
                            text = { Text("This action cannot be undone. Do you really want to clear all logs?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDeleteDialog = false
                                    scope.launch { adminViewModel.clearAllAuditLogs { } }
                                }) {
                                    Text("Delete", color = Color.Red)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    // ✅ Download button with confirmation
                    IconButton(onClick = { showDownloadDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Download Logs",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (showDownloadDialog) {
                        AlertDialog(
                            onDismissRequest = { showDownloadDialog = false },
                            title = { Text("Export Logs") },
                            text = { Text("Do you want to save the filtered logs as CSV in Documents/SafeHito?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDownloadDialog = false
                                    scope.launch { exportLogsAsCsv(context, filteredLogs) }
                                }) {
                                    Text("Save", color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDownloadDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        adminViewModel.refreshAuditLogs()
                        kotlinx.coroutines.delay(1000)
                        isRefreshing = false
                    }
                },
                indicator = { state, trigger ->
                    SwipeRefreshIndicator(
                        state = state,
                        refreshTriggerDistance = trigger,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        scale = true,
                        arrowEnabled = true,
                    )
                }
            ) {
                if (filteredLogs.isNotEmpty()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredLogs) { log ->
                            LogCard(
                                action = log.action,
                                timestamp = log.timestamp,
                                performedBy = log.performedBy,
                                details = log.details.ifEmpty { "No description available" },
                                category = log.category.ifEmpty { "SYSTEM" },
                                severity = log.severity.ifEmpty { "INFO" },
                                sessionId = log.sessionId,
                                userId = log.userId,
                                darkTheme = darkTheme
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (auditLogs.isEmpty()) "No logs available" else "No logs match the current filters",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ✅ Floating scroll-to-top button
            AnimatedVisibility(
                visible = showScrollToTop,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 80.dp)
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
                            scope.launch {
                                listState.animateScrollToItem(0)
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

// ✅ Save logs like your PDF generator (Documents/SafeHito)
suspend fun exportLogsAsCsv(context: Context, logs: List<AuditLog>) {
    if (logs.isEmpty()) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "⚠️ No logs to export", Toast.LENGTH_SHORT).show()
        }
        return
    }

    val filename = "SafeHito_Logs_${System.currentTimeMillis()}.csv"

    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/SafeHito")
    }

    val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

    try {
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                val writer = OutputStreamWriter(outputStream)
                writer.append("Action,Performed By,Timestamp,Severity,Category,Details,UserId,SessionId\n")

                val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
                logs.forEach { log ->
                    val date = sdf.format(Date(log.timestamp))
                    writer.append("${log.action},${log.performedBy},$date,${log.severity},${log.category},\"${log.details}\",${log.userId},${log.sessionId}\n")
                }

                writer.flush()
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "✅ Logs saved to Documents/SafeHito/$filename", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "❌ Failed to create log file", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
