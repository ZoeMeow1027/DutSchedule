package io.zoemeow.dutschedule.ui.component.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.activity.SettingsActivity
import io.zoemeow.dutschedule.ui.component.base.DialogBase

@Composable
fun SettingsActivity.AddNewSubjectFilterDialog(
    isVisible: Boolean = false,
    onDismiss: (() -> Unit)? = null,
    onDone: ((String, String, String) -> Unit)? = null
) {
    val schoolYearId = remember { mutableStateOf("") }
    val classId = remember { mutableStateOf("") }
    val subjectName = remember { mutableStateOf("") }

    DialogBase(
        modifier = Modifier.fillMaxWidth().padding(25.dp),
        title = "Add new filter",
        isVisible = isVisible,
        canDismiss = false,
        dismissClicked = { onDismiss?.let { it() } },
        content = {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Enter your subject filter (you can view templates in sv.dut.udn.vn) and tap \"Add\" to add to filter above.\n\nExample:\n - 19 | 01 | Subject A\n - xx | 94A | Subject B\n\nNote:\n- You need to enter carefully, otherwise you won\'t received notifications exactly.",
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = 10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = schoolYearId.value,
                            onValueChange = { if (it.length <= 2) schoolYearId.value = it },
                            label = { Text("School year ID") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .weight(0.5f)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        OutlinedTextField(
                            value = classId.value,
                            onValueChange = { if (it.length <= 3) classId.value = it },
                            label = { Text("Class ID") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .weight(0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.size(5.dp))
                    OutlinedTextField(
                        value = subjectName.value,
                        onValueChange = { subjectName.value = it },
                        label = { Text("Subject name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        actionButtons = {
            TextButton(
                onClick = { onDismiss?.let { it() } },
                content = { Text("Cancel") },
                modifier = Modifier.padding(start = 8.dp),
            )
            TextButton(
                onClick = { onDone?.let { it(schoolYearId.value, classId.value,subjectName.value) } },
                content = { Text("Save") },
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    )
}