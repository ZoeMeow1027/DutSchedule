package io.zoemeow.dutschedule.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

fun OpenLink(
    url: String,
    context: Context,
    customTab: Boolean = true
) {
    when (customTab) {
        false -> {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        true -> {
            val builder = CustomTabsIntent.Builder()
            val defaultColors = CustomTabColorSchemeParams.Builder().build()
            builder.setDefaultColorSchemeParams(defaultColors)

            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
        }
    }
}