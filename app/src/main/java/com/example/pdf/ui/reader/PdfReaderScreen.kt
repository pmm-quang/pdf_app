package com.example.pdf.ui.reader

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream

@Composable
fun PdfReaderScreen(filePath: String) {
    val context = LocalContext.current
    val renderer = remember(filePath) {
        try {
            val file = File(context.cacheDir, "temp.pdf")
            context.assets.open(filePath).use { inputStream ->
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

    if (renderer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Failed to open PDF")
        }
        return
    }

    val mutex = remember { Mutex() }
    val pageCount = renderer.pageCount
    val bitmaps = remember { mutableStateListOf<Bitmap?>().apply { addAll(List(pageCount) { null }) } }

    DisposableEffect(renderer) {
        onDispose {
            renderer.close()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                            if (bitmaps[index] == null) {
                                val page = renderer.openPage(index)
                                val newBitmap =
                                    Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                                page.render(newBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                page.close()
                                bitmaps[index] = newBitmap
                            }
                        }
                    }
                }
            }
        }
    }
}
