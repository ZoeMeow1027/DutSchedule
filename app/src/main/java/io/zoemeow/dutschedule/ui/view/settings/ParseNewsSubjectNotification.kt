package io.zoemeow.dutschedule.ui.view.settings

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.activity.SettingsActivity
import io.zoemeow.dutschedule.ui.component.base.SwitchWithTextInSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsActivity.ParseNewsSubjectNotification(
    snackBarHostState: SnackbarHostState,
    containerColor: Color,
    contentColor: Color
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        containerColor = containerColor,
        contentColor = contentColor,
        topBar = {
            TopAppBar(
                title = { Text("New parse method on notification") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
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
                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .padding(it)
                    .verticalScroll(rememberScrollState()),
                content = {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 5.dp),
                        shape = RoundedCornerShape(30.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(
                            alpha = getControlBackgroundAlpha()
                        ),
                        content = {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp),
                                content = {
                                    Text(
                                        "New Making up lesson in a subject",
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(bottom = 5.dp)
                                    )
                                    Text(
                                        when (getMainViewModel().appSettings.value.newsBackgroundParseNewsSubject) {
                                            true -> "Lecturer: ...\nOn ... at lesson(s) ...\nRoom will make up: ..."
                                            false -> "Person messaged: Class will MAKED UP at lesson 1-4, date: dd/MM/yyyy, at room A123"
                                        }
                                    )
                                }
                            )
                        }
                    )
                    SwitchWithTextInSurface(
                        text = "Use this feature",
                        enabled = true,
                        checked = getMainViewModel().appSettings.value.newsBackgroundParseNewsSubject,
                        onCheckedChange = {
                            getMainViewModel().appSettings.value = getMainViewModel().appSettings.value.clone(
                                newsBackgroundParseNewsSubject = !getMainViewModel().appSettings.value.newsBackgroundParseNewsSubject
                            )
                            getMainViewModel().saveSettings()
                        }
                    )
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp),
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_info_24),
                            contentDescription = "info_icon",
                            modifier = Modifier.size(24.dp),
                        )
                        Text("Use the new parser for news subject if supported. Turned off or unsupported news subject won't affected.")
                    }
                }
            )
        }
    )
}