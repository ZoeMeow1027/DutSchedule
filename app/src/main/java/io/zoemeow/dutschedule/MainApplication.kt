package io.zoemeow.dutschedule

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.HiltAndroidApp
import io.zoemeow.dutschedule.model.settings.AppSettings
import io.zoemeow.dutschedule.repository.FileModuleRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MainApplication: Application()