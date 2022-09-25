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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutapi.objects.accounts.SubjectScheduleItem
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.appsettings.BackgroundImage
import io.zoemeow.dutnotify.model.appsettings.SubjectCode
import io.zoemeow.dutnotify.model.enums.BackgroundImageType
import io.zoemeow.dutnotify.model.enums.LoginState
import io.zoemeow.dutnotify.model.enums.ProcessState
import io.zoemeow.dutnotify.model.enums.ServiceBroadcastOptions
import io.zoemeow.dutnotify.receiver.AppBroadcastReceiver
import io.zoemeow.dutnotify.service.AccountService
import io.zoemeow.dutnotify.ui.controls.CustomTitleAndExpandableColumn
import io.zoemeow.dutnotify.ui.theme.MainActivityTheme
import io.zoemeow.dutnotify.viewmodel.NewsFilterSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewsFilterSettingsActivity : ComponentActivity() {
    companion object {
        private var isInitialized = false
    }

    private lateinit var newsFilterViewModel: NewsFilterSettingsViewModel
    private lateinit var snackBarState: SnackbarHostState
    private lateinit var scope: CoroutineScope

    /**
     * Store all selected subject which user chosen.
     */
    private val selectedSubjects = mutableStateListOf<SubjectCode>()

    /**
     * Store all available subject which didn't in selected subjects.
     */
    private val availableSubjectFromAccount = mutableStateListOf<SubjectScheduleItem>()

    /**
     * Define selected item index in selected subjects list.
     */
    private val selectedAvailableSubjectFromAccountIndex = mutableStateOf(0)

    /**
     * Define selected item name. This will also display selected subject name
     * in outlined text box in 'add by subject schedule list' area.
     */
    private val selectedAvailableSubjectFromAccountName = mutableStateOf("")

    /**
     * Index for selected main body for expend.
     */
    private val selectedMainBodyIndex = mutableStateOf(0)

    /**
     * Detect if a setting in this activity has changed.
     */
    private val modifiedSettings = mutableStateOf(false)

    /**
     * Show 'Unsaved changes' dialog.
     */
    private val modifiedSettingsDialog = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            snackBarState = SnackbarHostState()
            scope = rememberCoroutineScope()
            newsFilterViewModel = viewModel()

            if (!isInitialized) {
                registerBroadcastReceiver(context = applicationContext)
                checkSettingsPermissionOnStartup(mainViewModel = newsFilterViewModel)

                // Re-login to receive new data from server.
                Intent(this@NewsFilterSettingsActivity, AccountService::class.java).apply {
                    putExtra(ServiceBroadcastOptions.ACTION, ServiceBroadcastOptions.ACTION_ACCOUNT_LOGINSTARTUP)
                    putExtra(ServiceBroadcastOptions.ARGUMENT_ACCOUNT_LOGINSTARTUP_PRELOAD, true)
                    putExtra(
                        ServiceBroadcastOptions.SOURCE_COMPONENT,
                        NewsFilterSettingsActivity::class.java.name
                    )
                }.also {
                    this@NewsFilterSettingsActivity.startService(it)
                }

                isInitialized = true
            }

            MainActivityTheme(
                appSettings = newsFilterViewModel.appSettings.value,
                content = @Composable {
                    MainScreen(mainViewModel = newsFilterViewModel)
                },
                appModeChanged = {
                    // Trigger for dark mode detection.
                    newsFilterViewModel.mainActivityIsDarkTheme.value = it
                },
            )
        }
    }

    override fun onDestroy() {
        isInitialized = false
        finish()
        super.onDestroy()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen(mainViewModel: NewsFilterSettingsViewModel) {
        MainDialog_ModifiedSettings(enabled = modifiedSettingsDialog)

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackBarState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(id = R.string.settings_subjectnewsfilter_name))
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
                                    if (modifiedSettings.value) {
                                        modifiedSettingsDialog.value = true
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
            LaunchedEffect(Unit) {
                selectedSubjects.addAll(mainViewModel.appSettings.value.newsFilterList)
            }

            BackHandler(
                enabled = modifiedSettings.value,
                onBack = { modifiedSettingsDialog.value = true }
            )

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
                MainBody_CurrentSubjectList()

                // Load from your subject schedule to add to filter.
                MainBody_AvailableSubjectList(
                    expended = selectedMainBodyIndex.value == 0,
                    onExpended = {
                        selectedMainBodyIndex.value = 0
                    }
                )
                // Add filter manually.
                MainBody_AddManually(
                    expended = selectedMainBodyIndex.value == 1,
                    onExpended = {
                        selectedMainBodyIndex.value = 1
                    }
                )
                // Reset to default
                MainBody_ClearAll(
                    expended = selectedMainBodyIndex.value == 2,
                    onExpended = {
                        selectedMainBodyIndex.value = 2
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun MainDialog_ModifiedSettings(
        enabled: MutableState<Boolean>
    ) {
        if (enabled.value) {
            AlertDialog(
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                ),
                modifier = Modifier
                    .padding(15.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                onDismissRequest = { enabled.value = false },
                title = { Text(stringResource(id = R.string.subjectnewsfilter_savechanges_title)) },
                dismissButton = {
                    Row {
                        // Just disable this dialog
                        TextButton(
                            content = { Text(stringResource(id = R.string.option_cancel)) },
                            onClick = { enabled.value = false }
                        )
                        Spacer(modifier = Modifier.size(5.dp))
                        // Dismiss changed settings.
                        TextButton(
                            content = { Text(stringResource(id = R.string.option_no)) },
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
                        content = { Text(stringResource(id = R.string.option_yes)) },
                        onClick = {
                            saveChanges()
                            setResult(RESULT_OK)
                            finish()
                        }
                    )
                },
                text = {
                    Text(stringResource(id = R.string.subjectnewsfilter_savechanges_description))
                },
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainBody_CurrentSubjectList() {
        CustomSurface {
            CustomTitleAndExpandableColumn(
                title = stringResource(id = R.string.subjectnewsfilter_currentlist_name),
                expanded = true,
                onExpanded = { },
            ) {
                if (selectedSubjects.isEmpty()) {
                    Text(text = stringResource(id = R.string.subjectnewsfilter_currentlist_empty))
                } else {
                    Text(
                        text = stringResource(id = R.string.subjectnewsfilter_currentlist_notempty),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        mainAxisAlignment = FlowMainAxisAlignment.Center
                    ) {
                        selectedSubjects.forEach {
                            InputChip(
                                selected = false,
                                onClick = {
                                    selectedSubjects.remove(it)
                                    modifiedSettings.value = true
                                    updateTemporarySettings()

                                    showSnackBarMessage(
                                        String.format(
                                            getString(R.string.subjectnewsfilter_snackbar_deleted),
                                            it.name
                                        )
                                    )
                                },
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
    private fun MainBody_AvailableSubjectList(
        expended: Boolean,
        onExpended: (() -> Unit)? = null
    ) {
        CustomSurface {
            CustomTitleAndExpandableColumn(
                title = stringResource(id = R.string.subjectnewsfilter_addbyschedule_name),
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
                        when (newsFilterViewModel.Account_LoginProcess.value) {
                            LoginState.LoggingIn -> {
                                Text(
                                    text = stringResource(id = R.string.subjectnewsfilter_addbyschedule_loggingin),
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                                CircularProgressIndicator()
                            }
                            LoginState.NotLoggedInButRemembered,
                            LoginState.NotLoggedIn,
                            LoginState.NotTriggered,
                            LoginState.AccountLocked -> {
                                Text(
                                    text = stringResource(id = R.string.subjectnewsfilter_addbyschedule_checkyouraccount),
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                            }
                            LoginState.LoggedIn -> {
                                when (newsFilterViewModel.Account_Process_SubjectSchedule.value) {
                                    ProcessState.Running -> {
                                        Text(
                                            text = stringResource(id = R.string.subjectnewsfilter_addbyschedule_loadingsubject),
                                            modifier = Modifier.padding(bottom = 10.dp)
                                        )
                                        CircularProgressIndicator()
                                    }
                                    else -> {
                                        val dropDownExpanded = remember { mutableStateOf(false) }
                                        LaunchedEffect(Unit) {
                                            updateTemporarySettings()
                                        }

                                        Text(
                                            text = stringResource(id = R.string.subjectnewsfilter_addbyschedule_addtips),
                                            modifier = Modifier.padding(bottom = 15.dp)
                                        )
                                        ExposedDropdownMenuBox(
                                            expanded = dropDownExpanded.value,
                                            onExpandedChange = {
                                                if (availableSubjectFromAccount.isNotEmpty())
                                                    dropDownExpanded.value = !dropDownExpanded.value
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 5.dp)
                                        ) {
                                            OutlinedTextField(
                                                readOnly = true,
                                                value = selectedAvailableSubjectFromAccountName.value,
                                                onValueChange = {},
                                                label = { Text(stringResource(id = R.string.subjectnewsfilter_addbyschedule_adddropdownname)) },
                                                trailingIcon = {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                                        expanded = dropDownExpanded.value
                                                    )
                                                },
                                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 5.dp)
                                            )
                                            ExposedDropdownMenu(
                                                expanded = dropDownExpanded.value,
                                                onDismissRequest = {
                                                    dropDownExpanded.value = false
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight()
                                                    .padding(bottom = 5.dp)
                                            ) {
                                                availableSubjectFromAccount.forEach {
                                                    DropdownMenuItem(
                                                        text = { Text(it.name) },
                                                        onClick = {
                                                            selectedMainBodyIndex.value =
                                                                availableSubjectFromAccount.indexOf(
                                                                    it
                                                                )
                                                            dropDownExpanded.value = false
                                                            updateTemporarySettings()
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
                                                    Text(stringResource(id = R.string.option_refresh))
                                                },
                                                onClick = {
                                                    Intent(
                                                        this@NewsFilterSettingsActivity,
                                                        AccountService::class.java
                                                    ).apply {
                                                        putExtra(
                                                            ServiceBroadcastOptions.ACTION,
                                                            ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTSCHEDULE
                                                        )
                                                        putExtra(
                                                            ServiceBroadcastOptions.SOURCE_COMPONENT,
                                                            NewsFilterSettingsActivity::class.java.name
                                                        )
                                                    }.also {
                                                        this@NewsFilterSettingsActivity.startService(
                                                            it
                                                        )
                                                    }
                                                },
                                                modifier = Modifier
                                                    .padding(end = 5.dp)
                                                //.weight(1f)
                                            )
                                            Button(
                                                content = {
                                                    Text(text = stringResource(id = R.string.option_add))
                                                },
                                                onClick = {
                                                    try {
                                                        val subjectScheduleItem =
                                                            availableSubjectFromAccount[selectedAvailableSubjectFromAccountIndex.value]
                                                        val item = SubjectCode(
                                                            studentYearId = subjectScheduleItem.id.studentYearId,
                                                            classId = subjectScheduleItem.id.classId,
                                                            name = subjectScheduleItem.name
                                                        )

                                                        if (!isDuplicate(item))
                                                            selectedSubjects.add(item)
                                                        modifiedSettings.value = true
                                                        updateTemporarySettings()

                                                        showSnackBarMessage(
                                                            String.format(
                                                                getString(R.string.subjectnewsfilter_snackbar_added),
                                                                item.name
                                                            )
                                                        )
                                                    } catch (ex: Exception) {
                                                        // Can't add to list.
                                                    }
                                                },
                                                modifier = Modifier
                                                    .padding(start = 5.dp)
                                                //.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainBody_AddManually(
        expended: Boolean,
        onExpended: (() -> Unit)? = null
    ) {
        val studentYearId = remember { mutableStateOf("") }
        val classId = remember { mutableStateOf("") }
        val subjectName = remember { mutableStateOf("") }
        CustomSurface {
            CustomTitleAndExpandableColumn(
                title = stringResource(id = R.string.subjectnewsfilter_addmanually_title),
                expanded = expended,
                onExpanded = { if (onExpended != null) onExpended() },
            ) {
                Text(
                    text = stringResource(id = R.string.subjectnewsfilter_addmanually_addtips),
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
                        label = { Text(stringResource(id = R.string.subjectnewsfilter_addmanually_subjecttextbox)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Button(
                        content = { Text(stringResource(id = R.string.option_add)) },
                        onClick = {
                            val item = SubjectCode(
                                studentYearId.value,
                                classId.value,
                                subjectName.value
                            )

                            if (!isDuplicate(item))
                                selectedSubjects.add(item)
                            modifiedSettings.value = true
                            updateTemporarySettings()

                            showSnackBarMessage(
                                String.format(
                                    getString(R.string.subjectnewsfilter_snackbar_added),
                                    item.name
                                )
                            )
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun MainBody_ClearAll(
        expended: Boolean,
        onExpended: (() -> Unit)? = null
    ) {
        CustomSurface {
            CustomTitleAndExpandableColumn(
                title = stringResource(id = R.string.subjectnewsfilter_clearall_title),
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
                        text = stringResource(id = R.string.subjectnewsfilter_clearall_deletetips),
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                    Button(
                        content = { Text(stringResource(id = R.string.options_clearall)) },
                        onClick = {
                            selectedSubjects.clear()
                            modifiedSettings.value = true
                            updateTemporarySettings()
                            showSnackBarMessage(getString(R.string.subjectnewsfilter_snackbar_deletedall))
                        }
                    )
                }
            }
        }
    }

    private fun isDuplicate(input: SubjectCode): Boolean {
        return try {
            return selectedSubjects.any { input.isEquals(it) }
        } catch (ex: Exception) {
            true
        }
    }

    private fun updateTemporarySettings() {
        fun isInIndexAvailableList(value: Int): Boolean {
            return (value >= 0) && (value <= availableSubjectFromAccount.size - 1)
        }

        availableSubjectFromAccount.apply {
            clear()
            newsFilterViewModel.Account_Data_SubjectSchedule.forEach {
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

        if (!isInIndexAvailableList(selectedAvailableSubjectFromAccountIndex.value)) {
            if (availableSubjectFromAccount.isEmpty()) {
                selectedAvailableSubjectFromAccountIndex.value = -1
                selectedAvailableSubjectFromAccountName.value =
                    getString(R.string.subjectnewsfilter_addfromsubjectschedule_nomore)
            } else {
                selectedAvailableSubjectFromAccountIndex.value = 0
                selectedAvailableSubjectFromAccountName.value =
                    availableSubjectFromAccount[selectedAvailableSubjectFromAccountIndex.value].name
            }
        } else selectedAvailableSubjectFromAccountName.value =
            availableSubjectFromAccount[selectedAvailableSubjectFromAccountIndex.value].name
    }

    private fun saveChanges() {
        newsFilterViewModel.appSettings.value =
            newsFilterViewModel.appSettings.value.modify(
                optionToModify = AppSettings.NEWSFILTER_FILTERLIST,
                value = arrayListOf<SubjectCode>().apply {
                    addAll(selectedSubjects)
                }
            )

        newsFilterViewModel.requestSaveChanges()
        showSnackBarMessage(application.getString(R.string.subjectnewsfilter_snackbar_successful))
        modifiedSettings.value = false
        modifiedSettingsDialog.value = false
    }

    private fun showSnackBarMessage(
        msg: String,
    ) {
        scope.launch {
            snackBarState.currentSnackbarData?.dismiss()
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

    private fun getAppBroadcastReceiver(): AppBroadcastReceiver {
        object : AppBroadcastReceiver() {
            override fun onNewsReloadRequested() {}
            override fun onAccountReloadRequested(newsType: String) {}
            override fun onSettingsReloadRequested() {}
            override fun onNewsScrollToTopRequested() {}
            override fun onSnackBarMessage(title: String?, forceCloseOld: Boolean) {}

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

    private fun registerBroadcastReceiver(context: Context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(
            getAppBroadcastReceiver(),
            IntentFilter().apply {
                addAction(AppBroadcastReceiver.SNACKBARMESSAGE)
                addAction(AppBroadcastReceiver.NEWS_SCROLLALLTOTOP)
                addAction(AppBroadcastReceiver.RUNTIME_PERMISSION_REQUESTED)
            }
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPermissionResult(
        permission: String?,
        granted: Boolean,
        notifyToUser: Boolean = false
    ) {
        // TODO: UNUSED_PARAMETER
        when (permission) {
            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                if (granted) {
                    newsFilterViewModel.reloadAppBackground(
                        context = this,
                        type = newsFilterViewModel.appSettings.value.backgroundImage.option
                    )
                } else {
                    newsFilterViewModel.appSettings.value =
                        newsFilterViewModel.appSettings.value.modify(
                            optionToModify = AppSettings.APPEARANCE_BACKGROUNDIMAGE,
                            value = BackgroundImage(
                                option = BackgroundImageType.Unset,
                                path = null
                            )
                        )
                    newsFilterViewModel.requestSaveChanges()
//                    mainViewModel.showSnackBarMessage(
//                        "Missing permission for background image. " +
//                                "This setting will be turned off to avoid another issues."
//                    )
                }
            }
            else -> {}
        }
    }

    private fun checkSettingsPermissionOnStartup(
        mainViewModel: NewsFilterSettingsViewModel
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
}