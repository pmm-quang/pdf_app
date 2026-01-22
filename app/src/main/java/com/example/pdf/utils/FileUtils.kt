package com.example.pdf.utils

import android.content.Context
import android.provider.MediaStore
import com.example.pdf.data.PdfFile
import java.io.File
import java.io.FileOutputStream

fun getPdfFiles(context: Context): List<PdfFile> {
    val pdfFiles = mutableListOf<PdfFile>()
    val projection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.DATA,
    )
    val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
    val selectionArgs = arrayOf("application/pdf")
    val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

    context.contentResolver.query(
        MediaStore.Files.getContentUri("external"),
        projection,
        selection,
        selectionArgs,
        sortOrder
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val name = cursor.getString(nameColumn)
            val path = cursor.getString(pathColumn)
            pdfFiles.add(PdfFile(id = 0, name = name, path = path, totalPages = 0, lastReadPage = 0, lastReadTime = null))
        }
    }
    return pdfFiles
}

fun copyPdfFromAssets(context: Context, fileName: String): String {
    val file = File(context.filesDir, fileName)
    if (!file.exists()) {
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open(fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return file.absolutePath
}
