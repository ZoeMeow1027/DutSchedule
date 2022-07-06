package io.zoemeow.dutapp.android.view.mainnavbar

import io.zoemeow.dutapp.android.R

object MainNavBarItemObject {
    val MainBarItems = listOf(
        MainNavBarItems(
            title = "Main",
            iconId = R.drawable.ic_baseline_main_24,
            route = "main"
        ),
        MainNavBarItems(
            title = "News",
            iconId = R.drawable.ic_baseline_news_24,
            route = "news"
        ),
        MainNavBarItems(
            title = "Account",
            iconId = R.drawable.ic_baseline_accountcircle_24,
            route = "account"
        ),
        MainNavBarItems(
            title = "Settings",
            iconId = R.drawable.ic_baseline_settings_24,
            route = "settings"
        ),
    )
}