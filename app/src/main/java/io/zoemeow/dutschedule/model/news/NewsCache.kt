package io.zoemeow.dutschedule.model.news

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NewsCache<T>(
    @SerializedName("news_list")
    val newsListByDate: ArrayList<NewsGroupByDate<T>> = arrayListOf(),
    @SerializedName("page_current")
    var pageCurrent: Int = 1,
    @SerializedName("last_modified_date")
    var lastModifiedDate: Long = 0
) : Serializable
