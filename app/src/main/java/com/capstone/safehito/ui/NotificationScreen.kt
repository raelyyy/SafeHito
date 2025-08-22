package com.capstone.safehito.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.capstone.safehito.viewmodel.NotificationViewModel
import com.capstone.safehito.util.toRelativeTime
import com.capstone.safehito.util.toDateOnly
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.capstone.safehito.ui.theme.LightBlueBackground
import com.capstone.safehito.ui.theme.ReadNotificationBackground
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    notificationViewModel: NotificationViewModel,
    onBack: () -> Unit = {},
    darkTheme: Boolean
) {
    BackHandler { onBack() }

    val notifications by notificationViewModel.notifications.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    fun refreshNotifications() {
        isRefreshing = true
        // Trigger a manual refresh of notifications
        notificationViewModel.loadNotifications()
        // Delay to show refresh indicator
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            kotlinx.coroutines.delay(1000)
            isRefreshing = false
        }
    }

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val hasUnread = notifications.any { !it.read }
                    val hasNotifications = notifications.isNotEmpty()
                    var showConfirmDialog by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { notificationViewModel.markAllAsRead() },
                        enabled = hasUnread
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = "Mark all as read",
                            tint = if (hasUnread) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.4f)
                        )
                    }

                    IconButton(
                        onClick = { showConfirmDialog = true },
                        enabled = hasNotifications
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Delete all",
                            tint = if (hasNotifications) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.4f)
                        )
                    }

                    if (showConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showConfirmDialog = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        notificationViewModel.deleteAllNotifications()
                                        showConfirmDialog = false
                                    }
                                ) {
                                    Text(
                                        "Delete All",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirmDialog = false }) {
                                    Text("Cancel")
                                }
                            },
                            title = { Text("Delete All Notifications") },
                            text = { Text("Are you sure you want to delete all notifications? This action cannot be undone.") }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { refreshNotifications() },
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
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "No Notifications Icon",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No important notifications yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }

            } else {
                val grouped = notifications
                    .sortedByDescending { it.time }
                    .groupBy { it.time.toDateOnly() }

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState, // 🔥 enable scroll tracking
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        grouped.forEach { (date, groupNotifications) ->
                            item {
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(groupNotifications) { notification ->
                                NotificationCard(
                                    message = notification.message,
                                    time = notification.time.toRelativeTime(),
                                    read = notification.read,
                                    onDelete = { notificationViewModel.deleteNotification(notification.id) },
                                    onClickMarkRead = {
                                        if (!notification.read) {
                                            notificationViewModel.markNotificationAsRead(notification.id)
                                        }
                                    },
                                    darkTheme = darkTheme
                                )
                            }
                        }
                    }

                    val buttonBackgroundColor = if (darkTheme) {
                        Color(0xFF2C2C2C) // Dark gray for dark mode
                    } else {
                        Color.White
                    }

                    AnimatedVisibility(
                        visible = showScrollToTop,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 28.dp, bottom = 30.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            shadowElevation = 8.dp,
                            color = buttonBackgroundColor, // 🔥 solid, theme-aware gray
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
                                    tint = Color(0xFF5DCCFC) // ✅ your solid custom color
                                )
                            }
                        }



                    }


                }
            }

        }
    }
}

@Composable
fun NotificationCard(
    message: String,
    time: String,
    read: Boolean,
    onDelete: () -> Unit,
    onClickMarkRead: () -> Unit,
    modifier: Modifier = Modifier,
    darkTheme: Boolean
) {

    val lowerMsg = message.lowercase()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val (icon, iconTint) = when {
        listOf("infected", "infection").any { it in lowerMsg } ->
            Icons.Default.BugReport to MaterialTheme.colorScheme.error
        listOf("warning", "alert", "issue", "caution").any { it in lowerMsg } ->
            Icons.Default.Warning to MaterialTheme.colorScheme.error
        "temperature" in lowerMsg ->
            Icons.Default.Thermostat to MaterialTheme.colorScheme.primary
        "oxygen" in lowerMsg || "dissolved" in lowerMsg ->
            Icons.Default.BubbleChart to MaterialTheme.colorScheme.primary
        "ph" in lowerMsg ->
            Icons.Default.Science to MaterialTheme.colorScheme.primary
        "turbidity" in lowerMsg ->
            Icons.Default.Opacity to MaterialTheme.colorScheme.primary
        else ->
            Icons.Default.Info to MaterialTheme.colorScheme.primary
    }

    val ReadNotificationBackground = Color.White

    val backgroundColor = when {
        listOf("infected", "infection").any { it in lowerMsg } && !read ->
            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        read -> {
            if (darkTheme ) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            } else {
                ReadNotificationBackground
            }
        }
        else -> {
            if (darkTheme ) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            } else {
                LightBlueBackground
            }
        }
    }

    val borderColor = when {
        (listOf("infected", "infection").any { it in lowerMsg } && !read) || "healthy" in lowerMsg ->
            MaterialTheme.colorScheme.error
        else -> Color.Transparent
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClickMarkRead() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (read) FontWeight.Normal else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete notification",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Notification") },
            text = { Text("Are you sure you want to delete this notification?") }
        )
    }
}


