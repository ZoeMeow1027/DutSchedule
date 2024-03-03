package io.zoemeow.dutschedule.ui.view.settings

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import io.zoemeow.dutschedule.ui.component.settings.ContentRegion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsActivity.NewsNotificationSettings(
    context: Context,
    snackBarHostState: SnackbarHostState?,
    containerColor: Color,
    contentColor: Color
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
            LargeTopAppBar(
                title = { Text("News Notification Settings") },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
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
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) {
        MainView(
            padding = it,
            fetchNewsInBackgroundDuration = getMainViewModel().appSettings.value.newsBackgroundDuration,
            onFetchNewsStateChanged = { enabled ->
                if (enabled) {
                    if (PermissionRequestActivity.checkPermissionScheduleExactAlarm(context).isGranted) {
                        // TODO: Fetch news in background onClick
                        val dataTemp = getMainViewModel().appSettings.value.clone(
                            fetchNewsBackgroundDuration = 30
                        )
                        getMainViewModel().appSettings.value = dataTemp
                        getMainViewModel().saveSettings()
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
                    getMainViewModel().saveSettings()
                }
            },
            onFetchNewsDurationClicked = {
                if (PermissionRequestActivity.checkPermissionScheduleExactAlarm(context).isGranted) {
                    // TODO: Fetch news in background onClick
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
            },
            isNewsGlobalEnabled = getMainViewModel().appSettings.value.newsBackgroundGlobalEnabled,
            onNewsGlobalStateChanged = { enabled ->
                val dataTemp = getMainViewModel().appSettings.value.clone(
                    newsBackgroundGlobalEnabled = enabled
                )
                getMainViewModel().appSettings.value = dataTemp
                getMainViewModel().saveSettings()
            },
            isNewsSubjectEnabled = getMainViewModel().appSettings.value.newsBackgroundSubjectEnabled,
            onNewsSubjectStateChanged = { code ->
                val dataTemp = getMainViewModel().appSettings.value.clone(
                    newsBackgroundSubjectEnabled = code
                )
                getMainViewModel().appSettings.value = dataTemp
                getMainViewModel().saveSettings()
            },
            subjectFilterList = getMainViewModel().appSettings.value.newsBackgroundFilterList,
            onSubjectFilterAdd = {

            },
            onSubjectFilterDelete = { code ->

            },
            onSubjectFilterClear = {

            },
            opacity = getControlBackgroundAlpha()
        )
    }
}

@Composable
private fun MainView(
    padding: PaddingValues = PaddingValues(0.dp),
    fetchNewsInBackgroundDuration: Int = 0,
    onFetchNewsStateChanged: (Boolean) -> Unit,
    onFetchNewsDurationClicked: () -> Unit,
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
    // isNewsSubjectEnabled:
    // - -1: Off
    // - 0: All
    // - 1: Your subject schedule list
    // - 2: Custom list

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
                // TODO: Refresh news state changed, default is 30 minutes
                onFetchNewsStateChanged(!(fetchNewsInBackgroundDuration > 0))
            }
        )
        ContentRegion(
            modifier = Modifier.padding(top = 10.dp),
            textModifier = Modifier.padding(horizontal = 20.dp),
            text = "Notification settings"
        ) {
            OptionItem(
                modifierInside = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                title = "Fetch news duration",
                description = when {
                    (fetchNewsInBackgroundDuration > 0) ->
                        String.format(
                            "Every %d minute%s",
                            fetchNewsInBackgroundDuration,
                            if (fetchNewsInBackgroundDuration != 1) "s" else ""
                        )
                    else -> "Disabled"
                },
                onClick = {
                    if (fetchNewsInBackgroundDuration > 0) {
                        onFetchNewsDurationClicked()
                    }
                }
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
                                title = "${code.subjectName} - ${code.subjectName}.Nh${code.classId}",
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
        fetchNewsInBackgroundDuration = 0,
        onFetchNewsStateChanged = { },
        onFetchNewsDurationClicked = { },
        isNewsGlobalEnabled = true,
        subjectFilterList = arrayListOf(
            SubjectCode("19", "12", "Nhập môn ngành"),
            SubjectCode("19", "12", "PBL3")
        ),
        isNewsSubjectEnabled = 2
    )
}