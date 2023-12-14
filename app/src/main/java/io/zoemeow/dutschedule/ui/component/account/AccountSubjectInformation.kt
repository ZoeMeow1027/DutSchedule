package io.zoemeow.dutschedule.ui.component.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.dutwrapper.dutwrapper.model.accounts.SubjectScheduleItem
import io.zoemeow.dutschedule.util.CustomDateUtils

@Composable
fun AccountSubjectInformation(
    modifier: Modifier = Modifier,
    item: SubjectScheduleItem,
    onClick: (() -> Unit)? = null,
    opacity: Float = 1f
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                onClick?.let { it() }
            },
        color = MaterialTheme.colorScheme.secondaryContainer.copy(
            alpha = opacity
        ),
        content = {
            Column(
                modifier = Modifier.padding(10.dp),
                content = {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = "${item.lecturer}\n\nSchedule:\n${item.subjectStudy.scheduleList.joinToString(
                            separator = "\n",
                            transform = { schItem ->
                                "- ${CustomDateUtils.dayOfWeekInString(schItem.dayOfWeek, true)} - Lesson ${schItem.lesson.start}-${schItem.lesson.end} - Room ${schItem.room}"
                            }
                        )}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            )
        }
    )
}