package io.zoemeow.dutnotify

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.zoemeow.dutnotify.receiver.AppBroadcastReceiver
import io.zoemeow.dutnotify.ui.theme.DefaultActivityTheme

class PermissionRequestActivity: ComponentActivity() {
    private val permissionRequestList = arrayListOf<String>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.getStringArrayExtra("permissions.list")?.isNotEmpty() == true) {
            permissionRequestList.addAll(intent.getStringArrayExtra("permissions.list")!!)
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }

        setContent {
            DefaultActivityTheme {
                Scaffold(
                    topBar = {
                        SmallTopAppBar(
                            title = { Text("Requested permission") }
                        )
                    },
                ) { padding ->
                    MainScreen(padding = padding)
                }
            }
        }
    }

    @Composable
    private fun MainScreen(
        padding: PaddingValues,
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(padding)
                .padding(start = 10.dp, end = 10.dp)
        ) {
            Text(
                "- Some permissions request you to be accepted as you have enabled a function before.\n" +
                        "- If you deny, app will still be functional, but functions related to permission " +
                        "list below will be turned off.",
                modifier = Modifier
                    .padding(bottom = 15.dp)
            )
            permissionRequestList.forEach {
                Text(it)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 15.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    content = { Text("Request to accept") },
                    onClick = { requestPermission() },
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.size(5.dp))
                Button(
                    content = { Text("Deny") },
                    onClick = {
                        sendBroadcastToActivity(arrayListOf<Pair<String, Boolean>>().apply {
                            permissionRequestList.forEach {
                                this.add(Pair(it, false))
                            }
                        })
                        setResult(RESULT_CANCELED)
                        finish()
                    },
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(permissionRequestList.toTypedArray())
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val permissionResultList = arrayListOf<Pair<String, Boolean>>()
            result.toList().forEach { item ->
                permissionResultList.add(Pair(item.first, item.second))
            }
            sendBroadcastToActivity(permissionResultList)
            filterDeniedPermissionsAndFinish(permissionResultList)
        }

    private fun sendBroadcastToActivity(permissionList: ArrayList<Pair<String, Boolean>>) {
        permissionList.forEach { permissionResult ->
            val intent = Intent(AppBroadcastReceiver.RUNTIME_PERMISSION_REQUESTED)
            intent.putExtra(AppBroadcastReceiver.RUNTIME_PERMISSION_NAME, permissionResult.first)
            intent.putExtra(AppBroadcastReceiver.RUNTIME_PERMISSION_RESULT, permissionResult.second)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }

    private fun filterDeniedPermissionsAndFinish(permissionList: ArrayList<Pair<String, Boolean>>) {
        permissionList.forEach { permissionItem ->
            val getPermissionString = permissionRequestList.firstOrNull { item -> item == permissionItem.first }
            if (getPermissionString != null)
                permissionRequestList.remove(getPermissionString)
        }
        if (permissionRequestList.isEmpty()) {
            setResult(RESULT_OK)
            finish()
        }
    }

    companion object {
        fun checkPermission(
            context: Context,
            permission: String
        ): Boolean {
            return ContextCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}