// UserSession: No changes needed; this code is good
package com.marzinp.badminton

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object UserSession {
    private val _isAdmin = MutableLiveData(false) // Default to not admin
    val isAdmin: LiveData<Boolean> get() = _isAdmin

    fun setAdminStatus(isAdmin: Boolean) {
        _isAdmin.value = isAdmin
    }
}