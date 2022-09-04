package io.zoemeow.dutnotify.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

fun openLink(
    url: String,
    context: Context,
    openLinkInCustomTab: Boolean,
) {
    when (openLinkInCustomTab) {
        false -> {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
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
