package com.example.pdf.data

import kotlinx.coroutines.flow.Flow

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
}