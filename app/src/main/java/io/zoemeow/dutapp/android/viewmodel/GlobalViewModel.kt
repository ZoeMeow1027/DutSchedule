package io.zoemeow.dutapp.android.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.zoemeow.dutapp.android.repository.SettingsFileRepository
import javax.inject.Inject

@HiltViewModel
class GlobalViewModel @Inject constructor(
    private val settingsFileRepository: SettingsFileRepository
): ViewModel() {
    companion object {
        private val instance: MutableState<GlobalViewModel?> = mutableStateOf(null)

        fun getInstance(): GlobalViewModel {
            return instance.value!!
        }

        fun setInstance(globalViewModel: GlobalViewModel) {
            this.instance.value = globalViewModel
        }
    }

    // App background image
    val backgroundImage = settingsFileRepository.backgroundImage

    // App dynamic color
    val dynamicColorEnabled = settingsFileRepository.dynamicColorEnabled

    // App mode layout
    val appTheme = settingsFileRepository.appTheme

    // School year settings
    val schoolYear = settingsFileRepository.schoolYear

    // Black theme (for AMOLED display)
    var blackTheme = settingsFileRepository.blackTheme

    // Open link in
    var openLinkType = settingsFileRepository.openLinkType

    fun requestSaveSettings() {
        settingsFileRepository.saveSettings()
    }

    init {
        settingsFileRepository.loadSettings()
    }
}