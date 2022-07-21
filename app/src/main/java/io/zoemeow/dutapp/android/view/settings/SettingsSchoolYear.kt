package io.zoemeow.dutapp.android.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.zoemeow.dutapp.android.viewmodel.GlobalViewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsSchoolYear(
    enabled: MutableState<Boolean>
) {
    val globalViewModel = GlobalViewModel.getInstance()

    val dropDownListSchoolYear = remember { mutableStateOf(false) }
    val dropDownListSemester = remember { mutableStateOf(false) }

    val schoolYearOption = listOf(
        "17","18","19","20","21","22","23","24"
    )
    val schoolYearOptionChose = remember { mutableStateOf(
        schoolYearOption[schoolYearOption.indexOf(globalViewModel.settings.schoolYear.year.toString())]
    ) }
    val semesterOption = listOf(
        "1", "2", "3"
    )
    val semesterOptionChose = remember { mutableStateOf(
        semesterOption[semesterOption.indexOf(globalViewModel.settings.schoolYear.semester.toString())]
    ) }

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
                    // enabled = enabledControl.value,
                    onClick = {
                        enabled.value = false
                    },
                    content = {
                        Text("Cancel")
                    }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = true,
                    onClick = {
                        globalViewModel.settings.schoolYear.year = 17 + schoolYearOption.indexOf(schoolYearOptionChose.value)
                        globalViewModel.settings.schoolYear.semester = 1 + semesterOption.indexOf(semesterOptionChose.value)
                        enabled.value = false
                    },
                    content = {
                        Text("Confirm")
                    }
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                ) {
                    ExposedDropdownMenuBox(
                        expanded = dropDownListSchoolYear.value,
                        onExpandedChange = { dropDownListSchoolYear.value = it },
                        content = {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                value = schoolYearOptionChose.value,
                                onValueChange = {},
                                label = { Text("School year") },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            )
                            ExposedDropdownMenu(
                                expanded = dropDownListSchoolYear.value,
                                onDismissRequest = { dropDownListSchoolYear.value = false },
                            ) {
                                schoolYearOption.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            schoolYearOptionChose.value = selectionOption
                                            dropDownListSchoolYear.value = false
                                        }
                                    )
                                }
                            }
                        }
                    )
                    ExposedDropdownMenuBox(
                        expanded = dropDownListSemester.value,
                        onExpandedChange = { dropDownListSemester.value = it },
                        content = {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                value = semesterOptionChose.value,
                                onValueChange = {},
                                label = { Text("Semester") },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            )
                            ExposedDropdownMenu(
                                expanded = dropDownListSemester.value,
                                onDismissRequest = { dropDownListSemester.value = false },
                            ) {
                                semesterOption.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            semesterOptionChose.value = selectionOption
                                            dropDownListSemester.value = false
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        )
    }
}