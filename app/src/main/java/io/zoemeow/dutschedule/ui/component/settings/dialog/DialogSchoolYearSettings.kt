package io.zoemeow.dutschedule.ui.component.settings.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.activity.SettingsActivity
import io.zoemeow.dutschedule.model.account.SchoolYearItem
import io.zoemeow.dutschedule.ui.component.base.DialogBase
import io.zoemeow.dutschedule.ui.component.base.OutlinedTextBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsActivity.DialogSchoolYearSettings(
    isVisible: Boolean = false,
    dismissRequested: (() -> Unit)? = null,
    currentSchoolYearItem: SchoolYearItem,
    onSubmit: ((SchoolYearItem) -> Unit)? = null
) {
    val currentSettings = remember { mutableStateOf(SchoolYearItem()) }
    val dropDownSchoolYear = remember { mutableStateOf(false) }
    val dropDownSemester = remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        currentSettings.value = currentSchoolYearItem
        dropDownSchoolYear.value = false
        dropDownSemester.value = false
    }

    DialogBase(
        modifier = Modifier
            .fillMaxWidth()
            .padding(25.dp),
        title = "School year settings",
        isVisible = isVisible,
        canDismiss = false,
        isTitleCentered = true,
        dismissClicked = {
            dismissRequested?.let { it() }
        },
        content = {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Edit your value below to adjust school year variable (careful when changing settings here)",
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = dropDownSchoolYear.value,
                    onExpandedChange = { dropDownSchoolYear.value = !dropDownSchoolYear.value },
                    content = {
                        OutlinedTextBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            title = "School year",
                            value = String.format("20%d-20%d", currentSettings.value.year, currentSettings.value.year+1)
                        )
                        DropdownMenu(
                            expanded = dropDownSchoolYear.value,
                            onDismissRequest = { dropDownSchoolYear.value = false },
                            content = {
                                23.downTo(10).forEach {
                                    DropdownMenuItem(
                                        text = { Text(String.format("20%2d-20%2d", it, it+1)) },
                                        onClick = {
                                            currentSettings.value = currentSettings.value.clone(
                                                year = it
                                            )
                                            dropDownSchoolYear.value = false
                                        }
                                    )
                                }
                            }
                        )
                    }
                )
                ExposedDropdownMenuBox(
                    expanded = dropDownSemester.value,
                    onExpandedChange = { dropDownSemester.value = !dropDownSemester.value },
                    content = {
                        OutlinedTextBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            title = "Semester",
                            value = String.format(
                                "Semester %d%s",
                                if (currentSettings.value.semester <= 2) currentSettings.value.semester else 2,
                                if (currentSettings.value.semester > 2) " (in summer)" else ""
                            )
                        )
                        DropdownMenu(
                            expanded = dropDownSemester.value,
                            onDismissRequest = { dropDownSemester.value = false },
                            content = {
                                1.rangeTo(3).forEach {
                                    DropdownMenuItem(
                                        text = { Text(String.format(
                                            "Semester %d%s",
                                            if (it <= 2) it else 2,
                                            if (it > 2) " (in summer)" else ""
                                        )) },
                                        onClick = {
                                            currentSettings.value = currentSettings.value.clone(
                                                semester = it
                                            )
                                            dropDownSemester.value = false
                                        }
                                    )
                                }
                            }
                        )
                    }
                )
            }
        },
        actionButtons = {
            TextButton(
                onClick = { onSubmit?.let { it(currentSettings.value) } },
                content = { Text("Save") },
                modifier = Modifier.padding(start = 8.dp),
            )
            TextButton(
                onClick = { dismissRequested?.let { it() } },
                content = { Text("Cancel") },
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    )
}