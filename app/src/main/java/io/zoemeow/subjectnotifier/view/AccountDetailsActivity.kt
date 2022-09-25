package io.zoemeow.subjectnotifier.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutapi.objects.accounts.AccountInformation
import io.zoemeow.dutapi.objects.accounts.SubjectFeeItem
import io.zoemeow.dutapi.objects.accounts.SubjectScheduleItem
import io.zoemeow.subjectnotifier.R
import io.zoemeow.subjectnotifier.model.appsettings.AppSettings
import io.zoemeow.subjectnotifier.model.appsettings.BackgroundImage
import io.zoemeow.subjectnotifier.model.enums.BackgroundImageType
import io.zoemeow.subjectnotifier.model.enums.ProcessState
import io.zoemeow.subjectnotifier.model.enums.ServiceBroadcastOptions
import io.zoemeow.subjectnotifier.receiver.AppBroadcastReceiver
import io.zoemeow.subjectnotifier.service.AccountService
import io.zoemeow.subjectnotifier.ui.custom.SubjectPreview
import io.zoemeow.subjectnotifier.viewmodel.MainViewModel

@AndroidEntryPoint
class AccountDetailsActivity : BaseActivity() {
    private val scaffoldTitle = mutableStateOf("")
    internal lateinit var mainViewModel: MainViewModel

    @Composable
    override fun OnPreloadOnce() {
        setAppSettings(mainViewModel.appSettings.value)
        registerBroadcastReceiver(context = applicationContext)
        checkSettingsPermissionOnStartup(mainViewModel = mainViewModel)
    }

    @Composable
    override fun OnPreload() {
        mainViewModel = viewModel()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun OnMainView() {
        val type: String? = intent.getStringExtra("type")

        val swipeRefreshStateSubjectSchedule = rememberSwipeRefreshState(false)
        val swipeRefreshStateSubjectFee = rememberSwipeRefreshState(false)
        val swipeRefreshStateAccInfo = rememberSwipeRefreshState(false)

        LaunchedEffect(Unit) {
            when (type) {
                "subject_schedule" -> {
                    scaffoldTitle.value =
                        applicationContext.getString(R.string.account_page_subjectschedule)
                    val intentService = Intent(applicationContext, AccountService::class.java)
                    intentService.putExtra(
                        ServiceBroadcastOptions.ACTION,
                        ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTSCHEDULE
                    )
                    intentService.putExtra(
                        ServiceBroadcastOptions.SOURCE_COMPONENT,
                        MainActivity::class.java.name
                    )
                    applicationContext.startService(intentService)
                }
                "subject_fee" -> {
                    scaffoldTitle.value =
                        applicationContext.getString(R.string.account_page_subjectfee)
                    val intentService = Intent(applicationContext, AccountService::class.java)
                    intentService.putExtra(
                        ServiceBroadcastOptions.ACTION,
                        ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTSCHEDULE
                    )
                    intentService.putExtra(
                        ServiceBroadcastOptions.SOURCE_COMPONENT,
                        MainActivity::class.java.name
                    )
                    applicationContext.startService(intentService)
                    val intentService2 = Intent(applicationContext, AccountService::class.java)
                    intentService2.putExtra(
                        ServiceBroadcastOptions.ACTION,
                        ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTFEE
                    )
                    intentService.putExtra(
                        ServiceBroadcastOptions.SOURCE_COMPONENT,
                        MainActivity::class.java.name
                    )
                    applicationContext.startService(intentService2)
                }
                "account_information" -> {
                    scaffoldTitle.value =
                        applicationContext.getString(R.string.account_page_accinfo)
                    val intentService = Intent(applicationContext, AccountService::class.java)
                    intentService.putExtra(
                        ServiceBroadcastOptions.ACTION,
                        ServiceBroadcastOptions.ACTION_ACCOUNT_ACCOUNTINFORMATION
                    )
                    intentService.putExtra(
                        ServiceBroadcastOptions.SOURCE_COMPONENT,
                        MainActivity::class.java.name
                    )
                    applicationContext.startService(intentService)
                }
                else -> {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        }

        LaunchedEffect(
            mainViewModel.Account_Process_SubjectSchedule.value,
            mainViewModel.Account_Process_SubjectFee.value,
            mainViewModel.Account_Process_AccountInformation.value
        ) {
            swipeRefreshStateSubjectSchedule.isRefreshing =
                mainViewModel.Account_Process_SubjectSchedule.value == ProcessState.Running
            swipeRefreshStateSubjectFee.isRefreshing =
                mainViewModel.Account_Process_SubjectFee.value == ProcessState.Running
            swipeRefreshStateAccInfo.isRefreshing =
                mainViewModel.Account_Process_AccountInformation.value == ProcessState.Running
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
                                    tint = if (mainViewModel.isDarkTheme.value) Color.White else Color.Black,
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
            contentColor = if (mainViewModel.isDarkTheme.value) Color.White else Color.Black,
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            if (subjectScheduleItem.value != null) {
                SubjectPreview.SubjectScheduleDetails(
                    dialogEnabled = dialogEnabled.value,
                    item = subjectScheduleItem.value,
                    darkTheme = mainViewModel.isDarkTheme.value,
                    onClose = {
                        dialogEnabled.value = false
                    }
                )
            }
            when (type) {
                "subject_schedule" -> {
                    SubjectScheduleList(
                        padding = padding,
                        subjectScheduleList = mainViewModel.Account_Data_SubjectSchedule,
                        swipeRefreshState = swipeRefreshStateSubjectSchedule,
                        reloadRequested = {
                            val intentService = Intent(applicationContext, AccountService::class.java)
                            intentService.putExtra(
                                ServiceBroadcastOptions.ACTION,
                                ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTSCHEDULE
                            )
                            intentService.putExtra(
                                ServiceBroadcastOptions.SOURCE_COMPONENT,
                                MainActivity::class.java.name
                            )
                            startService(intentService)
                        }
                    )
                }
                "subject_fee" -> {
                    SubjectFeeList(
                        padding = padding,
                        subjectFeeList = mainViewModel.Account_Data_SubjectFee,
                        swipeRefreshState = swipeRefreshStateSubjectFee,
                        reloadRequested = {
                            val intentService = Intent(applicationContext, AccountService::class.java)
                            intentService.putExtra(
                                ServiceBroadcastOptions.ACTION,
                                ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTSCHEDULE
                            )
                            intentService.putExtra(
                                ServiceBroadcastOptions.SOURCE_COMPONENT,
                                MainActivity::class.java.name
                            )
                            applicationContext.startService(intentService)
                            val intentService2 = Intent(applicationContext, AccountService::class.java)
                            intentService2.putExtra(
                                ServiceBroadcastOptions.ACTION,
                                ServiceBroadcastOptions.ACTION_ACCOUNT_SUBJECTFEE
                            )
                            intentService.putExtra(
                                ServiceBroadcastOptions.SOURCE_COMPONENT,
                                MainActivity::class.java.name
                            )
                            applicationContext.startService(intentService2)
                        }
                    )
                }
                "account_information" -> {
                    AccountInformation(
                        padding = padding,
                        accountInformation = mainViewModel.Account_Data_AccountInformation.value,
                        swipeRefreshState = swipeRefreshStateAccInfo,
                        reloadRequested = {
                            val intentService = Intent(applicationContext, AccountService::class.java)
                            intentService.putExtra(
                                ServiceBroadcastOptions.ACTION,
                                ServiceBroadcastOptions.ACTION_ACCOUNT_ACCOUNTINFORMATION
                            )
                            intentService.putExtra(
                                ServiceBroadcastOptions.SOURCE_COMPONENT,
                                MainActivity::class.java.name
                            )
                            startService(intentService)
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
                            SubjectPreview.CustomText(
                                "Total price: ${
                                    subjectFeeList.sumOf { it.price }.toLong()
                                } VND"
                            )
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
                                    mainViewModel.Account_Data_SubjectSchedule.firstOrNull {
                                        it.id.equalsTwoDigits(
                                            item.id
                                        )
                                    }
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
                                text = "${item.credit} credit(s), ${item.price} VND (${if (item.debt) "Not completed yet" else "completed"})",
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

    private fun onPermissionResult(
        permission: String?,
        granted: Boolean,
        notifyToUser: Boolean = false
    ) {
        when (permission) {
            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                if (granted) {
                    // Reload settings
                    mainViewModel.appSettings.value = mainViewModel.appSettings.value.clone()
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
            else -> {}
        }
    }

    private fun checkSettingsPermissionOnStartup(
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
}