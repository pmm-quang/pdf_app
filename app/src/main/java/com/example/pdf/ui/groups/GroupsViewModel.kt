package com.example.pdf.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdf.data.PdfRepository
import com.example.pdf.data.PdfSeries
import com.example.pdf.data.PdfSeriesWithFiles
import kotlinx.coroutines.launch

class GroupsViewModel(private val repository: PdfRepository) : ViewModel() {
    fun updateGroup(group: PdfSeries) {
        viewModelScope.launch {
            repository.updatePdfSeries(group)
        }
    }

    fun deleteGroup(group: PdfSeriesWithFiles) {
        viewModelScope.launch {
            repository.deletePdfSeries(group)
        }
    }
}
