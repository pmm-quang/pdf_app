package com.example.pdf.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pdf.PdfApplication
import com.example.pdf.ui.allpdfs.AllPdfsViewModel
import com.example.pdf.ui.groupdetail.GroupDetailViewModel
import com.example.pdf.ui.groups.PdfViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AllPdfsViewModel(pdfApplication().container.pdfRepository)
        }
        initializer {
            PdfViewModel(pdfApplication().container.pdfRepository)
        }
        initializer {
            GroupDetailViewModel(pdfApplication().container.pdfRepository)
        }
    }
}

fun CreationExtras.pdfApplication(): PdfApplication = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PdfApplication)
