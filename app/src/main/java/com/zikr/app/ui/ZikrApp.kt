package com.zikr.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zikr.app.model.DhikrPeriod
import com.zikr.app.ui.screens.DhikrScreen
import com.zikr.app.ui.screens.HomeScreen
import com.zikr.app.ui.theme.ZikrTheme

@Composable
fun ZikrApp(
    launchTarget: DhikrPeriod?,
    onDeepLinkConsumed: () -> Unit
) {
    ZikrTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToDhikr = { period ->
                        navController.navigate("dhikr/${period.name}") {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("dhikr/{period}") { backStackEntry ->
                val period = DhikrPeriod.fromString(
                    backStackEntry.arguments?.getString("period")
                )
                DhikrScreen(
                    period = period,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        LaunchedEffect(launchTarget) {
            if (launchTarget != null) {
                navController.navigate("dhikr/${launchTarget.name}") {
                    launchSingleTop = true
                }
                onDeepLinkConsumed()
            }
        }
    }
}
