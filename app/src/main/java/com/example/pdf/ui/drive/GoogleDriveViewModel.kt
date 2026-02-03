package com.example.pdf.ui.drive

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdf.data.PdfFile
import com.example.pdf.data.PdfRepository
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.util.Date

class GoogleDriveViewModel(private val repository: PdfRepository) : ViewModel() {

    val files = mutableStateOf<List<File>>(emptyList())
    val isLoading = mutableStateOf(false)
    val isDownloadComplete = mutableStateOf(false)

    fun getFiles(credential: GoogleAccountCredential) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            try {
                val drive = Drive.Builder(com.google.api.client.http.javanet.NetHttpTransport(), com.google.api.client.json.gson.GsonFactory(), credential)
                    .setApplicationName("Pdf App")
                    .build()

                val result = drive.files().list()
                    .setSpaces("drive")
                    .setQ("mimeType='application/pdf'")
                    .execute()

                files.value = result.files
            } catch (e: Exception) {
                Log.e(TAG, "getFiles: ", e)
            }
            isLoading.value = false
        }
    }

    fun downloadFiles(groupId: String, selectedFiles: List<File>, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            val downloadedFiles = mutableListOf<PdfFile>()
            val drive = Drive.Builder(com.google.api.client.http.javanet.NetHttpTransport(), com.google.api.client.json.gson.GsonFactory(), null)
                .setApplicationName("Pdf App")
                .build()

            selectedFiles.forEach { file ->
                try {
                    val filePath = java.io.File(context.filesDir, file.name).absolutePath
                    val outputStream = FileOutputStream(filePath)
                    drive.files().get(file.id).executeMediaAndDownloadTo(outputStream)
                    downloadedFiles.add(PdfFile(name = file.name, path = filePath, totalPages = 0, lastReadPage = 0, lastReadTime = Date()))
                } catch (e: Exception) {
                    Log.e(TAG, "downloadFiles: ", e)
                }
            }
            repository.addFilesToSeries(groupId.toLong(), downloadedFiles)
            isLoading.value = false
            isDownloadComplete.value = true
        }
    }
}