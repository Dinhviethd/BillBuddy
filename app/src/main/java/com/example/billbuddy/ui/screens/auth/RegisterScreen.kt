package com.example.billbuddy.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.util.Patterns
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.billbuddy.ui.viewmodel.AuthViewModel
import com.example.billbuddy.utils.Resource

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val authState by viewModel.authState

    LaunchedEffect(authState) {
        if (authState is Resource.Success) {
            onRegisterSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Join BillBuddy today",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(48.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = fieldColors
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = fieldColors
                    )

                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Confirm Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = if (confirmPass.isNotEmpty() && confirmPass != password) {
                            OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedBorderColor = MaterialTheme.colorScheme.error,
                                unfocusedBorderColor = MaterialTheme.colorScheme.error,
                                focusedLabelColor = MaterialTheme.colorScheme.error,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        } else fieldColors
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        email.isBlank() -> errorText = "Email không được để trống"
                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> errorText = "Email không hợp lệ"
                        password != confirmPass -> errorText = "Mật khẩu không khớp"
                        password.length < 6 -> errorText = "Mật khẩu phải có ít nhất 6 ký tự"
                        else -> {
                            errorText = null
                            viewModel.register(email, password)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = authState !is Resource.Loading,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
            ) {
                if (authState is Resource.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Register", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "Already have an account? Login",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val finalError = errorText ?: if (authState is Resource.Error) (authState as Resource.Error).message else null
            if (finalError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = finalError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}