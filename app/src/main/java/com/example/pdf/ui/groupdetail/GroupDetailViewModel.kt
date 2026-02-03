package com.example.pdf.ui.groupdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdf.data.PdfFile
import com.example.pdf.data.PdfRepository
import kotlinx.coroutines.launch

class GroupDetailViewModel(private val repository: PdfRepository) : ViewModel() {
    fun getGroup(groupId: String) = repository.getSeriesById(groupId.toLong())

    fun deleteFile(file: PdfFile) {
        viewModelScope.launch {
            repository.deleteFile(file)
        }
    }
}