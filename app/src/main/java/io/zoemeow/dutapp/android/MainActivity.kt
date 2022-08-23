package io.zoemeow.dutapp.android

import android.Manifest
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
import androidx.activity.result.contract.ActivityResultContracts
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
import io.zoemeow.dutapp.android.model.enums.LoginState
import io.zoemeow.dutapp.android.services.RefreshNewsService
import io.zoemeow.dutapp.android.ui.theme.MainActivityTheme
import io.zoemeow.dutapp.android.view.account.Account
import io.zoemeow.dutapp.android.view.activities.PermissionRequestActivity
import io.zoemeow.dutapp.android.view.navbar.MainBottomNavigationBar
import io.zoemeow.dutapp.android.view.navbar.MainNavRoutes
import io.zoemeow.dutapp.android.view.news.News
import io.zoemeow.dutapp.android.view.settings.Settings
import io.zoemeow.dutapp.android.viewmodel.*

@AndroidEntryPoint
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

            // Create scope for uiStatus
            mainViewModel.uiStatus.scope = rememberCoroutineScope()
            // Set SnackBar in MainActivity Scaffold
            mainViewModel.uiStatus.setSnackBarState(SnackbarHostState())
            // Set Activity
            mainViewModel.uiStatus.setActivity(this)


//            // Register notifications channel for news service
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                createNotificationChannelService()
//            }

            // Start services
            Intent(applicationContext, RefreshNewsService::class.java).also { intent ->
                // https://stackoverflow.com/a/47654126
                startService(intent)
            }

            // Check permission with background image option
            // Only one request when user start app.
            val firstTimeRequest = remember { mutableStateOf(true) }
            if (firstTimeRequest.value && (mainViewModel.settings.backgroundImage.value.option != BackgroundImageType.Unset)) {
                // Check permission with background image option

                if (PermissionRequestActivity.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    mainViewModel.uiStatus.reloadAppBackground(mainViewModel.settings.backgroundImage.value.option)
                } else {
                    val intent = Intent(this, PermissionRequestActivity::class.java)
                    intent.putExtra("permission.requested", arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                    permissionRequestActivityResult.launch(intent)
                }

                firstTimeRequest.value = false
            } else {
                firstTimeRequest.value = false
            }

            MainActivityTheme(
                dynamicColor = mainViewModel.settings.dynamicColorEnabled.value,
                darkMode = mainViewModel.settings.appTheme.value,
                blackTheme = mainViewModel.settings.blackTheme.value
            ) {
                MainScreen()
            }
        }
    }

    val permissionRequestActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            mainViewModel.uiStatus.reloadAppBackground(mainViewModel.settings.backgroundImage.value.option)

            // TODO: Find better solution instead of doing this here!!!
            mainViewModel.settings.blackTheme.value = !mainViewModel.settings.blackTheme.value
            mainViewModel.settings.blackTheme.value = !mainViewModel.settings.blackTheme.value
            // mainViewModel.uiStatus.updateComposeUI()
        }
        else {
            mainViewModel.settings.backgroundImage.value.option = BackgroundImageType.Unset
            mainViewModel.requestSaveChanges()

            // TODO: Find better solution instead of doing this here!!!
            mainViewModel.settings.blackTheme.value = !mainViewModel.settings.blackTheme.value
            mainViewModel.settings.blackTheme.value = !mainViewModel.settings.blackTheme.value

            // mainViewModel.uiStatus.updateComposeUI()

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
            containerColor = if (mainViewModel.settings.backgroundImage.value.option == BackgroundImageType.Unset)
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
                    startDestination =
                        if (arrayListOf(LoginState.NotTriggered, LoginState.NotLoggedIn).contains(mainViewModel.uiStatus.loginState.value))
                            MainNavRoutes.News.route else MainNavRoutes.Account.route,
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

    override fun onResume() {
        super.onResume()
        if (this::mainViewModel.isInitialized)
            mainViewModel.reLogin(silent = true, reloadSubject = false)
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

//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun createNotificationChannelService() {
//        val channel = NotificationChannel("dut_service", "Services", NotificationManager.IMPORTANCE_NONE)
//        channel.lightColor = android.graphics.Color.BLUE
//        channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
//        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        service.createNotificationChannel(channel)
//    }
}

