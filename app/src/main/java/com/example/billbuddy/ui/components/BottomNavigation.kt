package com.example.billbuddy.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AppBottomNavigation(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onAddClick: () -> Unit,
    onStatsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    BottomAppBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    label = "Trang chủ",
                    isSelected = currentRoute == "home",
                    onClick = onHomeClick
                )
                BottomNavItem(
                    icon = Icons.Default.DateRange,
                    label = "Lịch",
                    isSelected = currentRoute == "calendar",
                    onClick = onCalendarClick
                )

                Spacer(modifier = Modifier.width(48.dp))
                
                BottomNavItem(
                    icon = Icons.Default.PieChart,
                    label = "Thống kê",
                    isSelected = currentRoute == "statistics",
                    onClick = onStatsClick
                )
                BottomNavItem(
                    icon = Icons.Default.Person,
                    label = "Cá nhân",
                    isSelected = currentRoute == "profile",
                    onClick = onProfileClick
                )
            }
        }
    )
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) Color(0xFFD47500) else Color.Gray
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, contentDescription = label, tint = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
