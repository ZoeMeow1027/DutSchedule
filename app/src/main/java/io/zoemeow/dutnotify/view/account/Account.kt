package io.zoemeow.dutnotify.view.account

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.zoemeow.dutnotify.MainActivity
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.model.enums.AccountServiceCode
import io.zoemeow.dutnotify.model.enums.LoginState
import io.zoemeow.dutnotify.service.AccountService
import io.zoemeow.dutnotify.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Account(
    mainViewModel: MainViewModel,
) {
    val context = LocalContext.current as MainActivity

    // Module for Logout alert dialog
    val dialogLogoutEnabled = remember { mutableStateOf(false) }
    AccountDialogLogout(
        enabled = dialogLogoutEnabled,
        logoutRequest = {
            Intent(context, AccountService::class.java).apply {
                putExtra(AccountServiceCode.ACTION, AccountServiceCode.ACTION_LOGOUT)
            }.also {
                context.startService(it)
            }
        }
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            SmallTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Text(stringResource(id = if (mainViewModel.Account_HasSaved.value)
                        R.string.topbar_account_dashboard else R.string.topbar_account_notloggedin))
                },
                actions = {
                    if (mainViewModel.Account_HasSaved.value) {
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
            when (mainViewModel.Account_HasSaved.value) {
                false -> {
                    AccountPageNotLoggedIn(
                        padding = padding,
                        mainViewModel = mainViewModel
                    )
                }
                true -> {
                    AccountPageDashboard(
                        mainViewModel = mainViewModel,
                        padding = padding,
                    )
                }
            }
        }
    )
}