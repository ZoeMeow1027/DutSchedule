package io.zoemeow.dutschedule.activity

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.zoemeow.dutschedule.ui.component.permissionrequest.PermissionInformation

class PermissionRequestActivity : BaseActivity() {
    private val permissionStatusList = mutableStateListOf<PermissionCheckResult>()

    @Composable
    override fun OnPreloadOnce() {
        reloadPermissionStatus()
    }

    private fun reloadPermissionStatus() {
        permissionStatusList.clear()
        permissionStatusList.addAll(getAllPermissions(this))
    }

    @Composable
    override fun OnMainView(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color
    ) {
        MainView(
            context = context,
            snackBarHostState = snackBarHostState,
            containerColor = containerColor,
            contentColor = contentColor,
            navIconClicked = {
                setResult(RESULT_OK)
                finish()
            },
            fabClicked = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", context.packageName, null)
                context.startActivity(intent)
            },
            permissionRequest = {
                requestPermission(it)
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainView(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color,
        navIconClicked: (() -> Unit)? = null,
        fabClicked: (() -> Unit)? = null,
        permissionRequest: ((String) -> Unit)? = null
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        Scaffold(
            modifier = Modifier.fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            containerColor = containerColor,
            contentColor = contentColor,
            topBar = {
                LargeTopAppBar(
                    title = { Text(text = "Permissions request") },
                    colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                navIconClicked?.let { it() }
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
            bottomBar = {
                BottomAppBar(
                    containerColor = BottomAppBarDefaults.containerColor.copy(
                        alpha = getControlBackgroundAlpha()
                    ),
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = { fabClicked?.let { it() } },
                            icon = { Icon(Icons.Default.Settings, "") },
                            text = { Text("Open Android app settings") }
                        )
                    },
                    actions = {}
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .padding(horizontal = 15.dp),
                    content = {
                        Text(
                            "Below is all permissions requested by this app. Click a permission to grant that. " +
                                    "You can deny some permissions if you don't need some app features by open Android app settings.",
                            modifier = Modifier.padding(vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            content = {
                                permissionStatusList.forEach { item ->
                                    PermissionInformation(
                                        title = item.name,
//                                        description = "${item.code}\n\n${item.description}",
                                        description = "\n${item.description}",
                                        isRequired = false,
                                        isGranted = item.isGranted,
                                        padding = PaddingValues(bottom = 10.dp),
                                        opacity = getControlBackgroundAlpha(),
                                        clicked = {
                                            permissionRequest?.let {
                                                if (item.isGranted) {
                                                    showSnackBar("You're already granted this permission!", true)
                                                } else it(item.code)
                                            }
                                        }
                                    )
                                }
                            },
                        )
                    }
                )
            }
        )
    }

    private val permissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        // val permissionResultList = arrayListOf<Pair<String, Boolean>>()
        result.toList().forEach { _ -> // item ->
            // permissionResultList.add(Pair(item.first, item.second))
        }

        reloadPermissionStatus()
    }

    override fun onResume() {
        super.onResume()
        reloadPermissionStatus()
    }

    companion object {
        fun getAllPermissions(context: Context): List<PermissionCheckResult> {
            return listOf(
                checkPermissionNotification(context),
                checkPermissionManageExternalStorage(),
                checkPermissionScheduleExactAlarm(context)
            )
        }

        fun checkPermissionNotification(context: Context): PermissionCheckResult {
            return PermissionCheckResult(
                name = "Notifications",
                code = "android.permission.POST_NOTIFICATIONS",
                description = "Allow this app to send new announcements " +
                        "(news global and news subject) and other for you.",
                isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else true
            )
        }

        // https://stackoverflow.com/questions/73620790/android-13-how-to-request-write-external-storage
        fun checkPermissionManageExternalStorage(): PermissionCheckResult {
            return PermissionCheckResult(
                name = "Manage External Storage",
                code = "android.permission.MANAGE_EXTERNAL_STORAGE",
                description = "This app will use your current wallpaper as app background wallpaper. " +
                        "We promise not to upload or modify any data on your device, including your wallpaper. " +
                        "If you don't want to grant this permission, you can use \"Choose a image from media\" instead.",
                isGranted = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                        Environment.isExternalStorageManager()
                    else -> true
                }
            )
        }

        fun checkPermissionScheduleExactAlarm(context: Context): PermissionCheckResult {
            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return PermissionCheckResult(
                name = "Schedule Exact Alarm",
                code = "android.permission.SCHEDULE_EXACT_ALARM",
                description = "Allow this app to schedule service to update news in background for you.",
                isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else true
            )
        }
    }

    data class PermissionCheckResult(
        val name: String,
        val code: String,
        val description: String,
        val isGranted: Boolean
    )

    private fun requestPermission(permissionCode: String) {
        when (permissionCode) {
            Manifest.permission.SCHEDULE_EXACT_ALARM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.fromParts("package", packageName, null)
                    ).also {
                        this.startActivity(it)
                    }
                } else {
                    // TODO: Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM on Android 11 or older
                }
            }
            // https://stackoverflow.com/questions/73620790/android-13-how-to-request-write-external-storage
            Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.fromParts("package", packageName, null)
                    ).also {
                        this.startActivity(it)
                    }
                } else {
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null)
                    ).also {
                        this.startActivity(it)
                    }
                }
            }
            else -> {
                permissionRequestLauncher.launch(listOf(permissionCode).toTypedArray())
            }
        }
    }
}