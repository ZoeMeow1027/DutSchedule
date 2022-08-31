package io.zoemeow.dutapp.android.view.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import io.zoemeow.dutapi.objects.NewsGlobalItem
import io.zoemeow.dutapp.android.ui.theme.DefaultActivityTheme
import io.zoemeow.dutapp.android.utils.openLink
import io.zoemeow.dutapp.android.view.news.NewsDetailsGlobal
import io.zoemeow.dutapp.android.view.news.NewsDetailsSubject

class ExternalActivity: ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = mutableStateOf("")

        setContent {
            DefaultActivityTheme {
                val type: String? = intent.getStringExtra("type")
                var data: Any? = null

                when (type) {
                    "dut_news_global" -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            data = intent.getSerializableExtra("data", NewsGlobalItem::class.java)
                        else data = intent.getSerializableExtra("data")
                        title.value = "News Global detail"
                    }
                    "dut_news_subject" -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            data = intent.getSerializableExtra("data", NewsGlobalItem::class.java)
                        else data = intent.getSerializableExtra("data")
                        title.value = "News Subject detail"
                    }
                    else -> {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                }

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        SmallTopAppBar(
                            title = {
                                Text(title.value)
                            }
                        )
                    }
                ) { padding ->
                    when (type) {
                        "dut_news_global" -> NewsDetailsGlobal(
                            isDarkMode = isSystemInDarkTheme(),
                            padding = padding,
                            news = data as NewsGlobalItem,
                            linkClicked = {
                                openLink(it, this, true)
                            }
                        )
                        "dut_news_subject" -> NewsDetailsSubject(
                            isDarkMode = isSystemInDarkTheme(),
                            padding = padding,
                            news = data as NewsGlobalItem,
                            linkClicked = {
                                openLink(it, this, true)
                            }
                        )
                        else -> {

                        }
                    }
                }
            }
        }
    }
}