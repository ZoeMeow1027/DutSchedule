package io.zoemeow.dutschedule.ui.view.account

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import io.zoemeow.dutschedule.activity.AccountActivity
import io.zoemeow.dutschedule.model.ProcessState
import io.zoemeow.dutschedule.ui.component.base.ButtonBase
import io.zoemeow.dutschedule.ui.component.base.OutlinedTextBox
import io.zoemeow.dutschedule.ui.component.base.SimpleCardItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountActivity.TrainingResult(
    context: Context,
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
                title = { Text("Account Training Result") },
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
            if (getMainViewModel().accountTrainingStatus.processState.value != ProcessState.Running) {
                FloatingActionButton(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            getMainViewModel().accountLogin(
                                after = {
                                    if (it) { getMainViewModel().accountTrainingStatus.refreshData(force = true) }
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
                    if (getMainViewModel().accountTrainingStatus.processState.value == ProcessState.Running) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start,
                        content = {
                            getMainViewModel().accountTrainingStatus.data.value?.let {
                                fun graduateStatus(): String {
                                    val owned = ArrayList<String>()
                                    val missing = ArrayList<String>()
                                    if (it.graduateStatus?.hasSigGDTC == true) {
                                        owned.add("GDTC certificate")
                                    } else {
                                        missing.add("GDTC certificate")
                                    }
                                    if (it.graduateStatus?.hasSigGDQP == true) {
                                        owned.add("GDQP certificate")
                                    } else {
                                        missing.add("GDQP certificate")
                                    }
                                    if (it.graduateStatus?.hasSigEnglish == true) {
                                        owned.add("English certificate")
                                    } else {
                                        missing.add("English certificate")
                                    }
                                    if (it.graduateStatus?.hasSigIT == true) {
                                        owned.add("IT certificate")
                                    } else {
                                        missing.add("IT certificate")
                                    }
                                    val hasQualifiedGraduate = it.graduateStatus?.hasQualifiedGraduate == true

                                    val result = "- Owned certificate(s): ${owned.joinToString(", ")}\n- Missing certificate(s): ${missing.joinToString(", ")}\n- Has qualified graduate: ${if (hasQualifiedGraduate) "Yes" else "No (check information below)"}"
                                    owned.clear()
                                    missing.clear()
                                    return result
                                }

                                SimpleCardItem(
                                    title = "Your training result",
                                    isTitleCentered = true,
                                    padding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 7.dp),
                                    opacity = getControlBackgroundAlpha(),
                                    content = {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp)
                                                .padding(bottom = 10.dp),
                                            content = {
                                                OutlinedTextBox(
                                                    title = "Score (point / 4)",
                                                    value = "${it.trainingSummary?.avgTrainingScore4 ?: "(unknown)"}",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 5.dp)
                                                )
                                                OutlinedTextBox(
                                                    title = "School year updated",
                                                    value = it.trainingSummary?.schoolYearCurrent ?: "(unknown)",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 5.dp)
                                                )
                                                ButtonBase(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 5.dp),
                                                    horizontalArrangement = Arrangement.Center,
                                                    content = {
                                                        Text("View your training details")
                                                    },
                                                    clicked = {
                                                        val intent = Intent(context, AccountActivity::class.java)
                                                        intent.action = "acc_training_result_subjectresult"
                                                        context.startActivity(intent)
                                                    }
                                                )
                                            }
                                        )
                                    },
                                    clicked = {}
                                )
                                Spacer(modifier = Modifier.size(5.dp))
                                SimpleCardItem(
                                    title = "Graduate status",
                                    isTitleCentered = true,
                                    padding = PaddingValues(horizontal = 10.dp),
                                    opacity = getControlBackgroundAlpha(),
                                    content = {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp)
                                                .padding(bottom = 10.dp),
                                            content = {
                                                OutlinedTextBox(
                                                    title = "Certificate & graduate result",
                                                    value = graduateStatus(),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 5.dp)
                                                )
                                                OutlinedTextBox(
                                                    title = "Khen thuong",
                                                    value = it.graduateStatus?.info1 ?: "(unknown)",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 5.dp)
                                                )
                                                OutlinedTextBox(
                                                    title = "Ky luat",
                                                    value = it.graduateStatus?.info2 ?: "(unknown)",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 5.dp)
                                                )
                                                OutlinedTextBox(
                                                    title = "Information about graduation thesis approval",
                                                    value = it.graduateStatus?.info3 ?: "(unknown)",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 5.dp)
                                                )
                                                OutlinedTextBox(
                                                    title = "Information about graduate process approval",
                                                    value = it.graduateStatus?.approveGraduateProcessInfo ?: "(unknown)",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 5.dp)
                                                )
                                            }
                                        )
                                    },
                                    clicked = {}
                                )
                                Spacer(modifier = Modifier.size(5.dp))
                            }
                        }
                    )
                }
            )
        }
    )

    val hasRun = remember { mutableStateOf(false) }
    run {
        if (!hasRun.value) {
            CoroutineScope(Dispatchers.IO).launch {
                getMainViewModel().accountLogin(
                    after = {
                        if (it) {
                            getMainViewModel().accountTrainingStatus.refreshData()
                        }
                    }
                )
            }
            hasRun.value = true
        }
    }
}