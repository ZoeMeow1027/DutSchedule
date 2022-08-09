package io.zoemeow.dutapp.android.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
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
        "Built-in browser",
        "Default browser custom tab",
        "External browser"
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
                Text("When you click a link, open it in")
            },
            confirmButton = {

            },
            dismissButton = {
                TextButton(
                    onClick = {
                        enabled.value = false
                    },
                    content = {
                        Text("Cancel")
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
                }
            }
        )
    }
}