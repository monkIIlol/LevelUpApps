package com.example.levelup.ui.catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.levelup.LevelUpApp
import com.example.levelup.R
import com.example.levelup.data.model.CartItem
import com.example.levelup.data.repo.CartRepository
import com.example.levelup.data.repo.ProductRepository
import kotlinx.coroutines.launch

@Composable
fun ProductDetailScreen(
    productId: Int,
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
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = prod.name,
                modifier = Modifier.size(150.dp)
            )

            Spacer(Modifier.height(16.dp))
            Text(prod.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(prod.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text("Precio: $${prod.price}", fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = { if (quantity > 1) quantity-- }) { Text("-") }
                Text(quantity.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = { quantity++ }) { Text("+") }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    scope.launch {
                        cartRepo.addToCart(CartItem(productId = prod.id, quantity = quantity))
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
