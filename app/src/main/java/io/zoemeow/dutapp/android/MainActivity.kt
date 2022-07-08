package io.zoemeow.dutapp.android

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.zoemeow.dutapp.android.ui.theme.DUTAppForAndroidTheme
import io.zoemeow.dutapp.android.view.account.Account
import io.zoemeow.dutapp.android.view.main.Main
import io.zoemeow.dutapp.android.view.mainnavbar.MainBottomNavigationBar
import io.zoemeow.dutapp.android.view.mainnavbar.MainNavRoutes
import io.zoemeow.dutapp.android.view.news.News
import io.zoemeow.dutapp.android.view.settings.Settings
import io.zoemeow.dutapp.android.viewmodel.AccountViewModel
import io.zoemeow.dutapp.android.viewmodel.NewsViewModel

class MainActivity : ComponentActivity() {
    // https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted

    private lateinit var accountViewModel: AccountViewModel
    private lateinit var newsViewModel: NewsViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://blog.cpming.top/p/android-os-networkonmainthreadexception
        // For OkHttp3
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        setContent {
            DUTAppForAndroidTheme {
                AccountViewModel.setInstance(viewModel())
                accountViewModel = AccountViewModel.getInstance()

                NewsViewModel.setInstance(viewModel())
                newsViewModel = NewsViewModel.getInstance()
                newsViewModel.setContext(LocalContext.current)

                val navController = rememberNavController()

                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                // A scaffold container using the 'background' color from the theme
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text("DUT")
                            }
                        )
                    },
                    bottomBar = {
                        MainBottomNavigationBar(
                            navController = navController,
                            currentRoute = currentRoute
                        )
                    },
                    floatingActionButton = {
                        when (currentRoute) {
                            MainNavRoutes.News.route -> {
                                FloatingActionButton(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    onClick = {
                                        // TODO: Search in news global and news subject here!
                                    },
                                    content = {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_search_24),
                                            contentDescription = "Search",
                                        )
                                    }
                                )
                            }
                        }
                    },
                    content = { contentPadding ->
                        NavigationHost(
                            navController = navController,
                            padding = contentPadding
                        )
                    }
                )
            }
        }
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
