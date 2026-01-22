package com.example.pdf.ui.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.pdf.PdfApplication
import com.example.pdf.data.PdfSeriesWithFiles

@Composable
fun GroupsScreen(navController: NavController) {
    val application = LocalContext.current.applicationContext as PdfApplication
    val groups by application.container.pdfRepository.allPdfSeries.collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("create_group") }) {
                Icon(Icons.Filled.Add, contentDescription = "Create Group")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(groups) { group ->
                GroupListItemLazy(group = group) {
                    navController.navigate("group_detail/${group.series.id}")
                }
            }
        }
    }
}

@Composable
fun GroupListItemLazy(group: PdfSeriesWithFiles, onClick: () -> Unit) {
    Card(modifier = Modifier.clickable { onClick() }) {
        Text(text = group.series.name)
    }
}
