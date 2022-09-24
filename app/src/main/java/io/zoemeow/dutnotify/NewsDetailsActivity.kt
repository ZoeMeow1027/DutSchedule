package io.zoemeow.dutnotify

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.AndroidEntryPoint
import io.zoemeow.dutapi.objects.news.NewsGlobalItem
import io.zoemeow.dutapi.objects.news.NewsSubjectItem
import io.zoemeow.dutnotify.model.appsettings.AppSettings
import io.zoemeow.dutnotify.model.appsettings.BackgroundImage
import io.zoemeow.dutnotify.model.enums.BackgroundImageType
import io.zoemeow.dutnotify.receiver.AppBroadcastReceiver
import io.zoemeow.dutnotify.ui.theme.MainActivityTheme
import io.zoemeow.dutnotify.utils.AppUtils
import io.zoemeow.dutnotify.view.news.NewsDetailsGlobal
import io.zoemeow.dutnotify.view.news.NewsDetailsSubject
import io.zoemeow.dutnotify.viewmodel.MainViewModel

@AndroidEntryPoint
class NewsDetailsActivity : ComponentActivity() {
    private val newsTitle = mutableStateOf("")
    internal lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            mainViewModel = viewModel()

            LaunchedEffect(Unit) {
                registerBroadcastReceiver(context = applicationContext)
                checkSettingsPermissionOnStartup(mainViewModel = mainViewModel)
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

    @Suppress("DEPRECATION")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(
        mainViewModel: MainViewModel,
        intent: Intent,
    ) {
        val type: String? = intent.getStringExtra("type")
        var data: Any? = null

        newsTitle.value = stringResource(id = R.string.newsdetails_title)
        when (type) {
            "dut_news_global" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    data = intent.getSerializableExtra("data", NewsGlobalItem::class.java)
                else data = intent.getSerializableExtra("data")
            }
            "dut_news_subject" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    data = intent.getSerializableExtra("data", NewsSubjectItem::class.java)
                else data = intent.getSerializableExtra("data")
            }
            else -> {
                setResult(RESULT_CANCELED)
                finish()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f)
                    ),
                    title = {
                        Text(newsTitle.value)
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                                .clip(CircleShape)
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
                    AppUtils.openLink(it, this, mainViewModel.appSettings.value.openLinkInCustomTab)
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
                news = newsData as NewsSubjectItem,
                linkClicked = {
                    if (linkClicked != null)
                        linkClicked(it)
                }
            )
            else -> {

            }
        }
    }

    private fun getAppBroadcastReceiver(): AppBroadcastReceiver {
        object : AppBroadcastReceiver() {
            override fun onNewsReloadRequested() {}
            override fun onAccountReloadRequested(newsType: String) {}
            override fun onSettingsReloadRequested() {}
            override fun onNewsScrollToTopRequested() {}
            override fun onSnackBarMessage(title: String?, forceCloseOld: Boolean) {}

            override fun onPermissionRequested(
                permission: String?,
                granted: Boolean,
                notifyToUser: Boolean
            ) {
                onPermissionResult(permission, granted, notifyToUser)
            }
        }.apply {
            return this
        }
    }

    private fun registerBroadcastReceiver(context: Context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(
            getAppBroadcastReceiver(),
            IntentFilter().apply {
                addAction(AppBroadcastReceiver.SNACKBARMESSAGE)
                addAction(AppBroadcastReceiver.NEWS_SCROLLALLTOTOP)
                addAction(AppBroadcastReceiver.RUNTIME_PERMISSION_REQUESTED)
            }
        )
    }
}

fun NewsDetailsActivity.onPermissionResult(
    permission: String?,
    granted: Boolean,
    notifyToUser: Boolean = false
) {
    when (permission) {
        Manifest.permission.READ_EXTERNAL_STORAGE -> {
            if (granted) {
                mainViewModel.mainActivityBackgroundDrawable.value =
                    AppUtils.getCurrentWallpaperBackground(
                        context = this,
                        type = mainViewModel.appSettings.value.backgroundImage.option
                    )
            } else {
                mainViewModel.appSettings.value = mainViewModel.appSettings.value.modify(
                    optionToModify = AppSettings.APPEARANCE_BACKGROUNDIMAGE,
                    value = BackgroundImage(
                        option = BackgroundImageType.Unset,
                        path = null
                    )
                )
                mainViewModel.requestSaveChanges()
                mainViewModel.showSnackBarMessage(
                    "Missing permission for background image. " +
                            "This setting will be turned off to avoid another issues."
                )
            }
        }
        else -> {}
    }
}

fun NewsDetailsActivity.checkSettingsPermissionOnStartup(
    mainViewModel: MainViewModel
) {
    val permissionList = arrayListOf<String>()

    // Read external storage - Background Image
    if (mainViewModel.appSettings.value.backgroundImage.option != BackgroundImageType.Unset) {
        if (!PermissionRequestActivity.checkPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        else onPermissionResult(Manifest.permission.READ_EXTERNAL_STORAGE, true)
    }

    if (permissionList.isNotEmpty()) {
        Intent(this, PermissionRequestActivity::class.java)
            .apply {
                putExtra("permissions.list", permissionList.toTypedArray())
            }
            .also {
                this.startActivity(it)
            }
    }
}