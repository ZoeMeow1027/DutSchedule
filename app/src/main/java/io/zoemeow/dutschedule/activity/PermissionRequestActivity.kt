package io.zoemeow.dutschedule.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.zoemeow.dutschedule.model.permissionrequest.PermissionInfo
import io.zoemeow.dutschedule.ui.component.permissionrequest.PermissionInformation

class PermissionRequestActivity : BaseActivity() {
    @Composable
    override fun OnMainView(padding: PaddingValues) {
        val context = LocalContext.current
        MainView(
            padding = padding,
            context = context,
            navIconClicked = {
                setResult(RESULT_OK)
                finish()
            },
            fabClicked = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", context.packageName, null)
                context.startActivity(intent)
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainView(
        padding: PaddingValues,
        context: Context,
        navIconClicked: (() -> Unit)? = null,
        fabClicked: (() -> Unit)? = null
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(text = "Permission request") },
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
                    }
                )
            },
            bottomBar = {
                BottomAppBar(
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = { fabClicked?.let { it() } },
                            icon = { Icon(Icons.Default.Settings, "") },
                            text = { Text("Open app permission") }
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
                            "Below is all permissions requested by this app. You can " +
                                    "deny some permissions if you don't need some app features.",
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            content = {
                                generatePermissionList().forEach { item ->
                                    PermissionInformation(
                                        title = item.name,
                                        description = "${item.code}\n\n${item.description}",
                                        isRequired = item.required,
                                        isGranted = isPermissionGranted(
                                            item,
                                            context
                                        ),
                                        padding = PaddingValues(bottom = 10.dp)
                                    )
                                }
                            },
                        )
                    }
                )
            }
        )
    }

    companion object {
        fun generatePermissionList(): List<PermissionInfo> {
            return listOf(
                PermissionInfo(
                    name = "Notifications (optimal)",
                    code = "android.permission.POST_NOTIFICATIONS",
                    minSdk = 33,
                    description = "Allow this app to send new announcements " +
                            "(news global and news subject) and other for you.",
                    required = false,
                ),
                PermissionInfo(
                    name = "Manage External Storage (optimal)",
                    code = "android.permission.MANAGE_EXTERNAL_STORAGE",
                    minSdk = 30,
                    description = "Allow this app to get your current launcher wallpaper.",
                    required = false,
                )
            )
        }

        fun isPermissionGranted(permissionInfo: PermissionInfo, context: Context): Boolean {
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