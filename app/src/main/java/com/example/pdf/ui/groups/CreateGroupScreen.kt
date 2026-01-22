package com.example.pdf.ui.groups

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pdf.PdfApplication
import com.example.pdf.data.PdfFile
import com.example.pdf.data.PdfSeries
import com.example.pdf.utils.getPdfFiles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(navController: NavController) {
    var groupName by remember { mutableStateOf("") }
    var selectedFiles by remember { mutableStateOf(emptySet<PdfFile>()) }
    val context = LocalContext.current
    val pdfViewModel: PdfViewModel = viewModel(
        factory = PdfViewModelFactory((context.applicationContext as PdfApplication).container.pdfRepository)
    )
    var allFiles by remember { mutableStateOf(emptyList<PdfFile>()) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            allFiles = getPdfFiles(context)
        }
    }

    LaunchedEffect(Unit) {
        when (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )) {
            PackageManager.PERMISSION_GRANTED -> {
                allFiles = getPdfFiles(context)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    Scaffold {
        Column(modifier = Modifier.padding(it)) {
            TextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Text(text = "Select files:", modifier = Modifier.padding(horizontal = 16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(allFiles) { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFiles = if (selectedFiles.contains(file)) {
                                    selectedFiles - file
                                } else {
                                    selectedFiles + file
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedFiles.contains(file),
                            onCheckedChange = { isChecked ->
                                selectedFiles = if (isChecked) {
                                    selectedFiles + file
                                } else {
                                    selectedFiles - file
                                }
                            }
                        )
                        Text(text = file.name, modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }

            Button(
                onClick = {
                    val newGroup = PdfSeries(name = groupName)
                    pdfViewModel.insert(newGroup, selectedFiles.toList())
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = groupName.isNotBlank()
            ) {
                Text("Create Group")
            }
        }
    }
}
