package io.zoemeow.dutapp.android.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.*
import io.zoemeow.dutapp.android.R
import io.zoemeow.dutapp.android.ui.theme.DefaultActivityTheme
import io.zoemeow.dutapp.android.viewmodel.UIStatus

class BuiltInCustomTab: ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DefaultActivityTheme {
                // If baseUrl from Intent is null, quit immediately.
                val baseUrl = intent.getStringExtra("url")
                if (baseUrl == null || baseUrl.isEmpty()) {
                    setResult(RESULT_CANCELED)
                    finish()
                }

                val uiStatus: UIStatus = UIStatus.getInstance()

                // Title and url, which will display to SmallTopAppBar
                val barTitle: MutableState<String?> = remember { mutableStateOf("") }
                val barUrl: MutableState<String> = remember { mutableStateOf(baseUrl!!) }

                // WebView behavior (state, WebChrome, WebView, Navigator,...)
                val canBack: MutableState<Boolean> = remember { mutableStateOf(false) }
                val isPageLoading: MutableState<Boolean> = remember { mutableStateOf(false) }
                val pageProgress: MutableState<Int> = remember { mutableStateOf(0) }

                val navigator = rememberWebViewNavigator().apply {
                    LaunchedEffect(canGoBack) {
                        canBack.value = canGoBack
                    }
                }

                val state = rememberWebViewState(barUrl.value).apply {
                    LaunchedEffect(isLoading) {
                        isPageLoading.value = isLoading
                    }
                }

                val webViewClient = remember {
                    object: AccompanistWebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            // return super.shouldOverrideUrlLoading(view, request)
                            Log.d("POPUP", "Triggered ${request?.url.toString()}")
                            view?.loadUrl(request?.url.toString())
                            return true
                        }
                    }
                }

                val webChromeClient = remember {
                    object: AccompanistWebChromeClient() {
                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            super.onReceivedTitle(view, title)
                            barTitle.value = title
                        }

                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            pageProgress.value = newProgress
                            barUrl.value = if (state.content.getCurrentUrl() != null)
                                state.content.getCurrentUrl()!! else ""
                            isPageLoading.value = state.isLoading
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        SmallTopAppBar(
                            navigationIcon = {
                                // Close button
                                Surface(
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(60.dp)
                                        .clickable {
                                            navigator.stopLoading()
                                            setResult(RESULT_OK)
                                            finish()
                                        }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_close_24),
                                        contentDescription = "",
                                        tint = if (uiStatus.mainActivityIsDarkTheme.value) Color.White else Color.Black
                                    )
                                }
                            },
                            title = {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        // Title bar
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.Start,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight()
                                        ) {
                                            if (barTitle.value != null || barTitle.value!!.isNotEmpty()) {
                                                Text(
                                                    text = barTitle.value!!,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                )
                                            }
                                            Spacer(modifier = Modifier.size(0.dp))
                                            Text(
                                                text = barUrl.value,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                        if (isPageLoading.value) {
                                            LinearProgressIndicator(
                                                progress = (pageProgress.value.toDouble() / 100.toDouble()).toFloat(),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .align(alignment = Alignment.Bottom)
                                            )
                                        }
                                    }

                                }
                            },
                        )
                    }
                ) { contentPadding ->
                    Surface(
                        modifier = Modifier.padding(contentPadding)
                    ) {
                        WebView(
                            state = state,
                            navigator = navigator,
                            onCreated = {
                                it.settings.javaScriptEnabled = true
                                it.settings.domStorageEnabled = true
                                it.settings.javaScriptCanOpenWindowsAutomatically = true
                                it.settings.setSupportMultipleWindows(true)
                            },
                            captureBackPresses = true,
                            client = webViewClient,
                            chromeClient = webChromeClient,
                        )
                        if (isPageLoading.value) {
                            LinearProgressIndicator(
                                progress = (pageProgress.value.toDouble() / 100.toDouble()).toFloat(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}