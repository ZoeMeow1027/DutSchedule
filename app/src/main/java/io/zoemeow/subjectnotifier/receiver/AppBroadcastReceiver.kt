package io.zoemeow.subjectnotifier.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

abstract class AppBroadcastReceiver : BroadcastReceiver() {
    @Suppress("SpellCheckingInspection")
    companion object {
        const val NEWS_SCROLLALLTOTOP = "news.scrollalltotop"
        const val NEWS_RELOADREQUESTED_SERVICE_ACTIVITY = "news.reloadrequested.service.activity"

        const val SNACKBARMESSAGE = "snackbarmessage"
        const val SNACKBARMESSAGE_TEXT = "snackbarmessage.text"
        const val SNACKBARMESSAGE_CLOSEOLDMSG = "snackbarmessage.closeoldmsg"

        const val ACCOUNT_SUBJECTSCHEDULE_RELOADREQUESTED =
            "account.subjectschedule.reloadrequested"
        const val ACCOUNT_SUBJECTFEE_RELOADREQUESTED =
            "account.subjectfee.reloadrequested"
        const val ACCOUNT_ACCINFORMATION_RELOADREQUESTED =
            "account.accountinformation.reloadrequested"

        const val SETTINGS_RELOADREQUESTED =
            "settings.reloadrequested"

        const val RUNTIME_PERMISSION_REQUESTED = "runtime.permission.requested"
        const val RUNTIME_PERMISSION_NAME = "runtime.permission.name"
        const val RUNTIME_PERMISSION_RESULT = "runtime.permission.result"
        const val RUNTIME_PERMISSION_NOTIFY_RESULT = "runtime.permission.result.notify"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        when (intent.action) {
            NEWS_RELOADREQUESTED_SERVICE_ACTIVITY -> {
                onNewsReloadRequested()
            }
            NEWS_SCROLLALLTOTOP -> {
                onNewsScrollToTopRequested()
            }
            SNACKBARMESSAGE -> {
                onSnackBarMessage(
                    title = intent.getStringExtra(SNACKBARMESSAGE_TEXT),
                    forceCloseOld = intent.getBooleanExtra(SNACKBARMESSAGE_CLOSEOLDMSG, false),

                    )
            }
            ACCOUNT_SUBJECTSCHEDULE_RELOADREQUESTED -> {
                onAccountReloadRequested(ACCOUNT_SUBJECTSCHEDULE_RELOADREQUESTED)
            }
            ACCOUNT_SUBJECTFEE_RELOADREQUESTED -> {
                onAccountReloadRequested(ACCOUNT_SUBJECTFEE_RELOADREQUESTED)
            }
            ACCOUNT_ACCINFORMATION_RELOADREQUESTED -> {
                onAccountReloadRequested(ACCOUNT_ACCINFORMATION_RELOADREQUESTED)
            }
            SETTINGS_RELOADREQUESTED -> {
                onSettingsReloadRequested()
            }
            RUNTIME_PERMISSION_REQUESTED -> {
                onPermissionRequested(
                    permission = intent.getStringExtra(RUNTIME_PERMISSION_NAME),
                    granted = intent.getBooleanExtra(RUNTIME_PERMISSION_RESULT, false),
                    notifyToUser = intent.getBooleanExtra(RUNTIME_PERMISSION_NOTIFY_RESULT, false)
                )
            }
        }
    }

    abstract fun onNewsReloadRequested()
    abstract fun onNewsScrollToTopRequested()

    abstract fun onSnackBarMessage(
        title: String?,
        forceCloseOld: Boolean = false,
    )

    abstract fun onAccountReloadRequested(newsType: String)

    abstract fun onSettingsReloadRequested()

    abstract fun onPermissionRequested(
        permission: String?,
        granted: Boolean,
        notifyToUser: Boolean = false
    )
}