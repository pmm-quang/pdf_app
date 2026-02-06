package com.example.pdf.ui.groups

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pdf.PdfApplication
import com.example.pdf.data.PdfSeries
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(navController: NavController) {
    var groupName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val repository = (context.applicationContext as PdfApplication).container.pdfRepository
    val scope = rememberCoroutineScope()

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

            Button(
                onClick = {
                    val newGroup = PdfSeries(name = groupName)
                    scope.launch {
                        repository.insertPdfSeries(newGroup, emptyList())
                    }
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
