package com.example.pdf.ui.groupdetail

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pdf.PdfApplication
import com.example.pdf.R
import com.example.pdf.data.PdfFile
import com.example.pdf.ui.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(groupId: String, navController: NavController, onPdfClicked: (List<String>, Int) -> Unit) {
    val application = LocalContext.current.applicationContext as PdfApplication
    val viewModel: GroupDetailViewModel = viewModel(
        factory = ViewModelFactory(application.container.pdfRepository)
    )
    val group by viewModel.getGroup(groupId).collectAsState(initial = null)
    var fileToDelete by remember { mutableStateOf<PdfFile?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.series?.name ?: stringResource(R.string.group_details_title)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("google_drive/$groupId") }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.sync_with_google_drive))
            }
        }
    ) { padding ->
        group?.let { groupWithFiles ->
            if (groupWithFiles.files.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Inbox,
                            contentDescription = stringResource(R.string.empty_group_placeholder),
                            modifier = Modifier.size(128.dp)
                        )
                        Text(text = stringResource(R.string.empty_group_message_1))
                        Text(text = stringResource(R.string.empty_group_message_2))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(groupWithFiles.files) { file ->
                        PdfFileListItem(
                            file = file,
                            onClick = {
                                val filePaths = groupWithFiles.files.map { it.path }
                                val index = groupWithFiles.files.indexOf(file)
                                onPdfClicked(filePaths, index)
                            },
                            onDelete = { fileToDelete = file }
                        )
                    }
                }
            }
        }

        fileToDelete?.let { file ->
            AlertDialog(
                onDismissRequest = { fileToDelete = null },
                title = { Text(stringResource(R.string.delete_file_title)) },
                text = { Text(stringResource(R.string.delete_file_message, file.name)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteFile(file)
                            fileToDelete = null
                        }
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { fileToDelete = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun PdfFileListItem(file: PdfFile, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() })
    {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = file.name)
                Text(
                    text = String.format(
                        "%.2f MB",
                        file.size.toDouble() / (1024 * 1024)
                    ),
                    fontSize = 12.sp,
                    color = Color.Gray
                )

            }
        }
    }
}
