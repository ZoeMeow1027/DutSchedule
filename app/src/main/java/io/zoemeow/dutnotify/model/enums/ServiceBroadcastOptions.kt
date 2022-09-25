package io.zoemeow.dutnotify.model.enums

@Suppress("SpellCheckingInspection")
class ServiceBroadcastOptions {
    companion object {
        // Send to broadcast a action
        const val ACTION = "action"

        // News Service
        const val ACTION_NEWS_INITIALIZATION = "action.news.initialization"
        const val ACTION_NEWS_FETCHGLOBAL = "action.news.fetchglobal"
        const val ACTION_NEWS_FETCHSUBJECT = "action.news.fetchsubject"
        const val ACTION_NEWS_FETCHALL = "action.news.fetchall"
        const val ACTION_NEWS_FETCHALLBACKGROUND = "action.news.fetchallbackground"

        // Account Service
        const val ACTION_ACCOUNT_GETSTATUS_HASSAVEDLOGIN = "action.account.getstatus.hassavedlogin"
        const val ACTION_ACCOUNT_LOGIN = "action.account.login"
        const val ACTION_ACCOUNT_LOGINSTARTUP = "action.account.loginstartup"
        const val ACTION_ACCOUNT_LOGOUT = "action.account.logout"
        const val ACTION_ACCOUNT_SUBJECTSCHEDULE = "action.account.subjectschedule"
        const val ACTION_ACCOUNT_SUBJECTFEE = "action.account.subjectfee"
        const val ACTION_ACCOUNT_ACCOUNTINFORMATION = "action.account.accountinformation"

        // Add arguments
        const val ARGUMENT_NEWS_PAGEOPTION = "argument.news.pageoption"
        const val ARGUMENT_NEWS_PAGEOPTION_NEXTPAGE = "argument.news.pageoption.nextpage"
        const val ARGUMENT_NEWS_PAGEOPTION_RESETTO1 = "argument.news.pageoption.resetto1"
        const val ARGUMENT_NEWS_PAGEOPTION_GETPAGE1 = "argument.news.pageoption.getpage1"
        const val ARGUMENT_NEWS_NOTIFYTOUSER = "argument.news.notifytouser"
        const val ARGUMENT_ACCOUNT_LOGIN_USERNAME = "argument.account.login.username"
        const val ARGUMENT_ACCOUNT_LOGIN_PASSWORD = "argument.account.login.password"
        const val ARGUMENT_ACCOUNT_LOGIN_REMEMBERED = "argument.account.login.remembered"
        const val ARGUMENT_ACCOUNT_LOGIN_PRELOAD = "argument.account.login.preload"
        const val ARGUMENT_ACCOUNT_LOGINSTARTUP_PRELOAD = "argument.account.loginstartup.preload"
        const val ARGUMENT_ACCOUNT_SUBJECTSCHEDULE_SCHOOLYEAR =
            "argument.account.subjectschedule.schoolyear"
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