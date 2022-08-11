package io.zoemeow.dutapp.android.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import io.zoemeow.dutapp.android.model.enums.OpenLinkType
import io.zoemeow.dutapp.android.view.activity.BuiltInCustomTab

fun openLink(
    url: String,
    context: Context,
    openLinkType: OpenLinkType,
) {
    when (openLinkType) {
        OpenLinkType.InBrowser -> {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
        OpenLinkType.InCustomTabs -> {
            val builder = CustomTabsIntent.Builder()
            val defaultColors = CustomTabColorSchemeParams.Builder().build()
            builder.setDefaultColorSchemeParams(defaultColors)

            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
        }
        OpenLinkType.InBuiltIn -> {
            val intent = Intent(context, BuiltInCustomTab::class.java)
            intent.putExtra("url", url)
            context.startActivity(intent)
        }
    }
}
