package io.zoemeow.dutnotify.model.enums

enum class LoginState(value: Int) {
    NotTriggered(-2),
    AccountLocked(-1),
    NotLoggedIn(0),
    NotLoggedInButRemembered(1),
    LoggingIn(2),
    LoggedIn(3)
}