package io.zoemeow.dutapp.android.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.model.enums.AppSettingsCode
import io.zoemeow.dutapp.android.viewmodel.MainViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsRefreshNewsInterval(
    enabled: MutableState<Boolean>,
    mainViewModel: MainViewModel,
) {
    val newValue = remember { mutableStateOf(3) }

    LaunchedEffect(enabled.value) {
        newValue.value = mainViewModel.settings.value.refreshNewsIntervalInMinute
    }

    fun commitChanges() {
        // TODO: Reload service here!
        mainViewModel.settings.value = mainViewModel.settings.value.modify(
            AppSettingsCode.RefreshNewsInterval, newValue.value
        )
        mainViewModel.requestSaveChanges()
        enabled.value = false
    }

    if (enabled.value) {
        AlertDialog(
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            onDismissRequest = {
                enabled.value = false
            },
            title = {
                Text("Refresh news interval")
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        enabled.value = false
                    },
                    content = {
                        Text(stringResource(id = R.string.option_cancel))
                    }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = true,
                    onClick = {
                        commitChanges()
                    },
                    content = {
                        Text(stringResource(id = R.string.option_ok))
                    }
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Text("${newValue.value} minute${if (newValue.value > 1) "s" else ""}")
                    Slider(
                        value = newValue.value.toFloat(),
                        onValueChange = {
                            newValue.value = it.toInt()
                        },
                        valueRange = (1).toFloat()..(30).toFloat(),
                        steps = 30 - 1,
                        colors = SliderDefaults.colors(
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent,
                        )
                    )
                    Spacer(modifier = Modifier.size(15.dp))
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_info_24),
                            contentDescription = "info_icon",
                            tint = if (mainViewModel.uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black
                        )
                        Text("This will change interval while checking news (like time break). Still in alpha.")
                    }
                }
            }
        )
    }
}