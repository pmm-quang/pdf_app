package com.example.pdf.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfFileDao {
    @Query("SELECT * FROM pdf_files")
    fun getAll(): Flow<List<PdfFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pdfFile: PdfFile): Long
}
