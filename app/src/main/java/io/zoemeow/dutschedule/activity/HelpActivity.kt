package io.zoemeow.dutschedule.activity

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutschedule.model.helpandexternallink.HelpLinkInfo
import io.zoemeow.dutschedule.ui.component.helpandexternallink.HelpLinkClickable

@AndroidEntryPoint
class HelpActivity : BaseActivity() {
    @Composable
    override fun OnPreloadOnce() {

    }

    @Composable
    override fun OnMainView(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color
    ) {
        when (intent.action) {
            "view_externallink" -> {
                ExternalLinkView(
                    context = context,
                    snackBarHostState = snackBarHostState,
                    containerColor = containerColor,
                    contentColor = contentColor
                )
            }

            else -> {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ExternalLinkView(
        context: Context,
        snackBarHostState: SnackbarHostState,
        containerColor: Color,
        contentColor: Color
    ) {
        val focusRequester = remember { FocusRequester() }
        val searchText = remember { mutableStateOf("") }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            containerColor = containerColor,
            contentColor = contentColor,
            topBar = {
                TopAppBar(
                    title = { Text(text = "External links - DUT School") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                setResult(RESULT_OK)
                                finish()
                            },
                            content = {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    "Back to previous screen",
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        )
                    },
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .clickable { clearAllFocusAndHideKeyboard() },
                    content = {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 7.dp)
                                .clickable { clearAllFocusAndHideKeyboard() },
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(
                                alpha = getControlBackgroundAlpha()
                            ),
                            shape = RoundedCornerShape(7.dp),
                            content = {
                                Column(
                                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 15.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "",
                                        modifier = Modifier.padding(bottom = 7.dp)
                                    )
                                    Text(
                                        "Note: This page will show most used links related to DUT school, so it could be missed here. You can navigate to official DUT home page for more information."
                                    )
                                }
                            }
                        )
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp)
                                .focusRequester(focusRequester),
                            value = searchText.value,
                            onValueChange = { searchVal -> searchText.value = searchVal },
                            label = {
                                Text("Search a external link")
                            },
                            trailingIcon = {
                                if (searchText.value.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            searchText.value = ""
                                            focusRequester.requestFocus()
                                        },
                                        content = {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "Clear query",
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    clearAllFocusAndHideKeyboard()
                                }
                            )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .clickable { clearAllFocusAndHideKeyboard() },
                            content = {
                                HelpLinkInfo.getAllExternalLink().filter { item ->
                                    searchText.value.isEmpty() ||
                                            item.title.lowercase().contains(searchText.value.lowercase()) ||
                                            item.description?.lowercase()?.contains(searchText.value.lowercase()) ?: false ||
                                            item.url.lowercase().contains(searchText.value.lowercase())
                                }.toList().forEach { item ->
                                    HelpLinkClickable(
                                        item = item,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp),
                                        opacity = getControlBackgroundAlpha(),
                                        linkClicked = {
                                            clearAllFocusAndHideKeyboard()
                                            try {
                                                openLink(
                                                    url = item.url,
                                                    context = context,
                                                    customTab = getMainViewModel().appSettings.value.openLinkInsideApp
                                                )
                                            } catch (ex: Exception) {
                                                ex.printStackTrace()
                                            }
                                        }
                                    )
                                }
                            }
                        )
                    }
                )
            }
        )
    }
}