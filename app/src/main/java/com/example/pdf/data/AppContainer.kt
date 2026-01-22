package com.example.pdf.data

import android.content.Context

interface AppContainer {
    val pdfRepository: PdfRepository
    val assetRepository: AssetRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val pdfRepository: PdfRepository by lazy {
        PdfRepository(AppDatabase.getDatabase(context).pdfFileDao(), AppDatabase.getDatabase(context).pdfSeriesDao())
    }
    override val assetRepository: AssetRepository by lazy {
        AssetRepository(context)
    }
}