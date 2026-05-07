package com.example.billbuddy.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.billbuddy.ui.screens.auth.LoginScreen
import com.example.billbuddy.ui.screens.auth.RegisterScreen
import com.example.billbuddy.ui.screens.home.AddExpenseScreen
import com.example.billbuddy.ui.screens.home.HomeScreen
import com.example.billbuddy.ui.viewmodel.AuthViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    
    val startDestination = if (authViewModel.currentUser != null) Screen.Home.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = authViewModel,
                onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }
        
        composable(Screen.AddExpense.route) {
            AddExpenseScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveExpense = { date, category, amount, note ->
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Calendar.route) {  }
        composable(Screen.Statistics.route) {  }
        composable(Screen.Profile.route) {  }
    }
}
