package com.capstone.safehito.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color

private val routeMap = mapOf(
    "Dashboard" to "dashboard",
    "Scan" to "scan",
    "Records" to "records",
    "Profile" to "profile"
)

@Composable
fun FloatingNavBar(
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    darkTheme: Boolean
) {
    val items = listOf(
        Triple("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        Triple("Scan", Icons.Filled.Camera, Icons.Outlined.Camera),
        Triple("Records", Icons.Filled.Folder, Icons.Outlined.FolderOpen),
        Triple("Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )



    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp),
        shape = RoundedCornerShape(50),
        tonalElevation = 4.dp,
        color = if (darkTheme) MaterialTheme.colorScheme.surface else Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (label, filledIcon, outlinedIcon) ->
                val route = routeMap[label] ?: "dashboard"
                val isSelected = route == selectedRoute
                val tintColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                TextButton(onClick = { onItemSelected(route) }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) filledIcon else outlinedIcon,
                            contentDescription = label,
                            tint = tintColor
                        )
                        Text(
                            text = label,
                            color = tintColor,
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .width(50.dp)
                                    .height(4.dp)
                                    .background(tintColor, RoundedCornerShape(2.dp))
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
