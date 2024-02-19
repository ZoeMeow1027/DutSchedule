package io.zoemeow.dutschedule.ui.component.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.dutwrapper.dutwrapper.model.accounts.SubjectScheduleItem
import io.zoemeow.dutschedule.model.CustomClock

@Composable
fun LessonTodaySummaryItem(
    hasLoggedIn: Boolean = false,
    isLoading: Boolean = false,
    affectedList: List<SubjectScheduleItem> = listOf(),
    padding: PaddingValues = PaddingValues(),
    clicked: () -> Unit,
    opacity: Float = 1.0f
) {
    fun affectedListStringBuilder(): String {
        val result = arrayListOf<String>()
        val currentLesson = CustomClock.getCurrent().toDUTLesson2()
        affectedList.forEach { item ->
            val childResult = String.format(
                "%s (%s)",
                item.name,
                item.subjectStudy.scheduleList.filter { it.lesson.end >= currentLesson.lesson }.joinToString(
                    separator = ", ",
                    transform = { String.format("%d-%d", it.lesson.start, it.lesson.end) }
                )
            )
            result.add(childResult)
        }

        return result.joinToString(separator = "\n")
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
                    "You have %d%s lesson%s today:",
                    affectedList.size,
                    when {
                        (CustomClock.getCurrent().toDUTLesson2().lesson in 1.0..14.0) -> " remaining"
                        else -> ""
                    },
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
        opacity = opacity,
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