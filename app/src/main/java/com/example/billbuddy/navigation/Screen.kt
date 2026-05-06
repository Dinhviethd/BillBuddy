package com.example.billbuddy.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Register: Screen("register")
}
