package io.zoemeow.dutapp.android.utils

import java.math.BigInteger
import java.security.MessageDigest

// https://stackoverflow.com/a/64171625
fun getMD5(input: String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}
