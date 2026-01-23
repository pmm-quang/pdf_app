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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
                GroupDetailScreen(groupId = groupId, onPdfClicked = { filePaths, index ->
                    val encodedFilePaths = filePaths.joinToString(",") { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) }
                    navController.navigate("pdf_reader/$encodedFilePaths/$index")
                })
            }
        }
        composable(
            "pdf_reader/{filePaths}/{initialFileIndex}",
            arguments = listOf(
                navArgument("filePaths") { type = NavType.StringType },
                navArgument("initialFileIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val filePaths = backStackEntry.arguments?.getString("filePaths")?.split(",")
            val initialFileIndex = backStackEntry.arguments?.getInt("initialFileIndex")
            if (filePaths != null && initialFileIndex != null) {
                PdfReaderScreen(
                    filePaths = filePaths,
                    initialFileIndex = initialFileIndex,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable("create_group") {
            CreateGroupScreen(navController = navController)
        }
    }
}