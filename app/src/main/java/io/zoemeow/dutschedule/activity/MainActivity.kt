package io.zoemeow.dutschedule.activity

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.ui.component.base.ButtonBase
import io.zoemeow.dutschedule.ui.component.main.SummaryItem
import io.zoemeow.dutschedule.ui.theme.DutScheduleTheme

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    @Composable
    override fun OnMainView(padding: PaddingValues) {
        // A surface container using the 'background' color from the theme
        val context = LocalContext.current
        MainView(
            newsClicked = {
                this.startActivity(Intent(this, NewsActivity::class.java))
            },
            accountClicked = {
                this.startActivity(Intent(this, AccountActivity::class.java))
            },
            settingsClicked = {
                this.startActivity(Intent(this, SettingsActivity::class.java))
            },
            content = {
                SummaryItem(
                    padding = PaddingValues(15.dp),
                    title = "Today schedule",
                    content = "You have already done your subjects today! Good job!",
                    clicked = { },
                )
                SummaryItem(
                    padding = PaddingValues(bottom = 15.dp, start = 15.dp, end = 15.dp),
                    title = "Affected lessons by announcement",
                    content = "ie1i0921d - i029di12\n" +
                            "ie1i0921d - i029di12\n" +
                            "ie1i0921d - i029di12\n" +
                            "ie1i0921d - i029di12\n" +
                            "ie1i0921d - i029di12",
                    clicked = {},
                )
                SummaryItem(
                    padding = PaddingValues(bottom = 15.dp, start = 15.dp, end = 15.dp),
                    title = "News from sv.dut.udn.vn",
                    content = "Tap here to open news.\n\n" +
                            "7 new announcements today\n" +
                            "40 announcements on 7 days before",
                    clicked = {},
                )
                SummaryItem(
                    padding = PaddingValues(bottom = 15.dp, start = 15.dp, end = 15.dp),
                    title = "Update available",
                    content = "Tap here to update with Google Play Store.\n" +
                            "Latest version: 1.0",
                    clicked = {},
                )
                SummaryItem(
                    padding = PaddingValues(bottom = 15.dp, start = 15.dp, end = 15.dp),
                    title = "Missing some permissions",
                    content = "Some features won't work correctly if you don't grant these permissions:\n\n" +
                            "android.permissions.fewjfewojf\n" +
                            "android.permissions.fewjfewojf\n" +
                            "android.permissions.fewjfewojf",
                    clicked = {
                        context.startActivity(Intent(context, PermissionRequestActivity::class.java))
                    },
                )
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainView(
        newsClicked: (() -> Unit)? = null,
        accountClicked: (() -> Unit)? = null,
        settingsClicked: (() -> Unit)? = null,
        content: (@Composable ColumnScope.() -> Unit)? = null,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(title = { Text(text = "DutSchedule") })
            },
            bottomBar = {
                BottomAppBar(
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            content = {
                                ButtonBase(
                                    clicked = {
                                        newsClicked?.let { it() }
                                    },
                                    content = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_newspaper_24),
                                            "News",
                                            modifier = Modifier.size(30.dp).padding(end = 7.dp),
                                        )
                                        Text("News")
                                    }
                                )
                                ButtonBase(
                                    modifier = Modifier.padding(start = 10.dp),
                                    clicked = {
                                        accountClicked?.let {
                                            it()
                                        }
                                    },
                                    content = {
                                        Icon(
                                            Icons.Outlined.AccountCircle,
                                            "",
                                            modifier = Modifier.size(30.dp).padding(end = 7.dp),
                                        )
                                        Text("Account")
                                    }
                                )
                                ButtonBase(
                                    modifier = Modifier.padding(start = 10.dp),
                                    clicked = {
                                        settingsClicked?.let {
                                            it()
                                        }
                                    },
                                    content = {
                                        Icon(
                                            Icons.Default.Settings,
                                            "",
                                            modifier = Modifier.size(30.dp).padding(end = 7.dp),
                                        )
                                        Text("Settings")
                                    }
                                )
                            }
                        )
                    },
                )
            },
            content = { padding ->
                Surface(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    color = Color.Transparent,
                    content = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            content = {
                                content?.let { it() }
                            },
                        )
                    }
                )
            }
        )
    }

    @Preview(showBackground = true)
    @Composable
    private fun SummaryItemPreview() {
        DutScheduleTheme {
            MainView()
        }
    }
}