package io.zoemeow.dutschedule.model

enum class ProcessState(val value: Int) {
    NotRunYet(-1),
    Running(0),
    Failed(1),
    Successful(2)
}