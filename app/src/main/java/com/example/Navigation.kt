package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import androidx.compose.material.icons.filled.SportsCricket

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Cricket : Screen("cricket", "Cricket", Icons.Filled.SportsCricket)
    object Favorites : Screen("favorites", "Favorites", Icons.Filled.Star)
    object BetSlip : Screen("betslip", "Bet Slip", Icons.Filled.List)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Cricket,
    Screen.Favorites,
    Screen.BetSlip,
    Screen.Profile
)

@Composable
fun AppNavigation(repository: DataRepository, authViewModel: AuthViewModel, updateManager: AppUpdateManager? = null) {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onSplashComplete = { showSplash = false })
        return
    }

    if (updateManager != null) {
        val updateInfo by updateManager.checkForUpdates().collectAsState(initial = null)
        var skipUpdate by remember { mutableStateOf(false) }

        if (updateInfo != null && !skipUpdate) {
            UpdateScreen(
                updateInfo = updateInfo!!,
                onUpdateClicked = {
                    updateManager.downloadAndInstallUpdate(updateInfo!!.downloadUrl) { _ -> }
                },
                onSkipClicked = { skipUpdate = true }
            )
            return
        }
    }

    val user by authViewModel.user.collectAsState()
    val error by authViewModel.error.collectAsState()
    
    if (error != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { authViewModel.clearError() },
            title = { androidx.compose.material3.Text("Notice") },
            text = { androidx.compose.material3.Text(error!!) },
            confirmButton = { 
                androidx.compose.material3.TextButton(onClick = { authViewModel.clearError() }) { 
                    androidx.compose.material3.Text("OK") 
                } 
            }
        )
    }
    
    if (user != null) {
        MainApp(repository, authViewModel)
    } else {
        AuthNavigation(authViewModel)
    }
}

@Composable
fun AuthNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.navigate("login") { popUpTo("login") { inclusive = true } } }
            )
        }
    }
}

@Composable
fun MainApp(repository: DataRepository, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
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
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    repository = repository,
                    authViewModel = authViewModel,
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToMatch = { matchId ->
                        navController.navigate("match/$matchId")
                    }
                )
            }
            composable(Screen.Cricket.route) {
                CricketScreen(
                    repository = repository, 
                    authViewModel = authViewModel,
                    onNavigateToMatch = { matchId ->
                        navController.navigate("match/$matchId")
                    }
                )
            }
            composable(Screen.Favorites.route) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(
                        "Your favorites will appear here",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            composable(Screen.BetSlip.route) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(
                        "Your bet slip will appear here",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onNavigateToAdmin = { navController.navigate("admin") }
                )
            }
            composable("admin") {
                AdminScreen(onBack = { navController.popBackStack() })
            }
            composable("match/{matchId}") { backStackEntry ->
                val matchId = backStackEntry.arguments?.getString("matchId") ?: return@composable
                MatchDetailScreen(
                    matchId = matchId,
                    repository = repository,
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
