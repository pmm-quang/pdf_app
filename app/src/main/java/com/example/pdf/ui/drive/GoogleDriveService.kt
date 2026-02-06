package com.example.pdf.ui.drive

import android.content.Context
import android.util.Log
import com.example.pdf.data.PdfFile
import com.example.pdf.data.PdfRepository
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.util.Date

class GoogleDriveService(
    private val repository: PdfRepository,
    private val credential: GoogleAccountCredential
) {
    private val drive: Drive = Drive.Builder(
        NetHttpTransport(),
        GsonFactory.getDefaultInstance(),
        credential
    ).setApplicationName("Pdf App").build()

    suspend fun getFiles(folderId: String): List<com.google.api.services.drive.model.File> {
        return withContext(Dispatchers.IO) {
            try {
                val allFiles = mutableListOf<com.google.api.services.drive.model.File>()
                var pageToken: String? = null
                val query = "'$folderId' in parents and (mimeType='application/pdf' or mimeType='application/vnd.google-apps.folder') and trashed = false"
                do {
                    val result = drive.files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, mimeType)")
                        .setPageToken(pageToken)
                        .execute()

                    val files = result.files
                    if (files != null) {
                        allFiles.addAll(files)
                    }
                    pageToken = result.nextPageToken
                } while (pageToken != null)

                // Sort by type (folders first) and then by name
                allFiles.sortWith(compareBy({ it.mimeType != "application/vnd.google-apps.folder" }, { it.name }))
                allFiles
            } catch (e: Exception) {
                Log.e("GoogleDriveService", "Error getting files", e)
                emptyList()
            }
        }
    }

    suspend fun downloadFiles(
        groupId: String,
        selectedFiles: List<com.google.api.services.drive.model.File>,
        context: Context
    ) {
        withContext(Dispatchers.IO) {
            val downloadedFiles = mutableListOf<PdfFile>()
            selectedFiles.filter { it.mimeType == "application/pdf" }.forEach { file ->
                try {
                    val filePath = java.io.File(context.filesDir, file.name).absolutePath
                    val outputStream = FileOutputStream(filePath)
                    drive.files().get(file.id).executeMediaAndDownloadTo(outputStream)
                    downloadedFiles.add(
                        PdfFile(
                            name = file.name,
                            path = filePath,
                            totalPages = 0,
                            lastReadPage = 0,
                            lastReadTime = Date()
                        )
                    )
                } catch (e: Exception) {
                    Log.e("GoogleDriveService", "Error downloading file: ${file.name}", e)
                }
            }
            if (downloadedFiles.isNotEmpty()) {
                repository.addFilesToSeries(groupId.toLong(), downloadedFiles)
            }
        }
    }
}
