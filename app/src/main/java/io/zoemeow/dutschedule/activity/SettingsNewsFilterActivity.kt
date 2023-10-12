package io.zoemeow.dutschedule.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.ui.component.newsfilter.NewsFilterAddManually
import io.zoemeow.dutschedule.ui.component.newsfilter.NewsFilterClearAll
import io.zoemeow.dutschedule.ui.component.newsfilter.NewsFilterCurrentFilter

@AndroidEntryPoint
class SettingsNewsFilterActivity: BaseActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun OnMainView(padding: PaddingValues) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("News filter settings") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                setResult(RESULT_OK)
                                finish()
                            },
                            content = {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    "",
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        )
                    },
                )
            },
            content = {
                val tabIndex = remember { mutableIntStateOf(1) }

                Column(
                    modifier = Modifier.padding(it),
                    content = {
                        NewsFilterCurrentFilter()
                        NewsFilterAddManually(
                            expanded = tabIndex.value == 1,
                            onExpanded = { tabIndex.value = 1 }
                        )
                        NewsFilterClearAll(
                            expanded = tabIndex.value == 2,
                            onExpanded = { tabIndex.value = 2 }
                        )
                    }
                )
            }
        )
    }
}