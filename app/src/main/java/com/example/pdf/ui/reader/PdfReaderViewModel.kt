package com.example.pdf.ui.reader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class PageInfo(val width: Int, val height: Int)

data class PdfReaderState(
    val filePaths: List<String> = emptyList(),
    val currentFileIndex: Int = 0,
    val pageInfos: List<PageInfo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentFileName: String = ""
)

class PdfReaderViewModel : ViewModel() {

    private val _state = MutableStateFlow(PdfReaderState())
    val state: StateFlow<PdfReaderState> = _state.asStateFlow()

    private var renderer: PdfRenderer? = null
    private val rendererMutex = Mutex()

    fun loadPdf(context: Context, filePaths: List<String>, initialFileIndex: Int) {
        if (filePaths.isEmpty()) return
        _state.update {
            it.copy(
                filePaths = filePaths,
                currentFileIndex = initialFileIndex
            )
        }
        openPdfFile(context, filePaths[initialFileIndex])
    }

    fun changeFile(context: Context, newIndex: Int) {
        if (newIndex >= 0 && newIndex < _state.value.filePaths.size) {
            _state.update { it.copy(currentFileIndex = newIndex) }
            openPdfFile(context, _state.value.filePaths[newIndex])
        }
    }

    private fun openPdfFile(context: Context, filePath: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val pfd = withContext(Dispatchers.IO) {
                    rendererMutex.withLock {
                        renderer?.close()
                        try {
                            val file = File(filePath)
                            if (file.exists()) {
                                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                            } else {
                                val assetFileName = filePath.substringAfterLast("/")
                                val assetFile = File(context.cacheDir, "temp_asset_${assetFileName}")
                                if (!assetFile.exists() || assetFile.length() == 0L) {
                                    context.assets.open(filePath).use { input ->
                                        FileOutputStream(assetFile).use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                }
                                ParcelFileDescriptor.open(assetFile, ParcelFileDescriptor.MODE_READ_ONLY)
                            }
                        } catch (e: IOException) {
                            throw IOException("Failed to open PDF: $filePath", e)
                        }
                    }
                }
                renderer = PdfRenderer(pfd)
                val pageInfos = (0 until (renderer?.pageCount ?: 0)).map {
                    val page = renderer!!.openPage(it)
                    val info = PageInfo(page.width, page.height)
                    page.close()
                    info
                }
                _state.update {
                    it.copy(
                        isLoading = false,
                        pageInfos = pageInfos,
                        currentFileName = filePath.substringAfterLast("/")
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    suspend fun renderPageStrip(context: Context, pageIndex: Int, top: Int, height: Int): Bitmap? {
        return rendererMutex.withLock {
            val screenWidth = context.resources.displayMetrics.widthPixels
            withContext(Dispatchers.IO) {
                renderer?.openPage(pageIndex)?.use { page ->
                    val scale = screenWidth.toFloat() / page.width.toFloat()
                    
                    // Use the passed height to create a bitmap of the correct size
                    val bitmap = Bitmap.createBitmap(screenWidth, height, Bitmap.Config.ARGB_8888)

                    val matrix = Matrix()
                    matrix.postScale(scale, scale)
                    // Translate the page up so the correct strip is at the top
                    matrix.postTranslate(0f, -top.toFloat())

                    page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.IO) {
            rendererMutex.withLock {
                renderer?.close()
            }
        }
    }
}
