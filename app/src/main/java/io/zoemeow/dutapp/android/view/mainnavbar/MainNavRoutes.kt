package io.zoemeow.dutapp.android.view.mainnavbar

sealed class MainNavRoutes(val route: String) {
    object Main : MainNavRoutes("main")
    object News : MainNavRoutes("news")
    object Account : MainNavRoutes("account")
    object Settings : MainNavRoutes("settings")
}