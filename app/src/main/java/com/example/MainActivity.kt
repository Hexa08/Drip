package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.services.FirebaseService
import com.example.features.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase SDK with protective local fallback handling
        FirebaseService.initialize(applicationContext)

        val prefs = getSharedPreferences("drip_prefs", Context.MODE_PRIVATE)

        enableEdgeToEdge()
        setContent {
            // Dynamic theme setting stored inside shared_preferences as requested
            var darkTheme by remember {
                mutableStateOf(prefs.getBoolean("dark_theme_enabled", true))
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                // Initialize clean Compose NavHost structure
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("splash") {
                        SplashScreen(navController)
                    }
                    composable("onboarding") {
                        OnboardingScreen(navController)
                    }
                    composable("login") {
                        LoginScreen(navController)
                    }
                    composable("signup") {
                        SignUpScreen(navController)
                    }
                    composable("forgot_password") {
                        ForgotPasswordScreen(navController)
                    }
                    composable("navbar") {
                        NavbarContainer(
                            navController = navController,
                            darkTheme = darkTheme,
                            onThemeToggle = { isDark ->
                                darkTheme = isDark
                                prefs.edit().putBoolean("dark_theme_enabled", isDark).apply()
                            }
                        )
                    }
                    composable(
                        route = "product_detail/{productId}",
                        arguments = listOf(navArgument("productId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("productId") ?: 0
                        ProductDetailScreen(navController, id)
                    }
                    composable(
                        route = "checkout/{totalCost}",
                        arguments = listOf(navArgument("totalCost") { type = NavType.FloatType })
                    ) { backStackEntry ->
                        val cost = backStackEntry.arguments?.getFloat("totalCost")?.toDouble() ?: 0.0
                        CheckoutScreen(navController, cost)
                    }
                    composable(
                        route = "confirmation/{orderId}",
                        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("orderId") ?: ""
                        OrderConfirmationScreen(navController, id)
                    }
                    composable("my_orders") {
                        MyOrdersScreen(navController)
                    }
                    composable("my_addresses") {
                        MyAddressesScreen(navController)
                    }
                }
            }
        }
    }
}
