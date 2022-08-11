package io.zoemeow.dutapp.android.view.settings

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import io.zoemeow.dutapp.android.viewmodel.GlobalViewModel
import io.zoemeow.dutapp.android.viewmodel.UIStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsBackgroundImage(
    enabled: MutableState<Boolean>,
    globalViewModel: GlobalViewModel,
    uiStatus: UIStatus,
) {
    val optionList = listOf("Unset", "From device wallpaper", "Specific a image")
    val selectedOptionList = remember { mutableStateOf("") }

    LaunchedEffect(enabled.value) {
        selectedOptionList.value = optionList[globalViewModel.backgroundImage.value.option.ordinal]
    }

    fun commitChanges() {
        globalViewModel.backgroundImage.value.option =
            BackgroundImageType.values()[optionList.indexOf(selectedOptionList.value)]
        Log.d("Log", "${globalViewModel.backgroundImage.value.option}")
        globalViewModel.requestSaveSettings()

        uiStatus.updateComposeUI()
        enabled.value = false

        // TODO: Find better solution instead of doing this here!!!
        globalViewModel.blackTheme.value = !globalViewModel.blackTheme.value
        globalViewModel.blackTheme.value = !globalViewModel.blackTheme.value


        CoroutineScope(Dispatchers.IO).launch {
            uiStatus.checkPermissionAndReloadAppBackground(
                type = globalViewModel.backgroundImage.value.option,
                onRequested = {
                    Log.d("Log", "${selectedOptionList.value}")
                    uiStatus.requestPermissionAppBackground()
                }
            )
        }
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
                Text("Select your background image")
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
                                    if (optionList.indexOf(option) <= 1) {
                                        selectedOptionList.value = option
                                        commitChanges()
                                    }
                                }
                        ) {
                            RadioButton(
                                selected = (option == selectedOptionList.value),
                                onClick = {
                                    if (optionList.indexOf(option) <= 1) {
                                        selectedOptionList.value = option
                                        commitChanges()
                                    }
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
                        Text(
                            "- Remember, this feature is still in beta, so it isn't working well yet.\n" +
                                    "- \"Specific a image\" option is temporary disabled due to not working yet."
                        )
                    }
                }
            }
        )
    }
}