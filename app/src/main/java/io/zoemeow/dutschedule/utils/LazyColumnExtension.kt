package io.zoemeow.dutschedule.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun Modifier.endOfListReached(
    lazyListState: LazyListState,
    buffer: Int = 1,
    endOfListReached: () -> Unit
): Modifier {
    val shouldLoadMore = remember {
        derivedStateOf {
            try {
                val layoutInfo = lazyListState.layoutInfo
                val totalItemsNumber = layoutInfo.totalItemsCount
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.last().index + 1

                lastVisibleItemIndex > (totalItemsNumber - buffer)
            } catch (ex: Exception) {
                false
            }
        }
    }
    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .collect {
                if (shouldLoadMore.value)
                    endOfListReached()
            }
    }
    return this
}