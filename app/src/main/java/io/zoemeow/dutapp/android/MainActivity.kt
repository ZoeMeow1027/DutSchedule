package io.zoemeow.dutapp.android

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutapp.android.model.enums.AppTheme
import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import io.zoemeow.dutapp.android.ui.theme.MainActivityTheme
import io.zoemeow.dutapp.android.view.account.Account
import io.zoemeow.dutapp.android.view.main.Main
import io.zoemeow.dutapp.android.view.mainnavbar.MainBottomNavigationBar
import io.zoemeow.dutapp.android.view.mainnavbar.MainNavRoutes
import io.zoemeow.dutapp.android.view.news.News
import io.zoemeow.dutapp.android.view.settings.Settings
import io.zoemeow.dutapp.android.viewmodel.AccountViewModel
import io.zoemeow.dutapp.android.viewmodel.GlobalViewModel
import io.zoemeow.dutapp.android.viewmodel.NewsViewModel
import io.zoemeow.dutapp.android.viewmodel.UIStatus

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val uiStatus = UIStatus.getInstance()
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var globalViewModel: GlobalViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted
        permitAllPolicy()

        setContent {
            // Initialize GlobalViewModel
            globalViewModel = viewModel()
            GlobalViewModel.setInstance(globalViewModel)

            // Create scope for uiStatus
            uiStatus.scope = rememberCoroutineScope()
            // Set SnackBar in MainActivity Scaffold
            uiStatus.mainActivitySetSnackBarState(SnackbarHostState())
            // Set Activity
            uiStatus.setMainActivity(this)

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
                darkTheme = (
                        (globalViewModel.appTheme.value == AppTheme.FollowSystem &&
                                isSystemInDarkTheme()) ||
                        globalViewModel.appTheme.value == AppTheme.DarkMode
                ),
                blackTheme = globalViewModel.blackTheme.value
            ) {
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
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        MainBottomNavigationBar(
                            navController = navController,
                            currentRoute = currentRoute,
                            onClick = {
                                if (it.route == MainNavRoutes.News.route) {
                                    if (!uiStatus.newsDetectItemChosen(needClear = true))
                                        uiStatus.newsScrollListToTop()
                                }
                                else if (it.route == MainNavRoutes.Account.route) {
                                    if (accountViewModel.isLoggedIn.value) {
                                        if (uiStatus.accountCurrentPage.value != 1)
                                            uiStatus.accountCurrentPage.value = 1
                                    }
                                    else {
                                        if (uiStatus.accountCurrentPage.value != 0)
                                            uiStatus.accountCurrentPage.value = 0
                                    }
                                }
                            }
                        )
                    },
                    content = { contentPadding ->
                        NavigationHost(
                            navController = navController,
                            padding = contentPadding
                        )
                    },
                )
            }
        }
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

    @Composable
    private fun NavigationHost(
        navController: NavHostController,
        padding: PaddingValues
    ) {
        NavHost(
            navController = navController,
            startDestination = MainNavRoutes.Main.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(MainNavRoutes.Main.route) {
                Main()
            }

            composable(MainNavRoutes.News.route) {
                News(newsViewModel, uiStatus)
            }

            composable(MainNavRoutes.Account.route) {
                Account(globalViewModel, accountViewModel, uiStatus)
            }

            composable(MainNavRoutes.Settings.route) {
                Settings()
            }
        }
    }
}

