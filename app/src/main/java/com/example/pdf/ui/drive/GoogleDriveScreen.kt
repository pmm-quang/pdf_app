package com.example.pdf.ui.drive

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pdf.PdfApplication
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveScreen(groupId: String, navController: NavController) {
    val context = LocalContext.current
    val repository = (context.applicationContext as PdfApplication).container.pdfRepository
    val scope = rememberCoroutineScope()

    var googleDriveService by remember { mutableStateOf<GoogleDriveService?>(null) }
    var files by remember { mutableStateOf<List<File>>(emptyList()) }
    var selectedFiles by remember { mutableStateOf(emptySet<File>()) }
    var isLoading by remember { mutableStateOf(false) }
    var folderStack by remember { mutableStateOf(listOf("root")) }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_READONLY))
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    fun fetchFiles(service: GoogleDriveService?, folderId: String) {
        scope.launch {
            isLoading = true
            files = service?.getFiles(folderId) ?: emptyList()
            isLoading = false
        }
    }

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
                    val service = GoogleDriveService(repository, credential)
                    googleDriveService = service
                    fetchFiles(service, folderStack.last())
                }
            } catch (e: ApiException) {
                Log.w("GoogleDriveScreen", "signInResult:failed code=" + e.statusCode)
            }
        }
    }

    LaunchedEffect(Unit) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_READONLY))
                .apply { selectedAccount = account.account }
            val service = GoogleDriveService(repository, credential)
            googleDriveService = service
            fetchFiles(service, folderStack.last())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google Drive") },
                navigationIcon = {
                    if (folderStack.size > 1) {
                        IconButton(onClick = {
                            folderStack = folderStack.dropLast(1)
                            fetchFiles(googleDriveService, folderStack.last())
                        }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                        Text("Sign in to Google Drive")
                    }
                }
            } else {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(files, key = { it.id }) { file ->
                            val isFolder = file.mimeType == "application/vnd.google-apps.folder"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isFolder) {
                                            folderStack = folderStack + file.id
                                            fetchFiles(googleDriveService, file.id)
                                        } else {
                                            selectedFiles = if (selectedFiles.contains(file)) {
                                                selectedFiles - file
                                            } else {
                                                selectedFiles + file
                                            }
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isFolder) Icons.Filled.Folder else Icons.Filled.PictureAsPdf,
                                    contentDescription = if (isFolder) "Folder" else "PDF File"
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(text = file.name, modifier = Modifier.weight(1f))
                                if (!isFolder) {
                                    Checkbox(
                                        checked = selectedFiles.contains(file),
                                        onCheckedChange = { _ ->
                                            selectedFiles = if (selectedFiles.contains(file)) {
                                                selectedFiles - file
                                            } else {
                                                selectedFiles + file
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                googleDriveService?.downloadFiles(groupId, selectedFiles.toList(), context)
                                isLoading = false
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedFiles.isNotEmpty()
                    ) {
                        Text("Download Selected Files")
                    }
                }
            }
        }
    }
}
