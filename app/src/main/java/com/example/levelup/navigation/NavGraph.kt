package com.example.levelup.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.levelup.ui.auth.LoginScreen
import com.example.levelup.ui.auth.RegisterScreen
import com.example.levelup.ui.viewmodel.AuthViewModel
import com.example.levelup.ui.catalog.ProductListScreen
import com.example.levelup.ui.cart.CartScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Catalog : Screen("catalog")
    object Cart : Screen("cart")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { navController.navigate(Screen.Catalog.route) },
                onNavigateRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.popBackStack()
                    navController.navigate(Screen.Login.route)
                },
                onNavigateLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Catalog.route) {
            ProductListScreen(
                onNavigateCart = { navController.navigate(Screen.Cart.route) },
                onNavigateDetail = { } // manejado internamente
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
