package io.zoemeow.dutapp.android.view.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.zoemeow.dutapp.android.ui.theme.DefaultActivityTheme

class PermissionRequestActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareData(intent)

        setContent {
            DefaultActivityTheme {
                Scaffold(
                    topBar = {
                        SmallTopAppBar(
                            colors = TopAppBarDefaults.smallTopAppBarColors(
                                containerColor = Color.Transparent
                            ),
                            title = { Text("Permission(s) need your action") }
                        )
                    },
                ) { padding ->
                    Surface(
                        modifier = Modifier
                            .padding(padding)
                            .padding(start = 10.dp, end = 10.dp),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        @Composable
                        fun PermissionList(permissions: SnapshotStateList<PermissionRequesting>) {
                            permissions.forEach { permission ->
                                Text(text = "${permission.permission}${if (permission.granted) " (granted)" else ""}")
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Text(
                                text = if (permissions.size == 1)
                                    "This permission need your action to continue:"
                                else "These permissions need your action to continue:",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            PermissionList(permissions = permissions)
                            Spacer(modifier = Modifier.size(15.dp))
                            Text(
                                text = "If you don't accept, functions which needs permissions will not working unless you accept them.",
                            )
                            Spacer(modifier = Modifier.size(10.dp))
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        requestPermissionLauncher.launch(
                                            permissions.map { it.permission }.toTypedArray()
                                        )
                                    },
                                    content = {
                                        Text("Accept")
                                    }
                                )
                                Spacer(modifier = Modifier.size(5.dp))
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        if (permissions.any { it.granted } && permissions.any { !it.granted }) {
                                            notFullyPermission()
                                        }
                                        else cancelPermission()
                                    },
                                    content = {
                                        Text("Cancel")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private data class PermissionRequesting(
        val permission: String,
        var granted: Boolean = false,
    )

    private val permissions = mutableStateListOf<PermissionRequesting>()
    private fun isGrantedAll(): Boolean {
        val permissionNum = permissions.size
        val permissionGranted = permissions.filter { it.granted }.size

        return permissionGranted == permissionNum
    }

    private val requestPermissionLauncher = registerForActivityResult(RequestMultiplePermissions()) { result ->
        result.toList().forEach { pair ->
            permissions.firstOrNull { it.permission == pair.first }?.granted = pair.second
        }
        if (isGrantedAll()) {
            finishPermission()
        }
    }

    private fun prepareData(intent: Intent) {
        intent.getStringArrayExtra("permission.requested")?.toList()?.forEach {
            permissions.add(
                PermissionRequesting(
                    permission = it,
                    granted = false
                )
            )
        }
        if (permissions.size == 0) {
            cancelPermission()
        }
    }

    private fun notFullyPermission() {
        val intentResult = Intent()
        intentResult.putExtra(
            "permission.granted",
            permissions.filter { it.granted }.map { it.permission }.toTypedArray()
        )
        intentResult.putExtra(
            "permission.denied",
            permissions.filter { !it.granted }.map { it.permission }.toTypedArray()
        )
        setResult(RESULT_CANCELED, intentResult)
        finish()
    }

    private fun cancelPermission() {
        val intentResult = Intent()
        intentResult.putExtra(
            "permission.granted",
            ""
        )
        intentResult.putExtra(
            "permission.denied",
            ""
        )
        setResult(RESULT_CANCELED, intentResult)
        finish()
    }

    private fun finishPermission() {
        val intentResult = Intent()
        intentResult.putExtra(
            "permission.granted",
            permissions.map { it.permission }.toTypedArray()
        )
        intentResult.putExtra(
            "permission.denied",
            ""
        )
        setResult(RESULT_OK, intentResult)
        finish()
    }

    companion object {
        fun checkPermission(
            context: Context,
            permission: String
        ): Boolean {
            return ContextCompat.checkSelfPermission(
                context, permission
            )== PackageManager.PERMISSION_GRANTED
        }

        fun checkMultiPermission(
            context: Context,
            permissions: ArrayList<String>,
        ): ArrayList<String> {
            val granted = arrayListOf<String>()

            permissions.forEach { item ->
                if (ContextCompat.checkSelfPermission(
                        context, item
                    )== PackageManager.PERMISSION_GRANTED)
                   granted.add(item)
            }

            return granted
        }
    }
}