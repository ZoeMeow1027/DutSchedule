package io.zoemeow.subjectnotifier.view

import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import io.zoemeow.subjectnotifier.model.appsettings.AppSettings
import io.zoemeow.subjectnotifier.ui.theme.MainActivityTheme

abstract class BaseActivity: ComponentActivity() {
    private val appSettings = mutableStateOf(AppSettings())
    private val initialized = mutableStateOf(false)
    val isAppInDarkTheme = mutableStateOf(false)

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            initialized.value = savedInstanceState.getBoolean("initialized")
            savedInstanceState.getSerializable("initialized").apply {
                if (this != null) appSettings.value = this as AppSettings
            }
        }

        super.onCreate(savedInstanceState)

        // https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted
        permitAllPolicy()

        setContent {
            OnPreload()
            if (!initialized.value) {
                initialized.value = true
                OnPreloadOnce()
            }
            MainActivityTheme(
                appSettings = appSettings.value,
                content = @Composable { OnMainView() },
                appModeChanged = { isAppInDarkTheme.value = it }
            )
        }
    }

    @Composable
    abstract fun OnPreloadOnce()

    @Composable
    abstract fun OnPreload()

    @Composable
    abstract fun OnMainView()

    fun setAppSettings(appSettings: AppSettings) {
        this.appSettings.value = appSettings
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("initialized", initialized.value)
        outState.putSerializable("appsettings", appSettings.value)
        super.onSaveInstanceState(outState)
    }

    /**
     * This will bypass network on main thread exception.
     * Use this at your own risk.
     * Target: OkHttp3
     *
     * Source: https://blog.cpming.top/p/android-os-networkonmainthreadexception
     */
    private fun permitAllPolicy() {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }
}