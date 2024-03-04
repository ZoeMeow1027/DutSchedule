package io.zoemeow.dutschedule.ui.view.settings

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.activity.SettingsActivity
import io.zoemeow.dutschedule.model.settings.BackgroundImageOption
import io.zoemeow.dutschedule.ui.component.base.DividerItem
import io.zoemeow.dutschedule.ui.component.base.OptionItem
import io.zoemeow.dutschedule.ui.component.settings.ContentRegion
import io.zoemeow.dutschedule.ui.component.settings.dialog.DialogSchoolYearSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsActivity.ExperimentSettings(
    context: Context,
    snackBarHostState: SnackbarHostState,
    containerColor: Color,
    contentColor: Color
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val dialogSchoolYear = remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        containerColor = containerColor,
        contentColor = contentColor,
        topBar = {
            LargeTopAppBar(
                title = { Text("Experiment settings") },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            setResult(ComponentActivity.RESULT_CANCELED)
                            finish()
                        },
                        content = {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "",
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .padding(it)
                    .verticalScroll(rememberScrollState()),
                content = {
                    ContentRegion(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(top = 10.dp),
                        text = "Global variable settings",
                        content = {
                            OptionItem(
                                title = "Current school year settings",
                                description = String.format(
                                    "Year: 20%d-20%d, Semester: %s%s",
                                    getMainViewModel().appSettings.value.currentSchoolYear.year,
                                    getMainViewModel().appSettings.value.currentSchoolYear.year + 1,
                                    when (getMainViewModel().appSettings.value.currentSchoolYear.semester) {
                                        1 -> "1"
                                        2 -> "2"
                                        else -> "2"
                                    },
                                    if (getMainViewModel().appSettings.value.currentSchoolYear.semester > 2) " (in summer)" else ""
                                ),
                                onClick = {
                                    dialogSchoolYear.value = true
                                }
                            )
                        }
                    )
                    DividerItem(padding = PaddingValues(top = 5.dp, bottom = 15.dp))
                    ContentRegion(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(top = 10.dp),
                        text = "Notifications",
                        content = {
                            OptionItem(
                                title = "News notifications in background settings",
                                description = "Configure your settings",
                                onClick = {
                                    Intent(context, SettingsActivity::class.java).apply {
                                        action = "settings_newsnotificaitonsettings"
                                    }.also { intent -> context.startActivity(intent) }
                                }
                            )
                        }
                    )
                    DividerItem(padding = PaddingValues(top = 5.dp, bottom = 15.dp))
                    ContentRegion(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(top = 10.dp),
                        text = "Appearance",
                        content = {
                            OptionItem(
                                title = "Background opacity",
                                description = String.format(
                                    "%2.0f%% %s",
                                    (getMainViewModel().appSettings.value.backgroundImageOpacity * 100),
                                    if (getMainViewModel().appSettings.value.backgroundImage == BackgroundImageOption.None) {
                                        "(You need enable background image to take effect)"
                                    } else ""
                                ),
                                onClick = {
                                    showSnackBar("This option is in development. Check back soon.", true)
                                    /* TODO: Implement here: Background opacity */
                                }
                            )
                            OptionItem(
                                title = "Component opacity",
                                description = String.format(
                                    "%2.0f%% %s",
                                    (getMainViewModel().appSettings.value.componentOpacity * 100),
                                    if (getMainViewModel().appSettings.value.backgroundImage == BackgroundImageOption.None) {
                                        "(You need enable background image to take effect)"
                                    } else ""
                                ),
                                onClick = {
                                    showSnackBar("This option is in development. Check back soon.", true)
                                    /* TODO: Implement here: Component opacity */
                                }
                            )
                        }
                    )
                    DividerItem(padding = PaddingValues(top = 5.dp, bottom = 15.dp))
                    ContentRegion(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(top = 10.dp),
                        text = "Troubleshooting",
                        content = {
                            OptionItem(
                                title = "Debug log (not work yet)",
                                description = "Get debug log for this application to troubleshoot issues.",
                                onClick = {
                                    showSnackBar("This option is in development. Check back soon.", true)
                                    /* TODO: Implement here: Debug log */
                                }
                            )
                        }
                    )
                }
            )
        }
    )
    DialogSchoolYearSettings(
        isVisible = dialogSchoolYear.value,
        dismissRequested = { dialogSchoolYear.value = false },
        currentSchoolYearItem = getMainViewModel().appSettings.value.currentSchoolYear,
        onSubmit = {
            getMainViewModel().appSettings.value = getMainViewModel().appSettings.value.clone(
                currentSchoolYear = it
            )
            saveSettings()
            dialogSchoolYear.value = false
        }
    )
}