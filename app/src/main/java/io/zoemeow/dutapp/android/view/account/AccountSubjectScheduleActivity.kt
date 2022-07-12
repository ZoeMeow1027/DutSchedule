package io.zoemeow.dutapp.android.view.account

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.zoemeow.dutapi.objects.SubjectScheduleItem
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.ui.theme.DUTAppForAndroidTheme
import io.zoemeow.dutapp.android.utils.DateToString
import io.zoemeow.dutapp.android.utils.DayOfWeekInString
import io.zoemeow.dutapp.android.viewmodel.AccountViewModel

class AccountSubjectScheduleActivity: ComponentActivity()  {
    private lateinit var activityViewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityViewModel = AccountViewModel.getInstance()
        if (!activityViewModel.isLoggedIn.value) {
            setResult(RESULT_CANCELED)
            finish()
        }
        activityViewModel.getSubjectSchedule()

        setContent {
            DUTAppForAndroidTheme {
                MainScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = {
                        Text("Subject Schedule - ${activityViewModel.username.value}")
                    }
                )
            }
        ) { padding ->
            SubjectScheduleScreen(
                padding = padding,
                subjectScheduleList = activityViewModel.subjectScheduleList,
                isLoading = activityViewModel.processStateSubjectSchedule,
                reloadRequested = {
                    activityViewModel.getSubjectSchedule()
                }
            )
        }
    }

    @Composable
    private fun SubjectScheduleScreen(
        padding: PaddingValues,
        subjectScheduleList: SnapshotStateList<SubjectScheduleItem>,
        isLoading: MutableState<ProcessState>,
        reloadRequested: () -> Unit
    ) {
        val swipeRefreshState = rememberSwipeRefreshState(true)
        swipeRefreshState.isRefreshing = isLoading.value == ProcessState.Running

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                swipeRefreshState.isRefreshing = true
                reloadRequested()
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
            ) {
                items(subjectScheduleList) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                            // https://www.android--code.com/2021/09/jetpack-compose-box-rounded-corners_25.html
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp)
                        ) {
                            Text(
                                text = "ID: ${item.id}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.size(20.dp))
                            Text(
                                text = "Name: ${item.name}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Credit: ${item.credit}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Is high quality: ${item.isHighQuality}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Lecturer: ${item.lecturer}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            var schList = ""
                            for (schStudyItem in item.subjectStudy.scheduleList) {
                                schList += (if (schList.isNotEmpty()) "; " else "") +
                                        "${DayOfWeekInString(schStudyItem.dayOfWeek)}," +
                                        "${schStudyItem.lesson.start}-${schStudyItem.lesson.end}," +
                                        schStudyItem.room
                            }
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Schedule study: $schList",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            var schWeek = ""
                            for (schWeekList in item.subjectStudy.weekList) {
                                schWeek += (if (schWeek.isNotEmpty()) "; " else "") +
                                        "${schWeekList.start}-${schWeekList.end}"
                            }
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Schedule study week range: $schWeek",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Schedule exam group: ${item.subjectExam.group}${if (item.subjectExam.isGlobal) " (global exam)" else ""}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Schedule exam date: ${DateToString(item.subjectExam.date, "dd/MM/yyyy HH:mm", "GMT+7")}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Schedule exam room: ${item.subjectExam.room}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Point formula: ${item.pointFormula}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }

                item {
                    Surface(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Column {
                            Spacer(modifier = Modifier.size(10.dp))
                            Text(
                                text = "Total credit: ${activityViewModel.subjectScheduleList.sumOf{ it.credit }}"
                            )
                        }
                    }
                }
            }
        }
    }
}