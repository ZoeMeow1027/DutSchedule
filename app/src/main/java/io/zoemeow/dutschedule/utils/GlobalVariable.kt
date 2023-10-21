package io.zoemeow.dutschedule.utils

class GlobalVariable {
    companion object {
        // Session ID duration in milliseconds
        val SESSIONID_DURATION = 1000 * 60 * 30

        fun currentTimestampInMilliseconds(): Long {
            return System.currentTimeMillis()
        }

        fun isExpired(current: Long): Boolean {
            return (current + SESSIONID_DURATION) < currentTimestampInMilliseconds()
        }
    }
}