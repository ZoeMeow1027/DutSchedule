package io.zoemeow.dutapp.android.view.settings

import android.app.TimePickerDialog
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
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.model.appsettings.CustomClock
import io.zoemeow.dutapp.android.model.enums.AppSettingsCode
import io.zoemeow.dutapp.android.viewmodel.MainViewModel
import java.text.DecimalFormat

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
            val numberFormat = DecimalFormat("00")
            Text(text = title)
            Text(
                text = "${numberFormat.format(clock.hour)}:${numberFormat.format(clock.minute)}",
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
        remember { mutableStateOf(mainViewModel.settings.value.refreshNewsTimeStart) }
    val newTimeEnd: MutableState<CustomClock> =
        remember { mutableStateOf(mainViewModel.settings.value.refreshNewsTimeEnd) }

    LaunchedEffect(enabled.value) {
        newTimeStart.value = mainViewModel.settings.value.refreshNewsTimeStart
        newTimeEnd.value = mainViewModel.settings.value.refreshNewsTimeEnd
    }

    fun commitChanges() {
        // TODO: Reload service here!
        mainViewModel.settings.value = mainViewModel.settings.value.modify(
            AppSettingsCode.RefreshNewsTimeStart, newTimeStart.value
        )
        mainViewModel.settings.value = mainViewModel.settings.value.modify(
            AppSettingsCode.RefreshNewsTimeEnd, newTimeEnd.value
        )
        mainViewModel.requestSaveChanges()
        enabled.value = false
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
                Text(text = "Refresh news time picker")
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
                    Text("Click a time you want to change and remember to commit changes if you done.")
                    Spacer(modifier = Modifier.size(15.dp))
                    DisplayTime(
                        title = "Time start: ",
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
                        title = "Time end: ",
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
                            tint = if (mainViewModel.uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black
                        )
                        Text("This will change time range which app used to prevent using data and battery at night. Still in alpha.")
                    }
                }
            }
        )
    }
}