package com.example.pdf.ui.drive

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pdf.PdfApplication
import com.example.pdf.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.DriveScopes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveScreen(groupId: String, navController: NavController) {
    val context = LocalContext.current
    val application = context.applicationContext as PdfApplication
    var googleDriveService by remember { mutableStateOf(application.container.googleDriveService) }
    val viewModel: GoogleDriveViewModel = viewModel(
        factory = GoogleDriveViewModelFactory(groupId)
    )

    var folderStack by remember { mutableStateOf(listOf("root")) }
    val files = viewModel.files
    val selectedFiles = viewModel.selectedFiles
    val isLoading = viewModel.isLoading
    val downloadProgress = viewModel.downloadProgress

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_READONLY))
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_READONLY))
                        .apply { selectedAccount = account.account }
                    val service = GoogleDriveService(application.container.pdfRepository, credential)
                    application.container.googleDriveService = service
                    googleDriveService = service
                }
            } catch (e: ApiException) {
                Log.w("GoogleDriveScreen", "signInResult:failed code=" + e.statusCode)
            }
        }
    }

    LaunchedEffect(googleDriveService) {
        if (googleDriveService != null) {
            viewModel.fetchFiles(googleDriveService, folderStack.last())
        } else {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_READONLY))
                    .apply { selectedAccount = account.account }
                val service = GoogleDriveService(application.container.pdfRepository, credential)
                application.container.googleDriveService = service
                googleDriveService = service
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.google_drive_title)) },
                navigationIcon = {
                    if (folderStack.size > 1) {
                        IconButton(onClick = {
                            folderStack = folderStack.dropLast(1)
                            viewModel.fetchFiles(googleDriveService, folderStack.last())
                        }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (googleDriveService == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Button(onClick = { signInLauncher.launch(googleSignInClient.signInIntent) }) {
                        Text(stringResource(R.string.sign_in_to_google_drive))
                    }
                }
            } else if (isLoading && files.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(files, key = { it.id }) { file ->
                        val isFolder = file.mimeType == "application/vnd.google-apps.folder"
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isFolder) {
                                            folderStack = folderStack + file.id
                                            viewModel.fetchFiles(googleDriveService, file.id)
                                        } else {
                                            viewModel.toggleFileSelection(file)
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isFolder) Icons.Filled.Folder else Icons.Filled.PictureAsPdf,
                                    contentDescription = if (isFolder) stringResource(R.string.folder_description) else stringResource(R.string.pdf_file_description)
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(text = file.name, modifier = Modifier.weight(1f))
                                if (!isFolder) {
                                    Checkbox(
                                        checked = selectedFiles.contains(file),
                                        onCheckedChange = { _ -> viewModel.toggleFileSelection(file) }
                                    )
                                }
                            }
                            if (downloadProgress != null && downloadProgress.fileName == file.name) {
                                val downloadedMb = String.format("%.2f", downloadProgress.downloadedBytes / 1_000_000f)
                                val totalMb = if (downloadProgress.totalBytes > 0) String.format("%.2f", downloadProgress.totalBytes / 1_000_000f) else "???"

                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    if (downloadProgress.totalBytes > 0) {
                                        val percentage = (downloadProgress.downloadedBytes.toFloat() / downloadProgress.totalBytes.toFloat() * 100).toInt()
                                        LinearProgressIndicator(
                                            progress = downloadProgress.downloadedBytes.toFloat() / downloadProgress.totalBytes.toFloat(),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "$downloadedMb MB / $totalMb MB", fontSize = 12.sp)
                                            Text(text = "$percentage%", fontSize = 12.sp)
                                        }
                                    } else {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                        Text(text = "$downloadedMb MB / $totalMb MB", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                Button(
                    onClick = { viewModel.downloadSelectedFiles(googleDriveService, context) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedFiles.isNotEmpty() && !isLoading
                ) {
                    Text(stringResource(R.string.download_selected_files))
                }
            }
        }
    }
}