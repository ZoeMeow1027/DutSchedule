package io.zoemeow.dutapp.android.view.account

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.viewmodel.MainViewModel

@Composable
fun AccountPageDashboard(
    mainViewModel: MainViewModel,
    padding: PaddingValues,
) {
    @Composable
    fun BasicInformation(
        username: String? = null,
        schoolClass: String? = null,
        trainingProgramPlan: String? = null
    ) {
        Text(
            text = "Basic account information",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            text = "Username: ${username ?: "(unknown)" }",
        )
        Text(
            text = "Class: ${schoolClass ?: "(unknown)"}",
        )
        Text(
            text = "Training Program plan: ${trainingProgramPlan ?: "(unknown)"}",
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(start = 10.dp, end = 10.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        AccountSubWeekList(
            onDayAndWeekChanged = { weekAdjust, dayOfWeekChanged ->
                mainViewModel.uiStatus.accountCurrentDayOfWeek.value = if (dayOfWeekChanged < 7) dayOfWeekChanged else 0
                mainViewModel.filterSubjectScheduleByDay(mainViewModel.uiStatus.accountCurrentDayOfWeek.value)
            }
        )
        if (mainViewModel.uiStatus.procAccSubSch.value == ProcessState.Running) {
            Text("We are fetching your subject schedule list. Please wait...")
        }
        else {
            AccountSubDayView(
                mainViewModel = mainViewModel,
                currentDayOfWeek = mainViewModel.uiStatus.accountCurrentDayOfWeek.value
            )
        }
        Spacer(modifier = Modifier.size(15.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(10.dp)),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3F),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                BasicInformation(
                    username = mainViewModel.uiStatus.username.value,
                    schoolClass = mainViewModel.uiStatus.accountInformation.value?.schoolClass,
                    trainingProgramPlan = mainViewModel.uiStatus.accountInformation.value?.trainingProgramPlan,
                )
                Spacer(modifier = Modifier.size(15.dp))
                Surface(
                    modifier = Modifier
                        .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(25.dp))
                        .clickable {
                            mainViewModel.uiStatus.accountCurrentPage.value = 4
                        },
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "View Account Information",
                        modifier = Modifier.padding(15.dp),
                    )
                }
                Surface(
                    modifier = Modifier
                        .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(25.dp))
                        .clickable {
                            mainViewModel.uiStatus.accountCurrentPage.value = 2
                        },
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "View Subject Schedule",
                        modifier = Modifier.padding(15.dp),
                    )
                }
                Surface(
                    modifier = Modifier
                        .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(25.dp))
                        .clickable {
                            mainViewModel.uiStatus.accountCurrentPage.value = 3
                        },
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "View Subject Fee",
                        modifier = Modifier.padding(15.dp),
                    )
                }
            }
        }
    }
}