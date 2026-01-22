package com.example.pdf.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object MyLibrary : Screen("my_library", "My Library", Icons.Default.Home)
    object Discover : Screen("discover", "Discover", Icons.Default.Search)
}