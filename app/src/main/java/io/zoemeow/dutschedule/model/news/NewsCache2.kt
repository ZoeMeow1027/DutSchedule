package io.zoemeow.dutschedule.model.news

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NewsCache2<T>(
    @SerializedName("data")
    val data: ArrayList<T> = arrayListOf(),
    @SerializedName("nextpage")
    var nextPage: Int = 1,
    @SerializedName("lastrequest")
    var lastRequest: Long = 0
) : Serializable
