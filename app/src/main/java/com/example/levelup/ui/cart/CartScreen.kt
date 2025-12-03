package com.example.levelup.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import kotlinx.coroutines.delay

import com.example.levelup.data.remote.RetrofitClient
import com.example.levelup.data.remote.api.ProductApiService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    userEmail: String,
    onBack: () -> Unit,
    viewModel: CartViewModel? = null
) {
    val cartRepo = remember { CartRepository(LevelUpApp.database.cartDao()) }
    val cartViewModel = viewModel ?: remember(userEmail) { CartViewModel(cartRepo, userEmail) }

    // 👇 NUEVO: servicio API y repositorio de productos
    val productApi = remember {
        RetrofitClient.createService(ProductApiService::class.java)
    }

    val productRepo = remember {
        ProductRepository(
            dao = LevelUpApp.database.productDao(),
            api = productApi
        )
    }

    val cartItems by cartViewModel.cartItems.collectAsState()
    var total by remember { mutableStateOf(0) }

    var showPaymentMethods by remember { mutableStateOf(false) }
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val paymentMethods = listOf(
        "Tarjeta de crédito",
        "Transferencia bancaria",
        "PayPal"
    )

    // Cálculo del total usando los productos guardados en Room
    LaunchedEffect(cartItems) {
        val products = productRepo.getAllProducts()
        total = cartItems.sumOf { item ->
            val product = products.find { it.id == item.productId }
            (product?.price ?: 0) * item.quantity
        }
    }

    LaunchedEffect(isProcessingPayment, selectedMethod) {
        if (isProcessingPayment && selectedMethod != null) {
            delay(2000)
            cartViewModel.clearCart()
            isProcessingPayment = false
            showSuccessDialog = true
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { cartViewModel.clearCart() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Vaciar carrito")
                        }
                        Button(
                            onClick = { showPaymentMethods = true },
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Tu carrito está vacío")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    CartItemCard(
                        item = item,
                        productRepo = productRepo,
                        viewModel = cartViewModel
                    )
                }
            }
        }
    }

    if (showPaymentMethods) {
        AlertDialog(
            onDismissRequest = { showPaymentMethods = false },
            title = { Text("Selecciona un método de pago") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    paymentMethods.forEach { method ->
                        Button(
                            onClick = {
                                selectedMethod = method
                                showPaymentMethods = false
                                isProcessingPayment = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(method)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPaymentMethods = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (isProcessingPayment) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Procesando pago") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Confirmando tu compra...")
                }
            },
            confirmButton = {}
        )
    }

    if (showSuccessDialog && selectedMethod != null) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Compra finalizada") },
            text = { Text("Tu pago con $selectedMethod ha sido procesado exitosamente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        selectedMethod = null
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
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
                FilledTonalIconButton(onClick = { viewModel.decreaseQuantity(item) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Disminuir cantidad"
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(8.dp))
                FilledTonalIconButton(onClick = { viewModel.increaseQuantity(item) }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Aumentar cantidad"
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Subtotal: ${(productPrice * item.quantity).formatPrice()}")
        }
    }
}
