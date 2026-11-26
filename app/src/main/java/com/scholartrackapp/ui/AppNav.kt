package com.scholartrackapp.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.scholartrackapp.ScholarTrackApp
import com.scholartrackapp.ui.screens.*

object Routes {
    const val AUTH = "auth"
    const val DISCOVER = "discover"
    const val APPLICATIONS = "applications"
    const val DETAIL = "detail/{id}"
    const val FORM = "form?applicationId={applicationId}&scholarshipId={scholarshipId}"

    fun detail(id: Int) = "detail/$id"
    fun form(applicationId: Int = -1, scholarshipId: Int = -1) =
        "form?applicationId=$applicationId&scholarshipId=$scholarshipId"
}

@Composable
fun ScholarTrackNav() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBottomBar = route == Routes.DISCOVER || route == Routes.APPLICATIONS

    val app = LocalContext.current.applicationContext as ScholarTrackApp
    val startDest = if (app.container.repository.currentUserId.value != null) Routes.DISCOVER else Routes.AUTH

    Scaffold(bottomBar = {
        if (showBottomBar) {
            NavigationBar {
                NavigationBarItem(
                    selected = route == Routes.DISCOVER,
                    onClick = { nav.navigateTopLevel(Routes.DISCOVER) },
                    icon = { Icon(Icons.Default.Search, null) },
                    label = { Text("Discover") }
                )
                NavigationBarItem(
                    selected = route == Routes.APPLICATIONS,
                    onClick = { nav.navigateTopLevel(Routes.APPLICATIONS) },
                    icon = { Icon(Icons.Default.Favorite, null) },
                    label = { Text("My applications") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        app.container.repository.logout()
                        nav.navigate(Routes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) },
                    label = { Text("Logout") }
                )
            }
        }
    }) { padding ->
        NavHost(
            navController = nav,
            startDestination = startDest,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding)
        ) {
            composable(Routes.AUTH) {
                AuthScreen(
                    onAuthenticated = {
                        nav.navigate(Routes.DISCOVER) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.DISCOVER) {
                DiscoverScreen(onOpen = { nav.navigate(Routes.detail(it)) })
            }
            composable(Routes.APPLICATIONS) {
                ApplicationsScreen(
                    onAdd = { nav.navigate(Routes.form()) },
                    onEdit = { nav.navigate(Routes.form(applicationId = it)) }
                )
            }
            composable(
                Routes.DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) {
                ScholarshipDetailScreen(
                    onBack = { nav.popBackStack() },
                    onTrack = { nav.navigate(Routes.form(scholarshipId = it)) }
                )
            }
            composable(
                Routes.FORM,
                arguments = listOf(
                    navArgument("applicationId") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("scholarshipId") { type = NavType.IntType; defaultValue = -1 }
                )
            ) {
                ApplicationFormScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}

private fun androidx.navigation.NavController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
