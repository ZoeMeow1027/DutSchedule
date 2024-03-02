package io.zoemeow.dutschedule.ui.view.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.activity.MainActivity
import io.zoemeow.dutschedule.model.NotificationHistory
import io.zoemeow.dutschedule.model.ProcessState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivity.MainViewDashboard(
    snackBarHostState: SnackbarHostState,
    containerColor: Color,
    contentColor: Color,
    notificationList: List<NotificationHistory>,
    newsClicked: (() -> Unit)? = null,
    accountClicked: (() -> Unit)? = null,
    settingsClicked: (() -> Unit)? = null,
    externalLinkClicked: (() -> Unit)? = null,
    notificationClicked: (() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
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
                title = { Text(text = "DutSchedule") },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = BottomAppBarDefaults.containerColor.copy(
                    alpha = getControlBackgroundAlpha()
                ),
                actions = {
                    BadgedBox(
                        modifier = Modifier
                            .padding(start = 15.dp, end = 15.dp)
                            .clickable { newsClicked?.let { it() } },
                        badge = {
                            // Badge { }
                        },
                        content = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_newspaper_24),
                                "News",
                                modifier = Modifier.size(27.dp)
                            )
                        }
                    )
                    BadgedBox(
                        modifier = Modifier
                            .padding(end = 15.dp)
                            .clickable { settingsClicked?.let { it() } },
                        badge = {
                            // Badge { }
                        },
                        content = {
                            Icon(
                                Icons.Default.Settings,
                                "Settings",
                                modifier = Modifier.size(27.dp)
                            )
                        }
                    )
                    BadgedBox(
                        modifier = Modifier
                            .padding(end = 15.dp)
                            .clickable { externalLinkClicked?.let { it() } },
                        badge = {
                            // Badge { }
                        },
                        content = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_web_24),
                                "External links",
                                modifier = Modifier.size(27.dp)
                            )
                        }
                    )
                    BadgedBox(
                        modifier = Modifier.padding(end = 15.dp)
                            .clickable { notificationClicked?.let { it() } },
                        badge = {
                            if (notificationList.isNotEmpty()) {
                                Badge {
                                    Text(notificationList.size.toString())
                                }
                            }
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                "Notifications",
                                modifier = Modifier.size(27.dp),
                            )
                        }
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        text = {
                            Column(
                                modifier = Modifier.height(60.dp),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                                content = {
                                    Text(
                                        "Account",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    getMainViewModel().accountSession.value.let {
                                        Text(
                                            when (it.processState) {
                                                ProcessState.NotRunYet -> "Not logged in"
                                                ProcessState.Running -> "Fetching..."
//                                                ProcessState.Failed -> when (it.data.accountAuth.username == null) {
//                                                    true -> "Not logged in"
//                                                    false -> String.format(
//                                                        "%s (failed)",
//                                                        it.data.accountAuth.username
//                                                    )
//                                                }
                                                else -> it.data.accountAuth.username ?: "unknown"
                                            },
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            )
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (getMainViewModel().accountSession.value.processState == ProcessState.Failed) {
                                        Badge { Text("!") }
                                    }
                                },
                                content = {
                                    when (getMainViewModel().accountSession.value.processState) {
                                        ProcessState.Running -> CircularProgressIndicator(
                                            modifier = Modifier.size(26.dp),
                                            strokeWidth = 3.dp
                                        )
                                        else -> Icon(
                                            Icons.Outlined.AccountCircle,
                                            "Account",
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            )
                        },
                        onClick = { accountClicked?.let { it() } }
                    )
                }
            )
        },
        content = { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                color = Color.Transparent,
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        content = {
                            content?.let { it() }
                        },
                    )
                }
            )
        }
    )
}