package io.zoemeow.dutnotify.view.settings

import android.Manifest
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.zoemeow.dutnotify.MainActivity
import io.zoemeow.dutnotify.PermissionRequestActivity
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.model.appsettings.BackgroundImage
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.enums.BackgroundImageType
import io.zoemeow.dutnotify.onPermissionResult
import io.zoemeow.dutnotify.viewmodel.MainViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsBackgroundImage(
    enabled: MutableState<Boolean>,
    mainViewModel: MainViewModel,
) {
    val optionList = listOf(
        stringResource(id = R.string.settings_backgroundimage_none),
        stringResource(id = R.string.settings_backgroundimage_fromsystem),
        stringResource(id = R.string.settings_backgroundimage_specific),
    )
    val selectedOptionList = remember { mutableStateOf("") }
    val activity = (LocalContext.current) as MainActivity

    LaunchedEffect(enabled.value) {
        selectedOptionList.value =
            optionList[mainViewModel.appSettings.value.backgroundImage.option.ordinal]
    }

    fun commitChanges() {
        mainViewModel.appSettings.value = mainViewModel.appSettings.value.modify(
            optionToModify = AppSettings.APPEARANCE_BACKGROUNDIMAGE,
            value = BackgroundImage(
                option = BackgroundImageType.values()[optionList.indexOf(selectedOptionList.value)],
                path = null
            )
        )
        mainViewModel.requestSaveChanges()

        if (PermissionRequestActivity.checkPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            activity.onPermissionResult(Manifest.permission.READ_EXTERNAL_STORAGE, true)
        } else {
            Intent(activity, PermissionRequestActivity::class.java)
                .apply {
                    putExtra("permissions.list", arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                }
                .also {
                    activity.startActivity(it)
                }
        }

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
                Text(stringResource(id = R.string.settings_backgroundimage_name))
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_info_24),
                            contentDescription = "info_icon",
                            tint = if (mainViewModel.mainActivityIsDarkTheme.value) Color.White else Color.Black
                        )
                        Text(stringResource(id = R.string.settings_backgroundimage_note))
                    }
                }
            }
        )
    }
}