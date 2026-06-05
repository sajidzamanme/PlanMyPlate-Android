package com.teamconfused.planmyplate.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.teamconfused.planmyplate.util.SessionManager

class SettingsViewModel(private val sessionManager: SessionManager) : ViewModel() {
    fun logout() {
        sessionManager.clearSession()
    }

    fun getExpiryWarningDays(): Int {
        return sessionManager.getExpiryWarningDays()
    }

    fun saveExpiryWarningDays(days: Int) {
        sessionManager.saveExpiryWarningDays(days)
    }
}
