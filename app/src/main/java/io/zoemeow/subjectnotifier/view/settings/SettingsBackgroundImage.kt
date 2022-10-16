package io.zoemeow.subjectnotifier.view.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.zoemeow.subjectnotifier.R
import io.zoemeow.subjectnotifier.model.appsettings.AppSettings
import io.zoemeow.subjectnotifier.model.appsettings.BackgroundImage
import io.zoemeow.subjectnotifier.model.enums.BackgroundImageType
import io.zoemeow.subjectnotifier.view.MainActivity
import io.zoemeow.subjectnotifier.view.PermissionRequestActivity
import io.zoemeow.subjectnotifier.viewmodel.MainViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsBackgroundImage(
    enabled: MutableState<Boolean>,
    mainViewModel: MainViewModel,
) {
    data class AvailableOptions(
        val index: Int,
        val name: String,
        val enabled: Boolean,
        val msgIfDisabled: String? = null,
    )

    val optionList: ArrayList<AvailableOptions> = arrayListOf(
        AvailableOptions(
            index = 0,
            name = stringResource(id = R.string.settings_backgroundimage_none),
            enabled = true,
        ),
        AvailableOptions(
            index = 1,
            name = stringResource(id = R.string.settings_backgroundimage_fromsystem),
            enabled = (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU),
            msgIfDisabled = stringResource(id = R.string.settings_backgroundimage_fromsystem_disabledreason)
        ),
        AvailableOptions(
            index = 2,
            name = stringResource(id = R.string.settings_backgroundimage_specific),
            enabled = false,
            msgIfDisabled = stringResource(id = R.string.settings_backgroundimage_specific_disabledreason),
        ),
    )
    val selectedOptionList = remember { mutableStateOf(optionList[0]) }
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

        if (mainViewModel.appSettings.value.backgroundImage.option != BackgroundImageType.Unset) {
            if (!PermissionRequestActivity.checkPermission(
                    context = activity,
                    permission = Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Intent(activity, PermissionRequestActivity::class.java)
                    .apply {
                        putExtra("permissions.list", arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                    }
                    .also {
                        activity.startActivity(it)
                    }
            } else activity.onPermissionResult(Manifest.permission.READ_EXTERNAL_STORAGE, true)
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
                        fun optionClick() {
                            if (option.enabled) {
                                when (option.index) {
                                    0, 1 -> {
                                        selectedOptionList.value = option
                                        commitChanges()
                                    }
                                    else -> {

                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .clickable { optionClick() },
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                RadioButton(
                                    selected = (option == selectedOptionList.value),
                                    onClick = { optionClick() }
                                )
                                Spacer(modifier = Modifier.size(5.dp))
                                Text(text = option.name)
                            }

                            if (!option.enabled) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                ) {
                                    RadioButton(
                                        modifier = Modifier.alpha(0f),
                                        selected = false,
                                        onClick = { }
                                    )
                                    Spacer(modifier = Modifier.size(5.dp))
                                    Text(text = "${option.msgIfDisabled}")
                                }
                            }

                            Spacer(modifier = Modifier.size(5.dp))
                        }
                    }
                }
            }
        )
    }
}