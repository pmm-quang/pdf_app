package com.example.pdf.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_series")
data class PdfSeries(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
