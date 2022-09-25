package io.zoemeow.dutnotify.model.enums

enum class NewsPageType(
    @Suppress("UNUSED_PARAMETER") value: Int
) {
    /**
     * This will reset page number to 1.
     */
    ResetToPage1(0),

    /**
     * This will get news in page 1, but page number will remain here.
     */
    GetPage1(1),

    /**
     * Continue get next page number.
     */
    NextPage(2),
}