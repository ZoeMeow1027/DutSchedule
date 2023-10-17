package io.zoemeow.dutschedule.activity

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.model.account.AccountAuth
import io.zoemeow.dutschedule.repository.DutAccountRepository
import io.zoemeow.dutschedule.ui.component.account.AccountInfoBanner
import io.zoemeow.dutschedule.ui.component.account.LoginBannerNotLoggedIn
import io.zoemeow.dutschedule.ui.component.account.LoginDialog
import io.zoemeow.dutschedule.ui.component.account.LoginLoading
import io.zoemeow.dutschedule.ui.component.account.LogoutDialog
import io.zoemeow.dutschedule.ui.component.base.ButtonBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountActivity: BaseActivity() {
    private val dutAccountRepository = DutAccountRepository()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun OnMainView(padding: PaddingValues) {
        val loginDialogVisible = remember { mutableStateOf(false) }
        val loginDialogEnabled = remember { mutableStateOf(true) }
        val logoutDialogVisible = remember { mutableStateOf(false) }

        val loginStatus = remember { mutableIntStateOf(0) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Account") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                setResult(RESULT_OK)
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
                )
            },
            content = {
                Column(
                    modifier = Modifier.padding(it),
                    content = {
                        when (loginStatus.intValue) {
                            -1 -> {
                                LoginBannerNotLoggedIn(
                                    padding = PaddingValues(10.dp),
                                    clicked = {
                                        loginDialogVisible.value = true
                                    },
                                )
                            }
                            0 -> {
                                LoginLoading(
                                    padding = PaddingValues(10.dp)
                                )
                            }
                            1 -> {
                                AccountInfoBanner(
                                    padding = PaddingValues(10.dp),
                                    isLoading = false,
                                    username = "102190147",
                                    schoolClass = "19TCLC_DT3",
                                    trainingProgramPlan = "123456789012345678901234567890"
                                )
                                ButtonBase(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    content = { Text("Subject schedule") },
                                    horizontalArrangement = Arrangement.Start,
                                    isOutlinedButton = true,
                                )
                                ButtonBase(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    content = { Text("Subject fee") },
                                    horizontalArrangement = Arrangement.Start,
                                    isOutlinedButton = true,
                                )
                                ButtonBase(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    content = { Text("Account information") },
                                    horizontalArrangement = Arrangement.Start,
                                    isOutlinedButton = true,
                                )
                                ButtonBase(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    content = { Text("Logout") },
                                    horizontalArrangement = Arrangement.Start,
                                    isOutlinedButton = true,
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
                        loginStatus.intValue = 0
                        showSnackBar(
                            text = "Logging you in...",
                            clearPrevious = true,
                        )

                        getMainViewModel().accountSession.value = getMainViewModel().accountSession.value.clone(
                            accountAuth = AccountAuth(
                                username = username,
                                password = password,
                                rememberLogin = rememberLogin
                            )
                        )

                        val data = dutAccountRepository.login(
                            getMainViewModel().accountSession.value,
                            forceLogin = true,
                            onSessionChanged = { sessionId, timestamp ->
                                getMainViewModel().accountSession.value = getMainViewModel().accountSession.value.clone(
                                    sessionId = sessionId,
                                    sessionLastRequest = timestamp
                                )
                            }
                        )
                        when (data) {
                            true -> {
                                loginDialogEnabled.value = true
                                loginDialogVisible.value = false
                                loginStatus.intValue = 1
                                saveSettings()
                                showSnackBar(
                                    text = "Successfully logged in!",
                                    clearPrevious = true,
                                )
                            }
                            false -> {
                                loginDialogEnabled.value = true
                                loginStatus.intValue = -1
                                saveSettings()
                                showSnackBar(
                                    text = "Login failed! Please check your login information and try again.",
                                    clearPrevious = true,
                                )
                            }
                        }
                    }
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
                    loginStatus.intValue = -1
                    logoutDialogVisible.value = false
                    CoroutineScope(Dispatchers.IO).launch {
                        getMainViewModel().accountSession.value = getMainViewModel().accountSession.value.clone(
                            accountAuth = AccountAuth(),
                            sessionId = null,
                            sessionLastRequest = 0
                        )
                        saveSettings()
                        showSnackBar(
                            text = "Successfully logout!",
                            clearPrevious = true,
                        )
                    }
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

        run {
            CoroutineScope(Dispatchers.IO).launch {
                val data = dutAccountRepository.login(
                    getMainViewModel().accountSession.value,
                    forceLogin = true,
                    onSessionChanged = { sessionId, timestamp ->
                        getMainViewModel().accountSession.value = getMainViewModel().accountSession.value.clone(
                            sessionId = sessionId,
                            sessionLastRequest = timestamp
                        )
                    }
                )
                loginStatus.intValue = if (data) 1 else -1
            }
        }
    }
}