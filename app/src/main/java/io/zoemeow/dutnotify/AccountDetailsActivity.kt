package io.zoemeow.dutnotify

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.zoemeow.dutapi.objects.accounts.AccountInformation
import io.zoemeow.dutapi.objects.accounts.SubjectFeeItem
import io.zoemeow.dutapi.objects.accounts.SubjectScheduleItem
import io.zoemeow.dutnotify.model.appsettings.BackgroundImage
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.enums.BackgroundImageType
import io.zoemeow.dutnotify.model.enums.ProcessState
import io.zoemeow.dutnotify.ui.custom.SubjectPreview
import io.zoemeow.dutnotify.ui.theme.MainActivityTheme
import io.zoemeow.dutnotify.viewmodel.MainViewModel

class AccountDetailsActivity : ComponentActivity() {
    private val scaffoldTitle = mutableStateOf("")
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            mainViewModel = viewModel()

            val initialized = remember { mutableStateOf(false) }

            if (!initialized.value) {
                // Set to false to avoid another run
                initialized.value = true

                // Check permission with background image option
                // Only one request when user start app.
                if (mainViewModel.appSettings.value.backgroundImage.option != BackgroundImageType.Unset) {
                    if (PermissionRequestActivity.checkPermission(
                            this,
                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU)
                                Manifest.permission.READ_MEDIA_IMAGES
                            else Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        mainViewModel.reloadAppBackground(
                            context = this,
                            type = mainViewModel.appSettings.value.backgroundImage.option
                        )
                    } else {
                        val intent = Intent(this, PermissionRequestActivity::class.java)
                        intent.putExtra(
                            "permission.requested",
                            arrayOf(
                                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU)
                                    Manifest.permission.READ_MEDIA_IMAGES
                                else Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        )
                        permissionRequestActivityResult.launch(intent)
                    }
                }
            }

            MainActivityTheme(
                appSettings = mainViewModel.appSettings.value,
                content = @Composable {
                    MainScreen(
                        mainViewModel = mainViewModel,
                        intent = intent,
                    )
                },
                backgroundDrawable = mainViewModel.mainActivityBackgroundDrawable.value,
                appModeChanged = {
                    // Trigger for dark mode detection.
                    mainViewModel.mainActivityIsDarkTheme.value = it
                },
            )
        }
    }

    private val permissionRequestActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
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

//            mainViewModel.setPendingNotifications(
//                "Missing permission: READ_EXTERNAL_STORAGE. " +
//                        "This will revert background image option is unset.",
//                true
//            )
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(
        mainViewModel: MainViewModel,
        intent: Intent,
    ) {
        val type: String? = intent.getStringExtra("type")

        val swipeRefreshStateSubjectSchedule = rememberSwipeRefreshState(false)
        val swipeRefreshStateSubjectFee = rememberSwipeRefreshState(false)
        val swipeRefreshStateAccInfo = rememberSwipeRefreshState(false)

        LaunchedEffect(Unit) {
            when (type) {
                "subject_schedule" -> {
                    scaffoldTitle.value =
                        applicationContext.getString(R.string.account_page_subjectschedule)
                    mainViewModel.accountDataStore.fetchSubjectSchedule()
                }
                "subject_fee" -> {
                    scaffoldTitle.value =
                        applicationContext.getString(R.string.account_page_subjectfee)
                    mainViewModel.accountDataStore.fetchSubjectSchedule()
                    mainViewModel.accountDataStore.fetchSubjectFee()
                }
                "account_information" -> {
                    scaffoldTitle.value =
                        applicationContext.getString(R.string.account_page_accinfo)
                    mainViewModel.accountDataStore.fetchAccountInformation()
                }
                else -> {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        }

        LaunchedEffect(
            mainViewModel.accountDataStore.procAccSubSch.value,
            mainViewModel.accountDataStore.procAccSubFee.value,
            mainViewModel.accountDataStore.procAccInfo.value
        ) {
            swipeRefreshStateSubjectSchedule.isRefreshing =
                mainViewModel.accountDataStore.procAccSubSch.value == ProcessState.Running
            swipeRefreshStateSubjectFee.isRefreshing =
                mainViewModel.accountDataStore.procAccSubFee.value == ProcessState.Running
            swipeRefreshStateAccInfo.isRefreshing =
                mainViewModel.accountDataStore.procAccInfo.value == ProcessState.Running
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    title = {
                        Text(scaffoldTitle.value)
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                                .clip(CircleShape)
                                .clickable {
                                    setResult(RESULT_OK)
                                    finish()
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
            if (subjectScheduleItem.value != null) {
                SubjectPreview.SubjectScheduleDetails(
                    dialogEnabled = dialogEnabled.value,
                    item = subjectScheduleItem.value,
                    darkTheme = mainViewModel.mainActivityIsDarkTheme.value,
                    onClose = {
                        dialogEnabled.value = false
                    }
                )
            }
            when (type) {
                "subject_schedule" -> {
                    SubjectScheduleList(
                        padding = padding,
                        subjectScheduleList = mainViewModel.accountDataStore.subjectSchedule,
                        swipeRefreshState = swipeRefreshStateSubjectSchedule,
                        reloadRequested = {
                            mainViewModel.accountDataStore.fetchSubjectSchedule(mainViewModel.appSettings.value.schoolYear)
                        }
                    )
                }
                "subject_fee" -> {
                    SubjectFeeList(
                        padding = padding,
                        subjectFeeList = mainViewModel.accountDataStore.subjectFee,
                        swipeRefreshState = swipeRefreshStateSubjectFee,
                        reloadRequested = {
                            mainViewModel.accountDataStore.fetchSubjectSchedule(mainViewModel.appSettings.value.schoolYear)
                            mainViewModel.accountDataStore.fetchSubjectFee(mainViewModel.appSettings.value.schoolYear)
                        }
                    )
                }
                "account_information" -> {
                    AccountInformation(
                        padding = padding,
                        accountInformation = mainViewModel.accountDataStore.accountInformation.value,
                        swipeRefreshState = swipeRefreshStateAccInfo,
                        reloadRequested = {
                            mainViewModel.accountDataStore.fetchAccountInformation()
                        }
                    )
                }
                else -> {}
            }
        }
    }

    private val dialogEnabled = mutableStateOf(false)
    private val subjectScheduleItem: MutableState<SubjectScheduleItem?> = mutableStateOf(null)

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun SubjectScheduleList(
        padding: PaddingValues,
        subjectScheduleList: SnapshotStateList<SubjectScheduleItem>,
        swipeRefreshState: SwipeRefreshState,
        reloadRequested: () -> Unit,
    ) {
        SwipeRefresh(
            state = swipeRefreshState,
            modifier = Modifier.padding(padding),
            onRefresh = {
                swipeRefreshState.isRefreshing = true
                reloadRequested()
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                stickyHeader {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(
                                    start = 15.dp,
                                    end = 15.dp,
                                    top = 10.dp,
                                    bottom = 10.dp
                                ),
                        ) {
                            SubjectPreview.CustomText("Total credit: ${subjectScheduleList.sumOf { it.credit }}")
                            SubjectPreview.CustomText("Click a subject to view its details.")
                        }
                    }
                }
                items(subjectScheduleList) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 10.dp, bottom = 0.dp, start = 15.dp, end = 15.dp)
                            // https://www.android--code.com/2021/09/jetpack-compose-box-rounded-corners_25.html
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable {
                                subjectScheduleItem.value = item
                                dialogEnabled.value = true
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(
                                start = 15.dp,
                                end = 15.dp,
                                top = 10.dp,
                                bottom = 10.dp
                            )
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = item.lecturer,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun SubjectFeeList(
        padding: PaddingValues,
        subjectFeeList: SnapshotStateList<SubjectFeeItem>,
        swipeRefreshState: SwipeRefreshState,
        reloadRequested: () -> Unit
    ) {
        SwipeRefresh(
            state = swipeRefreshState,
            modifier = Modifier.padding(padding),
            onRefresh = {
                swipeRefreshState.isRefreshing = true
                reloadRequested()
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
            ) {
                stickyHeader {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(
                                    start = 15.dp,
                                    end = 15.dp,
                                    top = 10.dp,
                                    bottom = 10.dp
                                ),
                        ) {
                            SubjectPreview.CustomText("Total credit: ${subjectFeeList.sumOf { it.credit }}")
                            SubjectPreview.CustomText("Total price: ${subjectFeeList.sumOf { it.price }.toLong()} VND")
                            SubjectPreview.CustomText(
                                @Suppress("ReplaceSizeCheckWithIsNotEmpty")
                                if (subjectFeeList.count { it.debt == true } > 0)
                                    "${subjectFeeList.count { it.debt == true }} subject${if (subjectFeeList.count { it.debt == true } > 0) "s" else ""} isn't completed payment yet"
                                else "Completed payment"
                            )
                            SubjectPreview.CustomText("Click a subject to view its details.")
                        }
                    }
                }
                items(subjectFeeList) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 10.dp, bottom = 0.dp, start = 15.dp, end = 15.dp)
                            // https://www.android--code.com/2021/09/jetpack-compose-box-rounded-corners_25.html
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable {
                                subjectScheduleItem.value =
                                    mainViewModel.accountDataStore.subjectSchedule.firstOrNull { it.id.toString(false) == item.id.toString(false) }
                                dialogEnabled.value = true
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(
                                start = 15.dp,
                                end = 15.dp,
                                top = 10.dp,
                                bottom = 10.dp
                            )
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = "${item.credit} credit(s), ${item.price} VND (${if (item.debt) "Not purchased yet" else "Purchased"})",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AccountInformation(
        padding: PaddingValues,
        accountInformation: AccountInformation?,
        swipeRefreshState: SwipeRefreshState,
        reloadRequested: () -> Unit,
    ) {
        SwipeRefresh(
            state = swipeRefreshState,
            modifier = Modifier.padding(padding),
            onRefresh = {
                swipeRefreshState.isRefreshing = true
                reloadRequested()
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
            ) {
                item {
                    if (accountInformation != null) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(
                                start = 15.dp,
                                end = 15.dp,
                                top = 10.dp,
                                bottom = 10.dp
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(top = 10.dp, bottom = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = accountInformation.name,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                            SubjectPreview.CustomText("ID - Class: ${accountInformation.studentId} - ${accountInformation.schoolClass}")
                            SubjectPreview.CustomText("Date of birth: ${accountInformation.dateOfBirth}")
                            SubjectPreview.CustomText("National ID Card: ${accountInformation.nationalIdCard} (${accountInformation.nationalIdCardIssueDate} at ${accountInformation.nationalIdCardIssuePlace})")
                            SubjectPreview.CustomText("Citizen ID Card: ${accountInformation.citizenIdCard} (${accountInformation.citizenIdCardIssueDate})")
                            SubjectPreview.CustomText("Bank ID: ${accountInformation.accountBankId} (at ${accountInformation.accountBankName})")
                            SubjectPreview.CustomText("School email: ${accountInformation.schoolEmail}")
                        }
                    }
                }
            }
        }
    }
}