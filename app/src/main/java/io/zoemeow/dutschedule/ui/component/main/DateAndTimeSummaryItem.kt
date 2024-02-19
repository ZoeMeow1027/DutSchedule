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
import io.zoemeow.dutschedule.model.CustomClock
import io.zoemeow.dutschedule.utils.CustomDateUtil
import kotlinx.coroutines.delay

@Composable
fun DateAndTimeSummaryItem(
    padding: PaddingValues = PaddingValues(),
    isLoading: Boolean = false,
    currentSchoolWeek: DutSchoolYearItem? = null,
    opacity: Float = 1.0f
) {
    val dateTimeString = remember { mutableStateOf("") }

    SummaryItem(
        padding = padding,
        title = "Date & time today",
        isLoading = isLoading,
        opacity = opacity,
        clicked = { },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = {
                    Text(
                        text = dateTimeString.value,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 15.dp)
                    )
                    Text(
                        text = String.format(
                            "School year: %s - Week: %s\nCurrent lesson: %s",
                            currentSchoolWeek?.schoolYear ?: "(unknown)",
                            currentSchoolWeek?.week?.toString() ?: "(unknown)",
                            CustomClock.getCurrent().toDUTLesson2().name
                        ),
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
                "Date and time: %s\n(based on your current region)\n",
                CustomDateUtil.getCurrentDateAndTimeToString("dd/MM/yyyy HH:mm:ss"),
            ).also { dateTimeString.value = it }
            delay(1000)
        }
    }
}