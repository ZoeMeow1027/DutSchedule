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
import io.zoemeow.dutschedule.ui.component.base.ContentExpandable

@Composable
fun NewsFilterAddManually(
    expanded: Boolean = false,
    onExpanded: (() -> Unit)? = null,
) {
    val studentYearId = remember { mutableStateOf("") }
    val classId = remember { mutableStateOf("") }

    NewsFilterSurface {
        ContentExpandable(
            title = "Add manually",
            expanded = expanded,
            onExpanded = { onExpanded?.let { it() } },
            content = {
                Text(
                    text = "Enter your subject filter manually here",
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
//                    Spacer(modifier = Modifier.size(5.dp))
//                    OutlinedTextField(
//                        value = subjectName.value,
//                        onValueChange = { subjectName.value = it },
//                        label = { Text(stringResource(id = R.string.subjectnewsfilter_addmanually_subjecttextbox)) },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .wrapContentHeight()
//                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Button(
                        content = { Text("Add") },
                        onClick = {
//                            val item = SubjectCode(
//                                studentYearId.value,
//                                classId.value,
//                                subjectName.value
//                            )
//
//                            if (!isDuplicate(item))
//                                newsFilterViewModel.selectedSubjects.add(item)
//                            newsFilterViewModel.modifiedSettings.value = true
//                            updateTemporarySettings()
//
//                            showSnackBarMessage(
//                                String.format(
//                                    getString(R.string.subjectnewsfilter_snackbar_added),
//                                    item.name
//                                )
//                            )
                        },
                    )
                }
            }
        )
    }
}