package com.example.levelup.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.levelup.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    isAuthenticated: Boolean,
    onLoginSuccess: () -> Unit,
    onNavigateRegister: () -> Unit
) {
    val state = viewModel.uiState

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {

            Text(
                "Iniciar sesión",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Correo") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF39FF14),
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = Color(0xFF39FF14),
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                isError = state.emailError,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.emailError) Text("Correo inválido", color = MaterialTheme.colorScheme.error)

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Contraseña") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF39FF14),
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = Color(0xFF39FF14),
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                isError = state.passwordError,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            if (state.passwordError) Text("Mínimo 6 caracteres", color = MaterialTheme.colorScheme.error)

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { viewModel.login() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ingresar", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onNavigateRegister) {
                Text("¿No tienes cuenta? Regístrate")
            }

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}