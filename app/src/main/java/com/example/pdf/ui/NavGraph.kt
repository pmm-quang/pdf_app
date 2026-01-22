package com.example.pdf.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pdf.ui.allpdfs.AllPdfsScreen
import com.example.pdf.ui.groupdetail.GroupDetailScreen
import com.example.pdf.ui.groups.CreateGroupScreen
import com.example.pdf.ui.groups.GroupsScreen
import com.example.pdf.ui.reader.PdfReaderScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = "groups", modifier = modifier) {
        composable("groups") {
            GroupsScreen(navController = navController)
        }
        composable("all_pdfs") {
            AllPdfsScreen(navController = navController)
        }
        composable("group_detail/{groupId}") { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                GroupDetailScreen(groupId = groupId, onPdfClicked = {
                    navController.navigate("pdf_reader/$it")
                })
            }
        }
        composable(
            "pdf_reader/{filePath}",
            arguments = listOf(navArgument("filePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val filePath = backStackEntry.arguments?.getString("filePath")
            if (filePath != null) {
                PdfReaderScreen(filePath = filePath)
            }
        }
        composable("create_group") {
            CreateGroupScreen(navController = navController)
        }
    }
}
