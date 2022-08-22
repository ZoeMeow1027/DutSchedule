package io.zoemeow.dutapp.android.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.viewmodel.MainViewModel

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

    val schoolYearOptionVal = remember { mutableStateOf(21) }
    val schoolSemesterOptionVal = remember { mutableStateOf(3) }

    LaunchedEffect(enabled.value) {
        schoolYearOptionVal.value = mainViewModel.settings.schoolYear.value.year
        schoolSemesterOptionVal.value = mainViewModel.settings.schoolYear.value.semester
    }

    fun commitChanges() {
        mainViewModel.settings.schoolYear.value.year = schoolYearOptionVal.value
        mainViewModel.settings.schoolYear.value.semester = schoolSemesterOptionVal.value
        mainViewModel.requestSaveChanges()

        mainViewModel.uiStatus.updateComposeUI()
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
                Text("School year settings")
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
                        text = "Your settings after confirm:\n" +
                                "- Current school year: 20${schoolYearOptionVal.value}-" +
                                "20${schoolYearOptionVal.value + 1}\n" +
                                "- Current semester: ${
                                    if (schoolSemesterOptionVal.value < 3)
                                        schoolSemesterOptionVal.value
                                    else
                                        "3 (in summer)"
                                }",
                    )
                    Spacer(modifier = Modifier.size(15.dp))
                    SchoolYearOption(
                        title = "School Year",
                        value = schoolYearOptionVal.value,
                        onValueChanged = {
                            schoolYearOptionVal.value = it
                        },
                        minValue = 10,
                        maxValue = 30
                    )
                    SchoolYearOption(
                        title = "School Semester",
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