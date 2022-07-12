package io.zoemeow.dutapp.android.view.account

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.zoemeow.dutapi.objects.SubjectFeeItem
import io.zoemeow.dutapp.android.model.ProcessState
import io.zoemeow.dutapp.android.ui.theme.DUTAppForAndroidTheme
import io.zoemeow.dutapp.android.viewmodel.AccountViewModel

class AccountSubjectFeeActivity: ComponentActivity() {
    private lateinit var activityViewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityViewModel = AccountViewModel.getInstance()
        if (!activityViewModel.isLoggedIn.value) {
            setResult(RESULT_CANCELED)
            finish()
        }
        activityViewModel.getSubjectFee()

        setContent {
            DUTAppForAndroidTheme {
                MainScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = {
                        Text("Subject Fee - ${activityViewModel.username.value}")
                    }
                )
            }
        ) { padding ->

            SubjectFeeScreen(
                padding = padding,
                subjectFeeList = activityViewModel.subjectFeeList,
                isLoading = activityViewModel.processStateSubjectFee,
                reloadRequested = {
                    activityViewModel.getSubjectFee()
                }
            )
        }
    }

    @Composable
    private fun SubjectFeeScreen(
        padding: PaddingValues,
        subjectFeeList: SnapshotStateList<SubjectFeeItem>,
        isLoading: MutableState<ProcessState>,
        reloadRequested: () -> Unit
    ) {
        val swipeRefreshState = rememberSwipeRefreshState(true)
        swipeRefreshState.isRefreshing = isLoading.value == ProcessState.Running

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                swipeRefreshState.isRefreshing = true
                reloadRequested()
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
            ) {
                items(subjectFeeList) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                            // https://www.android--code.com/2021/09/jetpack-compose-box-rounded-corners_25.html
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp)
                        ) {
                            Text(
                                text = "ID: ${item.id}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.size(20.dp))
                            Text(
                                text = "Name: ${item.name}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Credit: ${item.credit}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Price: ${item.price} VND",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Status: ${if (item.debt) "Waiting for purchase..." else "Purchased"}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "Restudy: ${item.isRestudy}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }

                item {
                    Surface(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Column {
                            Spacer(modifier = Modifier.size(10.dp))
                            Text(
                                text = "Total credit: ${activityViewModel.subjectFeeList.sumOf{ it.credit }}"
                            )
                            Text(
                                text = "Total money you will purchase: ${activityViewModel.subjectFeeList.sumOf { it.price }} VND"
                            )
                        }
                    }
                }
            }
        }
    }
}