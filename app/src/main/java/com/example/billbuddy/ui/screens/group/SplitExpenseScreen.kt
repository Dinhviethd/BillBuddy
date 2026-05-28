package com.example.billbuddy.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.data.model.ExpenseSplit
import com.example.billbuddy.ui.viewmodel.GroupViewModel
import com.example.billbuddy.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitExpenseScreen(
    expenseId: String,
    viewModel: GroupViewModel,
    onNavigateBack: () -> Unit
) {
    // This is a simplified version. In a real app, you'd fetch the expense first.
    // For now, let's assume we have the expense details or we just want to create splits.
    
    var amount by remember { mutableStateOf(0L) }
    var description by remember { mutableStateOf("") }
    var selectedMembers by remember { mutableStateOf(setOf<String>()) }
    
    val splitStatus by viewModel.splitExpenseStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(splitStatus) {
        if (splitStatus is Resource.Success) {
            viewModel.resetStatus()
            onNavigateBack()
        } else if (splitStatus is Resource.Error) {
            snackbarHostState.showSnackbar(splitStatus?.message ?: "Lỗi khi chia tiền")
            viewModel.resetStatus()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chia tiền", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Tính năng chia tiền đang được hoàn thiện...", fontSize = 16.sp, color = Color.Gray)
            
            // Logic for splitting would go here:
            // 1. Select Group
            // 2. Select Members
            // 3. Enter Amount
            // 4. Choose Split Method (Equally, Percent, etc.)
            
            Button(
                onClick = {
                    // Example call
                    // viewModel.splitExpense(expense, splits)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E49E2))
            ) {
                Text("Xác nhận chia tiền", color = Color.White)
            }
        }
    }
}
