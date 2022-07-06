package io.zoemeow.dutapp.android

import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.zoemeow.dutapp.android.ui.theme.DUTAppForAndroidTheme
import io.zoemeow.dutapp.android.view.account.Account
import io.zoemeow.dutapp.android.view.main.Main
import io.zoemeow.dutapp.android.view.mainnavbar.MainBottomNavigationBar
import io.zoemeow.dutapp.android.view.mainnavbar.MainNavRoutes
import io.zoemeow.dutapp.android.view.news.News
import io.zoemeow.dutapp.android.view.settings.Settings


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://blog.cpming.top/p/android-os-networkonmainthreadexception
        // For OkHttp3
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted


        setContent {
            DUTAppForAndroidTheme {
                // A scaffold container using the 'background' color from the theme
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.background,
                    topBar = {

                    },
                    bottomBar = { MainBottomNavigationBar(navController = navController) },
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
                News()
            }

            composable(MainNavRoutes.Account.route) {
                Account()
            }

            composable(MainNavRoutes.Settings.route) {
                Settings()
            }
        }
    }
}
