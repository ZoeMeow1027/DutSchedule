package io.zoemeow.subjectnotifier.model.enums

enum class ProcessState(
    @Suppress("UNUSED_PARAMETER") value: Int
) {
    NotRanYet(-2),
    AlreadyRunning(-1),
    Running(0),
    Successful(1),
    Failed(2),
}