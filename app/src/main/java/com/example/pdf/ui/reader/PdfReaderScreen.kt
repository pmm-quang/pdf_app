package com.example.pdf.ui.reader

import android.util.Base64
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PdfReaderScreen(filePath: String) {
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    LaunchedEffect(filePath) {
        val pdfData = withContext(Dispatchers.IO) {
            val inputStream = context.assets.open(filePath)
            val bytes = inputStream.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }

        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <script src=\"file:///android_asset/pdfjs/build/pdf.js\"></script>
            </head>
            <body>
                <canvas id=\"pdf-canvas\"></canvas>
                <script>
                    const pdfData = atob('$pdfData');
                    const pdfjsLib = window['pdfjs-dist/build/pdf'];
                    pdfjsLib.GlobalWorkerOptions.workerSrc = 'file:///android_asset/pdfjs/build/pdf.worker.js';

                    const loadingTask = pdfjsLib.getDocument({data: pdfData});
                    loadingTask.promise.then(function(pdf) {
                        console.log('PDF loaded');
                        
                        // Fetch the first page
                        const pageNumber = 1;
                        pdf.getPage(pageNumber).then(function(page) {
                            console.log('Page loaded');
                            
                            const scale = 1.5;
                            const viewport = page.getViewport({scale: scale});

                            // Prepare canvas using PDF page dimensions
                            const canvas = document.getElementById('pdf-canvas');
                            const context = canvas.getContext('2d');
                            canvas.height = viewport.height;
                            canvas.width = viewport.width;

                            // Render PDF page into canvas context
                            const renderContext = {
                                canvasContext: context,
                                viewport: viewport
                            };
                            const renderTask = page.render(renderContext);
                            renderTask.promise.then(function () {
                                console.log('Page rendered');
                            });
                        });
                    }, function (reason) {
                        // PDF loading error
                        console.error(reason);
                    });
                </script>
            </body>
            </html>
        """

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    AndroidView(
        factory = { webView },
        modifier = Modifier.fillMaxSize()
    )
}
