package io.zoemeow.dutnotify.viewmodel

import android.Manifest
import android.app.Application
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.zoemeow.dutapi.objects.accounts.SubjectScheduleItem
import io.zoemeow.dutapi.objects.news.NewsGlobalItem
import io.zoemeow.dutapi.objects.news.NewsSubjectItem
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.enums.BackgroundImageType
import io.zoemeow.dutnotify.model.enums.LoginState
import io.zoemeow.dutnotify.model.news.NewsCache
import io.zoemeow.dutnotify.model.news.NewsGroupByDate
import io.zoemeow.dutnotify.module.AccountModule
import io.zoemeow.dutnotify.module.FileModule
import io.zoemeow.dutnotify.receiver.AppBroadcastReceiver
import io.zoemeow.dutnotify.util.getCurrentDayOfWeek
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val subjectScheduleItem: MutableState<SubjectScheduleItem?> = mutableStateOf(null)
    val subjectScheduleEnabled: MutableState<Boolean> = mutableStateOf(false)

    // Drawable and painter for background image
    val mainActivityBackgroundDrawable: MutableState<Drawable?> = mutableStateOf(null)

    /**
     * Get current drawable for background image. Image loaded will save to backgroundPainter.
     */
    fun reloadAppBackground(
        context: Context,
        type: BackgroundImageType,
    ) {
        try {
            // This will get background wallpaper from launcher.
            if (type == BackgroundImageType.FromWallpaper) {
                if (
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val wallpaperManager = WallpaperManager.getInstance(context)
                    mainActivityBackgroundDrawable.value = wallpaperManager.drawable
                } else throw Exception("Missing permission: READ_EXTERNAL_STORAGE")
            }
            // Otherwise set to null
            else mainActivityBackgroundDrawable.value = null
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * Main Activity: Check if current theme is dark mode.
     */
    val mainActivityIsDarkTheme: MutableState<Boolean> = mutableStateOf(false)

    /**
     * Account: Current page of account nav. route.
     *
     * 0: Not logged in,
     * 1: Dashboard,
     * 2: Subject schedule,
     * 3: Subject fee
     * 4: Account Information
     */
    val accountCurrentPage: MutableState<Int> = mutableStateOf(0)

    val accountCurrentDayOfWeek: MutableState<Int> = mutableStateOf(getCurrentDayOfWeek())

    // App settings
    val appSettings: MutableState<AppSettings> = mutableStateOf(AppSettings())

    // File Module
    private var file: FileModule

    // News UI area
    val newsDataStore: NewsDataStore = NewsDataStore(this)

    // Account area
    val accountDataStore: AccountDataStore = AccountDataStore(
        mainViewModel = this,
        accountModule = AccountModule(),
        appSettings = appSettings.value,
    )

    fun requestSaveChanges() {
        file.saveAppSettings(
            appSettings = appSettings.value
        )

        accountDataStore.saveSettings { accountSession ->
            file.saveAccountSettings(
                accountSession = accountSession
            )
        }
    }

    fun showSnackBarMessage(
        title: String,
        forceCloseOld: Boolean = false
    ) {
        val intent = Intent(AppBroadcastReceiver.SNACKBARMESSAGE)
        intent.putExtra(
            AppBroadcastReceiver.SNACKBARMESSAGE_TEXT,
            title
        )
        intent.putExtra(
            AppBroadcastReceiver.SNACKBARMESSAGE_CLOSEOLDMSG,
            forceCloseOld
        )
        LocalBroadcastManager.getInstance(getApplication<Application>().applicationContext)
            .sendBroadcast(intent)
    }

    fun requestSaveCache() {
        file.saveCacheNewsGlobal(
            NewsCache(
                newsListByDate = arrayListOf<NewsGroupByDate<NewsGlobalItem>>().apply {
                    addAll(newsDataStore.listNewsGlobalByDate)
                },
                pageCurrent = newsDataStore.newsGlobalPageCurrent.value
            )
        )
        file.saveCacheNewsSubject(
            NewsCache(
                newsListByDate = arrayListOf<NewsGroupByDate<NewsSubjectItem>>().apply {
                    addAll(newsDataStore.listNewsSubjectByDate)
                },
                pageCurrent = newsDataStore.newsSubjectPageCurrent.value
            )
        )
    }

    private fun getAppBroadcastReceiver(): AppBroadcastReceiver {
        object : AppBroadcastReceiver() {
            override fun onSnackBarMessage(title: String?, forceCloseOld: Boolean) {}
            override fun onNewsScrollToTopRequested() { }

            override fun onNewsReloadRequested() {
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        file.getCacheNewsGlobal().apply {
                            newsDataStore.newsGlobalPageCurrent.value = pageCurrent
                            newsDataStore.listNewsGlobalByDate.swapList(newsListByDate)
                        }
                        file.getCacheNewsSubject().apply {
                            newsDataStore.newsSubjectPageCurrent.value = pageCurrent
                            newsDataStore.listNewsSubjectByDate.swapList(newsListByDate)
                        }
                    }
                }
            }

            override fun onAccountReloadRequested(newsType: String) {
                when (newsType) {
                    ACCOUNT_SUBJECTSCHEDULE_RELOADREQUESTED -> {
                        if (accountDataStore.loginState.value == LoginState.LoggedIn) {
                            accountDataStore.fetchSubjectSchedule()
                        }
                    }
                    ACCOUNT_SUBJECTFEE_RELOADREQUESTED -> {
                        if (accountDataStore.loginState.value == LoginState.LoggedIn) {
                            accountDataStore.fetchSubjectFee()
                        }
                    }
                    ACCOUNT_ACCINFORMATION_RELOADREQUESTED -> {
                        if (accountDataStore.loginState.value == LoginState.LoggedIn) {
                            accountDataStore.fetchAccountInformation()
                        }
                    }
                }
            }

            override fun onSettingsReloadRequested() {
                appSettings.value = file.getAppSettings()
                accountDataStore.loadSettings(file.getAccountSettings())
            }
        }.apply {
            return this
        }
    }

    override fun onCleared() {
        LocalBroadcastManager.getInstance(getApplication<Application>().applicationContext)
            .unregisterReceiver(getAppBroadcastReceiver())
        Log.d("MainViewModel", "Destroyed")
        super.onCleared()
    }

    init {
        file = FileModule(application.applicationContext)
        appSettings.value = file.getAppSettings()
        accountDataStore.loadSettings(file.getAccountSettings())

        LocalBroadcastManager.getInstance(application.applicationContext).registerReceiver(
            getAppBroadcastReceiver(),
            IntentFilter(AppBroadcastReceiver.NEWS_RELOADREQUESTED_SERVICE_ACTIVITY)
        )

        // Reload news from cache
        val intent = Intent(AppBroadcastReceiver.NEWS_RELOADREQUESTED_SERVICE_ACTIVITY)
        LocalBroadcastManager.getInstance(application.applicationContext).sendBroadcast(intent)

        Log.d("MainViewModel", "Initialized")
    }
}

fun <T> SnapshotStateList<T>.swapList(element: Collection<T>) {
    clear()
    addAll(element)
}