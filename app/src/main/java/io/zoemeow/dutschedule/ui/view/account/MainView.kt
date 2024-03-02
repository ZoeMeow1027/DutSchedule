package io.zoemeow.dutschedule.ui.view.account

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.activity.AccountActivity
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.model.account.AccountAuth
import io.zoemeow.dutschedule.ui.component.account.AccountInfoBanner
import io.zoemeow.dutschedule.ui.component.account.LoginBannerNotLoggedIn
import io.zoemeow.dutschedule.ui.component.account.LoginDialog
import io.zoemeow.dutschedule.ui.component.account.LogoutDialog
import io.zoemeow.dutschedule.ui.component.base.ButtonBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountActivity.MainView(
    context: Context,
    snackBarHostState: SnackbarHostState,
    containerColor: Color,
    contentColor: Color
) {
    val loginDialogVisible = remember { mutableStateOf(false) }
    val loginDialogEnabled = remember { mutableStateOf(true) }
    val logoutDialogVisible = remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        containerColor = containerColor,
        contentColor = contentColor,
        topBar = {
            LargeTopAppBar(
                title = { Text("Account") },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            setResult(ComponentActivity.RESULT_OK)
                            finish()
                        },
                        content = {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "",
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .verticalScroll(rememberScrollState()),
                content = {
                    when (getMainViewModel().accountSession.accountSession.processState.value) {
                        ProcessState.NotRunYet -> {
                            LoginBannerNotLoggedIn(
                                opacity = getControlBackgroundAlpha(),
                                padding = PaddingValues(10.dp),
                                clicked = {
                                    loginDialogVisible.value = true
                                },
                            )
                        }
                        ProcessState.Failed -> {
                            ButtonBase(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                modifierInside = Modifier.padding(vertical = 7.dp),
                                content = { Text("Try to login again") },
                                horizontalArrangement = Arrangement.Start,
                                opacity = getControlBackgroundAlpha(),
                                clicked = {
                                    getMainViewModel().accountSession.reLogin()
                                }
                            )
                            ButtonBase(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                modifierInside = Modifier.padding(vertical = 7.dp),
                                content = { Text("Logout") },
                                horizontalArrangement = Arrangement.Start,
                                opacity = getControlBackgroundAlpha(),
                                clicked = {
                                    logoutDialogVisible.value = true
                                }
                            )
                        }
                        ProcessState.Running -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(120.dp).padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                content = {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.size(5.dp))
                                    Text("Logging in...")
                                }
                            )
                        }
                        ProcessState.Successful -> {
                            getMainViewModel().accountSession.accountInformation.let { accInfo ->
                                AccountInfoBanner(
                                    opacity = getControlBackgroundAlpha(),
                                    padding = PaddingValues(10.dp),
                                    isLoading = accInfo.processState.value == ProcessState.Running,
                                    username = accInfo.data.value?.studentId ?: "(unknown)",
                                    schoolClass = accInfo.data.value?.schoolClass ?: "(unknown)",
                                    trainingProgramPlan = accInfo.data.value?.trainingProgramPlan ?: "(unknown)"
                                )
                            }
                            ButtonBase(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                modifierInside = Modifier.padding(vertical = 7.dp),
                                content = { Text("Subject Information") },
                                horizontalArrangement = Arrangement.Start,
                                opacity = getControlBackgroundAlpha(),
                                clicked = {
                                    val intent = Intent(context, AccountActivity::class.java)
                                    intent.action = "subject_information"
                                    context.startActivity(intent)
                                }
                            )
                            ButtonBase(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                modifierInside = Modifier.padding(vertical = 7.dp),
                                content = { Text("Subject Fee") },
                                horizontalArrangement = Arrangement.Start,
                                opacity = getControlBackgroundAlpha(),
                                clicked = {
                                    val intent = Intent(context, AccountActivity::class.java)
                                    intent.action = "subject_fee"
                                    context.startActivity(intent)
                                }
                            )
                            ButtonBase(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                modifierInside = Modifier.padding(vertical = 7.dp),
                                content = { Text("Account Information") },
                                horizontalArrangement = Arrangement.Start,
                                opacity = getControlBackgroundAlpha(),
                                clicked = {
                                    val intent = Intent(context, AccountActivity::class.java)
                                    intent.action = "acc_info"
                                    context.startActivity(intent)
                                }
                            )
                            ButtonBase(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                modifierInside = Modifier.padding(vertical = 7.dp),
                                content = { Text("Account Training Result") },
                                horizontalArrangement = Arrangement.Start,
                                opacity = getControlBackgroundAlpha(),
                                clicked = {
                                    val intent = Intent(context, AccountActivity::class.java)
                                    intent.action = "acc_training_result"
                                    context.startActivity(intent)
                                }
                            )
                            ButtonBase(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                modifierInside = Modifier.padding(vertical = 7.dp),
                                content = { Text("Logout") },
                                horizontalArrangement = Arrangement.Start,
                                opacity = getControlBackgroundAlpha(),
                                clicked = {
                                    logoutDialogVisible.value = true
                                }
                            )
                        }
                    }
                }
            )
        }
    )
    LoginDialog(
        isVisible = loginDialogVisible.value,
        controlEnabled = loginDialogEnabled.value,
        loginClicked = { username, password, rememberLogin ->
            run {
                CoroutineScope(Dispatchers.IO).launch {
                    loginDialogEnabled.value = false
                    showSnackBar(
                        text = "Logging you in...",
                        clearPrevious = true,
                    )
                }
                getMainViewModel().accountSession.login(
                    accountAuth = AccountAuth(
                        username = username,
                        password = password,
                        rememberLogin = rememberLogin
                    ),
                    onCompleted = {
                        when (it) {
                            true -> {
                                loginDialogEnabled.value = true
                                loginDialogVisible.value = false
                                getMainViewModel().accountSession.reLogin()
                                showSnackBar(
                                    text = "Successfully logged in!",
                                    clearPrevious = true,
                                )
                            }
                            false -> {
                                loginDialogEnabled.value = true
                                showSnackBar(
                                    text = "Login failed! Please check your login information and try again.",
                                    clearPrevious = true,
                                )
                            }
                        }
                    }
                )
            }
        },
        cancelRequested = {
            loginDialogVisible.value = false
        },
        canDismiss = false,
        dismissClicked = {
            if (loginDialogEnabled.value) {
                loginDialogVisible.value = false
            }
        }
    )
    LogoutDialog(
        isVisible = logoutDialogVisible.value,
        canDismiss = true,
        logoutClicked = {
            run {
                loginDialogVisible.value = false
                getMainViewModel().accountSession.logout(
                    onCompleted = {
                        showSnackBar(
                            text = "Successfully logout!",
                            clearPrevious = true,
                        )
                    }
                )
            }
        },
        dismissClicked = {
            logoutDialogVisible.value = false
        }
    )
    BackHandler(
        enabled = loginDialogVisible.value || logoutDialogVisible.value,
        onBack = {
            if (loginDialogVisible.value) {
                loginDialogVisible.value = false
            }
            if (logoutDialogVisible.value) {
                logoutDialogVisible.value = false
            }
        }
    )
}