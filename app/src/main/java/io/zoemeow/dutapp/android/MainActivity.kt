package io.zoemeow.dutapp.android

import android.Manifest
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.zoemeow.dutapp.android.model.appsettings.BackgroundImage
import io.zoemeow.dutapp.android.model.enums.AppSettingsCode
import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import io.zoemeow.dutapp.android.model.enums.LoginState
import io.zoemeow.dutapp.android.services.RefreshNewsService
import io.zoemeow.dutapp.android.ui.theme.MainActivityTheme
import io.zoemeow.dutapp.android.utils.NotificationsUtils
import io.zoemeow.dutapp.android.view.account.Account
import io.zoemeow.dutapp.android.view.activities.PermissionRequestActivity
import io.zoemeow.dutapp.android.view.navbar.MainBottomNavigationBar
import io.zoemeow.dutapp.android.view.navbar.MainNavRoutes
import io.zoemeow.dutapp.android.view.news.News
import io.zoemeow.dutapp.android.view.settings.Settings
import io.zoemeow.dutapp.android.viewmodel.MainViewModel


class MainActivity : ComponentActivity() {
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted
        permitAllPolicy()

        setContent {
            // Initialize Main View Model
            mainViewModel = viewModel()
            MainViewModel.setInstance(mainViewModel)

            // Set SnackBar in MainActivity Scaffold
            mainViewModel.uiStatus.setSnackBarState(SnackbarHostState())
            // Create scope for uiStatus
            mainViewModel.uiStatus.scope = rememberCoroutineScope()
            // Set Activity
            mainViewModel.uiStatus.setActivity(this)

            val initialized = remember { mutableStateOf(false) }
            if (!initialized.value) {
                // Initialize
                mainViewModel.initialize()

                // Check permission with background image option
                // Only one request when user start app.
                if (mainViewModel.settings.value.backgroundImage.option != BackgroundImageType.Unset) {
                    if (PermissionRequestActivity.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        mainViewModel.uiStatus.reloadAppBackground(mainViewModel.settings.value.backgroundImage.option)
                    } else {
                        val intent = Intent(this, PermissionRequestActivity::class.java)
                        intent.putExtra("permission.requested", arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                        permissionRequestActivityResult.launch(intent)
                    }
                }

                // Register notifications channel for news service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationsUtils.initializeNotificationChannel(this)
                }

                // Initialize services
                // Uncomment if you fixed this issue.
                Intent(this, RefreshNewsService::class.java).also { intent ->
                    // https://stackoverflow.com/a/47654126
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        startForegroundService(intent)
                    else startService(intent)
                }

                // Set to false to avoid another run
                initialized.value = true
            }

            MainActivityTheme(
                appSettings = mainViewModel.settings.value
            ) {
                MainScreen()
            }
        }
    }

    val permissionRequestActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            mainViewModel.uiStatus.reloadAppBackground(mainViewModel.settings.value.backgroundImage.option)
        }
        else {
            mainViewModel.settings.value = mainViewModel.settings.value.modify(
                optionToModify = AppSettingsCode.BackgroundImage,
                value = BackgroundImage(
                    option = BackgroundImageType.Unset,
                    path = null
                )
            )
            mainViewModel.requestSaveChanges()

            mainViewModel.uiStatus.showSnackBarMessage(
                "Missing permission: READ_EXTERNAL_STORAGE. " +
                        "This will revert background image option is unset.",
                true
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
            snackbarHost = { SnackbarHost(hostState = mainViewModel.uiStatus.getSnackBarState()) },
            containerColor = if (mainViewModel.settings.value.backgroundImage.option == BackgroundImageType.Unset)
                MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
            contentColor = if (mainViewModel.uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black,
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                MainBottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    onClick = {
                        if (it.route == MainNavRoutes.News.route) {
                            if (!mainViewModel.uiStatus.newsDetectItemChosen(needClear = true))
                                mainViewModel.uiStatus.newsScrollListToTop()
                        } else if (it.route == MainNavRoutes.Account.route) {
                            if (arrayListOf(LoginState.NotTriggered, LoginState.NotLoggedIn).contains(mainViewModel.uiStatus.loginState.value)) {
                                if (mainViewModel.uiStatus.accountCurrentPage.value != 0)
                                    mainViewModel.uiStatus.accountCurrentPage.value = 0
                            } else {
                                if (mainViewModel.uiStatus.accountCurrentPage.value != 1)
                                    mainViewModel.uiStatus.accountCurrentPage.value = 1
                            }
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

    override fun onPause() {
        super.onPause()

        Log.d("Paused", "App paused.")
    }

    override fun onResume() {
        super.onResume()
        if (this::mainViewModel.isInitialized)
            mainViewModel.reLogin(silent = true, reloadSubject = false)
        Log.d("Resumed", "App resumed.")
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

