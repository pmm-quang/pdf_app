package com.example.pdf.ui.groupdetail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdf.PdfApplication
import com.example.pdf.data.PdfFile
import com.example.pdf.ui.ViewModelFactory

@Composable
fun GroupDetailScreen(groupId: String, onPdfClicked: (List<String>, Int) -> Unit) {
    val application = LocalContext.current.applicationContext as PdfApplication
    val viewModel: GroupDetailViewModel = viewModel(
        factory = ViewModelFactory(application.container.pdfRepository)
    )
    val group by viewModel.getGroup(groupId).collectAsState(initial = null)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { /* TODO: Handle selected PDF file */ }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { launcher.launch("application/pdf") }) {
                Icon(Icons.Filled.Add, contentDescription = "Add PDF")
            }
        }
    ) { padding ->
        group?.let { group ->
            if (group.files.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "This group is empty")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(group.files) { file ->
                        PdfFileListItem(file = file, onClick = {
                            val filePaths = group.files.map { it.path }
                            val index = group.files.indexOf(file)
                            onPdfClicked(filePaths, index)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun PdfFileListItem(file: PdfFile, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() })
    {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = file.name)
            Text(text = "${file.lastReadPage} / ${file.totalPages}")
        }
    }
}
