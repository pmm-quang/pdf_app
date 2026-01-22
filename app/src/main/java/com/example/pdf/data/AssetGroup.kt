package com.example.pdf.data

data class AssetGroup(
    val id: String,
    val name: String,
    val files: List<AssetFile>
)

data class AssetFile(
    val name: String,
    val path: String
)
