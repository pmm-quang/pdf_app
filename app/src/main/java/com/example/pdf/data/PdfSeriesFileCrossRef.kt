package com.example.pdf.data

import androidx.room.Entity

@Entity(primaryKeys = ["seriesId", "fileId"])
data class PdfSeriesFileCrossRef(
    val seriesId: Long,
    val fileId: Long
)
