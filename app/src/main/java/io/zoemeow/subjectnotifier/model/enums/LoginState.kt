package io.zoemeow.subjectnotifier.model.enums

enum class LoginState(
    @Suppress("UNUSED_PARAMETER") value: Int
) {
    NotTriggered(-2),
    AccountLocked(-1),
    NotLoggedIn(0),
    NotLoggedInButRemembered(1),
    LoggingIn(2),
    LoggedIn(3)
}