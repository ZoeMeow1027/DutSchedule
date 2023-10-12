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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.ui.component.account.AccountInfoBanner
import io.zoemeow.dutschedule.ui.component.account.LoginBannerNotLoggedIn
import io.zoemeow.dutschedule.ui.component.account.LoginDialog
import io.zoemeow.dutschedule.ui.component.base.ButtonBase

@AndroidEntryPoint
class AccountActivity : BaseActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun OnMainView(padding: PaddingValues) {
        val loginDialogVisible = remember { mutableStateOf(false) }
        val loginDialogEnabled = remember { mutableStateOf(true) }

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
                        LoginBannerNotLoggedIn(
                            padding = PaddingValues(10.dp),
                            clicked = {
                                loginDialogVisible.value = true
                            },
                        )
                        AccountInfoBanner(
                            padding = PaddingValues(10.dp),
                            isLoading = false,
                            username = "102190147",
                            schoolClass = "19TCLC_DT3",
                            trainingProgramPlan = "123456789012345678901234567890"
                        )
                        ButtonBase(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
                            content = { Text("Subject schedule") },
                            horizontalArrangement = Arrangement.Start,
                            isOutlinedButton = true,
                        )
                        ButtonBase(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
                            content = { Text("Subject fee") },
                            horizontalArrangement = Arrangement.Start,
                            isOutlinedButton = true,
                        )
                        ButtonBase(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
                            content = { Text("Account information") },
                            horizontalArrangement = Arrangement.Start,
                            isOutlinedButton = true,
                        )
                        ButtonBase(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
                            content = { Text("Logout") },
                            horizontalArrangement = Arrangement.Start,
                            isOutlinedButton = true,
                        )
                    }
                )
            }
        )
        LoginDialog(
            isVisible = loginDialogVisible.value,
            controlEnabled = loginDialogEnabled.value,
            loginClicked = { _, _ ->
                loginDialogEnabled.value = false
                showSnackBar(
                    text = "Logging you in...",
                    clearPrevious = true,
                )
            },
            cancelRequested = {
                loginDialogVisible.value = false
            },
            canDismiss = false,
            dismissClicked = {
                loginDialogVisible.value = false
            }
        )
        BackHandler(
            enabled = loginDialogVisible.value,
            onBack = {
                if (loginDialogVisible.value) {
                    loginDialogVisible.value = false
                }
            }
        )
    }
}