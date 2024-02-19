package io.zoemeow.dutschedule.ui.view.account

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.dutwrapper.dutwrapper.model.accounts.trainingresult.SubjectResult
import io.zoemeow.dutschedule.activity.AccountActivity
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.ui.component.base.OutlinedTextBox
import io.zoemeow.dutschedule.utils.TableCell
import io.zoemeow.dutschedule.utils.toNonAccent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountActivity.TrainingSubjectResult(
    snackBarHostState: SnackbarHostState,
    containerColor: Color,
    contentColor: Color
) {
    val selectedSubject = remember { mutableStateOf<SubjectResult?>(null) }
    val searchQuery = remember { mutableStateOf("") }
    val searchEnabled = remember { mutableStateOf(false) }
    val schYearOption = remember { mutableStateOf(false) }
    val schYearOptionText = remember { mutableStateOf("All school year items") }
    val focusRequester = remember { FocusRequester() }

    val modalBottomSheetEnabled = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    fun dismissSearchBar() {
        clearAllFocusAndHideKeyboard()
        searchQuery.value = ""
        searchEnabled.value = false
    }

    fun subjectResultToMap(item: SubjectResult): Map<String, String?> {
        return mapOf(
            "Subject Year" to (item.schoolYear ?: "(unknown)"),
            "Subject Code" to (item.id ?: "(unknown)"),
            "Credit" to item.credit.toString(),
            "Point formula" to (item.pointFormula ?: "(unknown)"),
            "BT" to item.pointBT?.toString(),
            "BV" to item.pointBV?.toString(),
            "CC" to item.pointCC?.toString(),
            "CK" to item.pointCK?.toString(),
            "GK" to item.pointGK?.toString(),
            "QT" to item.pointQT?.toString(),
            "TH" to item.pointTH?.toString(),
            "Point (T10 - T4 - By point char)" to String.format(
                "%s - %s - %s",
                if (item.resultT10 != null) String.format(
                    "%.2f",
                    item.resultT10
                ) else "unscored",
                if (item.resultT4 != null) String.format("%.2f", item.resultT4) else "unscored",
                if (item.resultByCharacter.isNullOrEmpty()) "(unscored)" else item.resultByCharacter
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        containerColor = containerColor,
        contentColor = contentColor,
        topBar = {
            TopAppBar(
                title = {
                    if (!searchEnabled.value) {
                        Text("Your subject result list")
                    } else {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            value = searchQuery.value,
                            onValueChange = {
                                if (searchEnabled.value) {
                                    searchQuery.value = it
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    clearAllFocusAndHideKeyboard()
                                }
                            ),
                            trailingIcon = {
                            },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (searchEnabled.value) {
                                dismissSearchBar()
                            } else {
                                setResult(ComponentActivity.RESULT_CANCELED)
                                finish()
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
                    if (!searchEnabled.value) {
                        IconButton(
                            onClick = {
                                searchEnabled.value = true
                            },
                            content = {
                                Icon(Icons.Default.Search, "Search")
                            }
                        )
                    } else null
                }
            )
        },
        floatingActionButton = {
            if (getMainViewModel().accountTrainingStatus.processState.value != ProcessState.Running) {
                FloatingActionButton(
                    onClick = {
                        clearAllFocusAndHideKeyboard()
                        CoroutineScope(Dispatchers.IO).launch {
                            getMainViewModel().accountLogin(
                                after = {
                                    if (it) { getMainViewModel().accountTrainingStatus.refreshData(force = true) }
                                }
                            )
                        }
                    },
                    content = {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                )
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                content = {
                    if (getMainViewModel().accountTrainingStatus.processState.value == ProcessState.Running) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .clickable { clearAllFocusAndHideKeyboard() },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                        content = {
                            // Filter
                            ExposedDropdownMenuBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 5.dp)
                                    .padding(bottom = 5.dp),
                                expanded = schYearOption.value,
                                onExpandedChange = { schYearOption.value = !schYearOption.value },
                                content = {
                                    OutlinedTextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        label = { Text("Select a school year to filter") },
                                        readOnly = true,
                                        value = schYearOptionText.value,
                                        onValueChange = { }
                                    )
                                    DropdownMenu(
                                        modifier = Modifier.fillMaxWidth(),
                                        expanded = schYearOption.value,
                                        onDismissRequest = { schYearOption.value = false},
                                        content = {
                                            DropdownMenuItem(
                                                modifier = Modifier.background(
                                                    color = when (schYearOptionText.value == "All school year items") {
                                                        true -> MaterialTheme.colorScheme.secondaryContainer
                                                        false -> MaterialTheme.colorScheme.surface
                                                    }
                                                ),
                                                text = { Text("All school year items") },
                                                onClick = {
                                                    schYearOptionText.value = "All school year items"
                                                    schYearOption.value = false
                                                }
                                            )
                                            (getMainViewModel().accountTrainingStatus.data.value?.subjectResultList?.map { it.schoolYear }?.toList()?.distinct()?.reversed() ?: listOf()).forEach {
                                                DropdownMenuItem(
                                                    modifier = Modifier.background(
                                                        color = when (schYearOptionText.value == it) {
                                                            true -> MaterialTheme.colorScheme.secondaryContainer
                                                            false -> MaterialTheme.colorScheme.surface
                                                        }
                                                    ),
                                                    text = { Text(it) },
                                                    onClick = {
                                                        schYearOptionText.value = it
                                                        schYearOption.value = false
                                                    }
                                                )
                                            }
                                        }
                                    )
                                }
                            )
                            // Data
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 5.dp)
                                    .padding(top = 7.dp)
                                    .height(IntrinsicSize.Min),
                                content = {
                                    TableCell(
                                        modifier = Modifier.fillMaxHeight(),
                                        backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = getControlBackgroundAlpha()),
                                        text = "Index",
                                        textAlign = TextAlign.Center,
                                        weight = 0.2f
                                    )
                                    TableCell(
                                        modifier = Modifier.fillMaxHeight(),
                                        backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = getControlBackgroundAlpha()),
                                        text = "Subject name",
                                        textAlign = TextAlign.Center,
                                        weight = 0.6f
                                    )
                                    TableCell(
                                        modifier = Modifier.fillMaxHeight(),
                                        backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = getControlBackgroundAlpha()),
                                        text = "Result (T4/C)",
                                        textAlign = TextAlign.Center,
                                        weight = 0.2f
                                    )
                                }
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top,
                                content = {
                                    getMainViewModel().accountTrainingStatus.data.value?.subjectResultList?.filter {
                                            p ->
                                        (schYearOptionText.value == "All school year items" || p.schoolYear == schYearOptionText.value) &&
                                                (searchQuery.value.isEmpty()
                                                        || p.name.toNonAccent().lowercase().contains(searchQuery.value.toNonAccent().lowercase()))
                                    }?.reversed()?.forEach { subjectItem ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 5.dp)
                                                .height(IntrinsicSize.Min)
                                                .clickable {
                                                    selectedSubject.value = subjectItem
                                                    modalBottomSheetEnabled.value = true
                                                },
                                            content = {
                                                TableCell(
                                                    modifier = Modifier.fillMaxHeight(),
                                                    backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = getControlBackgroundAlpha()),
                                                    text = "${subjectItem.index}",
                                                    textAlign = TextAlign.Center,
                                                    weight = 0.2f
                                                )
                                                TableCell(
                                                    modifier = Modifier.fillMaxHeight(),
                                                    backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = getControlBackgroundAlpha()),
                                                    text = "${subjectItem.name}",
                                                    contentAlign = Alignment.CenterStart,
                                                    textAlign = TextAlign.Start,
                                                    weight = 0.6f
                                                )
                                                TableCell(
                                                    modifier = Modifier.fillMaxHeight(),
                                                    backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = getControlBackgroundAlpha()),
                                                    text = String.format(
                                                        "%s (%s)",
                                                        if (subjectItem.resultT4 != null) "${subjectItem.resultT4}" else "---",
                                                        if (subjectItem.resultByCharacter != null) "${subjectItem.resultByCharacter}" else "-"
                                                    ),
                                                    textAlign = TextAlign.Center,
                                                    weight = 0.2f
                                                )
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    )
                    if (modalBottomSheetEnabled.value) {
                        ModalBottomSheet(
                            onDismissRequest = { modalBottomSheetEnabled.value = false },
                            sheetState = sheetState,
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 15.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                content = {
                                    Text(
                                        selectedSubject.value?.name ?: "(Unknown)",
                                        style = TextStyle(fontSize = 27.sp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 10.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    selectedSubject.value?.let { item ->
                                        subjectResultToMap(item).forEach { (key, value) ->
                                            if (value != null) {
                                                OutlinedTextBox(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    title = key,
                                                    value = value
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.size(5.dp))
                                }
                            )
                        }
                    }
                }
            )
        }
    )

    BackHandler(
        enabled = searchEnabled.value,
        onBack = {
            dismissSearchBar()
        }
    )

    val hasRun = remember { mutableStateOf(false) }
    run {
        if (!hasRun.value) {
            CoroutineScope(Dispatchers.IO).launch {
                getMainViewModel().accountLogin(
                    after = {
                        if (it) {
                            getMainViewModel().accountTrainingStatus.refreshData()
                        }
                    }
                )
            }
            hasRun.value = true
        }
    }
}