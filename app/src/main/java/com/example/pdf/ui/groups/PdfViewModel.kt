package com.example.pdf.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdf.data.PdfFile
import com.example.pdf.data.PdfRepository
import com.example.pdf.data.PdfSeries
import kotlinx.coroutines.launch

class PdfViewModel(private val repository: PdfRepository) : ViewModel() {

    fun insert(pdfSeries: PdfSeries, files: List<PdfFile>) = viewModelScope.launch {
        repository.insertPdfSeries(pdfSeries, files)
    }
}