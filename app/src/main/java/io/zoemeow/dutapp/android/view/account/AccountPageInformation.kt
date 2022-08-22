package io.zoemeow.dutapp.android.view.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import io.zoemeow.dutapi.objects.AccountInformation

@Composable
fun AccountPageInformation(
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
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(
                        start = 15.dp,
                        end = 15.dp,
                        top = 10.   dp,
                        bottom = 10.dp
                    )
                ) {

                }
            }
        }
    }
}