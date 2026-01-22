package com.example.pdf.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfSeriesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdfSeries(pdfSeries: PdfSeries): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdfSeriesFileCrossRef(crossRef: PdfSeriesFileCrossRef)

    @Transaction
    @Query("SELECT * FROM pdf_series")
    fun getSeriesWithFiles(): Flow<List<PdfSeriesWithFiles>>

    @Transaction
    @Query("SELECT * FROM pdf_series WHERE id = :id")
    fun getSeriesWithFiles(id: Long): Flow<PdfSeriesWithFiles>
}
