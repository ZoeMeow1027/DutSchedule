package io.zoemeow.dutnotify

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.appsettings.BackgroundImage
import io.zoemeow.dutnotify.model.enums.ServiceCode
import io.zoemeow.dutnotify.model.enums.BackgroundImageType
import io.zoemeow.dutnotify.receiver.AppBroadcastReceiver
import io.zoemeow.dutnotify.service.AccountService
import io.zoemeow.dutnotify.service.NewsService2
import io.zoemeow.dutnotify.ui.theme.MainActivityTheme
import io.zoemeow.dutnotify.utils.AppUtils
import io.zoemeow.dutnotify.utils.NotificationsUtils
import io.zoemeow.dutnotify.view.account.Account
import io.zoemeow.dutnotify.view.navbar.MainBottomNavigationBar
import io.zoemeow.dutnotify.view.navbar.MainNavRoutes
import io.zoemeow.dutnotify.view.news.News
import io.zoemeow.dutnotify.view.settings.Settings
import io.zoemeow.dutnotify.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private var isInitialized = false
    }

    internal lateinit var mainViewModel: MainViewModel
    private lateinit var snackBarState: SnackbarHostState
    private lateinit var lazyListStateGlobal: LazyListState
    private lateinit var lazyListStateSubject: LazyListState
    private lateinit var scope: CoroutineScope

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted
        permitAllPolicy()

        setContent {
            // Initialize Main View Model
            mainViewModel = viewModel()
            // Initialize snack bar host state
            snackBarState = SnackbarHostState()
            // Initialize scope
            scope = rememberCoroutineScope()
            // Initialize lazy list state
            lazyListStateGlobal = rememberLazyListState()
            lazyListStateSubject = rememberLazyListState()

            // Register notifications channel for news service
            // Works well with Android 13 without any issue.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                NotificationsUtils.initializeNotificationChannel(this)

            if (!isInitialized) {
                registerBroadcastReceiver(context = this@MainActivity)
                checkSettingsPermissionOnStartup(mainViewModel = mainViewModel)

                // Initialize refresh news services
                // Just to reload news. If schedule has been enabled,
                // this will be scheduled to new UnixTimestamp.

                // NewsService.startService(context = this@MainActivity)
                controlNewsServiceInBackground(false)

                NewsService2.startService(
                    context = this@MainActivity,
                    intent = Intent(this@MainActivity, NewsService2::class.java).apply {
                        putExtra(ServiceCode.ACTION, ServiceCode.ACTION_NEWS_INITIALIZATION)
                        putExtra(ServiceCode.ARGUMENT_NEWS_NOTIFYTOUSER, false)
                    }
                )
                NewsService2.startService(
                    context = this@MainActivity,
                    intent = Intent(this@MainActivity, NewsService2::class.java).apply {
                        putExtra(ServiceCode.ACTION, ServiceCode.ACTION_NEWS_FETCHALL)
                        putExtra(ServiceCode.ARGUMENT_NEWS_NOTIFYTOUSER, false)
                    }
                )

                Intent(this@MainActivity, AccountService::class.java).apply {
                    putExtra(ServiceCode.ACTION, ServiceCode.ACTION_ACCOUNT_GETSTATUS_HASSAVEDLOGIN)
                    putExtra(ServiceCode.SOURCE_COMPONENT, this@MainActivity::class.java.name)
                }.also {
                    this@MainActivity.startService(it)
                }

                Intent(this@MainActivity, AccountService::class.java).apply {
                    putExtra(ServiceCode.ACTION, ServiceCode.ACTION_ACCOUNT_LOGINSTARTUP)
                    putExtra(ServiceCode.ARGUMENT_ACCOUNT_LOGINSTARTUP_PRELOAD, true)
                    putExtra(ServiceCode.SOURCE_COMPONENT, this@MainActivity::class.java.name)
                }.also {
                    this@MainActivity.startService(it)
                }

                isInitialized = true
            }

            MainActivityTheme(
                appSettings = mainViewModel.appSettings.value,
                content = @Composable {
                    MainScreen()
                },
                backgroundDrawable = mainViewModel.mainActivityBackgroundDrawable.value,
                appModeChanged = {
                    // Trigger for dark mode detection.
                    mainViewModel.mainActivityIsDarkTheme.value = it
                },
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        // Initialize for NavController for main activity
        val navController = rememberNavController()
        // Nav Route
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        // A scaffold container using the 'background' color from the theme
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackBarState) },
            containerColor = if (mainViewModel.appSettings.value.backgroundImage.option == BackgroundImageType.Unset)
                MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.background.copy(
                alpha = 0.8f
            ),
            contentColor = if (mainViewModel.mainActivityIsDarkTheme.value) Color.White else Color.Black,
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                MainBottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    onClick = {
                        when (it.route) {
                            MainNavRoutes.News.route -> {
                                val intent = Intent(AppBroadcastReceiver.NEWS_SCROLLALLTOTOP)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                            }
                            else -> {}
                        }
                    }
                )
            },
            content = { contentPadding ->
                NavHost(
                    navController = navController,
                    startDestination = MainNavRoutes.News.route,
                    modifier = Modifier.padding(contentPadding)
                ) {
                    composable(MainNavRoutes.News.route) {
                        News(
                            mainViewModel = mainViewModel,
                            scope = scope,
                            lazyListTabGlobal = lazyListStateGlobal,
                            lazyListTabSubject = lazyListStateSubject,
                        )
                    }

                    composable(MainNavRoutes.Account.route) {
                        Account(
                            mainViewModel = mainViewModel,
                        )
                    }

                    composable(MainNavRoutes.Settings.route) {
                        Settings(
                            mainViewModel = mainViewModel,
                        )
                    }
                }
            },
        )
    }

    override fun onResume() {
        controlNewsServiceInBackground(false)
        super.onResume()
    }

    override fun onPause() {
        controlNewsServiceInBackground(true)
        super.onPause()
    }

    override fun onDestroy() {
        // Unregister to completely destroyed.
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(getAppBroadcastReceiver())
        controlNewsServiceInBackground(true)
        super.onDestroy()
    }

    private fun controlNewsServiceInBackground(enabled: Boolean) {
        try {
            if (enabled) {
                if (mainViewModel.appSettings.value.refreshNewsEnabled)
                    NewsService2.startService(
                        context = this@MainActivity,
                        intent = Intent(this@MainActivity, NewsService2::class.java).apply {
                            putExtra(ServiceCode.ACTION, ServiceCode.ACTION_NEWS_FETCHALLBACKGROUND)
                            putExtra(ServiceCode.ARGUMENT_NEWS_NOTIFYTOUSER, true)
                        }
                    )
            }
            else {
                NewsService2.cancelSchedule(this)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun getAppBroadcastReceiver(): AppBroadcastReceiver {
        object : AppBroadcastReceiver() {
            override fun onNewsReloadRequested() {}
            override fun onAccountReloadRequested(newsType: String) {}
            override fun onSettingsReloadRequested() { }

            override fun onNewsScrollToTopRequested() {
                if (!lazyListStateGlobal.isScrollInProgress)
                    scope.launch { lazyListStateGlobal.animateScrollToItem(index = 0) }
                if (!lazyListStateSubject.isScrollInProgress)
                    scope.launch { lazyListStateSubject.animateScrollToItem(index = 0) }
            }

            override fun onSnackBarMessage(title: String?, forceCloseOld: Boolean) {
                if (forceCloseOld)
                    snackBarState.currentSnackbarData?.dismiss()

                if (title != null) {
                    scope.launch {
                        snackBarState.showSnackbar(title)
                    }
                }
            }

            override fun onPermissionRequested(
                permission: String?,
                granted: Boolean,
                notifyToUser: Boolean
            ) {
                onPermissionResult(permission, granted, notifyToUser)
            }
        }.apply {
            return this
        }
    }

    /**
     * This will bypass network on main thread exception.
     * Use this at your own risk.
     * Target: OkHttp3
     *
     * Source: https://blog.cpming.top/p/android-os-networkonmainthreadexception
     */
    private fun permitAllPolicy() {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    private fun registerBroadcastReceiver(context: Context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(
            getAppBroadcastReceiver(),
            IntentFilter().apply {
                addAction(AppBroadcastReceiver.SNACKBARMESSAGE)
                addAction(AppBroadcastReceiver.NEWS_SCROLLALLTOTOP)
                addAction(AppBroadcastReceiver.RUNTIME_PERMISSION_REQUESTED)
            }
        )
    }
}

fun MainActivity.onPermissionResult(
    permission: String?,
    granted: Boolean,
    notifyToUser: Boolean = false
) {
    when (permission) {
        Manifest.permission.READ_EXTERNAL_STORAGE -> {
            if (granted) {
                mainViewModel.mainActivityBackgroundDrawable.value =
                    AppUtils.getCurrentWallpaperBackground(
                        context = this,
                        type = mainViewModel.appSettings.value.backgroundImage.option
                    )
            } else {
                mainViewModel.appSettings.value = mainViewModel.appSettings.value.modify(
                    optionToModify = AppSettings.APPEARANCE_BACKGROUNDIMAGE,
                    value = BackgroundImage(
                        option = BackgroundImageType.Unset,
                        path = null
                    )
                )
                mainViewModel.requestSaveChanges()
                mainViewModel.showSnackBarMessage(
                    "Missing permission for background image. " +
                            "This setting will be turned off to avoid another issues."
                )
            }
        }
        Manifest.permission.POST_NOTIFICATIONS -> {
            if (!granted) {
                mainViewModel.appSettings.value =
                    mainViewModel.appSettings.value.modify(
                        optionToModify = AppSettings.NEWSINBACKGROUND_ENABLED,
                        value = false
                    )
                mainViewModel.requestSaveChanges()
                mainViewModel.showSnackBarMessage(
                    "Missing permission for news notification in background. " +
                            "This setting will be turned off to avoid another issues."
                )
            } else {
                try {
                    val msg: String
                    if (mainViewModel.appSettings.value.refreshNewsEnabled) {
                        msg = getString(R.string.snackbar_newsinbackground_successfulenabled)
                    } else {
                        msg = getString(R.string.snackbar_newsinbackground_successfuldisabled)
                    }
                    if (notifyToUser)
                        mainViewModel.showSnackBarMessage(msg)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    val valueBefore = mainViewModel.appSettings.value.refreshNewsEnabled
                    mainViewModel.appSettings.value =
                        mainViewModel.appSettings.value.modify(
                            optionToModify = AppSettings.NEWSINBACKGROUND_ENABLED,
                            value = false
                        )
                    mainViewModel.requestSaveChanges()
                    mainViewModel.showSnackBarMessage(
                        if (valueBefore) getString(R.string.snackbar_newsinbackground_failedenabled)
                        else getString(R.string.snackbar_newsinbackground_faileddisabled) + " " +
                        getString(R.string.snackbar_newsinbackground_failedextended),
                    )
                }
            }
        }
        else -> { }
    }
}

fun MainActivity.checkSettingsPermissionOnStartup(
    mainViewModel: MainViewModel
) {
    val permissionList = arrayListOf<String>()

    // Notifications permission (required by Android 13 and up)
    if (mainViewModel.appSettings.value.refreshNewsEnabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionRequestActivity.checkPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) permissionList.add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Read external storage - Background Image
    if (mainViewModel.appSettings.value.backgroundImage.option != BackgroundImageType.Unset) {
        if (!PermissionRequestActivity.checkPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        else onPermissionResult(Manifest.permission.READ_EXTERNAL_STORAGE, true)
    }

    if (permissionList.isNotEmpty()) {
        Intent(this, PermissionRequestActivity::class.java)
            .apply {
                putExtra("permissions.list", permissionList.toTypedArray())
            }
            .also {
                this.startActivity(it)
            }
    }
}