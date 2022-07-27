package io.zoemeow.dutapp.android.view.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.model.enums.AppTheme
import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import io.zoemeow.dutapp.android.viewmodel.GlobalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsBackgroundImage(
    enabled: MutableState<Boolean>,
    globalViewModel: GlobalViewModel
) {
    val optionList = listOf("Unset", "From device wallpaper", "Specific a image")
    val selectedOptionList = remember { mutableStateOf("") }

    LaunchedEffect(enabled.value) {
        selectedOptionList.value = optionList[globalViewModel.backgroundImage.value.option.ordinal]
    }

    fun commitChanges() {
        globalViewModel.backgroundImage.value.option = BackgroundImageType.values()[optionList.indexOf(selectedOptionList.value)]

        globalViewModel.loadBackground()
        globalViewModel.requestSaveSettings()
        globalViewModel.update()
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
                        Image(
                            imageVector = ImageVector.vectorResource(
                                id = if (
                                    (globalViewModel.appTheme.value == AppTheme.FollowSystem && isSystemInDarkTheme()) ||
                                    globalViewModel.appTheme.value == AppTheme.DarkMode
                                )
                                    R.drawable.ic_baseline_info_white_24
                                else R.drawable.ic_baseline_info_black_24
                            ),
                            contentDescription = "info_icon",
                        )
                        Text(""""Specific a image" option is temporary disabled due to not working yet.""")
                    }
                }
            }
        )
    }
}