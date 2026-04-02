package com.kk.calorietracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.FoodBank
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kk.calorietracker.R
import com.kk.calorietracker.ui.food.FoodItemScreen
import com.kk.calorietracker.ui.meallog.MealLogScreen
import com.kk.calorietracker.ui.mealtype.MealTypeScreen
import com.kk.calorietracker.ui.targets.TargetsScreen
import com.kk.calorietracker.ui.trends.TrendsScreen
import kotlinx.serialization.Serializable

@Serializable object FoodItemRoute
@Serializable object MealTypeRoute
@Serializable object MealLogRoute
@Serializable object TrendsRoute
@Serializable object TargetsRoute

private data class BottomNavItem(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalorieTrackerNavGraph() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem(FoodItemRoute, stringResource(R.string.nav_food), Icons.Filled.FoodBank, Icons.Outlined.FoodBank),
        BottomNavItem(MealTypeRoute, stringResource(R.string.nav_meal_types), Icons.Filled.List, Icons.Outlined.List),
        BottomNavItem(MealLogRoute, stringResource(R.string.nav_log_meal), Icons.Filled.Restaurant, Icons.Outlined.Restaurant),
        BottomNavItem(TrendsRoute, stringResource(R.string.nav_trends), Icons.Filled.ShowChart, Icons.Outlined.ShowChart),
    )

    val isTargetsScreen = currentDestination?.hasRoute<TargetsRoute>() == true

    val screenTitle = when {
        currentDestination?.hasRoute<FoodItemRoute>() == true -> stringResource(R.string.screen_food_items)
        currentDestination?.hasRoute<MealTypeRoute>() == true -> stringResource(R.string.screen_meal_types)
        currentDestination?.hasRoute<MealLogRoute>() == true -> stringResource(R.string.screen_log_meal)
        currentDestination?.hasRoute<TrendsRoute>() == true -> stringResource(R.string.screen_trends)
        currentDestination?.hasRoute<TargetsRoute>() == true -> stringResource(R.string.screen_targets)
        else -> stringResource(R.string.app_name)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                actions = {
                    if (!isTargetsScreen) {
                        IconButton(onClick = { navController.navigate(TargetsRoute) }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(R.string.screen_targets),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hasRoute(item.route::class) == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                            )
                        },
                        label = { Text(item.label) },
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = FoodItemRoute,
            modifier = androidx.compose.ui.Modifier.padding(padding),
        ) {
            composable<FoodItemRoute> {
                FoodItemScreen(snackbarHostState = snackbarHostState)
            }
            composable<MealTypeRoute> {
                MealTypeScreen(snackbarHostState = snackbarHostState)
            }
            composable<MealLogRoute> {
                MealLogScreen(snackbarHostState = snackbarHostState)
            }
            composable<TrendsRoute> {
                TrendsScreen(
                    snackbarHostState = snackbarHostState,
                    onNavigateToTargets = { navController.navigate(TargetsRoute) },
                )
            }
            composable<TargetsRoute> {
                TargetsScreen(
                    snackbarHostState = snackbarHostState,
                    onNavigateToTrends = {
                        navController.navigate(TrendsRoute) {
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
    }
}
