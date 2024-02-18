package io.zoemeow.dutschedule.ui.view.account

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.activity.AccountActivity
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.ui.component.base.OutlinedTextBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountActivity.AccountInformation(
    snackBarHostState: SnackbarHostState,
    containerColor: Color,
    contentColor: Color
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        containerColor = containerColor,
        contentColor = contentColor,
        topBar = {
            LargeTopAppBar(
                title = { Text("Basic Information") },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            setResult(ComponentActivity.RESULT_OK)
                            finish()
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
        floatingActionButton = {
            if (getMainViewModel().accountInformation.processState.value != ProcessState.Running) {
                FloatingActionButton(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            getMainViewModel().accountLogin(
                                after = {
                                    if (it) {
                                        getMainViewModel().accountInformation.refreshData(force = true)
                                    }
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
                    .fillMaxSize()
                    .padding(padding),
                content = {
                    if (getMainViewModel().accountInformation.processState.value == ProcessState.Running) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 15.dp)
                            .padding(bottom = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                        content = {
                            getMainViewModel().accountInformation.data.value?.let { data ->
                                val mapPersonalInfo = mapOf(
                                    "Name" to (data.name ?: "(unknown)"),
                                    "Date of birth" to (data.dateOfBirth ?: "(unknown)"),
                                    "Place of birth" to (data.birthPlace ?: "(unknown)"),
                                    "Gender" to (data.gender ?: "(unknown)"),
                                    "National ID card" to (data.nationalIdCard ?: "(unknown)"),
                                    "National card issue place and date" to ("${data.nationalIdCardIssuePlace ?: "(unknown)"} on ${data.nationalIdCardIssueDate ?: "(unknown)"}"),
                                    "Citizen card date" to (data.citizenIdCardIssueDate ?: "(unknown)"),
                                    "Citizen ID card" to (data.citizenIdCard ?: "(unknown)"),
                                    "Bank card ID" to ("${data.accountBankId ?: "(unknown)"} (${data.accountBankName ?: "(unknown)"})"),
                                    "Personal email" to (data.personalEmail ?: "(unknown)"),
                                    "Phone number" to (data.phoneNumber ?: "(unknown)"),
                                    "Class" to (data.schoolClass ?: "(unknown)"),
                                    "Specialization" to (data.specialization ?: "(unknown)"),
                                    "Training program plan" to (data.trainingProgramPlan ?: "(unknown)"),
                                    "School email" to (data.schoolEmail ?: "(unknown)"),
                                )
                                Text("Click and hold a text field to show option to copy it.")
                                Spacer(modifier = Modifier.size(5.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top
                                ) {
                                    mapPersonalInfo.keys.forEach { title ->
                                        OutlinedTextBox(
                                            title = title,
                                            value = mapPersonalInfo[title] ?: "(unknown)",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            )
        }
    )
}