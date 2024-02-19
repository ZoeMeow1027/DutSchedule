package io.zoemeow.dutschedule.ui.view.settings

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.R
import io.zoemeow.dutschedule.activity.SettingsActivity
import io.zoemeow.dutschedule.model.settings.SubjectCode
import io.zoemeow.dutschedule.ui.component.base.DialogBase
import io.zoemeow.dutschedule.ui.component.settings.newsfilter.NewsFilterAddInNewsSubject
import io.zoemeow.dutschedule.ui.component.settings.newsfilter.NewsFilterAddManually
import io.zoemeow.dutschedule.ui.component.settings.newsfilter.NewsFilterClearAll
import io.zoemeow.dutschedule.ui.component.settings.newsfilter.NewsFilterCurrentFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsActivity.NewsFilterSettings(
    snackBarHostState: SnackbarHostState,
    containerColor: Color,
    contentColor: Color
) {
    val tempFilterList = remember {
        mutableStateListOf<SubjectCode>().also {
            it.addAll(getMainViewModel().appSettings.value.newsBackgroundFilterList)
        }
    }
    val modified = remember { mutableStateOf(false) }
    val exitWithoutSavingDialog = remember { mutableStateOf(false) }

    fun saveChanges(exit: Boolean = false) {
        getMainViewModel().appSettings.value = getMainViewModel().appSettings.value.clone(
            newsFilterList = getMainViewModel().appSettings.value.newsBackgroundFilterList.also {
                it.clear()
                it.addAll(tempFilterList.toList())
            }
        )
        getMainViewModel().saveSettings()
        modified.value = false

        if (!exit) {
            showSnackBar(
                text = "Saved changes!",
                clearPrevious = true
            )
        } else {
            tempFilterList.clear()
            setResult(ComponentActivity.RESULT_OK)
            finish()
        }
    }

    fun discardChangesAndExit() {
        tempFilterList.clear()
        setResult(ComponentActivity.RESULT_CANCELED)
        finish()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        containerColor = containerColor,
        contentColor = contentColor,
        topBar = {
            TopAppBar(
                title = { Text("News filter settings") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!modified.value) {
                                discardChangesAndExit()
                            } else {
                                exitWithoutSavingDialog.value = true
                            }
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
                actions = {
                    IconButton(
                        onClick = {
                            saveChanges()
                        },
                        content = {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_save_24),
                                "",
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    )
                }
            )
        },
        content = {
            val tabIndex = remember { mutableIntStateOf(1) }

            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(horizontal = 7.dp)
                    .verticalScroll(rememberScrollState()),
                content = {
                    NewsFilterCurrentFilter(
                        opacity = getControlBackgroundAlpha(),
                        selectedSubjects = tempFilterList,
                        onRemoveRequested = { subjectCode ->
                            tempFilterList.remove(subjectCode)
                            modified.value = true
                            showSnackBar(
                                text = "Removed $subjectCode. Save changes to apply your settings.",
                                clearPrevious = true
                            )
                        }
                    )
                    NewsFilterAddInNewsSubject(
                        opacity = getControlBackgroundAlpha(),
                        expanded = tabIndex.intValue == 1,
                        onExpanded = { tabIndex.intValue = 1 }
                    )
                    NewsFilterAddManually(
                        opacity = getControlBackgroundAlpha(),
                        expanded = tabIndex.intValue == 2,
                        onExpanded = { tabIndex.intValue = 2 },
                        onSubmit = { schoolYearItem, classItem, subjectName ->
                            tempFilterList.add(
                                SubjectCode(
                                    studentYearId = schoolYearItem,
                                    classId = classItem,
                                    subjectName = subjectName
                                )
                            )
                            modified.value = true
                            showSnackBar(
                                text = "Added ${schoolYearItem}.${classItem}. Save changes to apply your settings.",
                                clearPrevious = true
                            )
                        }
                    )
                    NewsFilterClearAll(
                        opacity = getControlBackgroundAlpha(),
                        expanded = tabIndex.intValue == 3,
                        onExpanded = { tabIndex.intValue = 3 },
                        onSubmit = {
                            if (tempFilterList.isNotEmpty()) {
                                tempFilterList.clear()
                                modified.value = true
                                showSnackBar(
                                    text = "Cleared! Remember to save changes to apply your settings.",
                                    clearPrevious = true
                                )
                            } else {
                                showSnackBar(
                                    text = "Nothing to clear!",
                                    clearPrevious = true
                                )
                            }
                        }
                    )
                }
            )
        }
    )
    DialogBase(
        modifier = Modifier
            .fillMaxWidth()
            .padding(25.dp),
        canDismiss = false,
        isTitleCentered = true,
        title = "Exit without saving?",
        isVisible = exitWithoutSavingDialog.value,
        content = {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("You have modified changes. Save them now?\n\n- Yes: Save changes and exit\n- No: Discard changes and exit\n- Cancel: Just close this dialog.")
            }
        },
        actionButtons = {
            TextButton(
                onClick = {
                    exitWithoutSavingDialog.value = false
                    saveChanges(exit = true)
                },
                content = { Text("Yes") },
                modifier = Modifier.padding(start = 8.dp),
            )
            TextButton(
                onClick = {
                    exitWithoutSavingDialog.value = false
                    discardChangesAndExit()
                },
                content = { Text("No") },
                modifier = Modifier.padding(start = 8.dp),
            )
            TextButton(
                onClick = {
                    exitWithoutSavingDialog.value = false
                },
                content = { Text("Cancel") },
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    )
    BackHandler(
        enabled = modified.value,
        onBack = {
            exitWithoutSavingDialog.value = true
        }
    )
}