package com.example.pdf.ui.reader

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    filePaths: List<String>,
    initialFileIndex: Int,
    onBack: () -> Unit,
    viewModel: PdfReaderViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var showFileList by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()

    LaunchedEffect(filePaths, initialFileIndex) {
        viewModel.loadPdf(context, filePaths, initialFileIndex)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = state.currentFileName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
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
                    onClick = { viewModel.changeFile(context, state.currentFileIndex - 1) },
                    enabled = state.currentFileIndex > 0
                ) {
                    Text("Previous")
                }

                val currentPage by remember {
                    derivedStateOf {
                        listState.layoutInfo.visibleItemsInfo.firstOrNull()?.let {
                            val key = it.key.toString()
                            if (key.startsWith("page_")) {
                                key.split('_').getOrNull(2)?.toIntOrNull() ?: 0
                            } else 0
                        } ?: 0
                    }
                }

                Button(onClick = { showFileList = true }) {
                    val pageCount = state.pageInfos.size
                    if (pageCount > 0) {
                        Text("${currentPage + 1} of $pageCount")
                    } else {
                        Text("0 of 0")
                    }
                }
                Button(
                    onClick = { viewModel.changeFile(context, state.currentFileIndex + 1) },
                    enabled = state.currentFileIndex < state.filePaths.size - 1
                ) {
                    Text("Next")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    Text(
                        text = "Error: ${state.error}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.pageInfos.isNotEmpty() -> {
                    val screenWidth = context.resources.displayMetrics.widthPixels
                    val stripHeight = screenWidth * 2 // Must match ViewModel

                    LaunchedEffect(state.currentFileIndex) {
                        listState.scrollToItem(0)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        state = listState,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        state.pageInfos.forEachIndexed { pageIndex, pageInfo ->
                            val scale = screenWidth.toFloat() / pageInfo.width.toFloat()
                            val scaledHeight = (pageInfo.height * scale).toInt()

                            if (scaledHeight <= stripHeight) {
                                // Page is short, render as a single item
                                item(key = "page_${state.currentFileIndex}_$pageIndex") {
                                    PageStrip(viewModel, state, pageIndex, 0, scaledHeight)
                                }
                            } else {
                                // Page is long, render as multiple strips
                                val stripCount = (scaledHeight + stripHeight - 1) / stripHeight
                                items(stripCount, key = { stripIndex -> "page_${state.currentFileIndex}_${pageIndex}_strip_$stripIndex" }) { stripIndex ->
                                    val top = stripIndex * stripHeight
                                    val heightForStrip = if (stripIndex == stripCount - 1) {
                                        scaledHeight - top // Last strip, use remaining height
                                    } else {
                                        stripHeight
                                    }
                                    PageStrip(viewModel, state, pageIndex, top, heightForStrip)
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
                val lazyListState = rememberLazyListState()
                LazyColumn(state = lazyListState) {
                    itemsIndexed(state.filePaths) { index, filePath ->
                        Text(
                            text = filePath.substringAfterLast('/'),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.changeFile(context, index)
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

@Composable
private fun PageStrip(
    viewModel: PdfReaderViewModel,
    state: PdfReaderState,
    pageIndex: Int,
    top: Int, // The y-coordinate of the top of the strip in the scaled page
    height: Int
) {
    val context = LocalContext.current
    var stripBitmap by remember(state.currentFileIndex, pageIndex, top) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(state.currentFileIndex, pageIndex, top) {
        stripBitmap = viewModel.renderPageStrip(context, pageIndex, top, height)
    }

    val density = LocalDensity.current
    val heightInDp = with(density) { height.toDp() }

    if (stripBitmap != null) {
        Image(
            bitmap = stripBitmap!!.asImageBitmap(),
            contentDescription = "PDF Page $pageIndex Strip at $top",
            modifier = Modifier
                .fillMaxWidth()
                .height(heightInDp),
            contentScale = ContentScale.FillBounds
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightInDp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
