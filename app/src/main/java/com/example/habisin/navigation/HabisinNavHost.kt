package com.example.habisin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habisin.ui.screens.AiScreen
import com.example.habisin.ui.screens.CartScreen
import com.example.habisin.ui.screens.FavoritesScreen
import com.example.habisin.ui.screens.HomeScreen
import com.example.habisin.ui.screens.LoginScreen
import com.example.habisin.ui.screens.OrdersScreen
import com.example.habisin.ui.screens.ProfileScreen
import com.example.habisin.ui.screens.RegisterScreen
import com.example.habisin.ui.screens.SellerScreen
import com.example.habisin.ui.screens.ShopDetailScreen
import com.example.habisin.ui.screens.ShopScreen

@Composable
fun HabisinNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.REGISTER) { RegisterScreen(navController) }
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.PROFILE) { ProfileScreen(navController) }
        composable(Routes.SELLER) { SellerScreen(navController) }
        composable(Routes.ORDERS) { OrdersScreen(navController) }
        composable(Routes.AI) { AiScreen(navController) }
        composable(Routes.FAVORITES) { FavoritesScreen(navController) }
        composable(Routes.CART) { CartScreen(navController) }
        composable(Routes.SHOPS) { ShopScreen(navController) }
        composable(Routes.SHOP_DETAIL, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            ShopDetailScreen(navController, entry.arguments?.getLong("id") ?: 0L)
        }
    }
}
