package io.zoemeow.dutapp.android.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
fun DateToString(date: Long, dateFormat: String, gmt: String = "UTC"): String {
    // "dd/MM/yyyy"
    // "dd/MM/yyyy HH:mm"
    val simpleDateFormat = SimpleDateFormat(dateFormat)
    simpleDateFormat.timeZone = TimeZone.getTimeZone(gmt)
    return simpleDateFormat.format(Date(date))
}