package io.zoemeow.dutnotify.view.account

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.model.enums.LoginState
import io.zoemeow.dutnotify.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Account(
    mainViewModel: MainViewModel,
) {
    val barTitle: MutableState<String> = remember { mutableStateOf("") }

    // Module for Logout alert dialog
    val dialogLogoutEnabled = remember { mutableStateOf(false) }
    AccountDialogLogout(
        enabled = dialogLogoutEnabled,
        logoutRequest = { mainViewModel.accountDataStore.logout() }
    )

    LaunchedEffect(mainViewModel.accountDataStore.loginState.value) {
        if (mainViewModel.accountDataStore.isStoreAccount()) {
            if (mainViewModel.accountCurrentPage.value < 1)
                mainViewModel.accountCurrentPage.value = 1
        } else {
            mainViewModel.accountCurrentPage.value = 0
        }
    }

    // Trigger when switch pages
    LaunchedEffect(
        mainViewModel.accountCurrentPage.value,
    ) {
        when (mainViewModel.accountCurrentPage.value) {
            0 -> {
                barTitle.value = "Not logged in"
            }
            1 -> {
                barTitle.value = "Dashboard"
            }
        }
    }

    // If logout, will return to not logged in screen
    BackHandler(
        enabled = (
                if (arrayListOf(LoginState.NotTriggered, LoginState.NotLoggedIn).contains(
                        mainViewModel.accountDataStore.loginState.value
                    )
                ) {
                    mainViewModel.accountCurrentPage.value > 0
                } else mainViewModel.accountCurrentPage.value > 1
                ),
        onBack = {
            mainViewModel.accountCurrentPage.value =
                if (arrayListOf(LoginState.NotTriggered, LoginState.NotLoggedIn).contains(
                        mainViewModel.accountDataStore.loginState.value
                    )
                ) 0 else 1
        }
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    if (mainViewModel.accountCurrentPage.value >= 2) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                                .clickable {
                                    mainViewModel.accountCurrentPage.value =
                                        if (arrayListOf(
                                                LoginState.NotLoggedInButRemembered,
                                                LoginState.LoggedIn
                                            ).contains(mainViewModel.accountDataStore.loginState.value)
                                        )
                                            1 else 0
                                },
                            content = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_arrow_back_24),
                                    contentDescription = "",
                                    tint = if (mainViewModel.mainActivityIsDarkTheme.value) Color.White else Color.Black,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        )
                    }
                },
                title = {
                    Text(
                        text = "${stringResource(id = R.string.topbar_account)}${
                            if (barTitle.value.isNotEmpty()) " (${barTitle.value})" else ""
                        }"
                    )
                },
                actions = {
                    if (mainViewModel.accountCurrentPage.value == 1) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                                .clickable {
                                    dialogLogoutEnabled.value = true
                                },
                            content = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_logout_24),
                                    contentDescription = "",
                                    tint = if (mainViewModel.mainActivityIsDarkTheme.value) Color.White else Color.Black,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        )
                    }
                }
            )
        },
        content = { padding ->
            when (mainViewModel.accountCurrentPage.value) {
                0 -> {
                    AccountPageNotLoggedIn(
                        padding = padding,
                        mainViewModel = mainViewModel
                    )
                }
                1 -> {
                    AccountPageDashboard(
                        mainViewModel = mainViewModel,
                        padding = padding,
                    )
                }
            }
        }
    )
}