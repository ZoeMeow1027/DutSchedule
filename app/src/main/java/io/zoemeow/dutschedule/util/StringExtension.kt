package io.zoemeow.dutschedule.util

import java.text.Normalizer

fun String.toNonAccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(temp, "")
}