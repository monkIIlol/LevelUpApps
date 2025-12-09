package com.example.levelup.ui.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.levelup.LevelUpApp
import com.example.levelup.data.repo.CartRepository
import com.example.levelup.data.repo.ProductRepository
import com.example.levelup.utils.formatPrice
import kotlinx.coroutines.launch
import com.example.levelup.data.remote.RetrofitClient
import com.example.levelup.data.remote.api.ProductApiService

@Composable
fun ProductDetailScreen(
    productId: Int,
    userEmail: String,
    onBack: () -> Unit
) {
    val productApi = remember { RetrofitClient.createService(ProductApiService::class.java) }
    val productRepo = remember { ProductRepository(LevelUpApp.database.productDao(), productApi) }
    val cartRepo = remember { CartRepository(LevelUpApp.database.cartDao()) }

    val scope = rememberCoroutineScope()

    var product by remember { mutableStateOf<com.example.levelup.data.model.Product?>(null) }
    var quantity by remember { mutableStateOf(1) }

    LaunchedEffect(productId) {
        product = productRepo.getProductById(productId)
    }

    product?.let { prod ->
        Column(
            modifier = Modifier
                .padding(24.dp)
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = prod.imageUrl,
                contentDescription = prod.name,
                modifier = Modifier
                    .size(220.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            Text(prod.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))

            Text(prod.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))

            Text(
                text = "Precio: ${prod.price.formatPrice()}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalIconButton(onClick = { if (quantity > 1) quantity-- }) {
                    androidx.compose.material3.Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Disminuir cantidad"
                    )
                }

                Text(
                    quantity.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                FilledTonalIconButton(onClick = { quantity++ }) {
                    androidx.compose.material3.Icon(
                        Icons.Filled.Add,
                        contentDescription = "Aumentar cantidad"
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        cartRepo.addToCart(userEmail, prod.id, quantity)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar al carrito")
            }

            Spacer(Modifier.height(14.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar")
            }
        }
    } ?: Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
