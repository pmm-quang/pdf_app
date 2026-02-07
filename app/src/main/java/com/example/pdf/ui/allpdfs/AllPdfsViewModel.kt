package com.example.pdf.ui.allpdfs

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdf.data.PdfFile
import com.example.pdf.data.PdfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AllPdfsViewModel(private val repository: PdfRepository) : ViewModel() {

    val pdfFiles: StateFlow<List<PdfFile>> = repository.allPdfFiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun rescanPdfs(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val existingPdfs = pdfFiles.first()
                val existingPaths = existingPdfs.map { it.path }.toSet()

                val projection = arrayOf(
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.SIZE
                )
                val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
                val selectionArgs = arrayOf("application/pdf")
                val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

                context.contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )?.use { cursor ->
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

                    while (cursor.moveToNext()) {
                        val name = cursor.getString(nameColumn)
                        val path = cursor.getString(dataColumn)
                        val size = cursor.getLong(sizeColumn)

                        if (!existingPaths.contains(path)) {
                            val file = File(path)
                            if (file.exists()) {
                                try {
                                    val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                                    val renderer = PdfRenderer(pfd)
                                    val pageCount = renderer.pageCount
                                    renderer.close()
                                    pfd.close()

                                    val pdfFile = PdfFile(
                                        name = name,
                                        path = path,
                                        size = size,
                                        totalPages = pageCount,
                                        lastReadPage = 0,
                                        lastReadTime = null
                                    )
                                    repository.insertPdfFile(pdfFile)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
