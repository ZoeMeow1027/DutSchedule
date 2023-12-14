package io.zoemeow.dutschedule.ui.component.settings.newsfilter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.ui.component.base.ExpandableContent
import io.zoemeow.dutschedule.ui.component.base.ExpandableContentDefaultTitle

@Composable
fun NewsFilterAddManually(
    expanded: Boolean = false,
    onExpanded: (() -> Unit)? = null,
    onSubmit: ((String, String, String) -> Unit)? = null,
    opacity: Float = 1.0f
) {
    val studentYearId = remember { mutableStateOf("") }
    val classId = remember { mutableStateOf("") }
    val subjectName = remember { mutableStateOf("") }

    ExpandableContent(
        opacity = opacity,
        title = {
            ExpandableContentDefaultTitle(title = "Add filter manually")
        },
        isTitleCentered = true,
        onTitleClicked = onExpanded,
        content = {
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
                        value = studentYearId.value,
                        onValueChange = { if (it.length <= 2) studentYearId.value = it },
                        label = { Text("First value") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .weight(0.5f)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    OutlinedTextField(
                        value = classId.value,
                        onValueChange = { if (it.length <= 3) classId.value = it },
                        label = { Text("Second value") },
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
                Spacer(modifier = Modifier.size(10.dp))
                Button(
                    content = { Text("Add") },
                    onClick = {
                        if (studentYearId.value.length >= 2 && classId.value.length >= 2 && subjectName.value.length >= 3) {
                            onSubmit?.let {
                                it(
                                    studentYearId.value,
                                    classId.value,
                                    subjectName.value
                                )
                            }
                        }
                    },
                )
            }
        },
        isContentVisible = expanded
    )
}