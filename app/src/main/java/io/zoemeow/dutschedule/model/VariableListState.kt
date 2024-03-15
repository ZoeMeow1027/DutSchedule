package io.zoemeow.dutschedule.model

import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class VariableListState<T>(
    val data: SnapshotStateList<T> = mutableStateListOf(),
    val lastRequest: MutableLongState = mutableLongStateOf(0),
    val processState: MutableState<ProcessState> = mutableStateOf(ProcessState.NotRunYet),
    val parameters: MutableMap<String, String> = mutableMapOf()
) {
    fun isExpired(): Boolean {
        return (lastRequest.longValue + ProcessVariable.expiredDuration) < System.currentTimeMillis()
    }

    fun isSuccessfulRequestExpired(): Boolean {
        return when (processState.value) {
            ProcessState.Successful -> isExpired()
            else -> true
        }
    }

    fun resetValue() {
        if (processState.value != ProcessState.Running) {
            data.clear()
            lastRequest.longValue = 0
            processState.value = ProcessState.NotRunYet
        }
    }
}