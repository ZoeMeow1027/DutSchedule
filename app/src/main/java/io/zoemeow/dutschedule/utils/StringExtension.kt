package io.zoemeow.dutschedule.utils

import java.math.BigInteger
import java.security.MessageDigest
import java.text.Normalizer

fun String.toNonAccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(temp, "")
}

// https://stackoverflow.com/a/64171625
fun String.calcMD5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(32, '0')
}

fun String.calcToSumByCharArray(): Int {
    var result = 0

    this.toByteArray().forEach {
        result += (it * 5)
    }

    return result
}

fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}