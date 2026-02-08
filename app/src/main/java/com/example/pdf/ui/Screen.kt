package com.example.pdf.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pdf.R

sealed class Screen(val route: String, @StringRes val titleRes: Int, val icon: ImageVector) {
    object MyLibrary : Screen("my_library", R.string.title_my_library, Icons.Default.Home)
    object Discover : Screen("discover", R.string.title_discover, Icons.Default.Search)
}