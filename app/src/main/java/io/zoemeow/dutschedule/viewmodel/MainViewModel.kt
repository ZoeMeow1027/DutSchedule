package io.zoemeow.dutschedule.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.zoemeow.dutschedule.model.settings.AppSettings
import io.zoemeow.dutschedule.repository.FileModuleRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fileModuleRepository: FileModuleRepository
) : ViewModel() {
    val appSettings: MutableState<AppSettings> = mutableStateOf(AppSettings())

    fun saveSettings() {
        viewModelScope.launch {
            fileModuleRepository.saveAppSettings(appSettings.value)
        }
    }

    init {
        viewModelScope.launch {
            appSettings.value = fileModuleRepository.getAppSettings()
        }
    }
}