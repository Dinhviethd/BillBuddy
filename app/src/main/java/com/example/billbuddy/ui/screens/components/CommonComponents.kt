package com.example.billbuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billbuddy.data.model.AppNotification
import com.example.billbuddy.data.model.NotificationType

@Composable
fun NotificationIconButton(
    notifications: List<AppNotification>,
    onRemoveNotification: (String) -> Unit = {},
    onClearAll: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }

    IconButton(onClick = { showDialog = true }) {
        BadgedBox(
            badge = {
                if (notifications.isNotEmpty()) {
                    Badge(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                        modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                    ) {
                        Text(notifications.size.toString())
                    }
                }
            }
        ) {
            Icon(
                imageVector = if (notifications.isNotEmpty()) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = if (notifications.isNotEmpty()) Color(0xFFD32F2F) else LocalContentColor.current
            )
        }
    }

    if (showDialog) {
        NotificationDialog(
            notifications = notifications,
            onDismiss = { showDialog = false },
            onRemove = onRemoveNotification,
            onClearAll = onClearAll
        )
    }
}

@Composable
fun NotificationDialog(
    notifications: List<AppNotification>,
    onDismiss: () -> Unit,
    onRemove: (String) -> Unit,
    onClearAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Thông báo",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Xóa hết", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        },
        text = {
            Box(modifier = Modifier.heightIn(max = 450.dp, min = 100.dp)) {
                if (notifications.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Không có thông báo mới",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications, key = { it.id }) { notification ->
                            NotificationItemRow(
                                notification = notification,
                                onRemove = { onRemove(notification.id) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Đóng")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun NotificationItemRow(
    notification: AppNotification,
    onRemove: () -> Unit
) {
    val (icon, color, bgColor) = when (notification.type) {
        NotificationType.URGENT -> Triple(Icons.Default.Error, Color(0xFFD32F2F), Color(0xFFFFEBEE))
        NotificationType.WARNING -> Triple(Icons.Default.Warning, Color(0xFFF57C00), Color(0xFFFFF3E0))
        NotificationType.INFO -> Triple(Icons.Default.Schedule, Color(0xFF1976D2), Color(0xFFE3F2FD))
        NotificationType.BUDGET_EXCEEDED -> Triple(Icons.Default.AccountBalanceWallet, Color(0xFFC2185B), Color(0xFFFCE4EC))
        NotificationType.SYSTEM -> Triple(Icons.Default.Notifications, Color(0xFF455A64), Color(0xFFECEFF1))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = "Số tiền: ${notification.amount}đ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Xóa",
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun StatisticsInfoBox() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE3F2FD)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF1976D2))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Thống kê", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                Text(
                    "Bạn đã tiết kiệm được 42% so với mục tiêu chi tiêu tháng này",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}
