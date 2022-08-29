package io.zoemeow.dutapp.android.view.settings

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.zoemeow.dutapp.android.MainActivity
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.model.appsettings.BackgroundImage
import io.zoemeow.dutapp.android.model.enums.AppSettingsCode
import io.zoemeow.dutapp.android.model.enums.BackgroundImageType
import io.zoemeow.dutapp.android.view.activities.PermissionRequestActivity
import io.zoemeow.dutapp.android.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    LaunchedEffect(enabled.value) {
        selectedOptionList.value = optionList[mainViewModel.settings.value.backgroundImage.option.ordinal]
    }

    fun commitChanges() {
        mainViewModel.settings.value = mainViewModel.settings.value.modify(
            optionToModify = AppSettingsCode.BackgroundImage,
            value = BackgroundImage(
                option = BackgroundImageType.values()[optionList.indexOf(selectedOptionList.value)],
                path = null
            )
        )
        mainViewModel.requestSaveChanges()

        if (mainViewModel.settings.value.backgroundImage.option != BackgroundImageType.Unset &&
            !PermissionRequestActivity.checkPermission(
                mainViewModel.uiStatus.pMainActivity.value!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            val intent = Intent(
                mainViewModel.uiStatus.pMainActivity.value!!,
                PermissionRequestActivity::class.java
            )
            intent.putExtra("permission.requested", arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            (mainViewModel.uiStatus.pMainActivity.value!! as MainActivity).permissionRequestActivityResult.launch(intent)
        }

        // TODO: Find better solution instead of doing this here!!!
        mainViewModel.settings.value.blackThemeEnabled = !mainViewModel.settings.value.blackThemeEnabled
        mainViewModel.settings.value.blackThemeEnabled = !mainViewModel.settings.value.blackThemeEnabled

        // mainViewModel.uiStatus.updateComposeUI()
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
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_info_24),
                            contentDescription = "info_icon",
                            tint = if (mainViewModel.uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black
                        )
                        Text(stringResource(id = R.string.settings_backgroundimage_note))
                    }
                }
            }
        )
    }
}