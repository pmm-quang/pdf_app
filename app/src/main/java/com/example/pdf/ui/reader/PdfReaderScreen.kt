package com.example.pdf.ui.reader

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    filePaths: List<String>,
    initialFileIndex: Int,
    onBack: () -> Unit
) {
    var currentFileIndex by remember(initialFileIndex) { mutableStateOf(initialFileIndex) }
    val currentFilePath = filePaths.getOrNull(currentFileIndex)
    var showFileList by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val renderer = remember(currentFilePath) {
        if (currentFilePath == null) return@remember null
        try {
            val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.pdf")
            context.assets.open(currentFilePath).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            PdfRenderer(fileDescriptor)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = currentFilePath?.substringAfterLast('/') ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { currentFileIndex-- },
                    enabled = currentFileIndex > 0
                ) {
                    Text("Previous")
                }
                Button(onClick = { showFileList = true }) {
                    Text("${currentFileIndex + 1} of ${filePaths.size}")
                }
                Button(
                    onClick = { currentFileIndex++ },
                    enabled = currentFileIndex < filePaths.size - 1
                ) {
                    Text("Next")
                }
            }
        }
    ) { innerPadding ->
        if (renderer == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding), contentAlignment = Alignment.Center
            ) {
                Text(if (currentFilePath != null) "Failed to open PDF" else "No file selected")
            }
        } else {
            val mutex = remember { Mutex() }
            val pageCount = renderer.pageCount
            val bitmaps = remember(renderer) { mutableStateListOf<Bitmap?>().apply { addAll(List(pageCount) { null }) } }

            DisposableEffect(renderer) {
                onDispose {
                    renderer.close()
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(
                    items = bitmaps,
                    key = { index, _ -> index }
                ) { index, bitmap ->
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "PDF Page ${index + 1}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentScale = ContentScale.FillWidth
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Loading page ${index + 1}...")
                            LaunchedEffect(index) {
                                mutex.withLock {
                                    if (bitmaps.getOrNull(index) == null) {
                                        renderer.openPage(index)?.use { page ->
                                            val newBitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                                            page.render(newBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                            bitmaps[index] = newBitmap
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFileList) {
        AlertDialog(
            onDismissRequest = { showFileList = false },
            title = { Text("Choose a file") },
            text = {
                LazyColumn {
                    itemsIndexed(filePaths) { index, filePath ->
                        Text(
                            text = filePath.substringAfterLast('/'),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    currentFileIndex = index
                                    showFileList = false 
                                }
                                .padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFileList = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}