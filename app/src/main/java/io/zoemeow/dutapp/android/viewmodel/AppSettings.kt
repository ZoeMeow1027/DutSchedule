package io.zoemeow.dutapp.android.viewmodel

import io.zoemeow.dutapp.android.repository.SettingsFileRepository

class AppSettings(
    private val settingsFileRepository: SettingsFileRepository
) {
    // App background image
    val backgroundImage = settingsFileRepository.backgroundImage

    // App dynamic color
    val dynamicColorEnabled = settingsFileRepository.dynamicColorEnabled

    // App mode layout
    val appTheme = settingsFileRepository.appTheme

    // School year settings
    val schoolYear = settingsFileRepository.schoolYear

    // Black theme (for AMOLED display)
    val blackTheme = settingsFileRepository.blackTheme

    // Open link in
    val openLinkInCustomTab = settingsFileRepository.openLinkInCustomTab

    fun saveSettings() {
        settingsFileRepository.saveSettings()
    }

    fun loadSettings() {
        settingsFileRepository.loadSettings()
    }
}