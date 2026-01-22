package com.example.pdf.ui.groupdetail

import androidx.lifecycle.ViewModel
import com.example.pdf.data.PdfRepository

class GroupDetailViewModel(private val repository: PdfRepository) : ViewModel() {
    fun getGroup(groupId: String) = repository.getSeriesById(groupId.toLong())
}