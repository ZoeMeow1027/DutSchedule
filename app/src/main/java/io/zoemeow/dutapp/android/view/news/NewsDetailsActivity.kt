package io.zoemeow.dutapp.android.view.news

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import io.zoemeow.dutapi.objects.NewsGlobalItem
import io.zoemeow.dutapp.android.ui.theme.DUTAppForAndroidTheme
import io.zoemeow.dutapp.android.utils.CalculateDayAgo
import io.zoemeow.dutapp.android.utils.DateToString


class NewsDetailsActivity : ComponentActivity() {
    private var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DUTAppForAndroidTheme {
                context = LocalContext.current

                val isNewsGlobalIntent: Any? = intent.extras?.get("isNewsGlobal")
                var newsGlobalItem: NewsGlobalItem? = null
                var newsSubjectItem: NewsGlobalItem? = null

                // If null, quit now
                if (isNewsGlobalIntent == null) {
                    finish()
                }
                else {
                    if (isNewsGlobalIntent as Boolean) {
                        newsGlobalItem = intent.extras?.get("newsItem") as NewsGlobalItem
                    }
                    else {
                        newsSubjectItem = intent.extras?.get("newsItem") as NewsGlobalItem
                    }

                    when (isNewsGlobalIntent as Boolean) {
                        true -> if (newsGlobalItem != null)
                            NewsDetailsGlobal(
                                news = newsGlobalItem,
                                linkClicked = { openLinkInCustomTab(it) }
                            )
                        false -> if (newsSubjectItem != null) {
                            NewsDetailsSubject(
                                news = newsSubjectItem,
                                linkClicked = {

                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun openLinkInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context?.startActivity(intent)
    }

    private fun openLinkInCustomTab(url: String) {
        val builder = CustomTabsIntent.Builder()
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .build()
        builder.setDefaultColorSchemeParams(defaultColors)

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    @Composable
    fun NewsDetailsGlobal(
        news: NewsGlobalItem,
        linkClicked: (String) -> Unit
    ) {
        NewsScreenCore { padding ->
            val optionsScrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(optionsScrollState)
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                        text = news.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "Posted on ${DateToString(news.date, "dd/MM/yyyy", "UTC")} (${CalculateDayAgo(news.date) ?: "..."})",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black
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
                                style = SpanStyle(color = if (isSystemInDarkTheme()) Color.White else Color.Black),
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
                    ClickableText(
                        text = annotatedString,
                        style = MaterialTheme.typography.bodyMedium,
                        onClick = {
                            try {
                                news.links?.forEach {
                                        item ->
                                    annotatedString
                                        .getStringAnnotations(item.position!!.toString(), it, it)
                                        .firstOrNull()
                                        ?.let { url -> linkClicked(url.item.lowercase()) }
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

    @Composable
    fun NewsDetailsSubject(
        news: NewsGlobalItem,
        linkClicked: (String) -> Unit,
    ) {
        NewsScreenCore { padding ->

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NewsScreenCore(
        content: @Composable (PaddingValues) -> Unit
    ) {
        // A scaffold container using the 'background' color from the theme
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { SmallTopAppBar(
                title = {
                    Text("News Details")
                }
            ) },
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.background,
            content = { content(it) }
        )
    }
}