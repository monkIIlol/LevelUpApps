package com.example.levelup.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.location.Location
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.levelup.LevelUpApp
import com.example.levelup.data.repo.UserRepository
import com.example.levelup.ui.viewmodel.ProfileViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Repositorio y ViewModel
    val userDao = remember { LevelUpApp.database.userDao() }
    val userRepo = remember { UserRepository(userDao) }
    val viewModel = remember { ProfileViewModel(userRepo) }

    val uiState by viewModel.uiState.collectAsState()

    // Cámara / Galería
    var tempBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tempPhotoUri by remember { mutableStateOf<String?>(null) } // foto pendiente sin guardar
    var isPreviewOpen by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let { bmp ->
            tempBitmap = bmp

            // Guardar bitmap en almacenamiento interno, pero sin persistir en BD aún
            val fileName = "profile_${System.currentTimeMillis()}.png"
            val file = File(context.filesDir, fileName)
            try {
                FileOutputStream(file).use { out ->
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                tempPhotoUri = file.absolutePath
            } catch (_: Exception) {
                tempPhotoUri = null
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            tempBitmap = null
            tempPhotoUri = it.toString() // solo en memoria hasta que apriete "Guardar foto"
        }
    }

    // Ubicación
    var locationText by remember { mutableStateOf("") }
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Coordenadas persistidas
    var locationLat by remember { mutableStateOf<Double?>(null) }
    var locationLng by remember { mutableStateOf<Double?>(null) }

    // Coordenadas temporales (selección desde mapa sin guardar)
    var tempLocationLat by remember { mutableStateOf<Double?>(null) }
    var tempLocationLng by remember { mutableStateOf<Double?>(null) }

    fun buildLocationLabel(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                val country = addr.countryName
                val label = listOfNotNull(city, country).joinToString(", ")
                if (label.isNotBlank()) label else "Lat: $lat, Lng: $lng"
            } else {
                "Lat: $lat, Lng: $lng"
            }
        } catch (_: Exception) {
            "Lat: $lat, Lng: $lng"
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        fusedClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val label = buildLocationLabel(it.latitude, it.longitude)

                    locationText = label
                    locationLat = it.latitude
                    locationLng = it.longitude
                    tempLocationLat = null
                    tempLocationLng = null

                    // Tu ViewModel ahora debería aceptar lat/lng además del label
                    viewModel.updateLocation(label, it.latitude, it.longitude)
                }
            }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            requestLocation()
        }
    }

    // Edición de datos
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var formError by remember { mutableStateOf<String?>(null) }
    var formSuccess by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                uiState.user != null -> {
                    val user = uiState.user!!

                    // Preparar valores iniciales (incluye ubicación)
                    LaunchedEffect(user) {
                        editName = user.name
                        editEmail = user.email
                        locationText = user.location.orEmpty()
                        locationLat = user.locationLat
                        locationLng = user.locationLng
                        tempLocationLat = null
                        tempLocationLng = null
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // FOTO DE PERFIL ARRIBA (tappable)
                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .clickable { isPreviewOpen = true },
                            contentAlignment = Alignment.Center
                        ) {
                            val model: Any? = when {
                                !tempPhotoUri.isNullOrBlank() -> tempPhotoUri
                                !user.photoUri.isNullOrBlank() -> user.photoUri
                                tempBitmap != null -> tempBitmap
                                else -> null
                            }

                            if (model != null) {
                                AsyncImage(
                                    model = model,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = CircleShape,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "Foto",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        val hasPendingPhotoChange =
                            !tempPhotoUri.isNullOrBlank() && tempPhotoUri != user.photoUri

                        // Botón GUARDAR FOTO (solo si hay cambio pendiente)
                        if (hasPendingPhotoChange) {
                            Button(
                                onClick = {
                                    viewModel.updatePhoto(tempPhotoUri ?: "")
                                    tempPhotoUri = null
                                    tempBitmap = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Guardar foto")
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        // Botón para eliminar foto (si existe algo)
                        if (!user.photoUri.isNullOrBlank() ||
                            !tempPhotoUri.isNullOrBlank() ||
                            tempBitmap != null
                        ) {
                            OutlinedButton(
                                onClick = {
                                    tempBitmap = null
                                    tempPhotoUri = null
                                    viewModel.updatePhoto("")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Eliminar foto")
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        // DATOS BÁSICOS SOLO LECTURA
                        OutlinedTextField(
                            value = user.name,
                            onValueChange = {},
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = user.email,
                            onValueChange = {},
                            label = { Text("Correo") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        )

                        Spacer(Modifier.height(8.dp))

                        val hiddenPassword = "*".repeat(user.password.length.coerceAtMost(12))
                        OutlinedTextField(
                            value = hiddenPassword,
                            onValueChange = {},
                            label = { Text("Contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            visualTransformation = PasswordVisualTransformation()
                        )

                        Spacer(Modifier.height(16.dp))

                        // BOTÓN EDITAR DATOS
                        Button(
                            onClick = {
                                isEditing = !isEditing
                                formError = null
                                formSuccess = null
                                currentPassword = ""
                                newPassword = ""
                                confirmNewPassword = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isEditing) "Cancelar edición" else "Editar datos")
                        }

                        if (isEditing) {
                            Spacer(Modifier.height(16.dp))

                            OutlinedTextField(
                                value = editName,
                                onValueChange = {
                                    editName = it
                                    formError = null
                                    formSuccess = null
                                },
                                label = { Text("Nuevo nombre") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = editEmail,
                                onValueChange = {
                                    editEmail = it
                                    formError = null
                                    formSuccess = null
                                },
                                label = { Text("Nuevo correo") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = currentPassword,
                                onValueChange = {
                                    currentPassword = it
                                    formError = null
                                    formSuccess = null
                                },
                                label = { Text("Contraseña actual") },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = PasswordVisualTransformation()
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = {
                                    newPassword = it
                                    formError = null
                                    formSuccess = null
                                },
                                label = { Text("Nueva contraseña (opcional)") },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = PasswordVisualTransformation()
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = confirmNewPassword,
                                onValueChange = {
                                    confirmNewPassword = it
                                    formError = null
                                    formSuccess = null
                                },
                                label = { Text("Repetir nueva contraseña") },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = PasswordVisualTransformation()
                            )

                            formError?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            formSuccess?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    when {
                                        editName.isBlank() || editEmail.isBlank() ->
                                            formError = "Nombre y correo no pueden estar vacíos"

                                        !editEmail.contains("@") ->
                                            formError = "Correo no válido"

                                        currentPassword.isBlank() ->
                                            formError = "Debes ingresar tu contraseña actual"

                                        currentPassword != user.password ->
                                            formError = "La contraseña actual no coincide"

                                        newPassword.isNotBlank() && newPassword != confirmNewPassword ->
                                            formError = "Las nuevas contraseñas no coinciden"

                                        else -> {
                                            val finalNewPassword =
                                                if (newPassword.isBlank()) null else newPassword

                                            viewModel.updateUserData(
                                                newName = editName,
                                                newEmail = editEmail,
                                                newPassword = finalNewPassword
                                            )
                                            formError = null
                                            formSuccess = "Datos actualizados"
                                            isEditing = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Guardar cambios")
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Ubicación (texto)
                        val hasCoords = locationLat != null && locationLng != null

                        Text(
                            text = "Ubicación: ${
                                if (locationText.isBlank()) "No definida" else locationText
                            }"
                        )

                        Spacer(Modifier.height(8.dp))

                        // Botón DETECTAR solo si NO hay coordenadas guardadas
                        if (!hasCoords) {
                            Button(
                                onClick = {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Detectar ubicación")
                            }

                            Spacer(Modifier.height(16.dp))
                        } else {
                            Spacer(Modifier.height(8.dp))
                        }

                        // Mapa interactivo (si tenemos coordenadas)
                        val baseLat = tempLocationLat ?: locationLat
                        val baseLng = tempLocationLng ?: locationLng

                        if (baseLat != null && baseLng != null) {
                            val center = LatLng(baseLat, baseLng)
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(center, 14f)
                            }

                            Text(
                                text = "Ubicación en mapa",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )

                            GoogleMap(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                cameraPositionState = cameraPositionState,
                                onMapClick = { latLng ->
                                    tempLocationLat = latLng.latitude
                                    tempLocationLng = latLng.longitude
                                }
                            ) {
                                Marker(
                                    state = MarkerState(position = center),
                                    title = "Ubicación seleccionada"
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            val hasPendingLocationChange =
                                tempLocationLat != null && tempLocationLng != null &&
                                        (tempLocationLat != locationLat ||
                                                tempLocationLng != locationLng)

                            if (hasPendingLocationChange) {
                                Button(
                                    onClick = {
                                        val lat = tempLocationLat!!
                                        val lng = tempLocationLng!!
                                        val label = buildLocationLabel(lat, lng)

                                        locationText = label
                                        locationLat = lat
                                        locationLng = lng

                                        viewModel.updateLocation(label, lat, lng)

                                        tempLocationLat = null
                                        tempLocationLng = null
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Guardar ubicación")
                                }
                                Spacer(Modifier.height(8.dp))
                            }

                            OutlinedButton(
                                onClick = {
                                    locationText = ""
                                    locationLat = null
                                    locationLng = null
                                    tempLocationLat = null
                                    tempLocationLng = null
                                    viewModel.clearLocation()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Eliminar ubicación")
                            }

                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay usuario en sesión.")
                    }
                }
            }

            // PREVIEW FULLSCREEN DE LA FOTO
            if (isPreviewOpen && uiState.user != null) {
                val user = uiState.user!!
                val model: Any? = when {
                    !tempPhotoUri.isNullOrBlank() -> tempPhotoUri
                    !user.photoUri.isNullOrBlank() -> user.photoUri
                    tempBitmap != null -> tempBitmap
                    else -> null
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (model != null) {
                                AsyncImage(
                                    model = model,
                                    contentDescription = "Foto de perfil ampliada",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(260.dp)
                                )
                            } else {
                                Text(
                                    text = "Sin foto de perfil",
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { cameraLauncher.launch(null) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Tomar foto")
                            }
                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Galería")
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { isPreviewOpen = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Cerrar",
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
