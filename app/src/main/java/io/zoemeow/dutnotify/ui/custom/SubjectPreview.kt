package io.zoemeow.dutnotify.ui.custom

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.zoemeow.dutapi.objects.accounts.SubjectScheduleItem
import io.zoemeow.dutnotify.util.dateToString
import io.zoemeow.dutnotify.util.getDayOfWeekToString

class SubjectPreview {
    companion object {
        private val paddingPerItem: PaddingValues = PaddingValues(bottom = 5.dp)

        @OptIn(ExperimentalComposeUiApi::class)
        @Composable
        fun SubjectScheduleDetails(
            dialogEnabled: Boolean,
            item: SubjectScheduleItem?,
            onClose: () -> Unit,
            darkTheme: Boolean = false,
        ) {
            if (dialogEnabled && item != null) {
                AlertDialog(
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false
                    ),
                    onDismissRequest = { onClose() },
                    modifier = Modifier
                        .padding(15.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    title = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(item.name ?: "(null)")
                            Text("by ${item.lecturer ?: "(null)"}")
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { onClose() },
                            content = { Text("OK") }
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start,
                        ) {
                            CustomText("ID: ${item.id.toString(false)}")
                            CustomText("Credit: ${item.credit}")
                            CustomText("Is high quality: ${item.isHighQuality}")
                            CustomText("Final score formula: ${item.pointFormula}")
                            // Subject study
                            Spacer(modifier = Modifier.size(15.dp))
                            ContentInBoxWithBorder(
                                title = "Schedule Study",
                                content = {
                                    var schList = ""
                                    for (schStudyItem in item.subjectStudy.scheduleList) {
                                        schList += (if (schList.isNotEmpty()) "; " else "") +
                                                "${getDayOfWeekToString(schStudyItem.dayOfWeek)}," +
                                                "${schStudyItem.lesson.start}-${schStudyItem.lesson.end}," +
                                                schStudyItem.room
                                    }
                                    CustomText("Day of week: $schList")
                                    var schWeek = ""
                                    for (schWeekList in item.subjectStudy.weekList) {
                                        schWeek += (if (schWeek.isNotEmpty()) "; " else "") +
                                                "${schWeekList.start}-${schWeekList.end}"
                                    }
                                    CustomText("Week range: $schWeek")
                                },
                                darkTheme = darkTheme,
                            )
                            // Subject examination
                            Spacer(modifier = Modifier.size(15.dp))
                            ContentInBoxWithBorder(
                                title = "Schedule Examination",
                                content = {
                                    if (item.subjectExam != null) {
                                        CustomText(
                                            "Group: ${item.subjectExam.group}" +
                                                    if (item.subjectExam.isGlobal) " (global exam)" else ""
                                        )
                                        CustomText(
                                            "Date: ${
                                                dateToString(
                                                    item.subjectExam.date,
                                                    "dd/MM/yyyy HH:mm",
                                                    "GMT+7"
                                                )
                                            }"
                                        )
                                        CustomText("Room: ${item.subjectExam.room}")
                                    }
                                    else {
                                        CustomText("Currently no examination schedule yet for this subject.")
                                    }
                                },
                                darkTheme = darkTheme,
                            )
                        }
                    }
                )
            }
        }

        @Composable
        fun CustomText(text: String) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(paddingPerItem),
            )
        }

        @Composable
        private fun ContentInBoxWithBorder(
            title: String,
            content: @Composable ColumnScope.() -> Unit,
            darkTheme: Boolean = false,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .border(
                        0.8.dp,
                        if (darkTheme) Color.White else Color.Black,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        content = content,
                    )
                }
            }
        }
    }
}