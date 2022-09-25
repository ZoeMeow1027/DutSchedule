package io.zoemeow.subjectnotifier.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
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
import io.zoemeow.subjectnotifier.R
import io.zoemeow.subjectnotifier.model.appsettings.AppSettings
import io.zoemeow.subjectnotifier.model.appsettings.BackgroundImage
import io.zoemeow.subjectnotifier.model.enums.BackgroundImageType
import io.zoemeow.subjectnotifier.model.enums.ServiceBroadcastOptions
import io.zoemeow.subjectnotifier.receiver.AppBroadcastReceiver
import io.zoemeow.subjectnotifier.service.AccountService
import io.zoemeow.subjectnotifier.service.NewsService
import io.zoemeow.subjectnotifier.utils.NotificationsUtils
import io.zoemeow.subjectnotifier.view.account.Account
import io.zoemeow.subjectnotifier.view.navbar.MainBottomNavigationBar
import io.zoemeow.subjectnotifier.view.navbar.MainNavRoutes
import io.zoemeow.subjectnotifier.view.news.News
import io.zoemeow.subjectnotifier.view.settings.Settings
import io.zoemeow.subjectnotifier.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    internal lateinit var mainViewModel: MainViewModel
    private lateinit var snackBarState: SnackbarHostState
    private lateinit var lazyListStateGlobal: LazyListState
    private lateinit var lazyListStateSubject: LazyListState
    private lateinit var scope: CoroutineScope

    @Composable
    override fun OnPreloadOnce() {
        registerBroadcastReceiver(context = applicationContext)
        checkSettingsPermissionOnStartup(mainViewModel = mainViewModel)

        // NewsService.startService(context = this@MainActivity)
        controlNewsServiceInBackground(false)

        // Initialize refresh news services
        // Just to reload news. If schedule has been enabled,
        // this will be scheduled to new UnixTimestamp.
        NewsService.startService(
            context = applicationContext,
            intent = Intent(applicationContext, NewsService::class.java).apply {
                putExtra(ServiceBroadcastOptions.ACTION, ServiceBroadcastOptions.ACTION_NEWS_INITIALIZATION)
                putExtra(ServiceBroadcastOptions.ARGUMENT_NEWS_NOTIFYTOUSER, false)
            }
        )

        Intent(applicationContext, AccountService::class.java).apply {
            putExtra(ServiceBroadcastOptions.ACTION, ServiceBroadcastOptions.ACTION_ACCOUNT_LOGINSTARTUP)
            putExtra(ServiceBroadcastOptions.ARGUMENT_ACCOUNT_LOGINSTARTUP_PRELOAD, true)
            putExtra(ServiceBroadcastOptions.SOURCE_COMPONENT, MainActivity::class.java.name)
        }.also {
            applicationContext.startService(it)
        }

        this@MainActivity.setAppSettings(mainViewModel.appSettings.value)
    }

    @Composable
    override fun OnPreload() {
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
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun OnMainView() {
        // Initialize for NavController for main activity
        val navController = rememberNavController()
        // Nav Route
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        mainViewModel.isDarkTheme.value = isAppInDarkTheme.value

        // A scaffold container using the 'background' color from the theme
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackBarState) },
            containerColor = if (mainViewModel.appSettings.value.backgroundImage.option == BackgroundImageType.Unset)
                MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.background.copy(
                alpha = 0.8f
            ),
            contentColor = if (mainViewModel.isDarkTheme.value) Color.White else Color.Black,
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
                    NewsService.startService(
                        context = this@MainActivity,
                        intent = Intent(this@MainActivity, NewsService::class.java).apply {
                            putExtra(ServiceBroadcastOptions.ACTION, ServiceBroadcastOptions.ACTION_NEWS_FETCHALLBACKGROUND)
                            putExtra(ServiceBroadcastOptions.ARGUMENT_NEWS_NOTIFYTOUSER, true)
                        }
                    )
            } else {
                NewsService.cancelSchedule(this)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun getAppBroadcastReceiver(): AppBroadcastReceiver {
        object : AppBroadcastReceiver() {
            override fun onNewsReloadRequested() {}
            override fun onAccountReloadRequested(newsType: String) {}
            override fun onSettingsReloadRequested() {}

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

    fun onPermissionResult(
        permission: String?,
        granted: Boolean,
        notifyToUser: Boolean = false
    ) {
        when (permission) {
            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                if (granted) {
                    // Reload settings
                    mainViewModel.appSettings.value = mainViewModel.appSettings.value.clone()
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
            else -> {}
        }
    }

    private fun checkSettingsPermissionOnStartup(
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
}