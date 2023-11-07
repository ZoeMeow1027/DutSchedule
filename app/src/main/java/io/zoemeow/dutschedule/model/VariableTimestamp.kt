package io.zoemeow.dutschedule.model

data class VariableTimestamp<T>(
    val timestamp: Long = 0,
    val processState: ProcessState = ProcessState.NotRunYet,
    val data: T,
)  {
    fun clone(
        timestamp: Long? = null,
        processState: ProcessState? = null,
        data: T? = null
    ): VariableTimestamp<T> {
        return VariableTimestamp<T>(
            timestamp = timestamp ?: this.timestamp,
            processState = processState ?: this.processState,
            data = data ?: this.data
        )
    }
}