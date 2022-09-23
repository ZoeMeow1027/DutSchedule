package io.zoemeow.dutnotify.model.enums

@Suppress("SpellCheckingInspection")
class ServiceCode {
    companion object {
        // Send to broadcast a action
        const val ACTION = "action"
        // Account Service
        const val ACTION_ACCOUNT_GETSTATUS_HASSAVEDLOGIN = "action.account.getstatus.hassavedlogin"
        const val ACTION_ACCOUNT_LOGIN = "action.account.login"
        const val ACTION_ACCOUNT_LOGINSTARTUP = "action.account.loginstartup"
        const val ACTION_ACCOUNT_LOGOUT = "action.account.logout"
        const val ACTION_ACCOUNT_SUBJECTSCHEDULE = "action.account.subjectschedule"
        const val ACTION_ACCOUNT_SUBJECTFEE = "action.account.subjectfee"
        const val ACTION_ACCOUNT_ACCOUNTINFORMATION = "action.account.accountinformation"

        // Add arguments
        const val ARGUMENT_ACCOUNT_LOGIN_USERNAME = "argument.account.login.username"
        const val ARGUMENT_ACCOUNT_LOGIN_PASSWORD = "argument.account.login.password"
        const val ARGUMENT_ACCOUNT_LOGIN_REMEMBERED = "argument.account.login.remembered"
        const val ARGUMENT_ACCOUNT_LOGIN_PRELOAD = "argument.account.login.preload"
        const val ARGUMENT_ACCOUNT_LOGINSTARTUP_PRELOAD = "argument.account.loginstartup.preload"
        const val ARGUMENT_ACCOUNT_SUBJECTSCHEDULE_SCHOOLYEAR = "argument.account.subjectschedule.schoolyear"
        const val ARGUMENT_ACCOUNT_SUBJECTFEE_SCHOOLYEAR = "argument.account.subjectfee.schoolyear"

        // Send to broadcast status.
        const val STATUS = "status"
        const val STATUS_PROCESSING = "status.processing"
        const val STATUS_ALREADYPROCESSING = "status.alreadyprocessing"
        const val STATUS_SUCCESSFUL = "status.successful"
        const val STATUS_FAILED = "status.failed"

        // Send to broadcast data.
        const val DATA = "data"

        // Send to broadcast a error message.
        const val ERRORMESSAGE = "errormessage"

        // Define service what component is required (REQUIRED)
        const val SOURCE_COMPONENT = "source.component"
    }
}