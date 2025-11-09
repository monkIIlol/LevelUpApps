package com.example.levelup.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.levelup.LevelUpApp
import com.example.levelup.data.model.CartItem
import com.example.levelup.data.repo.CartRepository
import com.example.levelup.data.repo.ProductRepository
import com.example.levelup.ui.viewmodel.CartViewModel
import com.example.levelup.utils.formatPrice
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit
) {
    val cartRepo = remember { CartRepository(LevelUpApp.database.cartDao()) }
    val productRepo = remember { ProductRepository(LevelUpApp.database.productDao()) }
    val viewModel = remember { CartViewModel(cartRepo) }
    val scope = rememberCoroutineScope()

    val cartItems by viewModel.cartItems.collectAsState()
    var total by remember { mutableStateOf(0) }

    LaunchedEffect(cartItems) {
        val products = productRepo.getAllProducts()
        total = cartItems.sumOf { item ->
            val product = products.find { it.id == item.productId }
            (product?.price ?: 0) * item.quantity
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrito de compras") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total: ${total.formatPrice()}", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { scope.launch { viewModel.clearCart() } }, modifier = Modifier.weight(1f)) {
                            Text("Vaciar carrito")
                        }
                        Button(
                            onClick = {
                                scope.launch { viewModel.clearCart() }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Finalizar compra")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Tu carrito está vacío")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    CartItemCard(item = item, productRepo = productRepo, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    productRepo: ProductRepository,
    viewModel: CartViewModel
) {
    var productName by remember { mutableStateOf("Producto") }
    var productPrice by remember { mutableStateOf(0) }

    LaunchedEffect(item.productId) {
        val product = productRepo.getProductById(item.productId)
        productName = product?.name ?: "Desconocido"
        productPrice = product?.price ?: 0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(productName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = { viewModel.decreaseQuantity(item) }, modifier = Modifier.size(36.dp)) { Text("-") }
                Spacer(Modifier.width(8.dp))
                Text("${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = { viewModel.increaseQuantity(item) }, modifier = Modifier.size(36.dp)) { Text("+") }
            }
            Spacer(Modifier.height(8.dp))
            Text("Subtotal: ${(productPrice * item.quantity).formatPrice()}")
        }
    }
}
