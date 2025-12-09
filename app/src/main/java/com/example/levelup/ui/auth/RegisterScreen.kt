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
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateLogin: () -> Unit
) {
    val state = viewModel.uiState

    // 👉 Navegar solo cuando realmente haya éxito
    LaunchedEffect(state.successMessage) {
        if (state.successMessage == "Registro exitoso") {
            onRegisterSuccess()
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
                "Crear cuenta",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Nombre") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF39FF14),
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = Color(0xFF39FF14),
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                isError = state.nameError,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.nameError) Text("Campo obligatorio", color = MaterialTheme.colorScheme.error)

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Correo electrónico") },
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
                onClick = {
                    viewModel.register()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrarse")
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onNavigateLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }

            state.errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            state.successMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
