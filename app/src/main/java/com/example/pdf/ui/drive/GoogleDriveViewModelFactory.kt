package com.example.pdf.ui.drive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GoogleDriveViewModelFactory(private val groupId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoogleDriveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoogleDriveViewModel(groupId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}