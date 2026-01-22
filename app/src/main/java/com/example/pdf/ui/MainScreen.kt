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
import com.example.pdf.ui.groups.GroupsScreen
import com.example.pdf.ui.reader.PdfReaderScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.MyLibrary,
        Screen.Discover,
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
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
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.MyLibrary.route, Modifier.padding(innerPadding)) {
            composable(Screen.MyLibrary.route) { GroupsScreen(navController) }
            composable(Screen.Discover.route) { AssetGroupsScreen(navController) }
            composable(
                "assetGroupFiles/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) { backStackEntry ->
                AssetGroupFilesScreen(navController, backStackEntry.arguments?.getString("groupId") ?: "")
            }
            composable(
                "reader/{filePath}",
                arguments = listOf(navArgument("filePath") { type = NavType.StringType })
            ) { backStackEntry ->
                PdfReaderScreen(backStackEntry.arguments?.getString("filePath") ?: "")
            }
        }
    }
}