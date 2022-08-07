package io.zoemeow.dutapp.android.view.account

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
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.viewmodel.AccountViewModel
import io.zoemeow.dutapp.android.viewmodel.GlobalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Account(
    globalViewModel: GlobalViewModel,
    accountViewModel: AccountViewModel
) {
    val barTitle: MutableState<String> = remember { mutableStateOf("") }

    // Module for Logout alert dialog
    val dialogLogoutEnabled = remember { mutableStateOf(false) }
    AccountLogoutDialog(
        enabled = dialogLogoutEnabled,
        logoutRequest = { accountViewModel.logout() }
    )

    val swipeRefreshStateSubjectSchedule = rememberSwipeRefreshState(false)
    val swipeRefreshStateSubjectFee = rememberSwipeRefreshState(false)

    LaunchedEffect(
        accountViewModel.isLoggedIn.value,
        accountViewModel.processStateSubjectSchedule.value,
        accountViewModel.processStateSubjectFee.value,
    ) {
        if (!accountViewModel.isLoggedIn.value)
            accountViewModel.accountPage.value = 0

        swipeRefreshStateSubjectSchedule.isRefreshing = accountViewModel.processStateSubjectSchedule.value == ProcessState.Running
        swipeRefreshStateSubjectFee.isRefreshing = accountViewModel.processStateSubjectFee.value == ProcessState.Running
    }

    // Trigger when switch pages
    LaunchedEffect(
        accountViewModel.accountPage.value
    ) {
        when (accountViewModel.accountPage.value) {
            0 -> {
                barTitle.value = "Not logged in"
            }
            1 -> {
                barTitle.value = "Dashboard"
            }
            2 -> {
                barTitle.value = "Subject Schedule"
                if (accountViewModel.subjectScheduleList.size == 0)
                    accountViewModel.getSubjectSchedule(globalViewModel.schoolYear.value)
            }
            3 -> {
                barTitle.value = "Subject Fee"
                if (accountViewModel.subjectFeeList.size == 0)
                    accountViewModel.getSubjectFee(globalViewModel.schoolYear.value)
            }
        }
    }

    // If logout, will return to not logged in screen
    BackHandler(
        enabled = (
                if (accountViewModel.isLoggedIn.value) {
                    accountViewModel.accountPage.value != 1
                }
                else accountViewModel.accountPage.value != 0
                ),
        onBack = {
            accountViewModel.accountPage.value =
                if (accountViewModel.isLoggedIn.value) 1 else 0
        }
    )

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = if (globalViewModel.isDarkMode.value) Color.White else Color.Black,
        topBar = {
            SmallTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    if (accountViewModel.accountPage.value >= 2) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                                .clickable {
                                    accountViewModel.accountPage.value =
                                        if (accountViewModel.isLoggedIn.value) 1 else 0
                                },
                            content = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_arrow_back_24),
                                    contentDescription = "",
                                    tint = if (globalViewModel.isDarkMode.value) Color.White else Color.Black,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        )
                    }
                },
                title = {
                    Text(text = "${stringResource(id = R.string.topbar_account)}${
                        if (barTitle.value.isNotEmpty()) " (${barTitle.value})" else ""
                    }")
                },
                actions = {
                    if (accountViewModel.accountPage.value == 1) {
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
                                    tint = if (globalViewModel.isDarkMode.value) Color.White else Color.Black,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        )
                    }
                }
            )
        },
        content = { padding ->
            when (accountViewModel.accountPage.value) {
                0 -> {
                    AccountNotLoggedIn(
                        padding = padding,
                        accountViewModel = accountViewModel
                    )
                }
                1 -> {
                    AccountDashboard(
                        globalViewModel = globalViewModel,
                        accountViewModel = accountViewModel,
                        padding = padding,
                    )
                }
                2 -> {
                    AccountSubjectSchedule(
                        padding = padding,
                        subjectScheduleList = accountViewModel.subjectScheduleList,
                        swipeRefreshState = swipeRefreshStateSubjectSchedule,
                        reloadRequested = {
                            accountViewModel.getSubjectSchedule(globalViewModel.schoolYear.value)
                        }
                    )
                }
                3 -> {
                    AccountSubjectFee(
                        padding = padding,
                        subjectFeeList = accountViewModel.subjectFeeList,
                        swipeRefreshState = swipeRefreshStateSubjectFee,
                        reloadRequested = {
                            accountViewModel.getSubjectFee(globalViewModel.schoolYear.value)
                        }
                    )
                }
            }
        }
    )
}