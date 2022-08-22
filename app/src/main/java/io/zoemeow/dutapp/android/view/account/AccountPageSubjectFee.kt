package io.zoemeow.dutapp.android.view.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import io.zoemeow.dutapi.objects.SubjectFeeItem

@Composable
fun AccountPageSubjectFee(
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
                        modifier = Modifier.padding(
                            start = 15.dp,
                            end = 15.dp,
                            top = 10.dp,
                            bottom = 10.dp
                        )
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
                            text = "Status: ${if (item.debt) "Waiting for completed..." else "Completed"}",
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
                Column(
                    modifier = Modifier.padding(15.dp)
                ) {
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "Total credit: ${subjectFeeList.sumOf { it.credit }}"
                    )
                    Text(
                        text = "Total money you will pay: ${
                            subjectFeeList.sumOf { it.price }.toLong()
                        } VND"
                    )
                }
            }
        }
    }
}