package com.scholartrackapp.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.scholartrackapp.data.remote.AppVersionDto
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

    val context = LocalContext.current
    val app = context.applicationContext as ScholarTrackApp
    val startDest = if (app.container.repository.currentUserId.value != null) Routes.DISCOVER else Routes.AUTH

    var availableUpdate by remember { mutableStateOf<AppVersionDto?>(null) }

    LaunchedEffect(Unit) {
        val currentCode = try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode.toInt()
            else @Suppress("DEPRECATION") info.versionCode
        } catch (_: PackageManager.NameNotFoundException) {
            1
        }
        availableUpdate = app.container.repository.checkForAppUpdate(currentCode)
    }

    availableUpdate?.let { update ->
        AlertDialog(
            onDismissRequest = { availableUpdate = null },
            title = { Text("App Update Available (v${update.versionName ?: "New"})") },
            text = {
                Text(
                    update.notes?.takeIf { it.isNotBlank() }
                        ?: "A newer version of ScholarTrack is ready from your laptop build. Tap Update Now to download and install it."
                )
            },
            confirmButton = {
                Button(onClick = {
                    val url = update.apkUrl ?: "https://github.com/bramo2-cpu/scholartrackapp/raw/main/releases/ScholarTrack-latest.apk"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    availableUpdate = null
                }) {
                    Text("Update Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { availableUpdate = null }) {
                    Text("Later")
                }
            }
        )
    }

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
