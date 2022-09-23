package io.zoemeow.dutnotify.view.settings

import android.app.TimePickerDialog
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.model.appsettings.CustomClock
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.enums.ServiceCode
import io.zoemeow.dutnotify.service.NewsService2
import io.zoemeow.dutnotify.viewmodel.MainViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsRefreshNewsTimeRange(
    enabled: MutableState<Boolean>,
    mainViewModel: MainViewModel
) {
    @Composable
    fun DisplayTime(
        title: String,
        clock: CustomClock,
        onClick: (() -> Unit)? = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable { if (onClick != null) onClick() },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = title)
            Text(
                text = "$clock",
                fontSize = 30.sp,
            )
        }
    }

    val context = LocalContext.current

    fun showTimePicker(
        clock: CustomClock,
        onSet: ((CustomClock) -> Unit)? = null
    ) {
        val mTimePickerDialog = TimePickerDialog(
            context,
            R.style.DialogMode,
            { _, mHour: Int, mMinute: Int ->
                if (onSet != null)
                    onSet(CustomClock(mHour, mMinute))
            },
            clock.hour,
            clock.minute,
            true
        )
        mTimePickerDialog.show()
    }

    val newTimeStart: MutableState<CustomClock> =
        remember { mutableStateOf(mainViewModel.appSettings.value.refreshNewsTimeStart) }
    val newTimeEnd: MutableState<CustomClock> =
        remember { mutableStateOf(mainViewModel.appSettings.value.refreshNewsTimeEnd) }

    LaunchedEffect(enabled.value) {
        newTimeStart.value = mainViewModel.appSettings.value.refreshNewsTimeStart
        newTimeEnd.value = mainViewModel.appSettings.value.refreshNewsTimeEnd
    }

    fun commitChanges() {
        // TODO: Reload service here!
        mainViewModel.appSettings.value = mainViewModel.appSettings.value.modify(
            AppSettings.NEWSINBACKGROUND_TIMESTART, newTimeStart.value
        )
        mainViewModel.appSettings.value = mainViewModel.appSettings.value.modify(
            AppSettings.NEWSINBACKGROUND_TIMEEND, newTimeEnd.value
        )
        mainViewModel.requestSaveChanges()
        enabled.value = false
        try {
            if (mainViewModel.appSettings.value.refreshNewsEnabled) {
                NewsService2.cancelSchedule(context)
                NewsService2.startService(
                    context = context,
                    intent = Intent(context, NewsService2::class.java).apply {
                        putExtra(ServiceCode.ACTION, ServiceCode.ACTION_NEWS_FETCHALL)
                        putExtra(ServiceCode.ARGUMENT_NEWS_NOTIFYTOUSER, false)
                    }
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    if (enabled.value) {
        AlertDialog(
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            onDismissRequest = {
                enabled.value = false
            },
            title = {
                Text(text = stringResource(id = R.string.settings_loadnewsinbackground_timeactive_name))
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        enabled.value = false
                    },
                    content = {
                        Text(stringResource(id = R.string.option_cancel))
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        commitChanges()
                    },
                    content = {
                        Text(stringResource(id = R.string.option_commit))
                    }
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(id = R.string.settings_loadnewsinbackground_timeactive_guide))
                    Spacer(modifier = Modifier.size(15.dp))
                    DisplayTime(
                        title = stringResource(id = R.string.settings_loadnewsinbackground_timeactive_timestart),
                        clock = newTimeStart.value,
                        onClick = {
                            showTimePicker(
                                clock = newTimeStart.value,
                                onSet = { newTimeStart.value = it }
                            )
                        }
                    )
                    Spacer(modifier = Modifier.size(15.dp))
                    DisplayTime(
                        title = stringResource(id = R.string.settings_loadnewsinbackground_timeactive_timeend),
                        clock = newTimeEnd.value,
                        onClick = {
                            showTimePicker(
                                clock = newTimeEnd.value,
                                onSet = { newTimeEnd.value = it }
                            )
                        }
                    )
                    Spacer(modifier = Modifier.size(15.dp))
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_info_24),
                            contentDescription = "info_icon",
                            tint = if (mainViewModel.mainActivityIsDarkTheme.value) Color.White else Color.Black
                        )
                        Text(stringResource(id = R.string.settings_loadnewsinbackground_timeactive_description))
                    }
                }
            }
        )
    }
}