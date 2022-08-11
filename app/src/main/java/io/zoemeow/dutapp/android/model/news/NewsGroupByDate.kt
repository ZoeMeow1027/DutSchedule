package io.zoemeow.dutapp.android.model.news

import java.io.Serializable

data class NewsGroupByDate<T>(
    val itemList: ArrayList<T> = ArrayList(),
    val date: Long = 0,
) : Serializable
