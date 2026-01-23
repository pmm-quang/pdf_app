package com.example.pdf.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pdf.ui.assets.AssetGroupFilesScreen
import com.example.pdf.ui.assets.AssetGroupsScreen
import com.example.pdf.ui.groupdetail.GroupDetailScreen
import com.example.pdf.ui.groups.CreateGroupScreen
import com.example.pdf.ui.groups.GroupsScreen
import com.example.pdf.ui.reader.PdfReaderScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.MyLibrary,
        Screen.Discover,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            if (currentDestination?.route?.startsWith("reader/") == false) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.MyLibrary.route, Modifier.padding(innerPadding)) {
            composable(Screen.MyLibrary.route) { GroupsScreen(navController) }
            composable(Screen.Discover.route) { AssetGroupsScreen(navController) }
            composable("create_group") { CreateGroupScreen(navController) }
            composable(
                "assetGroupFiles/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) { backStackEntry ->
                AssetGroupFilesScreen(navController, backStackEntry.arguments?.getString("groupId") ?: "")
            }
            composable("group_detail/{groupId}") { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")
                if (groupId != null) {
                    GroupDetailScreen(groupId = groupId, onPdfClicked = { filePaths, index ->
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
}