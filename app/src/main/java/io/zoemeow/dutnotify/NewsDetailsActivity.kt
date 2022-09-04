package io.zoemeow.dutnotify

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.zoemeow.dutapi.objects.NewsGlobalItem
import io.zoemeow.dutnotify.R
import io.zoemeow.dutnotify.model.appsettings.BackgroundImage
import io.zoemeow.dutnotify.model.enums.AppSettingsCode
import io.zoemeow.dutnotify.model.enums.BackgroundImageType
import io.zoemeow.dutnotify.ui.theme.MainActivityTheme
import io.zoemeow.dutnotify.util.openLink
import io.zoemeow.dutnotify.view.news.NewsDetailsGlobal
import io.zoemeow.dutnotify.view.news.NewsDetailsSubject
import io.zoemeow.dutnotify.viewmodel.MainViewModel

class NewsDetailsActivity : ComponentActivity() {
    private val newsTitle = mutableStateOf("")
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            mainViewModel = viewModel()

            val initialized = remember { mutableStateOf(false) }
            if (!initialized.value) {
                // Set to false to avoid another run
                initialized.value = true

                // Check permission with background image option
                // Only one request when user start app.
                if (mainViewModel.appSettings.value.backgroundImage.option != BackgroundImageType.Unset) {
                    if (PermissionRequestActivity.checkPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        mainViewModel.reloadAppBackground(
                            context = this,
                            type = mainViewModel.appSettings.value.backgroundImage.option
                        )
                    } else {
                        val intent = Intent(this, PermissionRequestActivity::class.java)
                        intent.putExtra(
                            "permission.requested",
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        )
                        permissionRequestActivityResult.launch(intent)
                    }
                }
            }

            MainActivityTheme(
                appSettings = mainViewModel.appSettings.value,
                content = @Composable {
                    MainScreen(
                        mainViewModel = mainViewModel,
                        intent = intent,
                    )
                },
                backgroundDrawable = mainViewModel.mainActivityBackgroundDrawable.value,
                appModeChanged = {
                    // Trigger for dark mode detection.
                    mainViewModel.mainActivityIsDarkTheme.value = it
                },
            )
        }
    }

    private val permissionRequestActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                mainViewModel.reloadAppBackground(
                    context = this,
                    type = mainViewModel.appSettings.value.backgroundImage.option
                )
            } else {
                mainViewModel.appSettings.value = mainViewModel.appSettings.value.modify(
                    optionToModify = AppSettingsCode.BackgroundImage,
                    value = BackgroundImage(
                        option = BackgroundImageType.Unset,
                        path = null
                    )
                )
                mainViewModel.requestSaveChanges()


//            mainViewModel.setPendingNotifications(
//                "Missing permission: READ_EXTERNAL_STORAGE. " +
//                        "This will revert background image option is unset.",
//                true
//            )
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(
        mainViewModel: MainViewModel,
        intent: Intent,
    ) {
        val type: String? = intent.getStringExtra("type")
        var data: Any? = null

        when (type) {
            "dut_news_global" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    data = intent.getSerializableExtra("data", NewsGlobalItem::class.java)
                else data = intent.getSerializableExtra("data")
                newsTitle.value = "News details"
            }
            "dut_news_subject" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    data = intent.getSerializableExtra("data", NewsGlobalItem::class.java)
                else data = intent.getSerializableExtra("data")
                newsTitle.value = "News details"
            }
            else -> {
                setResult(RESULT_CANCELED)
                finish()
            }
        }

        Scaffold(
            topBar = {
                SmallTopAppBar(
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    title = {
                        Text(newsTitle.value)
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                                .clickable {
                                    setResult(RESULT_OK)
                                    finish()
                                },
                            content = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_arrow_back_24),
                                    contentDescription = "",
                                    tint = if (mainViewModel.mainActivityIsDarkTheme.value) Color.White else Color.Black,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        )
                    }
                )
            },
            containerColor = if (mainViewModel.appSettings.value.backgroundImage.option == BackgroundImageType.Unset)
                MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.background.copy(
                alpha = 0.8f
            ),
            contentColor = if (mainViewModel.mainActivityIsDarkTheme.value) Color.White else Color.Black,
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            MainBody(
                padding = padding,
                newsType = type,
                newsData = data,
                linkClicked = {
                    openLink(it, this, mainViewModel.appSettings.value.openLinkInCustomTab)
                },
            )
        }
    }

    @Composable
    fun MainBody(
        padding: PaddingValues,
        newsType: String? = null,
        newsData: Any? = null,
        linkClicked: ((String) -> Unit)? = null
    ) {
        when (newsType) {
            "dut_news_global" -> NewsDetailsGlobal(
                isDarkMode = isSystemInDarkTheme(),
                padding = padding,
                news = newsData as NewsGlobalItem,
                linkClicked = {
                    if (linkClicked != null)
                        linkClicked(it)
                }
            )
            "dut_news_subject" -> NewsDetailsSubject(
                isDarkMode = isSystemInDarkTheme(),
                padding = padding,
                news = newsData as NewsGlobalItem,
                linkClicked = {
                    if (linkClicked != null)
                        linkClicked(it)
                }
            )
            else -> {

            }
        }
    }
}