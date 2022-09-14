package io.zoemeow.dutnotify.view.account

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
    fun WeekList_DayOfWeekButton(
        date: LocalDate,
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onClick() },
            color = (
                    if (selected)
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
                        if (weekList.indexOf(date) + 1 > 6)
                            0 else weekList.indexOf(date) + 1
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.size(3.dp))
                Text(
                    text = "${date.dayOfMonth}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Tg ${date.monthNumber}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    @Composable
    fun WeekList_SubjectButton(
        subjectName: String,
        subjectDest: String?,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onClick() },
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
                    text = subjectName,
                    style = MaterialTheme.typography.titleLarge
                )
                if (subjectDest != null) {
                    Text(
                        text = subjectDest,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(padding ?: PaddingValues(0.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format(
                stringResource(id = R.string.account_dashboard_dayandweekview_overview),
                week, year
            ),
            modifier = Modifier.padding(bottom = 10.dp)
        )
        // Week list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            weekList.forEach { item ->
                WeekList_DayOfWeekButton(
                    date = item,
                    selected = weekList.indexOf(item) + 1 == dayOfWeek,
                    onClick = { onDayOfWeekChanged(weekList.indexOf(item) + 1) },
                    modifier = Modifier
                        .padding(start = 4.dp, end = 4.dp)
                        .weight(1f),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
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
        // Day view
        if (!isGettingData) {
            if (mainViewModel.accountDataStore.subjectScheduleByDay.size > 0) {
                Text(
                    text = stringResource(id = R.string.account_dashboard_dayandweekview_subjectschedule),
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                mainViewModel.accountDataStore.subjectScheduleByDay.forEach { item ->
                    var desc = ""
                    item.subjectStudy.scheduleList.filter { schItem -> schItem.dayOfWeek == if (dayOfWeek > 6) 0 else dayOfWeek }
                        .forEach {
                            if (desc.isNotEmpty())
                                desc += "\n"
                            desc += String.format(
                                stringResource(id = R.string.account_dashboard_dayandweekview_subjectstatus),
                                it.lesson.start, it.lesson.end, it.room
                            )
                        }
                    WeekList_SubjectButton(
                        subjectName = item.name,
                        subjectDest = desc.ifEmpty { stringResource(id = R.string.account_dashboard_dayandweekview_unknowndata) },
                        onClick = { if (onItemClicked != null) onItemClicked(item) },
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
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