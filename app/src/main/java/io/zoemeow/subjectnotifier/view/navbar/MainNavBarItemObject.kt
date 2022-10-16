package io.zoemeow.subjectnotifier.view.navbar

import io.zoemeow.subjectnotifier.R

object MainNavBarItemObject {
    val MainBarItems = listOf(
        NavBarItems(
            titleByStringId = R.string.navbar_news,
            iconId = R.drawable.ic_baseline_news_24,
            route = "news"
        ),
        NavBarItems(
            titleByStringId = R.string.navbar_account,
            iconId = R.drawable.ic_baseline_accountcircle_24,
            route = "account"
        ),
        NavBarItems(
            titleByStringId = R.string.navbar_settings,
            iconId = R.drawable.ic_baseline_settings_24,
            route = "settings"
        ),
    )
}