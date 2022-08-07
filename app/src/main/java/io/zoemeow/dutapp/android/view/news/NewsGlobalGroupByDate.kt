package io.zoemeow.dutapp.android.view.news

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.zoemeow.dutapi.objects.NewsGlobalItem
import io.zoemeow.dutapp.android.model.NewsGroupByDate
import io.zoemeow.dutapp.android.utils.DateToString

@Composable
fun NewsGlobalGroupByDate(
    newsGroupByDate: NewsGroupByDate<NewsGlobalItem>,
    itemClicked: (NewsGlobalItem) -> Unit
) {
    Column(
        modifier = Modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = DateToString(newsGroupByDate.date, "dd/MM/yyyy"),
        )
        newsGroupByDate.itemList.forEach {item ->
            NewsGlobalItem(
                title = item.title ?: "",
                summary = item.contentString ?: "",
                clickable = {
                    itemClicked(item)
                }
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
    }
}