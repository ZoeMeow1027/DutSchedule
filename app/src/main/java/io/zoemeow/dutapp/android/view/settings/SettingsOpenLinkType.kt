package io.zoemeow.dutapp.android.view.settings

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
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.model.enums.OpenLinkType
import io.zoemeow.dutapp.android.viewmodel.GlobalViewModel
import io.zoemeow.dutapp.android.viewmodel.UIStatus

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsOpenLinkType(
    enabled: MutableState<Boolean>,
    globalViewModel: GlobalViewModel,
    uiStatus: UIStatus
) {
    val optionList = listOf(
        stringResource(id = R.string.settings_openlinkin_builtinbrowser),
        stringResource(id = R.string.settings_openlinkin_customtab),
        stringResource(id = R.string.settings_openlinkin_external),
    )
    val selectedOptionList = remember { mutableStateOf(0) }

    LaunchedEffect(enabled.value) {
        selectedOptionList.value = globalViewModel.openLinkType.value.ordinal
    }

    fun commitChanges() {
        globalViewModel.openLinkType.value = OpenLinkType.values()[selectedOptionList.value]
        globalViewModel.requestSaveSettings()

        uiStatus.updateComposeUI()
        enabled.value = false

        // TODO: Find better solution instead of doing this here!!!
        globalViewModel.blackTheme.value = !globalViewModel.blackTheme.value
        globalViewModel.blackTheme.value = !globalViewModel.blackTheme.value
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
                Text(stringResource(id = R.string.settings_openlinkin_name))
            },
            confirmButton = {

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
            text = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    optionList.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedOptionList.value = optionList.indexOf(option)
                                    commitChanges()
                                }
                        ) {
                            RadioButton(
                                selected = (optionList.indexOf(option) == selectedOptionList.value),
                                onClick = {
                                    selectedOptionList.value = optionList.indexOf(option)
                                    commitChanges()
                                }
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(text = option)
                        }
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
                            tint = if (uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black
                        )
                        Text(stringResource(id = R.string.settings_openlinkin_description))
                    }
                }
            }
        )
    }
}