package io.zoemeow.dutnotify.view.settings

import android.os.Build
import androidx.compose.foundation.clickable
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
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.model.enums.AppSettingsCode
import io.zoemeow.dutnotify.model.enums.AppTheme
import io.zoemeow.dutnotify.viewmodel.MainViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsAppTheme(
    enabled: MutableState<Boolean>,
    mainViewModel: MainViewModel,
) {
    val themeList = listOf(
        stringResource(id = R.string.settings_apptheme_followsystem),
        stringResource(id = R.string.settings_apptheme_dark),
        stringResource(id = R.string.settings_apptheme_light),
    )
    val selectedThemeList = remember { mutableStateOf("") }
    val dynamicColorEnabled = remember { mutableStateOf(true) }

    LaunchedEffect(enabled.value) {
        selectedThemeList.value = themeList[mainViewModel.appSettings.value.appTheme.ordinal]
        dynamicColorEnabled.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            mainViewModel.appSettings.value.dynamicColorEnabled else false
    }

    fun commitChanges() {
        mainViewModel.appSettings.value = mainViewModel.appSettings.value.modify(
            optionToModify = AppSettingsCode.AppTheme,
            value = AppTheme.values()[themeList.indexOf(selectedThemeList.value)]
        ).modify(
            optionToModify = AppSettingsCode.DynamicColorEnabled,
            value = dynamicColorEnabled.value
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
                Text(stringResource(id = R.string.settings_apptheme_name))
            },
            confirmButton = {
                TextButton(
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
                    horizontalAlignment = Alignment.Start
                ) {
                    themeList.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && option == themeList[0]) ||
                                        option != themeList[0]
                                    )
                                        selectedThemeList.value = option
                                }
                        ) {
                            RadioButton(
                                selected = (option == selectedThemeList.value),
                                onClick = {
                                    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && option == themeList[0]) ||
                                        option != themeList[0]
                                    )
                                        selectedThemeList.value = option
                                }
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(text = option)
                        }
                    }
                    Spacer(modifier = Modifier.size(5.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                                    dynamicColorEnabled.value = !dynamicColorEnabled.value
                            }
                    ) {
                        Checkbox(
                            checked = dynamicColorEnabled.value,
                            onCheckedChange = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                                    dynamicColorEnabled.value = it
                            },
                        )
                        Text(stringResource(id = R.string.settings_apptheme_dynamiccolor_enable))
                    }
                    Spacer(modifier = Modifier.size(15.dp))
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_info_24),
                            contentDescription = "info_icon",
                            tint = if (mainViewModel.mainActivityIsDarkTheme.value) Color.White else Color.Black
                        )
                        Text(stringResource(id = R.string.settings_apptheme_note))
                    }
                }
            }
        )
    }
}