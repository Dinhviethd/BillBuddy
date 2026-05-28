package com.example.billbuddy.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billbuddy.data.model.AppNotification
import com.example.billbuddy.data.model.Budget
import com.example.billbuddy.data.model.Category
import com.example.billbuddy.data.model.CategoryType
import com.example.billbuddy.navigation.Screen
import com.example.billbuddy.ui.components.AppBottomNavigation
import com.example.billbuddy.ui.components.NotificationIconButton
import com.example.billbuddy.ui.theme.AmberDark
import com.example.billbuddy.ui.viewmodel.AuthViewModel
import com.example.billbuddy.ui.viewmodel.StatisticsViewModel
import com.example.billbuddy.utils.Resource
import java.util.Locale

data class SettingItem(
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTint: Color,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    statsViewModel: StatisticsViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onSignOut: () -> Unit,
    notifications: List<AppNotification> = emptyList()
) {
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showBudgetListDialog by remember { mutableStateOf(false) }

    val statsUiState by statsViewModel.uiState.collectAsState()

    if (showSignOutDialog) {
        SignOutDialog(
            onConfirm = {
                showSignOutDialog = false
                onSignOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    if (showBudgetDialog) {
        BudgetDialog(
            categories = statsUiState.categories,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { categoryId, amount ->
                statsViewModel.setBudget(categoryId, amount)
                showBudgetDialog = false
            }
        )
    }

    if (showBudgetListDialog) {
        BudgetListDialog(
            budgets = statsUiState.budgets,
            categories = statsUiState.categories,
            onDismiss = { showBudgetListDialog = false },
            onDeleteBudget = { statsViewModel.deleteBudget(it) },
            onAddBudget = {
                showBudgetListDialog = false
                showBudgetDialog = true
            }
        )
    }

    Scaffold(
        topBar = {
            ProfileTopBar(
                notifications = notifications
            )
        },
        bottomBar = {
            AppBottomNavigation(
                currentRoute = Screen.Profile.route,
                onHomeClick = onNavigateToHome,
                onCalendarClick = onNavigateToCalendar,
                onAddClick = onNavigateToAddExpense,
                onStatsClick = onNavigateToStatistics,
                onProfileClick = {}
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddExpense,
                shape = CircleShape,
                containerColor = Color(0xFFD47500),
                contentColor = Color.White,
                modifier = Modifier.offset(y = 50.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { 
                val userData = (viewModel.userData.value as? Resource.Success)?.data
                ProfileCard(
                    user = userData,
                    firebaseUser = viewModel.currentUser,
                    onEditClick = onNavigateToEditProfile
                ) 
            }

            item {
                val accountItems = listOf(
                    SettingItem(
                        icon = Icons.Default.Group,
                        iconBgColor = Color(0xFFBBDEFB),
                        iconTint = Color(0xFF1976D2),
                        title = "Nhóm",
                        subtitle = "Quản lý chi tiêu chung",
                        onClick = onNavigateToGroups
                    ),
                    SettingItem(
                        icon = Icons.Default.ReceiptLong,
                        iconBgColor = Color(0xFFC8E6C9),
                        iconTint = Color(0xFF388E3C),
                        title = "Khoản nợ",
                        subtitle = "Theo dõi nợ và cho vay",
                        onClick = onNavigateToDebts
                    ),
                    SettingItem(
                        icon = Icons.Default.LockReset,
                        iconBgColor = Color(0xFFE1BEE7),
                        iconTint = Color(0xFF7B1FA2),
                        title = "Đổi mật khẩu",
                        subtitle = "Cập nhật bảo mật tài khoản",
                        onClick = onNavigateToChangePassword
                    )
                )
                SettingSection(title = "Tài khoản", items = accountItems)
            }

            item {
                val settingItems = listOf(
                    SettingItem(
                        icon = Icons.Default.Savings,
                        iconBgColor = Color(0xFFFFF9C4),
                        iconTint = Color(0xFFFBC02D),
                        title = "Hạn mức chi tiêu",
                        subtitle = "Thiết lập cảnh báo chi tiêu",
                        onClick = { showBudgetListDialog = true }
                    )
                )
                SettingSection(title = "Cài đặt", items = settingItems)
            }

            item {
                SignOutButton(onClick = { showSignOutDialog = true })
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(
    notifications: List<AppNotification>
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Cá nhân",
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            NotificationIconButton(
                notifications = notifications
            )
        }
    )
}

@Composable
fun ProfileCard(
    user: com.example.billbuddy.data.model.User?,
    firebaseUser: com.google.firebase.auth.FirebaseUser?,
    onEditClick: () -> Unit
) {
    val displayName = user?.displayName ?: firebaseUser?.displayName ?: "Người dùng"
    val email = user?.email ?: firebaseUser?.email ?: "Chưa cập nhật email"
    val initials = if (displayName.isNotBlank()) {
        displayName.split(" ").filter { it.isNotEmpty() }.take(2).map { it[0] }.joinToString("").uppercase()
    } else "BB"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8B931)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFF3E0)
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onEditClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(0xFFD47500),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Sửa",
                        color = Color(0xFFD47500),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SettingSection(title: String, items: List<SettingItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingRow(item = item)
                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                            thickness = 0.5.dp,
                            color = Color(0xFFEEEEEE)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingRow(item: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = item.iconBgColor.copy(alpha = 0.6f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Labels
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Chevron
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SignOutButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Đăng xuất",
                color = Color(0xFFE53935),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
fun SignOutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Đăng xuất",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Đăng xuất",
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Hủy",
                    color = Color.Gray
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thiết lập hạn mức") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        val name = categories.find { it.documentId == selectedCategoryId }?.name ?: "Chọn danh mục (Trống = Tổng)"
                        Text(name)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Tổng chi tiêu") }, onClick = { selectedCategoryId = ""; expanded = false })
                        categories.filter { it.type == CategoryType.EXPENSE }.forEach { category ->
                            DropdownMenuItem(text = { Text(category.name) }, onClick = { selectedCategoryId = category.documentId; expanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Số tiền hạn mức") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                val amt = amount.toLongOrNull() ?: 0L
                if (amt > 0) onConfirm(selectedCategoryId, amt)
            }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun BudgetListDialog(
    budgets: List<Budget>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onDeleteBudget: (String) -> Unit,
    onAddBudget: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hạn mức chi tiêu")
                IconButton(onClick = { onDismiss() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (budgets.isEmpty()) {
                    Text(
                        "Chưa có hạn mức nào được thiết lập.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    budgets.forEach { budget ->
                        val categoryName = categories.find { it.documentId == budget.categoryId }?.name ?: "Tổng chi tiêu"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(categoryName, fontWeight = FontWeight.Medium)
                                Text(formatMoney(budget.amount.toDouble()), color = AmberDark, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { onDeleteBudget(budget.documentId) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                    }
                }
                
                Button(
                    onClick = onAddBudget,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberDark)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thêm hạn mức")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

private fun formatMoney(amount: Double): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
    return formatter.format(amount).replace("₫", "đ")
}
