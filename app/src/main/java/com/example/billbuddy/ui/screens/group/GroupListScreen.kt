package com.example.billbuddy.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billbuddy.data.model.Group
import com.example.billbuddy.ui.theme.AppBackground
import com.example.billbuddy.ui.viewmodel.GroupViewModel
import com.example.billbuddy.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(
    viewModel: GroupViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddGroup: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit
) {
    val groupsResource by viewModel.groups.collectAsState()
    val joinStatus by viewModel.joinGroupStatus.collectAsState()
    
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinGroupName by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(joinStatus) {
        when (joinStatus) {
            is Resource.Success -> {
                showJoinDialog = false
                joinGroupName = ""
                viewModel.resetStatus()
                snackbarHostState.showSnackbar("Gia nhập nhóm thành công")
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(joinStatus?.message ?: "Lỗi khi gia nhập nhóm")
                viewModel.resetStatus()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Nhóm",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showJoinDialog = true }) {
                        Icon(Icons.Default.GroupAdd, contentDescription = "Join Group")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddGroup,
                shape = CircleShape,
                containerColor = Color(0xFFD47500),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(28.dp))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppBackground
    ) { innerPadding ->
        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                title = { Text("Gia nhập nhóm") },
                text = {
                    Column {
                        Text("Nhập tên nhóm để gia nhập")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = joinGroupName,
                            onValueChange = { joinGroupName = it },
                            placeholder = { Text("Tên nhóm") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.joinGroupByName(joinGroupName) },
                        enabled = joinGroupName.isNotBlank() && joinStatus !is Resource.Loading
                    ) {
                        Text("Gia nhập")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }

        when (val resource = groupsResource) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = resource.message ?: "Unknown Error", color = Color.Red)
                }
            }
            is Resource.Success -> {
                val groups = resource.data ?: emptyList()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Section Title
                    item {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "Nhóm của tôi",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${groups.size} nhóm",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    // Group Items
                    items(groups) { group ->
                        GroupCard(group, onClick = { onNavigateToGroupDetail(group.documentId) })
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun GroupSummaryCard(groupCount: Int) {
// ...
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color(0xFF212121),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Tổng quan nhóm",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Số nhóm", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        "$groupCount",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tổng chi tiêu", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        "17,700,000đ",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFF44336)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Thành viên", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        "21",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun GroupCard(group: Group, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFBBDEFB).copy(alpha = 0.6f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = group.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.Gray
                    )
                    Text(
                        text = "${group.memberIds.size} thành viên",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            // Total Expense
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "0đ", // TODO: Calculate actual total
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD32F2F)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Text(
                        text = "Chi tiết",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD47500)
                    )
                }
            }
        }
    }
}
