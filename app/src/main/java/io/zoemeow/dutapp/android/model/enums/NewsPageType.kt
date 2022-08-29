package io.zoemeow.dutapp.android.model.enums

enum class NewsPageType {
    /**
     * This will reset page number to 1.
     */
    ResetToPage1,

    /**
     * This will get news in page 1, but page number will remain here.
     */
    GetFirstPage,

    /**
     * Continue get next page number.
     */
    NextPage
}