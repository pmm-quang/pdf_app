package com.example.pdf.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pdf.ui.allpdfs.AllPdfsScreen
import com.example.pdf.ui.assets.AssetGroupFilesScreen
import com.example.pdf.ui.assets.AssetGroupsScreen
import com.example.pdf.ui.drive.GoogleDriveScreen
import com.example.pdf.ui.groupdetail.GroupDetailScreen
import com.example.pdf.ui.groups.CreateGroupScreen
import com.example.pdf.ui.groups.GroupsScreen
import com.example.pdf.ui.reader.PdfReaderScreen
import com.example.pdf.ui.settings.AccountSettingsScreen
import com.example.pdf.ui.settings.LanguageSettingsScreen
import com.example.pdf.ui.settings.SettingsScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.MyLibrary.route, modifier = modifier) {
        composable(Screen.MyLibrary.route) { GroupsScreen(navController) }
        composable(Screen.Discover.route) { AssetGroupsScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("account_settings") { AccountSettingsScreen() }
        composable("language_settings") { LanguageSettingsScreen(navController) }
        composable("create_group") { CreateGroupScreen(navController) }
        composable("all_pdfs") {
            AllPdfsScreen(navController = navController)
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
                GroupDetailScreen(groupId = groupId, navController = navController, onPdfClicked = { filePaths, index ->
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
            val filePaths = backStackEntry.arguments?.getString("filePaths")?.split(",")?.map { URLDecoder.decode(it, "UTF-8") }
            val initialFileIndex = backStackEntry.arguments?.getInt("initialFileIndex")
            if (filePaths != null && initialFileIndex != null) {
                PdfReaderScreen(
                    filePaths = filePaths,
                    initialFileIndex = initialFileIndex,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(
            "google_drive/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                GoogleDriveScreen(groupId = groupId, navController = navController)
            }
        }
    }
}
