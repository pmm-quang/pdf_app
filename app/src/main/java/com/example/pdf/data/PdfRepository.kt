package com.example.pdf.data

import kotlinx.coroutines.flow.Flow
import java.io.File

class PdfRepository(private val pdfFileDao: PdfFileDao, private val pdfSeriesDao: PdfSeriesDao) {

    val allPdfFiles: Flow<List<PdfFile>> = pdfFileDao.getAll()
    val allPdfSeries: Flow<List<PdfSeriesWithFiles>> = pdfSeriesDao.getSeriesWithFiles()

    fun getSeriesById(id: Long): Flow<PdfSeriesWithFiles> = pdfSeriesDao.getSeriesWithFiles(id)

    suspend fun insertPdfFile(pdfFile: PdfFile) {
        pdfFileDao.insert(pdfFile)
    }

    suspend fun insertPdfSeries(pdfSeries: PdfSeries, files: List<PdfFile>) {
        val seriesId = pdfSeriesDao.insertPdfSeries(pdfSeries)
        files.forEach { file ->
            val fileId = pdfFileDao.insert(file)
            pdfSeriesDao.insertPdfSeriesFileCrossRef(PdfSeriesFileCrossRef(seriesId, fileId))
        }
    }

    suspend fun addFilesToSeries(seriesId: Long, files: List<PdfFile>) {
        files.forEach { file ->
            val fileId = pdfFileDao.insert(file)
            pdfSeriesDao.insertPdfSeriesFileCrossRef(PdfSeriesFileCrossRef(seriesId, fileId))
        }
    }

    suspend fun deleteFile(file: PdfFile) {
        // Delete the physical file
        try {
            val physicalFile = File(file.path)
            if (physicalFile.exists()) {
                physicalFile.delete()
            }
        } catch (e: Exception) {
            // Log or handle the exception
            e.printStackTrace()
        }

        // Delete the file record from the database
        pdfFileDao.delete(file)
    }
}