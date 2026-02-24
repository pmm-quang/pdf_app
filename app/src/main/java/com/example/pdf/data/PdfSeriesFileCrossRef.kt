package com.example.pdf.data

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["seriesId", "fileId"],
    foreignKeys = [
        ForeignKey(
            entity = PdfSeries::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PdfFile::class,
            parentColumns = ["id"],
            childColumns = ["fileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PdfSeriesFileCrossRef(
    val seriesId: Long,
    val fileId: Long
)
