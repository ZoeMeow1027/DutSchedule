package io.zoemeow.dutschedule.ui.component.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.activity.SettingsActivity
import io.zoemeow.dutschedule.model.settings.SubjectCode
import io.zoemeow.dutschedule.ui.component.base.DialogBase

@Composable
fun SettingsActivity.DeleteASubjectFilterDialog(
    subjectCode: SubjectCode = SubjectCode("", "", ""),
    isVisible: Boolean = false,
    onDismiss: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null
) {
    DialogBase(
        modifier = Modifier
            .fillMaxWidth()
            .padding(25.dp),
        title = "Delete subject filter?",
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
                    text = String.format(
                        "%s\n%s\n\n%s",
                        "Are you sure you want to delete this filter?",
                        String.format("%s [%s.Nh%s]", subjectCode.subjectName, subjectCode.studentYearId, subjectCode.classId),
                        "This action is undone!"
                    ),
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }
        },
        actionButtons = {
            TextButton(
                onClick = { onDismiss?.let { it() } },
                content = { Text("No, take me back") },
                modifier = Modifier.padding(start = 8.dp),
            )
            ElevatedButton(
                colors = ButtonDefaults.elevatedButtonColors().copy(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                onClick = { onDone?.let { it() } },
                content = { Text("Yes, delete it") },
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    )
}