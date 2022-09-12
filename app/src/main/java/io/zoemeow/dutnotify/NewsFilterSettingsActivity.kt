package io.zoemeow.dutnotify

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import io.zoemeow.dutapi.objects.accounts.SubjectScheduleItem
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.appsettings.BackgroundImage
import io.zoemeow.dutnotify.model.appsettings.SubjectCode
import io.zoemeow.dutnotify.model.enums.BackgroundImageType
import io.zoemeow.dutnotify.model.enums.LoginState
import io.zoemeow.dutnotify.model.enums.ProcessState
import io.zoemeow.dutnotify.receiver.AppBroadcastReceiver
import io.zoemeow.dutnotify.ui.controls.CustomTitleAndExpandableColumn
import io.zoemeow.dutnotify.ui.theme.MainActivityTheme
import io.zoemeow.dutnotify.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class NewsFilterSettingsActivity: ComponentActivity() {
    internal lateinit var mainViewModel: MainViewModel
    private lateinit var snackBarState: SnackbarHostState
    private lateinit var scope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            mainViewModel = viewModel()

            LaunchedEffect(Unit) {
                registerBroadcastReceiver(context = applicationContext)

                checkSettingsPermissionOnStartup(mainViewModel = mainViewModel)

                // Re-login to receive new data from server.
                mainViewModel.accountDataStore.reLogin(
                    silent = true,
                    reloadSubject = true,
                    schoolYearItem = mainViewModel.appSettings.value.schoolYear,
                )
            }

            MainActivityTheme(
                appSettings = mainViewModel.appSettings.value,
                content = @Composable {
                    MainScreen(mainViewModel = mainViewModel)
                },
                backgroundDrawable = mainViewModel.mainActivityBackgroundDrawable.value,
                appModeChanged = {
                    // Trigger for dark mode detection.
                    mainViewModel.mainActivityIsDarkTheme.value = it
                },
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Composable
    fun MainScreen(mainViewModel: MainViewModel) {
        snackBarState = SnackbarHostState()
        scope = rememberCoroutineScope()

        val unsavedChanges = remember { mutableStateOf(false) }
        val unSaveChangedDialog = remember { mutableStateOf(false) }

        val subjectCodeTempList = remember { mutableStateListOf<SubjectCode>() }

        fun saveChanges() {
            mainViewModel.appSettings.value =
                mainViewModel.appSettings.value.modify(
                    optionToModify = AppSettings.NEWSFILTER_FILTERLIST,
                    value = arrayListOf<SubjectCode>().apply {
                        addAll(subjectCodeTempList)
                    }
                )
            mainViewModel.requestSaveChanges()
            unsavedChanges.value = false
            showSnackBarMessage("Successfully saved changes!", true)
        }

        if (unSaveChangedDialog.value) {
            AlertDialog(
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                ),
                modifier = Modifier
                    .padding(15.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                onDismissRequest = { unSaveChangedDialog.value = false },
                title = { Text("Save unsaved changes?") },
                dismissButton = {
                    Row {
                        // Just disable this dialog
                        TextButton(
                            content = { Text("Cancel") },
                            onClick = { unSaveChangedDialog.value = false }
                        )
                        Spacer(modifier = Modifier.size(5.dp))
                        // Dismiss changed settings.
                        TextButton(
                            content = { Text("No") },
                            onClick = {
                                setResult(RESULT_CANCELED)
                                finish()
                            }
                        )
                    }
                },
                confirmButton = {
                    // Save changes
                    TextButton(
                        content = { Text("Yes") },
                        onClick = {
                            saveChanges()
                            setResult(RESULT_OK)
                            finish()
                        }
                    )
                },
                text = {
                    Text("You have unsaved changes. Save them?")
                },
            )
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackBarState) },
            topBar = {
                SmallTopAppBar(
                    title = {
                        Text("News filter settings")
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (unsavedChanges.value) {
                                        unSaveChangedDialog.value = true
                                    } else {
                                        setResult(RESULT_OK)
                                        finish()
                                    }
                                },
                            content = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_arrow_back_24),
                                    contentDescription = "",
                                    tint = if (mainViewModel.mainActivityIsDarkTheme.value) Color.White else Color.Black,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        )
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                                .clip(CircleShape)
                                .clickable {
                                    saveChanges()
                                },
                            content = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_save_24),
                                    contentDescription = "",
                                    tint = if (mainViewModel.mainActivityIsDarkTheme.value) Color.White else Color.Black,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        )
                    }
                )
            },
            containerColor = if (mainViewModel.appSettings.value.backgroundImage.option == BackgroundImageType.Unset)
                MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.background.copy(
                alpha = 0.8f
            ),
            contentColor = if (mainViewModel.mainActivityIsDarkTheme.value) Color.White else Color.Black,
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            MainBody(
                mainViewModel = mainViewModel,
                padding = padding,
                subjectCodeTempList = subjectCodeTempList,
                unsavedChanges = unsavedChanges,
                onUnsavedChangesDialog = { unSaveChangedDialog.value = true }
            )
        }
    }

    @Composable
    fun MainBody(
        mainViewModel: MainViewModel,
        padding: PaddingValues,
        subjectCodeTempList: SnapshotStateList<SubjectCode>,
        unsavedChanges: MutableState<Boolean>,
        onUnsavedChangesDialog: (() -> Unit)? = null,
    ) {
        val availableList = remember { mutableStateListOf<SubjectScheduleItem>() }
        val selectedIndex = remember { mutableStateOf(0) }
        val selectedFilterName = remember { mutableStateOf("") }

        val columnAddBySubSch = remember { mutableStateOf(true) }
        val columnAddManually = remember { mutableStateOf(false) }
        val columnResetToDefault = remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            subjectCodeTempList.addAll(mainViewModel.appSettings.value.newsFilterList)
        }

        BackHandler(
            enabled = unsavedChanges.value,
            onBack = {
                if (onUnsavedChangesDialog != null) {
                    onUnsavedChangesDialog()
                }
            }
        )

        fun isInIndexAvailableList(value: Int): Boolean {
            return (value >= 0) && (value <= availableList.size - 1)
        }

        fun isDuplicate(input: SubjectCode): Boolean {
            return try {
                return subjectCodeTempList.any { input.isEquals(it) }
            }
            catch (ex: Exception) {
                true
            }
        }

        fun updateRequested() {
            availableList.apply {
                clear()
                mainViewModel.accountDataStore.subjectSchedule.forEach {
                    val item = SubjectCode(
                        studentYearId = it.id.studentYearId,
                        classId = it.id.classId,
                        name = it.name
                    )

                    if (!isDuplicate(item)) {
                        add(it)
                    }
                }
            }

            if (!isInIndexAvailableList(selectedIndex.value)) {
                if (availableList.isEmpty()) {
                    selectedIndex.value = -1
                    selectedFilterName.value = "No more subject available"
                } else {
                    selectedIndex.value = 0
                    selectedFilterName.value = availableList[selectedIndex.value].name
                }
            } else selectedFilterName.value = availableList[selectedIndex.value].name
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState())
                .padding(padding),
        ) {
            // Your current subject schedule and subject codes you has added before.
            SurfaceCurrentSubjectCodeList(
                subjectCodeList = subjectCodeTempList,
                deleteRequested = {
                    subjectCodeTempList.remove(it)
                    unsavedChanges.value = true
                    updateRequested()
                    showSnackBarMessage("Deleted \"${it.name}\".")
                },
            )

            // Load from your subject schedule to add to filter.
            SurfaceAddFromSubjectSchedule(
                availableList = availableList,
                selectedIndex = selectedIndex,
                selectedFilterValue = selectedFilterName,
                updateRequested = { updateRequested() },
                addRequested = {
                    if (!isDuplicate(it))
                        subjectCodeTempList.add(it)
                    unsavedChanges.value = true
                    updateRequested()
                    showSnackBarMessage("Added \"${it.name}\".")
                },
                reloadAvailableNewsRequested = {
                    mainViewModel.accountDataStore.fetchSubjectSchedule()
                },
                expended = columnAddBySubSch.value,
                onExpended = {
                    columnAddBySubSch.value = true
                    columnAddManually.value = false
                    columnResetToDefault.value = false
                }
            )

            // Add filter manually.
            SurfaceAddManually(
                addRequested = {
                    if (!isDuplicate(it))
                        subjectCodeTempList.add(it)
                    unsavedChanges.value = true
                    updateRequested()
                    showSnackBarMessage("Added \"${it.name}\".")
                },
                expended = columnAddManually.value,
                onExpended = {
                    columnAddBySubSch.value = false
                    columnAddManually.value = true
                    columnResetToDefault.value = false
                }
            )

            // Reset to default
            SurfaceResetToDefault(
                onDelete = {
                    subjectCodeTempList.clear()
                    unsavedChanges.value = true
                    updateRequested()
                    showSnackBarMessage("Cleared all filters. Remember save changes to take effect.")
                },
                expended = columnResetToDefault.value,
                onExpended = {
                    columnAddBySubSch.value = false
                    columnAddManually.value = false
                    columnResetToDefault.value = true
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SurfaceCurrentSubjectCodeList(
        subjectCodeList: SnapshotStateList<SubjectCode>,
        deleteRequested: (SubjectCode) -> Unit,
    ) {
        CustomSurface {
            CustomTitleAndExpandableColumn(
                title = "Current Subject Code list",
                expanded = true,
                onExpanded = { },
            ) {
                if (subjectCodeList.isEmpty()) {
                    Text(
                        text = "- Your subject codes is empty! That\'s mean, all subject news will notify you.\n" +
                                "- To enable this feature, just add at least one item. Your filter list will be here."
                    )
                }
                else {
                    Text(
                        text = "Your current subject code list (click a item to remove)",
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        mainAxisAlignment = FlowMainAxisAlignment.Center
                    ) {
                        subjectCodeList.forEach {
                            InputChip(
                                selected = false,
                                onClick = { deleteRequested(it) },
                                label = { Text(it.toString()) },
                                modifier = Modifier.padding(end = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SurfaceAddFromSubjectSchedule(
        availableList: SnapshotStateList<SubjectScheduleItem>,
        selectedIndex: MutableState<Int>,
        selectedFilterValue: MutableState<String>,
        updateRequested: () -> Unit,
        addRequested: (SubjectCode) -> Unit,
        reloadAvailableNewsRequested: () -> Unit,
        expended: Boolean,
        onExpended: (() -> Unit)? = null,
    ) {
        CustomSurface {
            CustomTitleAndExpandableColumn(
                title = "Add from your subject schedule",
                expanded = expended,
                onExpanded = { if (onExpended != null) onExpended() },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (mainViewModel.accountDataStore.loginState.value == LoginState.LoggingIn) {
                            Text(
                                text = "We\'re re-login you in. Please wait...",
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            CircularProgressIndicator()
                        }
                        else if (mainViewModel.accountDataStore.loginState.value != LoginState.LoggedIn) {
                            Text(
                                text = "You need to login or check your account or check your internet before you can use this feature.",
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                        else if (mainViewModel.accountDataStore.procAccSubSch.value == ProcessState.Running) {
                            Text(
                                text = "We\'re loading your subject schedule. Please wait...",
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            CircularProgressIndicator()
                        }
                        else {
                            val dropDownExpanded = remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                updateRequested()
                            }

                            Text(
                                text = "Choose a item and tap \"Add\" to add to filter above. Added item to filter won\'t be showed on this drop down again.",
                                modifier = Modifier.padding(bottom = 15.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = dropDownExpanded.value,
                                onExpandedChange = {
                                    if (availableList.isNotEmpty())
                                        dropDownExpanded.value = !dropDownExpanded.value
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 5.dp)
                            ) {
                                OutlinedTextField(
                                    readOnly = true,
                                    value = selectedFilterValue.value,
                                    onValueChange = {},
                                    label = { Text("Choose your subject") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded.value) },
                                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 5.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = dropDownExpanded.value,
                                    onDismissRequest = { dropDownExpanded.value = false },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(bottom = 5.dp)
                                ) {
                                    availableList.forEach {
                                        DropdownMenuItem(
                                            text = { Text(it.name) },
                                            onClick = {
                                                selectedIndex.value = availableList.indexOf(it)
                                                dropDownExpanded.value = false
                                                updateRequested()
                                            }
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    content = {
                                        Text("Refresh")
                                    },
                                    onClick = {
                                        reloadAvailableNewsRequested()
                                    },
                                    modifier = Modifier
                                        .padding(end = 5.dp)
                                        .weight(1f)
                                )
                                Button(
                                    content = {
                                        Text("Add filter")
                                    },
                                    onClick = {
                                        try {
                                            val subjectScheduleItem = availableList[selectedIndex.value]
                                            val item = SubjectCode(
                                                studentYearId = subjectScheduleItem.id.studentYearId,
                                                classId = subjectScheduleItem.id.classId,
                                                name = subjectScheduleItem.name
                                            )
                                            addRequested(item)
                                        } catch (ex: Exception) {
                                            // Can't add to list.
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(start = 5.dp)
                                        .weight(1f)
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SurfaceAddManually(
        addRequested: (SubjectCode) -> Unit,
        expended: Boolean,
        onExpended: (() -> Unit)? = null,
    ) {
        val studentYearId = remember { mutableStateOf("") }
        val classId = remember { mutableStateOf("") }
        val subjectName = remember { mutableStateOf("") }
        CustomSurface {
            CustomTitleAndExpandableColumn(
                title = "Add filter manually",
                expanded = expended,
                onExpanded = { if (onExpended != null) onExpended() },
            ) {
                Text(
                    text = "Enter your subject filter (you can view templates in sv.dut.udn.vn) and tap \"Add\" to add it to list in above.\n\n" +
                            "- Example:\n" +
                            "  - 19 | 01 | Subject name\n" +
                            "  - xx | 94A | Subject name\n\n" +
                            "Note: You need to enter carefully, otherwise you won\'t received notifications exactly.",
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = 10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = studentYearId.value,
                            onValueChange = { if (it.length <= 2) studentYearId.value = it },
                            label = { Text("First value") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .weight(0.5f)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        OutlinedTextField(
                            value = classId.value,
                            onValueChange = { if (it.length <= 3) classId.value = it },
                            label = { Text("Second value") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .weight(0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.size(5.dp))
                    OutlinedTextField(
                        value = subjectName.value,
                        onValueChange = { subjectName.value = it },
                        label = { Text("Subject name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Button(
                        content = { Text("Add filter") },
                        onClick = {
                            val item = SubjectCode(
                                studentYearId.value,
                                classId.value,
                                subjectName.value
                            )
                            addRequested(item)
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun SurfaceResetToDefault(
        onDelete: () -> Unit,
        expended: Boolean,
        onExpended: (() -> Unit)? = null,
    ) {
        CustomSurface {
            CustomTitleAndExpandableColumn(
                title = "Clear all filters",
                expanded = expended,
                onExpanded = { if (onExpended != null) onExpended() },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Just click button below to clear.\n" +
                                "Note: This will delete all filters you added before and cannot be undone.",
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                    Button(
                        content = { Text("Confirm clear") },
                        onClick = { onDelete() }
                    )
                }
            }
        }
    }

    private fun showSnackBarMessage(
        msg: String,
        closeOld: Boolean = true,
    ) {
        scope.launch {
            if (closeOld) {
                snackBarState.currentSnackbarData?.dismiss()
            }

            snackBarState.showSnackbar(msg)
        }
    }

    @Composable
    private fun CustomSurface(
        content: @Composable () -> Unit
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 5.dp, bottom = 5.dp, start = 10.dp, end = 10.dp)
                .clip(RoundedCornerShape(10.dp)),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            content()
        }
    }
}


fun NewsFilterSettingsActivity.getAppBroadcastReceiver(): AppBroadcastReceiver {
    object : AppBroadcastReceiver() {
        override fun onNewsReloadRequested() {}
        override fun onAccountReloadRequested(newsType: String) {}
        override fun onSettingsReloadRequested() { }
        override fun onNewsScrollToTopRequested() { }
        override fun onSnackBarMessage(title: String?, forceCloseOld: Boolean) { }

        override fun onPermissionRequested(
            permission: String?,
            granted: Boolean,
            notifyToUser: Boolean
        ) {
            onPermissionResult(permission, granted, notifyToUser)
        }
    }.apply {
        return this
    }
}

fun NewsFilterSettingsActivity.onPermissionResult(
    permission: String?,
    granted: Boolean,
    notifyToUser: Boolean = false
) {
    when (permission) {
        Manifest.permission.READ_EXTERNAL_STORAGE -> {
            if (granted) {
                mainViewModel.reloadAppBackground(
                    context = this,
                    type = mainViewModel.appSettings.value.backgroundImage.option
                )
            } else {
                mainViewModel.appSettings.value = mainViewModel.appSettings.value.modify(
                    optionToModify = AppSettings.APPEARANCE_BACKGROUNDIMAGE,
                    value = BackgroundImage(
                        option = BackgroundImageType.Unset,
                        path = null
                    )
                )
                mainViewModel.requestSaveChanges()
                mainViewModel.showSnackBarMessage(
                    "Missing permission for background image. " +
                            "This setting will be turned off to avoid another issues."
                )
            }
        }
        else -> { }
    }
}

fun NewsFilterSettingsActivity.registerBroadcastReceiver(context: Context) {
    LocalBroadcastManager.getInstance(context).registerReceiver(
        getAppBroadcastReceiver(),
        IntentFilter().apply {
            addAction(AppBroadcastReceiver.SNACKBARMESSAGE)
            addAction(AppBroadcastReceiver.NEWS_SCROLLALLTOTOP)
            addAction(AppBroadcastReceiver.RUNTIME_PERMISSION_REQUESTED)
        }
    )
}

fun NewsFilterSettingsActivity.checkSettingsPermissionOnStartup(
    mainViewModel: MainViewModel
) {
    val permissionList = arrayListOf<String>()

    // Read external storage - Background Image
    if (mainViewModel.appSettings.value.backgroundImage.option != BackgroundImageType.Unset) {
        if (!PermissionRequestActivity.checkPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        else onPermissionResult(Manifest.permission.READ_EXTERNAL_STORAGE, true)
    }

    if (permissionList.isNotEmpty()) {
        Intent(this, PermissionRequestActivity::class.java)
            .apply {
                putExtra("permissions.list", permissionList.toTypedArray())
            }
            .also {
                this.startActivity(it)
            }
    }
}