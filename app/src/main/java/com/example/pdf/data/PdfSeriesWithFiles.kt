package com.example.pdf.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PdfSeriesWithFiles(
    @Embedded val series: PdfSeries,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PdfSeriesFileCrossRef::class,
            parentColumn = "seriesId",
            entityColumn = "fileId"
        )
    )
    val files: List<PdfFile>
)
