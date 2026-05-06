package com.example.billbuddy.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Register : Screen("register")
    object Calendar : Screen("calendar")
    object AddExpense : Screen("add_expense")
    object Statistics : Screen("statistics")
    object Profile : Screen("profile")
}
