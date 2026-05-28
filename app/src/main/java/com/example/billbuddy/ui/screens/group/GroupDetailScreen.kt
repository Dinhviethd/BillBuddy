package com.example.billbuddy.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billbuddy.data.model.Category
import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.data.model.ExpenseSplit
import com.example.billbuddy.data.model.Group
import com.example.billbuddy.data.model.SplitStatus
import com.example.billbuddy.ui.viewmodel.GroupViewModel
import com.example.billbuddy.utils.Resource
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    viewModel: GroupViewModel,
    onNavigateBack: () -> Unit
) {
    val groupResource by viewModel.currentGroup.collectAsState()
    val splitsResource by viewModel.userSplits.collectAsState()
    val settledHistoryResource by viewModel.settledHistory.collectAsState()
    val memberEmails by viewModel.memberEmails.collectAsState()
    val groupCategories by viewModel.groupCategories.collectAsState()
    val leaveStatus by viewModel.leaveGroupStatus.collectAsState()
    val settleStatus by viewModel.settleStatus.collectAsState()
    val createCategoryStatus by viewModel.createCategoryStatus.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddExpense by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }
    var selectedHistoryItem by remember { mutableStateOf<ExpenseSplit?>(null) }

    LaunchedEffect(groupId) {
        viewModel.getGroupById(groupId)
    }

    LaunchedEffect(leaveStatus, settleStatus, createCategoryStatus) {
        val currentLeave = leaveStatus
        val currentSettle = settleStatus
        val currentCreateCat = createCategoryStatus

        if (currentLeave is Resource.Success || currentSettle is Resource.Success || currentCreateCat is Resource.Success) {
            val msg = when {
                currentLeave is Resource.Success -> "Đã rời nhóm"
                currentSettle is Resource.Success -> "Đã thanh toán và lưu vào chi tiêu cá nhân"
                currentCreateCat is Resource.Success -> "Đã thêm loại chi tiêu"
                else -> ""
            }
            
            viewModel.resetStatus()
            
            if (msg.isNotEmpty()) {
                snackbarHostState.showSnackbar(msg)
            }
            
            if (currentLeave is Resource.Success) {
                onNavigateBack()
            }
            if (currentCreateCat is Resource.Success) {
                showAddCategory = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    val title = if (groupResource is Resource.Success) (groupResource as Resource.Success).data?.name ?: "Chi tiết nhóm" else "Đang tải..."
                    Text(title, fontWeight = FontWeight.Bold) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddCategory = true }) {
                        Icon(Icons.Default.Category, contentDescription = "Add Category")
                    }
                    IconButton(onClick = { viewModel.leaveGroup(groupId) }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Leave Group", tint = Color.Red)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExpense = true },
                containerColor = Color(0xFF5E49E2),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Group Expense")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val resource = groupResource) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(resource.message ?: "Error loading group")
                    }
                }
                is Resource.Success -> {
                    val group = resource.data!!
                    GroupDetailContent(
                        group = group,
                        splits = (splitsResource as? Resource.Success)?.data ?: emptyList(),
                        settledHistory = (settledHistoryResource as? Resource.Success)?.data ?: emptyList(),
                        memberEmails = memberEmails,
                        onSettle = { viewModel.settleSplit(it) },
                        onHistoryItemClick = { selectedHistoryItem = it }
                    )
                }
            }

            if (showAddExpense && groupResource is Resource.Success) {
                val group = (groupResource as Resource.Success).data!!
                val categories = (groupCategories as? Resource.Success)?.data ?: emptyList()
                AddGroupExpenseDialog(
                    group = group,
                    memberEmails = memberEmails,
                    categories = categories,
                    onDismiss = { showAddExpense = false },
                    onConfirm = { expense, splits ->
                        viewModel.splitExpense(expense, splits)
                        showAddExpense = false
                    }
                )
            }

            if (showAddCategory) {
                var newCatName by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showAddCategory = false },
                    title = { Text("Thêm loại chi tiêu mới") },
                    text = {
                        OutlinedTextField(
                            value = newCatName,
                            onValueChange = { newCatName = it },
                            label = { Text("Tên loại (VD: Ăn uống, Tiền điện...)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(onClick = { viewModel.createGroupCategory(groupId, newCatName) }) {
                            Text("Thêm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddCategory = false }) { Text("Hủy") }
                    }
                )
            }
            
            selectedHistoryItem?.let { historyItem ->
                SettlementDetailPopup(
                    historyItem = historyItem,
                    email = memberEmails[historyItem.paidBy] ?: historyItem.paidBy,
                    groupCategories = (groupCategories as? Resource.Success)?.data ?: emptyList(),
                    onDismiss = { selectedHistoryItem = null }
                )
            }
        }
    }
}

@Composable
fun GroupDetailContent(
    group: Group,
    splits: List<ExpenseSplit>,
    settledHistory: List<ExpenseSplit>,
    memberEmails: Map<String, String>,
    onSettle: (ExpenseSplit) -> Unit,
    onHistoryItemClick: (ExpenseSplit) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Members Section
        item {
            Text("Thành viên (${group.memberIds.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        items(group.memberIds) { memberId ->
            MemberItem(memberEmails[memberId] ?: memberId)
        }

        // User's Personal Splits Section (Pending)
        val pendingSplits = splits.filter { it.status == SplitStatus.PENDING }
        if (pendingSplits.isNotEmpty()) {
            item {
                Text("Khoản bạn cần trả", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFD32F2F))
            }
            items(pendingSplits) { split ->
                PersonalSplitItem(split, onSettle)
            }
        }

        // Settlement History Section
        item {
            Text("Lịch sử chi trả", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        if (settledHistory.isEmpty()) {
            item {
                Text("Chưa có lịch sử chi trả", color = Color.Gray, fontSize = 14.sp)
            }
        }
        items(settledHistory) { historyItem ->
            SettlementHistoryItem(
                historyItem = historyItem, 
                memberEmail = memberEmails[historyItem.paidBy] ?: historyItem.paidBy,
                onClick = { onHistoryItemClick(historyItem) }
            )
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun MemberItem(email: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF5E49E2).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF5E49E2))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(email, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun PersonalSplitItem(split: ExpenseSplit, onSettle: (ExpenseSplit) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(split.description, fontWeight = FontWeight.Bold)
                Text("${split.amount} đ", color = Color(0xFFD32F2F))
            }
            Button(
                onClick = { onSettle(split) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text("Trả", color = Color.White)
            }
        }
    }
}

@Composable
fun SettlementHistoryItem(historyItem: ExpenseSplit, memberEmail: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(memberEmail, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(historyItem.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text("${historyItem.amount} đ", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
        }
    }
}

@Composable
fun SettlementDetailPopup(historyItem: ExpenseSplit, email: String, groupCategories: List<Category>, onDismiss: () -> Unit) {
    // Find category name if it exists (assuming categoryId might be stored in future or derived)
    // For now, we'll show a default or based on group context
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        },
        title = {
            Text("Chi tiết thanh toán", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF5E49E2), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Người trả: ", fontWeight = FontWeight.Medium)
                    Text(email)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF5E49E2), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nội dung: ", fontWeight = FontWeight.Medium)
                    Text(historyItem.description)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Danh mục: ", fontWeight = FontWeight.Medium)
                    Text("Chi tiêu nhóm", color = Color(0xFFE91E63))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Số tiền: ", fontWeight = FontWeight.Medium)
                    Text("${historyItem.amount} đ", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trạng thái: ", fontWeight = FontWeight.Medium)
                    Text("Đã thanh toán", color = Color(0xFF4CAF50))
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupExpenseDialog(
    group: Group,
    memberEmails: Map<String, String>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Expense, List<ExpenseSplit>) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var splitMode by remember { mutableIntStateOf(0) } // 0: Auto, 1: Manual %
    
    val memberPercentages = remember { mutableStateMapOf<String, Float>().apply {
        group.memberIds.forEach { this[it] = 100f / group.memberIds.size }
    } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm chi tiêu nhóm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Số tiền") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Loại chi tiêu", fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.heightIn(max = 100.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.documentId,
                            onClick = { selectedCategoryId = cat.documentId },
                            label = { Text(cat.name) }
                        )
                    }
                }
                
                Text("Phương thức chia", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = splitMode == 0,
                        onClick = { splitMode = 0 },
                        label = { Text("Tự động (Đều)") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = splitMode == 1,
                        onClick = { splitMode = 1 },
                        label = { Text("Thủ công (%)") }
                    )
                }

                if (splitMode == 1) {
                    val totalAmount = amount.toLongOrNull() ?: 0L
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(group.memberIds) { memberId ->
                            val percent = memberPercentages[memberId] ?: 0f
                            val calculated = (totalAmount * percent / 100).toLong()
                            Column {
                                Text("${memberEmails[memberId] ?: memberId}: ${percent.toInt()}% ($calculated đ)")
                                Slider(
                                    value = percent,
                                    onValueChange = { memberPercentages[memberId] = it },
                                    valueRange = 0f..100f
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val totalAmount = amount.toLongOrNull() ?: 0L
                val expense = Expense(
                    amount = totalAmount,
                    description = description,
                    groupId = group.documentId,
                    date = Timestamp.now(),
                    categoryId = selectedCategoryId // Using selected category
                )
                
                val splits = group.memberIds.map { memberId ->
                    val splitAmount = if (splitMode == 0) {
                        totalAmount / group.memberIds.size
                    } else {
                        (totalAmount * (memberPercentages[memberId] ?: 0f) / 100).toLong()
                    }
                    ExpenseSplit(
                        amount = splitAmount,
                        description = description,
                        paidBy = memberId,
                        groupId = group.documentId,
                        status = SplitStatus.PENDING
                    )
                }
                onConfirm(expense, splits)
            }) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
