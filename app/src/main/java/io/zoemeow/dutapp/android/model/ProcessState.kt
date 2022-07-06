package io.zoemeow.dutapp.android.model

enum class ProcessState(val result: Int) {
    Unknown(-3),
    NotRun(-2),
    Running(-1),
    Successful(0),
    Failed(1),
}