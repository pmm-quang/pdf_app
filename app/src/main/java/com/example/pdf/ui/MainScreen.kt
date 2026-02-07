package com.example.pdf.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pdf.ui.assets.AssetGroupFilesScreen
import com.example.pdf.ui.assets.AssetGroupsScreen
import com.example.pdf.ui.drive.GoogleDriveScreen
import com.example.pdf.ui.groupdetail.GroupDetailScreen
import com.example.pdf.ui.groups.CreateGroupScreen
import com.example.pdf.ui.groups.GroupsScreen
import com.example.pdf.ui.reader.PdfReaderScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Screen.MyLibrary.route) {
        composable(Screen.MyLibrary.route) { GroupsScreen(navController) }
        composable(Screen.Discover.route) { AssetGroupsScreen(navController) }
        composable("create_group") { CreateGroupScreen(navController) }
        composable("google_drive/{groupId}", arguments = listOf(navArgument("groupId") { type = NavType.StringType })) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                GoogleDriveScreen(groupId = groupId, navController = navController)
            }
        }
        composable(
            "assetGroupFiles/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            AssetGroupFilesScreen(navController, backStackEntry.arguments?.getString("groupId") ?: "")
        }
        composable("group_detail/{groupId}") { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                GroupDetailScreen(
                    groupId = groupId,
                    navController = navController,
                    onPdfClicked = { filePaths, index ->
                    val encodedFilePaths = filePaths.joinToString(",") { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) }
                    navController.navigate("reader/$encodedFilePaths/$index")
                })
            }
        }
        composable(
            "reader/{filePaths}/{initialFileIndex}",
            arguments = listOf(
                navArgument("filePaths") { type = NavType.StringType },
                navArgument("initialFileIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val filePaths = backStackEntry.arguments?.getString("filePaths")?.split(",")?.map { java.net.URLDecoder.decode(it, "UTF-8") }
            val initialFileIndex = backStackEntry.arguments?.getInt("initialFileIndex")
            if (filePaths != null && initialFileIndex != null) {
                PdfReaderScreen(
                    filePaths = filePaths,
                    initialFileIndex = initialFileIndex,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
