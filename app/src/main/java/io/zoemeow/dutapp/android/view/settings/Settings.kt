package io.zoemeow.dutapp.android.view.settings

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import io.zoemeow.dutapp.android.BuildConfig
import io.zoemeow.dutapp.android.ui.custom.CustomDivider
import io.zoemeow.dutapp.android.ui.custom.SettingsOptionItem
import io.zoemeow.dutapp.android.utils.openLinkInCustomTab
import io.zoemeow.dutapp.android.viewmodel.GlobalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings() {
    val globalViewModel = GlobalViewModel.getInstance()
    val context: MutableState<Context?> = remember { mutableStateOf(null) }
    context.value = LocalContext.current

    val schoolYearSettingsEnabled = remember { mutableStateOf(false) }

    SettingsSchoolYear(schoolYearSettingsEnabled)
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(text = "Settings")
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier.padding(padding),
                content = {
                    if (schoolYearSettingsEnabled.value)
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    SettingsOptionItem(
                        title = "Change school year",
                        description = "Current: School year: " +
                                "20${globalViewModel.settings.schoolYear.year}-" +
                                "20${globalViewModel.settings.schoolYear.year + 1}, " +
                                "Semester: ${
                                    if (globalViewModel.settings.schoolYear.semester < 3)
                                        globalViewModel.settings.schoolYear.semester
                                    else
                                        "3 (in summer)"
                                }. Affect to your subjects display",
                        clickable = {
                            schoolYearSettingsEnabled.value = true
                        }
                    )
                    CustomDivider()
                    SettingsOptionItem(
                        title = "Version: ${BuildConfig.VERSION_NAME}",
                        description = "(This feature are under development. Stay tuned!)",
                    )
                    SettingsOptionItem(
                        title = "Changelog (click to open in browser)",
                        description = "(This feature are under development. Stay tuned!)",
                        clickable = {
//                            openLinkInCustomTab(
//                                context.value!!,
//                                "https://github.com/ZoeMeow5466/DUTApp.Android"
//                            )
                        }
                    )
                    SettingsOptionItem(
                        title = "GitHub (click to open in browser)",
                        description = "https://github.com/ZoeMeow5466/DUTApp.Android",
                        clickable = {
                            openLinkInCustomTab(
                                context.value!!,
                                "https://github.com/ZoeMeow5466/DUTApp.Android"
                            )
                        }
                    )
                }
            )
        }
    )
}