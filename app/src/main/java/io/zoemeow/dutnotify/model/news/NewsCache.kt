package io.zoemeow.dutnotify.model.news

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NewsCache<T>(
    @SerializedName("news_list")
    val newsListByDate: ArrayList<NewsGroupByDate<T>> = arrayListOf(),
    @SerializedName("page_current")
    var pageCurrent: Int = 1,
): Serializable