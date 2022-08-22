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
import io.zoemeow.dutapp.android.model.enums.LoginState
import io.zoemeow.dutapp.android.viewmodel.MainViewModel

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
        logoutRequest = { mainViewModel.logout() }
    )

    val swipeRefreshStateSubjectSchedule = rememberSwipeRefreshState(false)
    val swipeRefreshStateSubjectFee = rememberSwipeRefreshState(false)
    val swipeRefreshStateAccInfo = rememberSwipeRefreshState(false)

    LaunchedEffect(
        mainViewModel.uiStatus.procAccSubSch.value,
        mainViewModel.uiStatus.procAccSubFee.value,
        mainViewModel.uiStatus.procAccInfo.value
    ) {
        swipeRefreshStateSubjectSchedule.isRefreshing =
            mainViewModel.uiStatus.procAccSubSch.value == ProcessState.Running
        swipeRefreshStateSubjectFee.isRefreshing =
            mainViewModel.uiStatus.procAccSubFee.value == ProcessState.Running
        swipeRefreshStateAccInfo.isRefreshing =
            mainViewModel.uiStatus.procAccInfo.value == ProcessState.Running
    }

    LaunchedEffect(mainViewModel.uiStatus.loginState.value) {
        if (mainViewModel.isStoreAccount()) {
            if (mainViewModel.uiStatus.accountCurrentPage.value < 1)
                mainViewModel.uiStatus.accountCurrentPage.value = 1
        }
        else {
            mainViewModel.uiStatus.accountCurrentPage.value = 0
        }
    }

    // Trigger when switch pages
    LaunchedEffect(
        mainViewModel.uiStatus.accountCurrentPage.value,
    ) {
        when (mainViewModel.uiStatus.accountCurrentPage.value) {
            0 -> {
                barTitle.value = "Not logged in"
            }
            1 -> {
                barTitle.value = "Dashboard"
            }
            2 -> {
                barTitle.value = "Subject Schedule"
                if (mainViewModel.uiStatus.subjectSchedule.size == 0)
                    mainViewModel.fetchSubjectSchedule(mainViewModel.settings.schoolYear.value)
            }
            3 -> {
                barTitle.value = "Subject Fee"
                if (mainViewModel.uiStatus.subjectFee.size == 0)
                    mainViewModel.fetchSubjectFee(mainViewModel.settings.schoolYear.value)
            }
            4 -> {
                barTitle.value = "Account Information"
                if (mainViewModel.uiStatus.accountInformation.value == null)
                    mainViewModel.fetchAccountInformation()
            }
        }
    }

    // If logout, will return to not logged in screen
    BackHandler(
        enabled = (
                if (arrayListOf(LoginState.NotLoggedInButRemembered, LoginState.LoggedIn).contains(mainViewModel.uiStatus.loginState.value)) {
                    mainViewModel.uiStatus.accountCurrentPage.value > 1
                } else mainViewModel.uiStatus.accountCurrentPage.value > 0
                ),
        onBack = {
            mainViewModel.uiStatus.accountCurrentPage.value =
                if (arrayListOf(LoginState.NotLoggedInButRemembered, LoginState.LoggedIn).contains(mainViewModel.uiStatus.loginState.value)) 1 else 0
        }
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            SmallTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    if (mainViewModel.uiStatus.accountCurrentPage.value >= 2) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                                .clickable {
                                    mainViewModel.uiStatus.accountCurrentPage.value =
                                        if (arrayListOf(LoginState.NotLoggedInButRemembered, LoginState.LoggedIn).contains(mainViewModel.uiStatus.loginState.value))
                                            1 else 0
                                },
                            content = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_arrow_back_24),
                                    contentDescription = "",
                                    tint = if (mainViewModel.uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black,
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
                    if (mainViewModel.uiStatus.accountCurrentPage.value == 1) {
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
                                    tint = if (mainViewModel.uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        )
                    }
                }
            )
        },
        content = { padding ->
            when (mainViewModel.uiStatus.accountCurrentPage.value) {
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
                2 -> {
                    AccountPageSubjectSchedule(
                        padding = padding,
                        subjectScheduleList = mainViewModel.uiStatus.subjectSchedule,
                        swipeRefreshState = swipeRefreshStateSubjectSchedule,
                        reloadRequested = {
                            mainViewModel.fetchSubjectSchedule(mainViewModel.settings.schoolYear.value)
                        }
                    )
                }
                3 -> {
                    AccountPageSubjectFee(
                        padding = padding,
                        subjectFeeList = mainViewModel.uiStatus.subjectFee,
                        swipeRefreshState = swipeRefreshStateSubjectFee,
                        reloadRequested = {
                            mainViewModel.fetchSubjectFee(mainViewModel.settings.schoolYear.value)
                        }
                    )
                }
                4 -> {
                    AccountPageInformation(
                        padding = padding,
                        accountInformation = mainViewModel.uiStatus.accountInformation.value,
                        swipeRefreshState = swipeRefreshStateAccInfo,
                        reloadRequested = {
                            mainViewModel.fetchAccountInformation()
                        }
                    )
                }
            }
        }
    )
}