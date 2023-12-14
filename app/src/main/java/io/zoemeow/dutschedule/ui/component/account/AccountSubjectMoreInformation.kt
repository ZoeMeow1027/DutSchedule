package io.zoemeow.dutschedule.ui.component.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.dutwrapper.dutwrapper.model.accounts.SubjectScheduleItem
import io.zoemeow.dutschedule.model.settings.SubjectCode
import io.zoemeow.dutschedule.ui.component.base.DialogBase
import io.zoemeow.dutschedule.util.CustomDateUtils

@Composable
fun AccountSubjectMoreInformation(
    item: SubjectScheduleItem? = null,
    isVisible: Boolean = false,
    onAddToFilterRequested: ((SubjectCode) -> Unit)? = null,
    dismissClicked: (() -> Unit)? = null,
) {
    DialogBase(
        modifier = Modifier
            .fillMaxWidth()
            .padding(25.dp),
        title = "${item?.name ?: "(unknown)"}\n${item?.lecturer ?: "(unknown)"}",
        isVisible = isVisible,
        isTitleCentered = true,
        canDismiss = true,
        dismissClicked = {
            dismissClicked?.let { it() }
        },
        content = {
            Column(
                horizontalAlignment = Alignment.Start,
            ) {
                CustomText("ID: ${item?.id?.toString(false) ?: "(unknown)"}")
                CustomText("Credit: ${item?.credit ?: "(unknown)"}")
                CustomText("Is high quality: ${item?.isHighQuality ?: "(unknown)"}")
                CustomText("Final score formula: ${item?.pointFormula ?: "(unknown)"}")
                // Subject study
                Spacer(modifier = Modifier.size(15.dp))
                ContentInBoxWithBorder(
                    title = "Schedule Study",
                    content = {
                        var schList = ""
                        item?.let {
                            schList = it.subjectStudy.scheduleList.joinToString(
                                separator = "; ",
                                transform = { item1 ->
                                    "${CustomDateUtils.dayOfWeekInString(item1.dayOfWeek + 1)},${item1.lesson.start}-${item1.lesson.end},${item1.room}"
                                }
                            )
                        }
                        CustomText("Day of week: $schList")
                        var schWeek = ""
                        item?.let {
                            schWeek = it.subjectStudy.weekList.joinToString(
                                separator = "; ",
                                transform = { item1 ->
                                    "${item1.start}-${item1.end}"
                                }
                            )
                        }
                        CustomText("Week range: $schWeek")
                    },
                )
                // Subject examination
                Spacer(modifier = Modifier.size(15.dp))
                ContentInBoxWithBorder(
                    title = "Schedule Examination",
                    content = {
                        if (item != null) {
                            CustomText(
                                "Group: ${item.subjectExam.group}" +
                                        if (item.subjectExam.isGlobal) " (global exam)" else ""
                            )
                            CustomText(
                                "Date: ${
                                    CustomDateUtils.dateToString(
                                        item.subjectExam.date,
                                        "dd/MM/yyyy HH:mm",
                                        "GMT+7"
                                    )
                                }"
                            )
                            CustomText("Room: ${item.subjectExam.room}")

                        } else {
                            CustomText("Currently no examination schedule yet for this subject.")
                        }
                    }
                )
            }
        },
        actionButtons = {
            TextButton(
                onClick = {
                    onAddToFilterRequested?.let { callBack ->
                        item?.let {  item ->
                            callBack(
                                SubjectCode(
                                    studentYearId = item.id.studentYearId,
                                    classId = item.id.classId,
                                    subjectName = item.name
                                )
                            )
                        }
                    }
                },
                content = { Text("Add to news filter") },
                modifier = Modifier.padding(start = 8.dp),
            )
            TextButton(
                onClick = { dismissClicked?.let { it() } },
                content = { Text("OK") },
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    )
}

@Composable
private fun CustomText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 5.dp),
    )
}

@Composable
private fun ContentInBoxWithBorder(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(10.dp),
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