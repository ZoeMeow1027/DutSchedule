package io.zoemeow.dutschedule.ui.view.settings

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.activity.PermissionRequestActivity
import io.zoemeow.dutschedule.activity.SettingsActivity
import io.zoemeow.dutschedule.model.settings.SubjectCode
import io.zoemeow.dutschedule.ui.component.base.CheckboxOption
import io.zoemeow.dutschedule.ui.component.base.DividerItem
import io.zoemeow.dutschedule.ui.component.base.OptionItem
import io.zoemeow.dutschedule.ui.component.base.RadioButtonOption
import io.zoemeow.dutschedule.ui.component.base.SimpleCardItem
import io.zoemeow.dutschedule.ui.component.base.SwitchWithTextInSurface
import io.zoemeow.dutschedule.ui.component.settings.AddNewSubjectFilterDialog
import io.zoemeow.dutschedule.ui.component.settings.ContentRegion
import io.zoemeow.dutschedule.ui.component.settings.DeleteASubjectFilterDialog
import io.zoemeow.dutschedule.ui.component.settings.DeleteAllSubjectFilterDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsActivity.NewsNotificationSettings(
    context: Context,
    snackBarHostState: SnackbarHostState?,
    containerColor: Color,
    contentColor: Color
) {
    // val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val dialogAddNew = remember { mutableStateOf(false) }
    val tempDeleteItem: MutableState<SubjectCode> = remember { mutableStateOf(SubjectCode("","","")) }
    val dialogDeleteItem = remember { mutableStateOf(false) }
    val dialogDeleteAll = remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            snackBarHostState?.let {
                SnackbarHost(hostState = it)
            }
        },
        containerColor = containerColor,
        contentColor = contentColor,
        topBar = {
            TopAppBar(
                title = { Text("News Notification Settings") },
                // colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            setResult(ComponentActivity.RESULT_OK)
                            finish()
                        },
                        content = {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "Back",
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    )
                }
                // scrollBehavior = scrollBehavior
            )
        },
    ) {
        MainView(
            padding = it,
            fetchNewsInBackgroundDuration = getMainViewModel().appSettings.value.newsBackgroundDuration,
            onFetchNewsStateChanged = { duration ->
                if (duration > 0) {
                    if (PermissionRequestActivity.checkPermissionScheduleExactAlarm(context).isGranted) {
                        // TODO: Fetch news in background onClick
                        val dataTemp = getMainViewModel().appSettings.value.clone(
                            fetchNewsBackgroundDuration = duration
                        )
                        getMainViewModel().appSettings.value = dataTemp
                        getMainViewModel().saveSettings(saveSettingsOnly = true)
                        showSnackBar(
                            text = "Successfully enabled fetch news in background! News will refresh every $duration minute(s).",
                            clearPrevious = true
                        )
                    } else {
                        showSnackBar(
                            text = "You need to enable Alarms & Reminders in Android app settings to use this feature.",
                            clearPrevious = true,
                            actionText = "Open",
                            action = {
                                Intent(context, PermissionRequestActivity::class.java).also { intent ->
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                } else {
                    val dataTemp = getMainViewModel().appSettings.value.clone(
                        fetchNewsBackgroundDuration = 0
                    )
                    getMainViewModel().appSettings.value = dataTemp
                    getMainViewModel().saveSettings(saveSettingsOnly = true)
                    showSnackBar(
                        text = "Successfully disabled fetch news in background!",
                        clearPrevious = true
                    )
                }
            },
            isNewSubjectNotificationParseEnabled = getMainViewModel().appSettings.value.newsBackgroundParseNewsSubject,
            onNewSubjectNotificationParseStateChanged = {
                Intent(context, SettingsActivity::class.java).apply {
                    action = "settings_newssubjectnewparse"
                }.also { intent -> context.startActivity(intent) }
            },
            isNewsGlobalEnabled = getMainViewModel().appSettings.value.newsBackgroundGlobalEnabled,
            onNewsGlobalStateChanged = { enabled ->
                val dataTemp = getMainViewModel().appSettings.value.clone(
                    newsBackgroundGlobalEnabled = enabled
                )
                getMainViewModel().appSettings.value = dataTemp
                getMainViewModel().saveSettings(saveSettingsOnly = true)
                showSnackBar(
                    text = "Successfully ${
                        if (enabled) "enabled" else "disabled"
                    } global news notification!",
                    clearPrevious = true
                )
            },
            isNewsSubjectEnabled = getMainViewModel().appSettings.value.newsBackgroundSubjectEnabled,
            onNewsSubjectStateChanged = f@ { code ->
                if (code == 1) {
                    showSnackBar(
                        text = "\"Match your subject schedule\" option is in development. Check back soon.",
                        clearPrevious = true
                    )
                    return@f
                }

                val dataTemp = getMainViewModel().appSettings.value.clone(
                    newsBackgroundSubjectEnabled = code
                )
                getMainViewModel().appSettings.value = dataTemp
                getMainViewModel().saveSettings(saveSettingsOnly = true)
                showSnackBar(
                    text = "Done! You will notify \"${
                        when (code) {
                            -1 -> "nothing"
                            0 -> "all subject news notifications"
                            1 -> "news match your subject schedule"
                            2 -> "news match your filter list"
                            else -> "(unknown)"
                        }
                    }\".",
                    clearPrevious = true
                )
            },
            subjectFilterList = getMainViewModel().appSettings.value.newsBackgroundFilterList,
            onSubjectFilterAdd = {
                // TODO: Add a filter
                dialogAddNew.value = true
            },
            onSubjectFilterDelete = { data ->
                // TODO: Delete a filter
                tempDeleteItem.value = data
                dialogDeleteItem.value = true
            },
            onSubjectFilterClear = {
                // TODO: Delete all filters
                dialogDeleteAll.value = true
            },
            opacity = getControlBackgroundAlpha()
        )
    }
    AddNewSubjectFilterDialog(
        isVisible = dialogAddNew.value,
        onDismiss = { dialogAddNew.value = false },
        onDone = { syId, cId, subName ->
            // TODO: Add item manually
            try {
                val item = SubjectCode(syId, cId, subName)
                getMainViewModel().appSettings.value.newsBackgroundFilterList.add(item)
                getMainViewModel().saveSettings(saveSettingsOnly = true)
                showSnackBar(
                    String.format("Successfully added %s [%s.Nh%s]", subName, syId, subName),
                    clearPrevious = true
                )
            } catch (_: Exception) { }

            dialogAddNew.value = false
        }
    )
    DeleteASubjectFilterDialog(
        subjectCode = tempDeleteItem.value,
        isVisible = dialogDeleteItem.value,
        onDismiss = { dialogDeleteItem.value = false },
        onDone = {
            // TODO: Clear item on tempDeleteItem.value
            try {
                getMainViewModel().appSettings.value.newsBackgroundFilterList.remove(tempDeleteItem.value)
                getMainViewModel().saveSettings(saveSettingsOnly = true)
                showSnackBar(
                    String.format(
                        "Successfully deleted %s [%s.Nh%s]",
                        tempDeleteItem.value.subjectName,
                        tempDeleteItem.value.studentYearId,
                        tempDeleteItem.value.classId
                    ),
                    clearPrevious = true
                )
            } catch (_: Exception) { }

            dialogDeleteItem.value = false
        }
    )
    DeleteAllSubjectFilterDialog(
        isVisible = dialogDeleteAll.value,
        onDismiss = { dialogDeleteAll.value = false },
        onDone = {
            // TODO: Clear all items
            try {
                getMainViewModel().appSettings.value.newsBackgroundFilterList.clear()
                getMainViewModel().saveSettings(saveSettingsOnly = true)
                showSnackBar(
                    "Successfully cleared all filters!",
                    clearPrevious = true
                )
            } catch (_: Exception) { }
            dialogDeleteAll.value = false
        }
    )
    BackHandler(dialogAddNew.value || dialogDeleteItem.value || dialogDeleteAll.value) {
        if (dialogAddNew.value) {
            dialogAddNew.value = false
        }
        if (dialogDeleteItem.value) {
            dialogDeleteItem.value = false
        }
        if (dialogDeleteAll.value) {
            dialogDeleteAll.value = false
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MainView(
    padding: PaddingValues = PaddingValues(0.dp),
    fetchNewsInBackgroundDuration: Int = 0,
    onFetchNewsStateChanged: ((Int) -> Unit)? = null,
    isNewSubjectNotificationParseEnabled: Boolean = false,
    onNewSubjectNotificationParseStateChanged: (() -> Unit)? = null,
    isNewsGlobalEnabled: Boolean = false,
    onNewsGlobalStateChanged: ((Boolean) -> Unit)? = null,
    isNewsSubjectEnabled: Int = -1,
    onNewsSubjectStateChanged: ((Int) -> Unit)? = null,
    subjectFilterList: ArrayList<SubjectCode> = arrayListOf(),
    onSubjectFilterAdd: (() -> Unit)? = null,
    onSubjectFilterDelete: ((SubjectCode) -> Unit)? = null,
    onSubjectFilterClear: (() -> Unit)? = null,
    opacity: Float = 1f
) {
    val durationTemp = remember {
        mutableIntStateOf(fetchNewsInBackgroundDuration)
    }

    Column(
        modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        SwitchWithTextInSurface(
            text = "Refresh news in background",
            enabled = true,
            checked = fetchNewsInBackgroundDuration > 0,
            onCheckedChange = {
                // Refresh news state changed, default is 30 minutes
                onFetchNewsStateChanged?.let { it(when {
                    (fetchNewsInBackgroundDuration > 0) -> 0
                    else -> 30
                }) }
            }
        )
        ContentRegion(
            modifier = Modifier.padding(top = 10.dp),
            textModifier = Modifier.padding(horizontal = 20.dp),
            text = "Notification settings"
        ) {
            SimpleCardItem(
                padding = PaddingValues(horizontal = 20.4.dp, vertical = 5.dp),
                title = "Fetch news duration",
                clicked = { },
                opacity = opacity,
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 15.dp)
                            .padding(top = 5.dp, bottom = 10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Current duration settings: ${ when (fetchNewsInBackgroundDuration) {
                                0 -> "Disabled"
                                1 -> "1 minute"
                                else -> "$fetchNewsInBackgroundDuration minutes"
                            }
                            }",
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        Slider(
                            valueRange = 5f..240f,
                            steps = 236,
                            value = durationTemp.intValue.toFloat(),
                            enabled = fetchNewsInBackgroundDuration > 0,
                            colors = SliderDefaults.colors(
                                activeTickColor = Color.Transparent,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTickColor = Color.Transparent,
                                inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                            ),
                            onValueChange = {
                                durationTemp.intValue = it.toInt()
                            }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            content = {
                                Text("${durationTemp.intValue} minute${if (durationTemp.intValue != 1) "s" else ""}")
                            }
                        )
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(top = 7.dp),
                            horizontalArrangement = Arrangement.Center,
                            content = {
                                listOf(15, 30, 60).forEach { min ->
                                    SuggestionChip(
                                        modifier = Modifier.padding(horizontal = 5.dp),
                                        icon = {
                                            if (durationTemp.intValue == min) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    "Selected",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            if (fetchNewsInBackgroundDuration > 0) {
                                                durationTemp.intValue = min
                                            }
                                        },
                                        label = { Text(if (min == 0) "Turn off" else "$min min") }
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.size(5.dp))
                        ElevatedButton(
                            onClick = {
                                if (fetchNewsInBackgroundDuration > 0) {
                                    onFetchNewsStateChanged?.let { it(durationTemp.intValue) }
                                }
                            },
                            content = {
                                Text("Save")
                            }
                        )
                    }
                }
            )
            OptionItem(
                modifierInside = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                title = "News parse method on notification",
                description = when (isNewSubjectNotificationParseEnabled) {
                    true -> "Enabled (special notification for news subject)"
                    false -> "Disabled (regular notification for news subject)"
                },
                onClick = { onNewSubjectNotificationParseStateChanged?.let { it() } }
            )
        }
        DividerItem(padding = PaddingValues(top = 5.dp, bottom = 15.dp))
        ContentRegion(
            textModifier = Modifier.padding(horizontal = 20.dp),
            text = "Global news notification"
        ) {
            CheckboxOption(
                title = "Enable global news notification",
                modifierInside = Modifier.padding(horizontal = 6.5.dp),
                isEnabled = fetchNewsInBackgroundDuration > 0,
                isChecked = isNewsGlobalEnabled,
                onClick = {
                    // TODO: Refresh news state changed
                    onNewsGlobalStateChanged?.let { it(!isNewsGlobalEnabled) }
                }
            )
        }
        ContentRegion(
            modifier = Modifier.padding(top = 10.dp),
            textModifier = Modifier.padding(horizontal = 20.dp),
            text = "Subject news notification"
        ) {
            RadioButtonOption(
                modifierInside = Modifier.padding(horizontal = 6.5.dp),
                title = "Off",
                isEnabled = fetchNewsInBackgroundDuration > 0,
                isChecked = isNewsSubjectEnabled == -1,
                onClick = {
                    // TODO: Subject news notification off - onClick
                    onNewsSubjectStateChanged?.let { it(-1) }
                }
            )
            RadioButtonOption(
                modifierInside = Modifier.padding(horizontal = 6.5.dp),
                title = "All subject news notifications",
                isEnabled = fetchNewsInBackgroundDuration > 0,
                isChecked = isNewsSubjectEnabled == 0,
                onClick = {
                    // TODO: Subject news notification all - onClick
                    onNewsSubjectStateChanged?.let { it(0) }
                }
            )
            RadioButtonOption(
                modifierInside = Modifier.padding(horizontal = 6.5.dp),
                title = "Match your subject schedule",
                isEnabled = fetchNewsInBackgroundDuration > 0,
                isChecked = isNewsSubjectEnabled == 1,
                onClick = {
                    // TODO: Subject news notification your subject schedule - onClick
                    onNewsSubjectStateChanged?.let { it(1) }
                }
            )
            RadioButtonOption(
                modifierInside = Modifier.padding(horizontal = 6.5.dp),
                title = "Follow custom list",
                isEnabled = fetchNewsInBackgroundDuration > 0,
                isChecked = isNewsSubjectEnabled == 2,
                onClick = {
                    // TODO: Subject news notification custom list - onClick
                    onNewsSubjectStateChanged?.let { it(2) }
                }
            )
        }
        DividerItem(padding = PaddingValues(top = 5.dp, bottom = 15.dp))
        ContentRegion(
            textModifier = Modifier.padding(horizontal = 20.dp),
            text = "News subject filter"
        ) {
            if (isNewsSubjectEnabled != 2) {
                SimpleCardItem(
                    padding = PaddingValues(horizontal = 20.4.dp, vertical = 7.dp),
                    title = "News subject filter list is disabled",
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(horizontal = 15.dp)
                                .padding(bottom = 15.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text("To manage your subject news filter, please check \"Follow custom list\" option first.")
                        }
                    },
                    clicked = { },
                    opacity = opacity
                )
            }
            SimpleCardItem(
                padding = PaddingValues(horizontal = 20.4.dp, vertical = 5.dp),
                title = "Your current filter list",
                clicked = { },
                opacity = opacity,
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 15.dp)
                            .padding(bottom = 15.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (subjectFilterList.size == 0) {
                            Text("Your added subject filter will shown here.")
                        }
                        subjectFilterList.forEach { code ->
                            OptionItem(
                                modifier = Modifier.padding(vertical = 3.dp),
                                modifierInside = Modifier,
                                title = "${code.subjectName} [${code.studentYearId}.Nh${code.classId}]",
                                onClick = { },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (fetchNewsInBackgroundDuration > 0) {
                                                onSubjectFilterDelete?.let { it(code) }
                                            }
                                        },
                                        content = {
                                            Icon(Icons.Default.Delete, "Delete")
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            )
            OptionItem(
                modifierInside = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                title = "Add a subject news filter",
                leadingIcon = { Icon(Icons.Default.Add, "Add a subject news filter") },
                isEnabled = isNewsSubjectEnabled == 2,
                onClick = {
                    // TODO: Add a subject news filter
                    onSubjectFilterAdd?.let { it() }
                }
            )
            OptionItem(
                modifierInside = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                title = "Clear all subject news filter list",
                leadingIcon = { Icon(Icons.Default.Delete, "Clear all subject news filter") },
                isEnabled = isNewsSubjectEnabled == 2,
                onClick = {
                    // TODO: Clear all subject news filter list
                    onSubjectFilterClear?.let { it() }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainViewPreview() {
    MainView(
        fetchNewsInBackgroundDuration = 30,
        onFetchNewsStateChanged = { },
        isNewsGlobalEnabled = true,
        subjectFilterList = arrayListOf(
            SubjectCode("19", "12", "Nhập môn ngành"),
            SubjectCode("19", "12", "PBL3")
        ),
        isNewsSubjectEnabled = 2
    )
}