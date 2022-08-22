package io.zoemeow.dutapp.android.view.navbar

sealed class MainNavRoutes(val route: String) {
    object News : MainNavRoutes("news")
    object Account : MainNavRoutes("account")
    object Settings : MainNavRoutes("settings")
}