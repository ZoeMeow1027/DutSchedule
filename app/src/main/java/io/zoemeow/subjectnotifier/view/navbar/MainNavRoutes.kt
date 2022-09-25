package io.zoemeow.subjectnotifier.view.navbar

sealed class MainNavRoutes(val route: String) {
    object News : MainNavRoutes("news")
    object Account : MainNavRoutes("account")
    object Settings : MainNavRoutes("settings")
}