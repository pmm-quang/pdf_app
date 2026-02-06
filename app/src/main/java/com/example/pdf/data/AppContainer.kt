package com.example.pdf.data

import android.content.Context
import com.example.pdf.ui.drive.GoogleDriveService

interface AppContainer {
    val pdfRepository: PdfRepository
    val assetRepository: AssetRepository
    var googleDriveService: GoogleDriveService?
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val pdfRepository: PdfRepository by lazy {
        PdfRepository(AppDatabase.getDatabase(context).pdfFileDao(), AppDatabase.getDatabase(context).pdfSeriesDao())
    }
    override val assetRepository: AssetRepository by lazy {
        AssetRepository(context)
    }
    override var googleDriveService: GoogleDriveService? = null
}