package io.zoemeow.dutnotify.view.account

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.zoemeow.dutapi.Utils
import io.zoemeow.dutapi.objects.accounts.AccountInformation
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.model.enums.ProcessState
import io.zoemeow.dutnotify.AccountDetailsActivity
import io.zoemeow.dutnotify.ui.custom.SubjectPreview
import io.zoemeow.dutnotify.utils.DUTDateUtils.Companion.getCurrentDayOfWeek
import io.zoemeow.dutnotify.utils.DUTDateUtils.Companion.getDUTWeek
import io.zoemeow.dutnotify.utils.DUTDateUtils.Companion.getDateListFromWeek
import io.zoemeow.dutnotify.viewmodel.MainViewModel
import kotlinx.datetime.LocalDate

@Composable
fun AccountPageDashboard(
    mainViewModel: MainViewModel,
    padding: PaddingValues,
) {
    val context = LocalContext.current

    @Composable
    fun BasicInformation(
        username: String? = null,
        accountInformation: AccountInformation?,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Text(
                text = "Basic account information",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            Text(
                text = "Username: ${username ?: "(unknown)"}",
                modifier = Modifier.padding(bottom = 5.dp),
            )
            Text(
                text = "Class: ${accountInformation?.schoolClass ?: "(unknown)"}",
                modifier = Modifier.padding(bottom = 5.dp),
            )
            Text(
                text = "Training Program plan: ${accountInformation?.trainingProgramPlan ?: "(unknown)"}",
                modifier = Modifier.padding(bottom = 5.dp),
            )
        }
    }

    @Composable
    fun CustomButton(
        text: String,
        clickable: () -> Unit
    ) {
        Surface(
            modifier = Modifier
                .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(25.dp))
                .clickable { clickable() },
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
            )
        }
    }

    if (mainViewModel.subjectScheduleEnabled.value && mainViewModel.subjectScheduleItem.value != null) {
        SubjectPreview.SubjectScheduleDetails(
            dialogEnabled = mainViewModel.subjectScheduleEnabled.value,
            item = mainViewModel.subjectScheduleItem.value,
            darkTheme = mainViewModel.mainActivityIsDarkTheme.value,
            onClose = {
                mainViewModel.subjectScheduleEnabled.value = false
            }
        )
    }

    val dayOfWeek = remember { mutableStateOf(getCurrentDayOfWeek() - 1) }
    val week = remember { mutableStateOf(getDUTWeek()) }
    val weekList = remember { mutableStateListOf<LocalDate>().apply {
        addAll(getDateListFromWeek(week = week.value))
    } }
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
            mainViewModel = mainViewModel,
            isGettingData = (mainViewModel.accountDataStore.procAccSubSch.value == ProcessState.Running),
            dayOfWeek = dayOfWeek.value,
            onDayOfWeekChanged = {
                dayOfWeek.value = it
                mainViewModel.accountDataStore.filterSubjectScheduleByDay(dayOfWeek.value)
            },
            week = week.value,
            weekList = weekList,
            onWeekChanged = {
                week.value = it
                weekList.apply {
                    clear()
                    addAll(getDateListFromWeek(week = week.value))
                }
            },
            year = Utils.getDUTSchoolYear(System.currentTimeMillis()).name,
            onYearChanged = { },
            onResetView = {
                dayOfWeek.value = getCurrentDayOfWeek() - 1
                mainViewModel.accountDataStore.filterSubjectScheduleByDay(dayOfWeek.value)
                week.value = getDUTWeek()
                weekList.apply {
                    clear()
                    addAll(getDateListFromWeek(week = week.value))
                }
            },
            onItemClicked = {
                mainViewModel.subjectScheduleItem.value = it
                mainViewModel.subjectScheduleEnabled.value = true
            },
        )
        Spacer(modifier = Modifier.size(15.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(10.dp)),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.0F),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BasicInformation(
                    username = mainViewModel.accountDataStore.username.value,
                    accountInformation = mainViewModel.accountDataStore.accountInformation.value,
                )
                Spacer(modifier = Modifier.size(15.dp))
                CustomButton(
                    text = stringResource(id = R.string.account_dashboard_viewaccinfo),
                    clickable = {
                        val intent = Intent(context, AccountDetailsActivity::class.java)
                        intent.putExtra("type", "account_information")
                        context.startActivity(intent)
                    }
                )
                CustomButton(
                    text = stringResource(id = R.string.account_dashboard_viewsubjectschedule),
                    clickable = {
                        val intent = Intent(context, AccountDetailsActivity::class.java)
                        intent.putExtra("type", "subject_schedule")
                        context.startActivity(intent)
                    }
                )
                CustomButton(
                    text = stringResource(id = R.string.account_dashboard_viewsubjectfee),
                    clickable = {
                        val intent = Intent(context, AccountDetailsActivity::class.java)
                        intent.putExtra("type", "subject_fee")
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}