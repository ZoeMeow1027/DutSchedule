package io.zoemeow.dutschedule.ui.view.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.activity.MainActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivity.MainViewTabbed(
    snackBarHostState: SnackbarHostState,
    containerColor: Color,
    contentColor: Color,
) {
    val selectedTab = remember { mutableStateOf("News") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        containerColor = containerColor,
        contentColor = contentColor,
        topBar = {
            TopAppBar(
                title = { Text("DutSchedule") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = containerColor,
                contentColor = contentColor,
                content = {
                    mapOf(
                        "News" to painterResource(id = R.drawable.ic_baseline_newspaper_24),
                        "Account" to Icons.Default.AccountCircle,
                        "Settings" to Icons.Default.Settings
                    ).forEach(
                        action = {
                            when (it.value) {
                                is Painter -> {
                                    NavigationBarItem(
                                        selected = selectedTab.value == it.key,
                                        onClick = { selectedTab.value = it.key },
                                        icon = {
                                            Icon(painter = it.value as Painter, it.key)
                                        },
                                        label = { Text(it.key) }
                                    )
                                }
                                is ImageVector -> {
                                    NavigationBarItem(
                                        selected = selectedTab.value == it.key,
                                        onClick = { selectedTab.value = it.key },
                                        icon = {
                                            Icon(imageVector = it.value as ImageVector, it.key)
                                        },
                                        label = { Text(it.key) }
                                    )
                                }
                                else -> { }
                            }
                        }
                    )
                }
            )
        },
        content = {
            // TODO: Avoid error, must be changed
            val d = it
        }
    )
}