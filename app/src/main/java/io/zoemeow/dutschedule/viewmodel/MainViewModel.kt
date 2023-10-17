package io.zoemeow.dutschedule.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.zoemeow.dutschedule.model.account.AccountSession
import io.zoemeow.dutschedule.model.settings.AppSettings
import io.zoemeow.dutschedule.repository.DutAccountRepository
import io.zoemeow.dutschedule.repository.FileModuleRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fileModuleRepository: FileModuleRepository,
    private val dutAccountRepository: DutAccountRepository,
) : ViewModel() {
    val appSettings: MutableState<AppSettings> = mutableStateOf(AppSettings())
    val accountSession: MutableState<AccountSession> = mutableStateOf(AccountSession())

    fun saveSettings() {
        viewModelScope.launch {
            fileModuleRepository.saveAppSettings(appSettings.value)
            fileModuleRepository.saveAccountSession(accountSession.value)
        }
    }

    init {
        viewModelScope.launch {
            appSettings.value = fileModuleRepository.getAppSettings()
            accountSession.value = fileModuleRepository.getAccountSession()
        }
    }
}