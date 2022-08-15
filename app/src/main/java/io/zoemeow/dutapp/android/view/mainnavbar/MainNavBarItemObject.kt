package io.zoemeow.dutapp.android.view.mainnavbar

import io.zoemeow.dutapp.android.R

object MainNavBarItemObject {
    val MainBarItems = listOf(
        MainNavBarItems(
            titleByStringId = R.string.navbar_main,
            iconId = R.drawable.ic_baseline_main_24,
            route = "main"
        ),
        MainNavBarItems(
            titleByStringId = R.string.navbar_news,
            iconId = R.drawable.ic_baseline_news_24,
            route = "news"
        ),
        MainNavBarItems(
            titleByStringId = R.string.navbar_account,
            iconId = R.drawable.ic_baseline_accountcircle_24,
            route = "account"
        ),
        MainNavBarItems(
            titleByStringId = R.string.navbar_settings,
            iconId = R.drawable.ic_baseline_settings_24,
            route = "settings"
        ),
    )
}