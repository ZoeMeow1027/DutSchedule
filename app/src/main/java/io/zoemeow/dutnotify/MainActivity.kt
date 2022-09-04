package io.zoemeow.dutnotify

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import io.zoemeow.dutnotify.model.appsettings.BackgroundImage
import io.zoemeow.dutnotify.model.enums.AppSettingsCode
import io.zoemeow.dutnotify.model.enums.BackgroundImageType
import io.zoemeow.dutnotify.model.enums.LoginState
import io.zoemeow.dutnotify.receiver.AppBroadcastReceiver
import io.zoemeow.dutnotify.service.NewsRefreshService
import io.zoemeow.dutnotify.ui.theme.MainActivityTheme
import io.zoemeow.dutnotify.util.NotificationsUtils
import io.zoemeow.dutnotify.view.account.Account
import io.zoemeow.dutnotify.view.navbar.MainBottomNavigationBar
import io.zoemeow.dutnotify.view.navbar.MainNavRoutes
import io.zoemeow.dutnotify.view.news.News
import io.zoemeow.dutnotify.view.settings.Settings
import io.zoemeow.dutnotify.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var snackBarState: SnackbarHostState
    private lateinit var lazyListStateGlobal: LazyListState
    private lateinit var lazyListStateSubject: LazyListState
    private lateinit var scope: CoroutineScope

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                NotificationsUtils.initializeNotificationChannel(this)

            LaunchedEffect(Unit) {
                LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
                    getAppBroadcastReceiver(),
                    IntentFilter().apply {
                        addAction(AppBroadcastReceiver.SNACKBARMESSAGE)
                        addAction(AppBroadcastReceiver.NEWS_SCROLLALLTOTOP)
                    }
                )

                // Check permission with background image option
                // Only one request when user start app.
                if (mainViewModel.appSettings.value.backgroundImage.option != BackgroundImageType.Unset) {
                    if (PermissionRequestActivity.checkPermission(
                            applicationContext,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        mainViewModel.reloadAppBackground(
                            context = applicationContext,
                            type = mainViewModel.appSettings.value.backgroundImage.option
                        )
                    } else {
                        val intent =
                            Intent(applicationContext, PermissionRequestActivity::class.java)
                        intent.putExtra(
                            "permission.requested",
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        )
                        permissionRequestActivityResult.launch(intent)
                    }
                }

                // Initialize Refresh news services
                NewsRefreshService.startService(applicationContext)

                mainViewModel.accountDataStore.reLogin(
                    silent = false,
                    reloadSubject = true
                )
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

//        LiveDataUtils.InitializeShowNotifications(
//            lifecycleOwner = this,
//            liveData = mainViewModel.pendingNotifications,
//            scope = scope,
//            snackBarState = snackBarState
//        )

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
                            MainNavRoutes.Account.route -> {
                                if (arrayListOf(
                                        LoginState.NotTriggered,
                                        LoginState.NotLoggedIn
                                    ).contains(mainViewModel.accountDataStore.loginState.value)
                                ) {
                                    if (mainViewModel.accountCurrentPage.value != 0)
                                        mainViewModel.accountCurrentPage.value = 0
                                } else {
                                    if (mainViewModel.accountCurrentPage.value != 1)
                                        mainViewModel.accountCurrentPage.value = 1
                                }
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

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(getAppBroadcastReceiver())
        super.onDestroy()
    }

    private fun getAppBroadcastReceiver(): AppBroadcastReceiver {
        object : AppBroadcastReceiver() {
            override fun onNewsReloadRequested() {}
            override fun onAccountReloadRequested(newsType: String) {}

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
        }.apply {
            return this
        }
    }

    val permissionRequestActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                mainViewModel.reloadAppBackground(
                    context = this,
                    type = mainViewModel.appSettings.value.backgroundImage.option
                )
            } else {
                mainViewModel.appSettings.value = mainViewModel.appSettings.value.modify(
                    optionToModify = AppSettingsCode.BackgroundImage,
                    value = BackgroundImage(
                        option = BackgroundImageType.Unset,
                        path = null
                    )
                )
                mainViewModel.requestSaveChanges()

                mainViewModel.showSnackBarMessage(
                    "Missing permission: READ_EXTERNAL_STORAGE." +
                            "This will revert background image option is unset."
                )
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
}

