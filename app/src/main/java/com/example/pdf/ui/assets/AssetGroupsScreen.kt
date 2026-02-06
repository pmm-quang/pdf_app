package com.example.pdf.ui.assets

import androidx.compose.foundation.clickable
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
import com.example.pdf.data.AssetGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetGroupsScreen(navController: NavController) {
    val context = LocalContext.current
    val application = context.applicationContext as PdfApplication
    val assetRepository = application.container.assetRepository
    var assetGroups by remember { mutableStateOf<List<AssetGroup>>(emptyList()) }

    LaunchedEffect(Unit) {
        assetGroups = assetRepository.getAssetGroups()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Discover") })
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(assetGroups) { group ->
                AssetGroupListItem(group = group) {
                    navController.navigate("assetGroupFiles/${group.id}")
                }
            }
        }
    }
}

@Composable
fun AssetGroupListItem(group: AssetGroup, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = group.name,
            modifier = Modifier.padding(16.dp)
        )
    }
}
