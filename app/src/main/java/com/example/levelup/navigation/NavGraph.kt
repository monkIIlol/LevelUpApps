package com.example.levelup.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.levelup.ui.auth.LoginScreen
import com.example.levelup.ui.auth.RegisterScreen
import com.example.levelup.ui.catalog.ProductListScreen
import com.example.levelup.ui.cart.CartScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.levelup.data.session.SessionManager
import com.example.levelup.ui.viewmodel.AuthViewModel



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
    val currentUser by SessionManager.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
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
            val user = currentUser
            LaunchedEffect(user) {
                if (user == null) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            user?.let {
                ProductListScreen(
                    currentUserEmail = it.email
                )
            }
        }

        composable(Screen.Cart.route) {
            val user = currentUser
            if (user == null) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                CartScreen(
                    userEmail = user.email,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
