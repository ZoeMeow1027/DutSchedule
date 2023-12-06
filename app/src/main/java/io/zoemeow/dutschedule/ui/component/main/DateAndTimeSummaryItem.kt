package io.zoemeow.dutschedule.ui.component.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.dutwrapper.dutwrapper.model.utils.DutSchoolYearItem
import io.zoemeow.dutschedule.util.CustomDateUtils
import io.zoemeow.dutschedule.util.DUTLesson
import kotlinx.coroutines.delay

@Composable
fun DateAndTimeSummaryItem(
    padding: PaddingValues = PaddingValues(),
    isLoading: Boolean = false,
    currentSchoolWeek: DutSchoolYearItem? = null
) {
    val dateTimeString = remember { mutableStateOf("") }

    SummaryItem(
        padding = padding,
        title = "Date & time today",
        isLoading = isLoading,
        clicked = { },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = {
                    Text(
                        text = dateTimeString.value,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(horizontal = 15.dp)
                            .padding(bottom = 10.dp)
                    )
                }
            )
        }
    )

    LaunchedEffect(Unit) {
        while (true) {
            String.format(
                "Date and time: %s\n(based on your current region)\n\nSchool year: %s - Week: %s\nCurrent lesson: %s",
                CustomDateUtils.getCurrentDateAndTimeToString("dd/MM/yyyy HH:mm:ss"),
                currentSchoolWeek?.schoolYear ?: "(unknown)",
                currentSchoolWeek?.week?.toString() ?: "(unknown)",
                when (DUTLesson.getCurrentLesson().toDUTLesson()) {
                    -3 -> "(unknown)"
                    -2 -> "Not started yet"
                    -1 -> "Breaking on noon..."
                    0 -> "Done for today!"
                    else -> DUTLesson.getCurrentLesson().toDUTLesson().toString()
                }
            ).also { dateTimeString.value = it }
            delay(1000)
        }
    }
}