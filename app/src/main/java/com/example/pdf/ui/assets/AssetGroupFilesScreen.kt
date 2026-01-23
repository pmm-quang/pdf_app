package com.example.pdf.ui.assets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pdf.PdfApplication
import com.example.pdf.data.AssetFile
import com.example.pdf.data.AssetGroup
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetGroupFilesScreen(navController: NavController, groupId: String) {
    val context = LocalContext.current
    val application = context.applicationContext as PdfApplication
    val assetRepository = application.container.assetRepository
    var group by remember { mutableStateOf<AssetGroup?>(null) }

    LaunchedEffect(groupId) {
        group = assetRepository.getAssetGroup(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(group?.name ?: "Files") })
        }
    ) { padding ->
        group?.let { assetGroup ->
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(assetGroup.files) { file ->
                    AssetFileListItem(file = file) {
                        val filePaths = assetGroup.files.map { it.path }
                        val index = assetGroup.files.indexOf(file)
                        val encodedFilePaths = filePaths.joinToString(",") { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) }
                        navController.navigate("reader/${encodedFilePaths}/${index}")
                    }
                }
            }
        }
    }
}

@Composable
fun AssetFileListItem(file: AssetFile, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = file.name)
            Text(text = file.path)
        }
    }
}
