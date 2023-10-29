package io.zoemeow.dutschedule.ui.component.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import io.dutwrapperlib.dutwrapper.objects.news.NewsGlobalItem
import io.zoemeow.dutschedule.util.CustomDateUtils

@Composable
fun NewsDetailsGlobal(
    isDarkMode: Boolean = false,
    padding: PaddingValues,
    news: NewsGlobalItem,
    linkClicked: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = news.title,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = "⏱ ${
                    CustomDateUtils.dateToString(
                        news.date,
                        "dd/MM/yyyy",
                        "UTC"
                    )
                } (${CustomDateUtils.unixToDuration(news.date)})",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.size(10.dp))
            Divider(
                color = Color.Gray,
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.size(10.dp))
            val annotatedString = buildAnnotatedString {
                if (news.contentString != null) {
                    // Parse all string to annotated string.
                    append(news.contentString)
                    // Adjust color for annotated string to follow system mode.
                    addStyle(
                        style = SpanStyle(color = if (isDarkMode) Color.White else Color.Black),
                        start = 0,
                        end = news.contentString.length
                    )
                    // Adjust for detected link.
                    news.links?.forEach {
                        addStringAnnotation(
                            tag = it.position!!.toString(),
                            annotation = it.url!!,
                            start = it.position,
                            end = it.position + it.text!!.length
                        )
                        addStyle(
                            style = SpanStyle(color = Color(0xff64B5F6)),
                            start = it.position,
                            end = it.position + it.text.length
                        )
                    }
                }
            }
            SelectionContainer {
                ClickableText(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyLarge,
                    onClick = {
                        try {
                            news.links?.forEach { item ->
                                annotatedString
                                    .getStringAnnotations(item.position!!.toString(), it, it)
                                    .firstOrNull()
                                    ?.let { url ->
                                        var urlTemp = url.item
                                        urlTemp =
                                            urlTemp.replace(
                                                "http://",
                                                "http://",
                                                ignoreCase = true
                                            )
                                        urlTemp = urlTemp.replace(
                                            "https://",
                                            "https://",
                                            ignoreCase = true
                                        )
                                        linkClicked(urlTemp)
                                    }
                            }
                        } catch (_: Exception) {
                            // TODO: Exception for can't open link here!
                        }
                    }
                )
            }
        }
    }
}