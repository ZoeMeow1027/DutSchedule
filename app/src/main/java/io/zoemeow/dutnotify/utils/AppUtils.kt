package io.zoemeow.dutnotify.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.distinctUntilChanged
import java.math.BigInteger
import java.security.MessageDigest

class AppUtils {
    companion object {
        @Composable
        fun LazyList_EndOfListHandler(
            listState: LazyListState,
            buffer: Int = 1,
            onLoadMore: () -> Unit
        ) {
            val loadMore = remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val totalItemsNumber = layoutInfo.totalItemsCount
                    val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

                    lastVisibleItemIndex > (totalItemsNumber - buffer)
                }
            }

            LaunchedEffect(loadMore) {
                snapshotFlow { loadMore.value }
                    .distinctUntilChanged()
                    .collect {
                        if (loadMore.value)
                            onLoadMore()
                    }
            }
        }

        fun openLink(
            url: String,
            context: Context,
            openLinkInCustomTab: Boolean,
        ) {
            when (openLinkInCustomTab) {
                false -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
                true -> {
                    val builder = CustomTabsIntent.Builder()
                    val defaultColors = CustomTabColorSchemeParams.Builder().build()
                    builder.setDefaultColorSchemeParams(defaultColors)

                    val customTabsIntent = builder.build()
                    customTabsIntent.launchUrl(context, Uri.parse(url))
                }
            }
        }

        // https://stackoverflow.com/a/64171625
        fun getMD5(input: String): String {
            val md = MessageDigest.getInstance("MD5")
            return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
        }

        fun calcMD5CharValue(input: String): Int {
            var result: Int = 0

            val byteArray = input.toByteArray()
            byteArray.forEach {
                result += (it * 5)
            }

            return result
        }
    }
}