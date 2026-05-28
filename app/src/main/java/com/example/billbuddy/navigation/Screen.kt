package com.example.billbuddy.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Register : Screen("register")
    object Calendar : Screen("calendar")
    object AddExpense : Screen("add_expense")
    object Statistics : Screen("statistics")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object ChangePassword : Screen("change_password")
    object DebtList : Screen("debt_list")
    object AddDebt : Screen("add_debt")
    object DebtDetail : Screen("debt_detail/{debtId}") {
        fun createRoute(debtId: String) = "debt_detail/$debtId"
    }
    object GroupList : Screen("group_list")
    object AddGroup : Screen("add_group")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }
    object SplitExpense : Screen("split_expense/{expenseId}") {
        fun createRoute(expenseId: String) = "split_expense/$expenseId"
    }
}
