package io.zoemeow.dutschedule.activity

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
import io.zoemeow.dutschedule.model.permissionrequest.PermissionInfo
import io.zoemeow.dutschedule.model.permissionrequest.PermissionList
import io.zoemeow.dutschedule.ui.component.permissionrequest.PermissionInformation

class PermissionRequestActivity : BaseActivity() {
    private val permissionStatusList = mutableStateListOf<PermissionInfo>()

    @Composable
    override fun OnPreloadOnce() {
        reloadPermissionStatus()
    }

    private fun reloadPermissionStatus() {
        permissionStatusList.clear()
        permissionStatusList.addAll(PermissionList.getAllRequiredPermissions())
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
                permissionRequestLauncher.launch(listOf(it).toTypedArray())
            },
            permissionExtraAction = {
                context.startActivity(it)
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
        permissionRequest: ((String) -> Unit)? = null,
        permissionExtraAction: ((Intent) -> Unit)? = null
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
                                        title = "${item.name}${if (!item.required) " (optimal)" else ""}",
//                                        description = "${item.code}\n\n${item.description}",
                                        description = "\n${item.description}",
                                        isRequired = item.required,
                                        isGranted = isPermissionGranted(
                                            item,
                                            context
                                        ),
                                        padding = PaddingValues(bottom = 10.dp),
                                        opacity = getControlBackgroundAlpha(),
                                        clicked = {
                                            if (!isPermissionGranted(item, context)) {
                                                if (item.extraAction == null) {
                                                    permissionRequest?.let { it(item.code) }
                                                } else {
                                                    permissionExtraAction?.let { it(item.extraAction) }
                                                }
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
        val permissionResultList = arrayListOf<Pair<String, Boolean>>()
        result.toList().forEach { item ->
            permissionResultList.add(Pair(item.first, item.second))
        }

        reloadPermissionStatus()
    }

    override fun onResume() {
        super.onResume()
        reloadPermissionStatus()
    }

    companion object {
        // https://stackoverflow.com/questions/73620790/android-13-how-to-request-write-external-storage
        fun isPermissionGranted(permissionInfo: PermissionInfo, context: Context): Boolean {
            // MANAGE_EXTERNAL_STORAGE
            if (permissionInfo.code == "android.permission.MANAGE_EXTERNAL_STORAGE") {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Environment.isExternalStorageManager()
                } else {
                    return isPermissionGranted(permissionInfo.code, permissionInfo.minSdk, context)
                }
            }

            // Another permissions
            return isPermissionGranted(permissionInfo.code, permissionInfo.minSdk, context)
        }

        private fun isPermissionGranted(
            permissionCode: String,
            minSdk: Int,
            context: Context
        ): Boolean {
            if (Build.VERSION.SDK_INT < minSdk)
                return true

            return ContextCompat.checkSelfPermission(
                context, permissionCode
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}