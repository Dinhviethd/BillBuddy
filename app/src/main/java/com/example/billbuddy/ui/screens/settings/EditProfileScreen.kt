package com.example.billbuddy.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.billbuddy.data.model.User
import com.example.billbuddy.ui.viewmodel.AuthViewModel
import com.example.billbuddy.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val userDataResult = viewModel.userData.value
    val currentUser = viewModel.currentUser
    val existingUser = (userDataResult as? Resource.Success)?.data

    var displayName by remember { mutableStateOf(existingUser?.displayName ?: currentUser?.displayName ?: "") }
    var email by remember { mutableStateOf(existingUser?.email ?: currentUser?.email ?: "") }
    var phone by remember { mutableStateOf(existingUser?.phoneNumber ?: "") }

    val updateState = viewModel.updateState.value

    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) {
            onNavigateBack()
        }
    }

    val initials = if (displayName.isNotBlank()) {
        displayName.split(" ").filter { it.isNotEmpty() }.take(2).map { it[0] }.joinToString("").uppercase()
    } else "BB"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chỉnh sửa cá nhân", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            currentUser?.let {
                                val updatedUser = User(
                                    documentId = it.uid,
                                    email = email,
                                    displayName = displayName,
                                    phoneNumber = phone,
                                    createdAt = existingUser?.createdAt,
                                    updatedAt = com.google.firebase.Timestamp.now()
                                )
                                viewModel.updateProfile(updatedUser)
                            }
                        },
                        enabled = updateState !is Resource.Loading
                    ) {
                        Text("Lưu", fontWeight = FontWeight.Bold, color = Color(0xFFD47500))
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar section
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8B931)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Form fields
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Họ và tên") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Email, null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Số điện thoại") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Phone, null) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    currentUser?.let {
                        val updatedUser = User(
                            documentId = it.uid,
                            email = email,
                            displayName = displayName,
                            phoneNumber = phone,
                            createdAt = existingUser?.createdAt,
                            updatedAt = com.google.firebase.Timestamp.now()
                        )
                        viewModel.updateProfile(updatedUser)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = updateState !is Resource.Loading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD47500))
            ) {
                if (updateState is Resource.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Cập nhật thông tin", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
