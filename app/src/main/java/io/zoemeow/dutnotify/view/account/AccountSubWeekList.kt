package io.zoemeow.dutnotify.view.account

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.zoemeow.dutapi.objects.accounts.SubjectScheduleItem
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.utils.DUTDateUtils.Companion.dayOfWeekToString
import io.zoemeow.dutnotify.viewmodel.MainViewModel
import kotlinx.datetime.LocalDate

@Composable
fun AccountSubWeekList(
    mainViewModel: MainViewModel,
    padding: PaddingValues? = null,
    isGettingData: Boolean = false,
    dayOfWeek: Int,
    onDayOfWeekChanged: (Int) -> Unit,
    week: Int,
    weekList: SnapshotStateList<LocalDate>,
    onWeekChanged: (Int) -> Unit,
    year: String?,
    onYearChanged: () -> Unit,
    onResetView: () -> Unit,
    onItemClicked: ((SubjectScheduleItem?) -> Unit)? = null
) {
    @Composable
    fun CustomSurfaceButton(
        text: String,
        onClick: () -> Unit
    ) {
        Surface(
            modifier = Modifier
                .padding(2.dp)
                .wrapContentWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable { onClick() },
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 10.dp,
                    bottom = 10.dp
                ),
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth()
            .wrapContentHeight(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.3F)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(padding ?: PaddingValues(0.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Week list
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                weekList.forEach { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(start = 4.dp, end = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onDayOfWeekChanged(
                                    weekList.indexOf(item) + 1
                                )
                            },
                        color = (
                                if (weekList.indexOf(item) + 1 == dayOfWeek)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondaryContainer
                                )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayOfWeekToString(
                                    if (weekList.indexOf(item) + 1 > 6)
                                        0 else weekList.indexOf(item) + 1
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Spacer(modifier = Modifier.size(3.dp))
                            Text(
                                text = "${item.dayOfMonth}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "Tg ${item.monthNumber}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.size(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text("Current week: $week")
                    if (year != null)
                        Text("$year")
                }
                CustomSurfaceButton(
                    text = "<",
                    onClick = { if (week > 1) onWeekChanged(week - 1) }
                )
                Spacer(modifier = Modifier.size(5.dp))
                CustomSurfaceButton(
                    text = stringResource(id = R.string.account_dashboard_dayandweekview_today),
                    onClick = { onResetView() }
                )
                Spacer(modifier = Modifier.size(5.dp))
                CustomSurfaceButton(
                    text = ">",
                    onClick = { if (week < 53) onWeekChanged(week + 1) }
                )
            }
            Spacer(modifier = Modifier.size(5.dp))
            // Day view
            if (!isGettingData) {
                if (mainViewModel.accountDataStore.subjectScheduleByDay.size > 0) {
                    Text(
                        text = stringResource(id = R.string.account_dashboard_dayandweekview_subjectschedule),
                        modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
                    )
                    mainViewModel.accountDataStore.subjectScheduleByDay.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(top = 5.dp, bottom = 5.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    if (onItemClicked != null)
                                        onItemClicked(item)
                                },
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(10.dp),
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Log.d("Day of week", (if (dayOfWeek > 6) 0 else dayOfWeek).toString())
                                item.subjectStudy.scheduleList.filter { schItem -> schItem.dayOfWeek == if (dayOfWeek > 6) 0 else dayOfWeek }
                                    .forEach {
                                        Text(
                                            text = "Lesson: ${it.lesson.start}-${it.lesson.end}, Room: ${it.room}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                            }
                        }
                    }
                } else Text(
                    text = stringResource(id = R.string.account_dashboard_dayandweekview_nosubject),
                    modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
                )
            } else Text(
                text = stringResource(id = R.string.account_dashboard_dayandweekview_fetching)
            )
        }
    }
}