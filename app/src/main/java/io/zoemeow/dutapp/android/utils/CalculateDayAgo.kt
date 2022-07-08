package io.zoemeow.dutapp.android.utils

fun CalculateDayAgo(unix: Long): String? {
    val unixDuration = (System.currentTimeMillis() - unix) / 1000

    if (unixDuration < 10) {
        return "Just now"
    }
    else if (unixDuration < 60) {
        val unixSec = unixDuration % 60
        return "$unixSec second${if (unixSec.toInt() != 1) "s" else ""} ago"
    }
    else if (unixDuration < 60*60) {
        val unixMin = unixDuration / 60
        return "$unixMin minute${if (unixMin.toInt() != 1) "s" else ""} ago"
    }
    else if (unixDuration < 60*60*24) {
        val unixHr = unixDuration / 60 / 60
        return "$unixHr hour${if (unixHr.toInt() != 1) "s" else ""} ago"
    }
    else if (unixDuration < 60*60*24*30) {
        val unixDay = unixDuration / 60 / 60 / 24
        return "$unixDay day${if (unixDay.toInt() != 1) "s" else ""} ago"
    }
    else if (unixDuration < 60*60*24*365) {
        val unixMonth = unixDuration / 60 / 60 / 24 / 30
        return "$unixMonth month${if (unixMonth.toInt() != 1) "s" else ""} ago"
    }
    else {
        val unixYear = unixDuration / 60 / 60 / 24 / 365
        return "$unixYear year${if (unixYear.toInt() != 1) "s" else ""} ago"
    }
}