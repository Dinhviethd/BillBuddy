package com.example.billbuddy.ui.screens.home

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billbuddy.ui.viewmodel.ExpenseViewModel
import com.example.billbuddy.utils.Resource
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val initialDate = remember {
        val calendar = Calendar.getInstance()
        String.format(
            Locale.US,
            "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    var date by remember { mutableStateOf(initialDate) }
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"

    val categoryOptions = remember {
        listOf("Ăn uống", "Giải trí", "Mua sắm", "Di chuyển", "Sức khỏe")
    }

    val saveState by viewModel.saveState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(saveState) {
        when (val result = saveState) {
            is Resource.Success -> {
                viewModel.clearSaveState()
                onNavigateBack()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(result.message ?: "Lưu thất bại")
                viewModel.clearSaveState()
            }
            else -> Unit
        }
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm mới giao dịch", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = Color(0xFF5E49E2),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Thông tin giao dịch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    // Transaction Type Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { transactionType = "EXPENSE" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (transactionType == "EXPENSE") Color(0xFFE8B931) else Color(0xFFF0F0F0)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Chi tiêu", color = if (transactionType == "EXPENSE") Color.White else Color.Gray)
                        }
                        Button(
                            onClick = { transactionType = "INCOME" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (transactionType == "INCOME") Color(0xFF4CAF50) else Color(0xFFF0F0F0)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Thu nhập", color = if (transactionType == "INCOME") Color.White else Color.Gray)
                        }
                    }

                    Column {
                        Text("Ngày", color = Color.Gray, fontSize = 14.sp)
                        OutlinedTextField(
                            value = date,
                            onValueChange = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() },
                            enabled = false,
                            trailingIcon = {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { datePickerDialog.show() }
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.LightGray,
                                disabledTrailingIconColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    if (transactionType == "EXPENSE") {
                        Column {
                            Text("Danh mục chi tiêu", color = Color.Gray, fontSize = 14.sp)
                            Text(
                                text = if (category.isBlank()) "Chưa chọn" else category,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                categoryOptions.chunked(3).forEach { rowOptions ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowOptions.forEach { option ->
                                            FilterChip(
                                                selected = category == option,
                                                onClick = { category = option },
                                                label = { Text(option) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Column {
                        Text(if (transactionType == "EXPENSE") "Số tiền chi tiêu" else "Số tiền thu nhập", color = Color.Gray, fontSize = 14.sp)
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    Column {
                        Text("Ghi chú (tùy chọn)", color = Color.Gray, fontSize = 14.sp)
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = { Text("Thêm ghi chú...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Hủy", color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                                if ((transactionType == "EXPENSE" && category.isBlank()) || parsedAmount <= 0.0) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Vui lòng nhập đầy đủ thông tin")
                                    }
                                } else {
                                    viewModel.addExpense(date, category.trim(), parsedAmount, note.trim(), transactionType)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E49E2)),
                            shape = RoundedCornerShape(8.dp),
                            enabled = saveState !is Resource.Loading
                        ) {
                            Text("Lưu", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
