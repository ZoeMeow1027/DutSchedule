package io.zoemeow.dutapp.android

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import io.zoemeow.dutapp.android.ui.custom.BackgroundImage
import io.zoemeow.dutapp.android.ui.theme.DUTAppForAndroidTheme
import io.zoemeow.dutapp.android.view.account.Account
import io.zoemeow.dutapp.android.view.main.Main
import io.zoemeow.dutapp.android.view.mainnavbar.MainBottomNavigationBar
import io.zoemeow.dutapp.android.view.mainnavbar.MainNavRoutes
import io.zoemeow.dutapp.android.view.news.News
import io.zoemeow.dutapp.android.view.settings.Settings
import io.zoemeow.dutapp.android.viewmodel.AccountViewModel
import io.zoemeow.dutapp.android.viewmodel.GlobalViewModel
import io.zoemeow.dutapp.android.viewmodel.NewsViewModel

class MainActivity : ComponentActivity() {
    // https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted

    private lateinit var accountViewModel: AccountViewModel
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var globalViewModel: GlobalViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permitAllPolicy()

        setContent {
            DUTAppForAndroidTheme {
                // Initialize GlobalViewModel
                GlobalViewModel.setInstance(viewModel())
                globalViewModel = GlobalViewModel.getInstance()
                // Set activity to variable in GlobalViewModel
                globalViewModel.setActivity(this)
                // Load backgrounds if its settings is enabled
                globalViewModel.LoadBackground()
                // Set SnackBar in MainActivity Scaffold
                globalViewModel.snackBarHostState = SnackbarHostState()

                // Initialize AccountViewModel
                AccountViewModel.setInstance(viewModel())
                accountViewModel = AccountViewModel.getInstance()

                // Initialize NewsViewModel
                newsViewModel = viewModel()

                // Initialize for NavController for main activity
                val navController = rememberNavController()
                // Nav Route
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                // If background image not null, directly show here.
                BackgroundImage(globalViewModel.backgroundPainter)

                // A scaffold container using the 'background' color from the theme
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = globalViewModel.snackBarHostState) },
                    containerColor = if (globalViewModel.settings.backgroundImageOption == BackgroundImageType.None)
                        MaterialTheme.colorScheme.background else Color.Transparent,
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        MainBottomNavigationBar(
                            navController = navController,
                            currentRoute = currentRoute,
                            onClick = {
                                if (it.route == MainNavRoutes.News.route) {
                                    if (
                                        newsViewModel.newsSubjectItemChose.value != null ||
                                        newsViewModel.newsGlobalItemChose.value != null
                                    ) {
                                        newsViewModel.newsSubjectItemChose.value = null
                                        newsViewModel.newsGlobalItemChose.value = null
                                    }
                                    else {
                                        newsViewModel.scrollNewsListToTop()
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
            startDestination = MainNavRoutes.News.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(MainNavRoutes.Main.route) {
                Main()
            }

            composable(MainNavRoutes.News.route) {
                News(newsViewModel)
            }

            composable(MainNavRoutes.Account.route) {
                Account(accountViewModel)
            }

            composable(MainNavRoutes.Settings.route) {
                Settings()
            }
        }
    }
}
