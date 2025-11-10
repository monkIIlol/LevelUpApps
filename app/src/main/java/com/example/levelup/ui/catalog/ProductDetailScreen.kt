package com.example.levelup.ui.catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.levelup.LevelUpApp
import com.example.levelup.data.repo.CartRepository
import com.example.levelup.data.repo.ProductRepository
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.ui.layout.ContentScale



@Composable
fun ProductDetailScreen(
    productId: Int,
    userEmail: String,
    onBack: () -> Unit
) {
    val productRepo = remember { ProductRepository(LevelUpApp.database.productDao()) }
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
            val imageRes = productImageResource(prod.imageUrl)
            Image(
                painter = painterResource(imageRes),
                contentDescription = prod.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 16.dp)            )

            Text(prod.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text(prod.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text("Precio: $${prod.price}", fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalIconButton(
                    onClick = { if (quantity > 1) quantity-- }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Disminuir cantidad"
                    )
                }
                Text(quantity.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                FilledTonalIconButton(onClick = { quantity++ }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Aumentar cantidad"
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
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

            Spacer(Modifier.height(16.dp))
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
