package io.zoemeow.dutapp.android

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import io.zoemeow.dutapp.android.services.RefreshNewsService
import io.zoemeow.dutapp.android.ui.theme.MainActivityTheme
import io.zoemeow.dutapp.android.view.account.Account
import io.zoemeow.dutapp.android.view.main.Main
import io.zoemeow.dutapp.android.view.mainnavbar.MainBottomNavigationBar
import io.zoemeow.dutapp.android.view.mainnavbar.MainNavRoutes
import io.zoemeow.dutapp.android.view.news.News
import io.zoemeow.dutapp.android.view.settings.Settings
import io.zoemeow.dutapp.android.viewmodel.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var uiStatus: UIStatus
    private lateinit var appCacheViewModel: AppCacheViewModel
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var globalViewModel: GlobalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted
        permitAllPolicy()

        // Register notifications channel for news service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannelService()
        // Start services
        Intent(applicationContext, RefreshNewsService::class.java).also { intent ->
            // https://stackoverflow.com/a/47654126
            startService(intent)
        }

        setContent {
            // Initialize app cache
            appCacheViewModel = viewModel()
            AppCacheViewModel.setInstance(appCacheViewModel)

            // Initialize UIStatus
            uiStatus = UIStatus.getInstance()
            // Create scope for uiStatus
            uiStatus.scope = rememberCoroutineScope()
            // Set SnackBar in MainActivity Scaffold
            uiStatus.mainActivitySetSnackBarState(SnackbarHostState())
            // Set Activity
            uiStatus.setMainActivity(this)

            // Initialize GlobalViewModel
            globalViewModel = viewModel()
            GlobalViewModel.setInstance(globalViewModel)

            // Initialize AccountViewModel
            accountViewModel = viewModel()

            // Initialize NewsViewModel
            newsViewModel = viewModel()

            // Check permission with background image option
            // Only one request when user start app.
            val firstTimeRequest = remember { mutableStateOf(true) }
            if (firstTimeRequest.value && (globalViewModel.backgroundImage.value.option != BackgroundImageType.Unset)) {
                // Check permission with background image option
                uiStatus.checkPermissionAndReloadAppBackground(
                    type = globalViewModel.backgroundImage.value.option,
                    onRequested = {
                        if (firstTimeRequest.value)
                            uiStatus.requestPermissionAppBackground()

                        // Disable this value to avoid another request.
                        firstTimeRequest.value = false
                    }
                )
            }

            MainActivityTheme(
                dynamicColor = globalViewModel.dynamicColorEnabled.value,
                darkMode = globalViewModel.appTheme.value,
                blackTheme = globalViewModel.blackTheme.value
            ) {
                MainScreen()
            }
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
            snackbarHost = { SnackbarHost(hostState = uiStatus.mainActivityGetSnackBarState()) },
            containerColor = if (globalViewModel.backgroundImage.value.option == BackgroundImageType.Unset)
                MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
            contentColor = if (uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black,
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                MainBottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    onClick = {
                        if (it.route == MainNavRoutes.News.route) {
                            if (!uiStatus.newsDetectItemChosen(needClear = true))
                                uiStatus.newsScrollListToTop()
                        } else if (it.route == MainNavRoutes.Account.route) {
                            if (accountViewModel.isLoggedIn.value || accountViewModel.isRememberLoggedIn.value) {
                                if (uiStatus.accountCurrentPage.value != 1)
                                    uiStatus.accountCurrentPage.value = 1
                            } else {
                                if (uiStatus.accountCurrentPage.value != 0)
                                    uiStatus.accountCurrentPage.value = 0
                            }
                        }
                    }
                )
            },
            content = { contentPadding ->
                NavHost(
                    navController = navController,
                    startDestination = MainNavRoutes.Main.route,
                    modifier = Modifier.padding(contentPadding)
                ) {
                    composable(MainNavRoutes.Main.route) {
                        Main()
                    }

                    composable(MainNavRoutes.News.route) {
                        News(
                            uiStatus = uiStatus,
                            appCacheViewModel = appCacheViewModel,
                            newsViewModel = newsViewModel,
                        )
                    }

                    composable(MainNavRoutes.Account.route) {
                        Account(
                            uiStatus = uiStatus,
                            globalViewModel = globalViewModel,
                            accountViewModel = accountViewModel,
                        )
                    }

                    composable(MainNavRoutes.Settings.route) {
                        Settings()
                    }
                }
            },
        )
    }

    override fun onResume() {
        super.onResume()
        if (this::accountViewModel.isInitialized)
            accountViewModel.reLogin()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 0) {
            uiStatus.checkPermissionAndReloadAppBackground(
                type = globalViewModel.backgroundImage.value.option,
                onSuccessful = {
                    Log.d("Permission", "Triggered successful")
                    uiStatus.updateComposeUI()
                },
                onRequested = {
                    Log.d("Permission", "Triggered request -> failed")
                    globalViewModel.backgroundImage.value.option = BackgroundImageType.Unset
                    globalViewModel.requestSaveSettings()
                    uiStatus.updateComposeUI()
                    uiStatus.showSnackBarMessage(
                        "Missing permission: READ_EXTERNAL_STORAGE. " +
                                "This will revert background image option is unset."
                    )
                }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelService() {
        val channel = NotificationChannel("dut_service", "Services", NotificationManager.IMPORTANCE_NONE)
        channel.lightColor = android.graphics.Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
    }
}

