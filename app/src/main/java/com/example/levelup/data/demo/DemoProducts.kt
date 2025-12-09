package com.example.levelup.data.demo

import com.example.levelup.data.model.Product

object DemoProducts {

    /**
     * Cambia esto a false cuando ya no quieras usar productos demo.
     * También puedes dejarlo true solo en debug.
     */
    const val ENABLE_DEMO_PRODUCTS: Boolean = false

    val items: List<Product> = listOf(
        Product(
            name = "PlayStation 5",
            description = "Consola PlayStation 5 estándar con 825GB SSD",
            price = 499_990,
            stock = 8,
            imageUrl = "https://via.placeholder.com/300x200/003791/FFFFFF?text=PlayStation+5"
        ),
        Product(
            name = "Xbox Series X",
            description = "Consola Xbox Series X con 1TB SSD",
            price = 479_990,
            stock = 6,
            imageUrl = "https://via.placeholder.com/300x200/107C10/FFFFFF?text=Xbox+Series+X"
        ),
        Product(
            name = "Nintendo Switch OLED",
            description = "Consola híbrida con pantalla OLED de 7 pulgadas",
            price = 389_990,
            stock = 10,
            imageUrl = "https://via.placeholder.com/300x200/E60012/FFFFFF?text=Switch+OLED"
        ),
        Product(
            name = "Auriculares Gaming RGB",
            description = "Headset con sonido envolvente 7.1 y micrófono desmontable",
            price = 59_990,
            stock = 25,
            imageUrl = "https://via.placeholder.com/300x200/4A148C/FFFFFF?text=Headset+RGB"
        ),
        Product(
            name = "Teclado Mecánico RGB",
            description = "Teclado mecánico con switches rojos y retroiluminación personalizable",
            price = 69_990,
            stock = 18,
            imageUrl = "https://via.placeholder.com/300x200/1A237E/FFFFFF?text=Teclado+RGB"
        ),
        Product(
            name = "Mouse Gamer 16000 DPI",
            description = "Mouse óptico con 8 botones programables",
            price = 39_990,
            stock = 30,
            imageUrl = "https://via.placeholder.com/300x200/004D40/FFFFFF?text=Mouse+Gamer"
        ),
        Product(
            name = "Silla Gamer Ergonómica",
            description = "Silla con soporte lumbar y reclinación 160°",
            price = 199_990,
            stock = 5,
            imageUrl = "https://via.placeholder.com/300x200/263238/FFFFFF?text=Silla+Gamer"
        ),
        Product(
            name = "Monitor 27\" 165Hz",
            description = "Monitor 2K con tasa de refresco de 165Hz",
            price = 249_990,
            stock = 7,
            imageUrl = "https://via.placeholder.com/300x200/01579B/FFFFFF?text=Monitor+27+165Hz"
        ),
        Product(
            name = "PlayStation 5",
            description = "Consola PlayStation 5 estándar con 825GB SSD",
            price = 499_990,
            stock = 8,
            imageUrl = "https://via.placeholder.com/300x200/003791/FFFFFF?text=PlayStation+5"
        ),
        Product(
            name = "Xbox Series X",
            description = "Consola Xbox Series X con 1TB SSD",
            price = 479_990,
            stock = 6,
            imageUrl = "https://via.placeholder.com/300x200/107C10/FFFFFF?text=Xbox+Series+X"
        ),
        Product(
            name = "Nintendo Switch OLED",
            description = "Consola híbrida con pantalla OLED de 7 pulgadas",
            price = 389_990,
            stock = 10,
            imageUrl = "https://via.placeholder.com/300x200/E60012/FFFFFF?text=Switch+OLED"
        ),
        Product(
            name = "Auriculares Gaming RGB",
            description = "Headset con sonido envolvente 7.1 y micrófono desmontable",
            price = 59_990,
            stock = 25,
            imageUrl = "https://via.placeholder.com/300x200/4A148C/FFFFFF?text=Headset+RGB"
        ),
        Product(
            name = "Teclado Mecánico RGB",
            description = "Teclado mecánico con switches rojos y retroiluminación personalizable",
            price = 69_990,
            stock = 18,
            imageUrl = "https://via.placeholder.com/300x200/1A237E/FFFFFF?text=Teclado+RGB"
        ),
        Product(
            name = "Mouse Gamer 16000 DPI",
            description = "Mouse óptico con 8 botones programables",
            price = 39_990,
            stock = 30,
            imageUrl = "https://via.placeholder.com/300x200/004D40/FFFFFF?text=Mouse+Gamer"
        ),
        Product(
            name = "Silla Gamer Ergonómica",
            description = "Silla con soporte lumbar y reclinación 160°",
            price = 199_990,
            stock = 5,
            imageUrl = "https://via.placeholder.com/300x200/263238/FFFFFF?text=Silla+Gamer"
        ),
        Product(
            name = "Monitor 27\" 165Hz",
            description = "Monitor 2K con tasa de refresco de 165Hz",
            price = 249_990,
            stock = 7,
            imageUrl = "https://via.placeholder.com/300x200/01579B/FFFFFF?text=Monitor+27+165Hz"
        )
    )
}
