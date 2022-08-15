package io.zoemeow.dutapp.android.model

enum class ProcessState(val result: Int) {
    Unknown(-4),
    NotRanYet(-3),
    Scheduled(-2),
    Running(-1),
    Successful(0),
    Failed(1),
}