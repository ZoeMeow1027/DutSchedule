package io.zoemeow.dutschedule.ui.component.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.dutwrapper.dutwrapper.model.accounts.SubjectScheduleItem
import io.zoemeow.dutschedule.util.CustomDateUtils
import io.zoemeow.dutschedule.util.DUTLesson

@Composable
fun LessonTodaySummaryItem(
    hasLoggedIn: Boolean = false,
    isLoading: Boolean = false,
    affectedList: ArrayList<SubjectScheduleItem> = arrayListOf(),
    padding: PaddingValues = PaddingValues(),
    clicked: () -> Unit,
) {
    fun affectedListStringBuilder(): String {
        var result = ""

        val currentDayOfWeek = CustomDateUtils.getCurrentDayOfWeek()
        val currentLesson = DUTLesson.getCurrentLesson()

        affectedList.forEach { item ->
            val lessonAffected = item.subjectStudy.scheduleList.filter {
                // TODO: Wait for library update
                (it.dayOfWeek + 1) == currentDayOfWeek && (
                        it.lesson.start < currentLesson.toDUTLesson() ||
                                (it.lesson.start >= currentLesson.toDUTLesson() && it.lesson.end < currentLesson.toDUTLesson())
                        )
            }.joinToString(
                separator = ", ",
                transform = { it.lesson.toString()}
            )
            val childResult = String.format(
                "%s (%s)",
                item.name,
                lessonAffected
            )
            result = result.plus(String.format("%s\n", childResult))
        }

        return result
    }

    fun summaryStringBuilder(): String {
        return if (!hasLoggedIn) {
            "You haven't logged in! We can't fetch data for you!\nLog in to continue using this function."
        } else if (affectedList.isEmpty()) {
            "You have completed all lessons today. Have a rest!"
        } else {
            String.format(
                "%s\n\n%s",
                String.format(
                    "You have %d lesson%s today:",
                    affectedList.size,
                    if (affectedList.size != 1) "s" else ""
                ),
                affectedListStringBuilder()
            )
        }
    }

    SummaryItem(
        padding = padding,
        title = "Your subjects today",
        clicked = clicked,
        isLoading = isLoading,
        content = {
            Text(
                text = summaryStringBuilder(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .padding(bottom = 10.dp)
            )
        }
    )
}