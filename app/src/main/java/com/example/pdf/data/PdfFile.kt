package com.example.pdf.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "pdf_files")
data class PdfFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val path: String,
    val totalPages: Int,
    val lastReadPage: Int,
    val lastReadTime: Date?
)
