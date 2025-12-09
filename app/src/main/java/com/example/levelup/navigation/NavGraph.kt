package com.example.levelup.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.levelup.data.session.SessionManager
import com.example.levelup.ui.admin.AdminScreen
import com.example.levelup.ui.auth.LoginScreen
import com.example.levelup.ui.auth.RegisterScreen
import com.example.levelup.ui.cart.CartScreen
import com.example.levelup.ui.catalog.ProductListScreen
import com.example.levelup.ui.profile.ProfileScreen
import com.example.levelup.ui.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Catalog : Screen("catalog")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
    object Admin : Screen("admin")
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
                isAuthenticated = currentUser != null,
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
                val isAdmin = it.email.contains("admin", ignoreCase = true)

                ProductListScreen(
                    currentUserEmail = it.email,
                    currentUserName = it.name,
                    currentUserPhoto = it.photoUri,   // 👉 PASAMOS FOTO
                    isAdmin = isAdmin,
                    onLogout = {
                        SessionManager.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onProfile = { navController.navigate(Screen.Profile.route) },
                    onAdmin = { navController.navigate(Screen.Admin.route) }
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

        composable(Screen.Profile.route) {
            val user = currentUser
            if (user == null) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                ProfileScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Admin.route) {
            val user = currentUser
            if (user == null) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                // Regla simple: si el correo contiene "admin" es administrador
                if (user.email.contains("admin", ignoreCase = true)) {
                    AdminScreen(onBack = { navController.popBackStack() })
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}
