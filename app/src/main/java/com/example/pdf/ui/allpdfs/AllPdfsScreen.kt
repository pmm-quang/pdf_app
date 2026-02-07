package com.example.pdf.ui.allpdfs

import android.Manifest
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pdf.data.PdfFile
import com.example.pdf.ui.AppViewModelProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.net.URLEncoder
import java.text.CharacterIterator
import java.text.StringCharacterIterator

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AllPdfsScreen(navController: NavController, viewModel: AllPdfsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val context = LocalContext.current
    val pdfFiles by viewModel.pdfFiles.collectAsState()
    val permissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    if (permissionState.status.isGranted) {
        LaunchedEffect(Unit) {
            viewModel.rescanPdfs(context)
        }
    }

    LazyColumn {
        items(pdfFiles) { file ->
            PdfFileItem(file = file, navController = navController, filePaths = pdfFiles.map { it.path })
        }
    }
}

@Composable
fun PdfFileItem(file: PdfFile, navController: NavController, filePaths: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                val fileIndex = filePaths.indexOf(file.path)
                val encodedFilePaths = filePaths.map { URLEncoder.encode(it, "UTF-8") }.toTypedArray()
                navController.navigate("pdf_reader/${encodedFilePaths.joinToString(",")}/$fileIndex")
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = file.name)
            Text(text = formatBytes(file.size))
        }
    }
}

private fun formatBytes(bytes: Long): String {
    var bytes = bytes
    if (-1000 < bytes && bytes < 1000) {
        return "$bytes B"
    }
    val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
    while (bytes <= -999950 || bytes >= 999950) {
        bytes /= 1000
        ci.next()
    }
    return String.format("%.1f %cB", bytes / 1000.0, ci.current())
}