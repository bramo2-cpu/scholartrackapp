package com.scholartrackapp.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.scholartrackapp.ui.screens.*

object Routes {
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
            }
        }
    }) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.DISCOVER,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding)
        ) {
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

