package com.example.billbuddy.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.billbuddy.ui.screens.auth.LoginScreen
import com.example.billbuddy.ui.screens.auth.RegisterScreen
import com.example.billbuddy.ui.screens.expense.AddExpenseScreen
import com.example.billbuddy.ui.screens.home.HomeScreen
import com.example.billbuddy.ui.screens.statistics.StatisticsScreen
import com.example.billbuddy.ui.screens.settings.ProfileScreen
import com.example.billbuddy.ui.screens.settings.EditProfileScreen
import com.example.billbuddy.ui.screens.settings.ChangePasswordScreen
import com.example.billbuddy.ui.screens.calendar.CalendarScreen
import com.example.billbuddy.ui.screens.debt.DebtListScreen
import com.example.billbuddy.ui.screens.debt.AddDebtScreen
import com.example.billbuddy.ui.screens.debt.DebtDetailScreen
import com.example.billbuddy.ui.screens.group.GroupListScreen
import com.example.billbuddy.ui.viewmodel.AuthViewModel
import com.example.billbuddy.ui.viewmodel.DebtViewModel
import com.example.billbuddy.ui.viewmodel.ExpenseViewModel
import com.example.billbuddy.ui.viewmodel.CalendarViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val debtViewModel: DebtViewModel = hiltViewModel()
    val expenseViewModel: ExpenseViewModel = hiltViewModel()
    val calendarViewModel: CalendarViewModel = hiltViewModel()
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
                viewModel = expenseViewModel,
                onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }
        
        composable(Screen.AddExpense.route) {
            AddExpenseScreen(
                viewModel = expenseViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(
                viewModel = calendarViewModel,
                onNavigateHome = {
                    navController.navigate(Screen.Home.route)
                },
                onNavigateAddExpense = {
                    navController.navigate(Screen.AddExpense.route)
                },
                onNavigateStatistics = {
                    navController.navigate(Screen.Statistics.route)
                },
                onNavigateProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToCalendar = {
                    navController.navigate(Screen.Calendar.route)
                },
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToChangePassword = {
                    navController.navigate(Screen.ChangePassword.route)
                },
                onNavigateToGroups = {
                    navController.navigate(Screen.GroupList.route)
                },
                onNavigateToDebts = {
                    navController.navigate(Screen.DebtList.route)
                },
                onSignOut = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.GroupList.route) {
            GroupListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddGroup = {
                    // navController.navigate(Screen.AddGroup.route)
                }
            )
        }
        composable(Screen.DebtList.route) {
            DebtListScreen(
                viewModel = debtViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddDebt = {
                    navController.navigate(Screen.AddDebt.route)
                },
                onNavigateToDebtDetail = { debtId ->
                    navController.navigate(Screen.DebtDetail.createRoute(debtId))
                }
            )
        }
        composable(Screen.AddDebt.route) {
            AddDebtScreen(
                viewModel = debtViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.DebtDetail.route) { backStackEntry ->
            val debtId = backStackEntry.arguments?.getString("debtId") ?: ""
            DebtDetailScreen(
                debtId = debtId,
                viewModel = debtViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
