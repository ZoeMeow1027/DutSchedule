package io.zoemeow.dutschedule.model

data class VariableTimestamp<T>(
    val lastRequest: Long = 0,
    val processState: ProcessState = ProcessState.NotRunYet,
    val data: T,
)  {
    // Session ID duration in milliseconds
    private val expiredDuration = 1000 * 60 * 5

    fun clone(
        lastRequest: Long? = null,
        processState: ProcessState? = null,
        data: T? = null
    ): VariableTimestamp<T> {
        return VariableTimestamp(
            lastRequest = lastRequest ?: this.lastRequest,
            processState = processState ?: this.processState,
            data = data ?: this.data
        )
    }

    fun isExpired(): Boolean {
        return (lastRequest + expiredDuration) < System.currentTimeMillis()
    }

    fun isSuccessfulRequestExpired(): Boolean {
        return when (processState) {
            ProcessState.Successful -> isExpired()
            else -> true
        }
    }
}