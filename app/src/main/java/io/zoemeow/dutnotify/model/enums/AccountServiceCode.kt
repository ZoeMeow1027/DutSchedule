package io.zoemeow.dutnotify.model.enums

@Suppress("SpellCheckingInspection")
class AccountServiceCode {
    companion object {
        // Send to broadcast a action
        const val ACTION = "action"
        const val ACTION_GETSTATUS_HASSAVEDLOGIN = "action.getstatus.hassavedlogin"
        const val ACTION_LOGIN = "action.login"
        const val ACTION_LOGINSTARTUP = "action.loginstartup"
        const val ACTION_LOGOUT = "action.logout"
        const val ACTION_SUBJECTSCHEDULE = "action.subjectschedule"
        const val ACTION_SUBJECTFEE = "action.subjectfee"
        const val ACTION_ACCOUNTINFORMATION = "action.accountinformation"

        const val ARGUMENT_LOGIN_USERNAME = "argument.login.username"
        const val ARGUMENT_LOGIN_PASSWORD = "argument.login.password"
        const val ARGUMENT_LOGIN_REMEMBERED = "argument.login.remembered"
        const val ARGUMENT_LOGIN_PRELOAD = "argument.login.preload"
        const val ARGUMENT_LOGINSTARTUP_PRELOAD = "argument.loginstartup.preload"
        const val ARGUMENT_SUBJECTSCHEDULE_SCHOOLYEAR = "argument.subjectschedule.schoolyear"
        const val ARGUMENT_SUBJECTFEE_SCHOOLYEAR = "argument.subjectfee.schoolyear"

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