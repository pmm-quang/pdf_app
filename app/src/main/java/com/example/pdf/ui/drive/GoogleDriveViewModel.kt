package com.example.pdf.ui.drive

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.api.services.drive.model.File
import kotlinx.coroutines.launch

data class DownloadProgress(val fileName: String, val downloadedBytes: Long, val totalBytes: Long)

class GoogleDriveViewModel(private val groupId: String) : ViewModel() {

    var files by mutableStateOf<List<File>>(emptyList())
        private set

    var selectedFiles by mutableStateOf(emptySet<File>())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var downloadProgress by mutableStateOf<DownloadProgress?>(null)
        private set

    fun fetchFiles(googleDriveService: GoogleDriveService?, folderId: String) {
        viewModelScope.launch {
            isLoading = true
            files = googleDriveService?.getFiles(folderId) ?: emptyList()
            isLoading = false
        }
    }

    fun toggleFileSelection(file: File) {
        selectedFiles = if (selectedFiles.contains(file)) {
            selectedFiles - file
        } else {
            selectedFiles + file
        }
    }

    fun downloadSelectedFiles(googleDriveService: GoogleDriveService?, context: Context) {
        if (selectedFiles.isEmpty()) return
        viewModelScope.launch {
            isLoading = true
            // Immediately set progress for the first file to show the UI.
            val firstFile = selectedFiles.first()
            downloadProgress = DownloadProgress(firstFile.name, 0L, 0L) // 0 downloaded, 0 total to show indeterminate progress

            googleDriveService?.downloadFiles(groupId, selectedFiles.toList(), context) { fileName, downloaded, total ->
                // The callback is on a background thread, so launch a new coroutine
                // on the main thread to update the UI state.
                viewModelScope.launch {
                    downloadProgress = DownloadProgress(fileName, downloaded, total)
                }
            }
            isLoading = false
            downloadProgress = null // Reset progress after download is complete
            selectedFiles = emptySet() // Clear selection after download
        }
    }
}