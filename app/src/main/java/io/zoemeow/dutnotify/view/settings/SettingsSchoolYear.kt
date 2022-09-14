package io.zoemeow.dutnotify.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.model.account.SchoolYearItem
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.viewmodel.MainViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsSchoolYear(
    enabled: MutableState<Boolean>,
    mainViewModel: MainViewModel,
) {
    @Composable
    fun SchoolYearOption(
        title: String,
        value: Int,
        onValueChanged: (Int) -> Unit,
        minValue: Int? = null,
        maxValue: Int? = null,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Text(
                text = title,
            )
            Spacer(modifier = Modifier.size(5.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Button(
                    onClick = {
                        if (minValue != null && minValue < value)
                            onValueChanged(value - 1)
                    },
                    content = { Text("-", style = MaterialTheme.typography.bodyLarge) }
                )
                Spacer(modifier = Modifier.size(15.dp))
                Text("$value", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.size(15.dp))
                Button(
                    onClick = {
                        if (maxValue != null && maxValue > value)
                            onValueChanged(value + 1)
                    },
                    content = { Text("+", style = MaterialTheme.typography.bodyLarge) }
                )
            }
        }
    }

    val schoolYearOptionVal = remember { mutableStateOf(22) }
    val schoolSemesterOptionVal = remember { mutableStateOf(1) }

    LaunchedEffect(enabled.value) {
        schoolYearOptionVal.value = mainViewModel.appSettings.value.schoolYear.year
        schoolSemesterOptionVal.value = mainViewModel.appSettings.value.schoolYear.semester
    }

    fun commitChanges() {
        mainViewModel.appSettings.value = mainViewModel.appSettings.value.modify(
            optionToModify = AppSettings.ACCOUNT_SCHOOLYEAR,
            value = SchoolYearItem(
                year = schoolYearOptionVal.value,
                semester = schoolSemesterOptionVal.value,
            )
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
                Text(stringResource(id = R.string.settings_schoolyear_name))
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
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = String.format(
                            stringResource(id = R.string.settings_schoolyear_afterchange),
                            schoolYearOptionVal.value,
                            schoolYearOptionVal.value + 1,
                            schoolSemesterOptionVal.value
                        ),
                    )
                    Spacer(modifier = Modifier.size(15.dp))
                    SchoolYearOption(
                        title = stringResource(id = R.string.settings_schoolyear_schoolyearmodifyoption),
                        value = schoolYearOptionVal.value,
                        onValueChanged = {
                            schoolYearOptionVal.value = it
                        },
                        minValue = 10,
                        maxValue = 30
                    )
                    SchoolYearOption(
                        title = stringResource(id = R.string.settings_schoolyear_schoolyearmodifyoption),
                        value = schoolSemesterOptionVal.value,
                        onValueChanged = {
                            schoolSemesterOptionVal.value = it
                        },
                        minValue = 1,
                        maxValue = 3
                    )
                }
            }
        )
    }
}