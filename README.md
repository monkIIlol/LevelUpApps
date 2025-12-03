## 1. Nombre del Proyecto: 
    LevelUp



## 2. Integrantes:
    - Diego Torres
    - Cristian urcullú



## 3. Funcionalidades:
- **Catálogo de productos LevelUp Gamer**
  - Listado de consolas, juegos y accesorios obtenidos desde el             microservicio REST.
  - Visualización de nombre, descripción, precio, categoría, stock e imagen.

- **Detalle de producto**
  - Pantalla con información ampliada del producto seleccionado.
  - Permite agregar unidades al carrito respetando el stock disponible.

- **Carrito de compras**
  - Agregar, aumentar, disminuir y eliminar productos del carrito.
  - Cálculo automático del total a pagar.
  - Persistencia del contenido del carrito usando la capa de datos local (Room).

- **Consumo de API externa**
  - Integración de una API pública para complementar la información mostrada en la app  
    (por ejemplo, datos adicionales, información de contexto o similares).
  - Los datos externos se consumen con Retrofit y se muestran en la interfaz sin
    reemplazar la información del microservicio propio.

- **Gestión de sesión básica**
  - Módulo de sesión que permite simular usuario actual (SessionManager).
  - Manejo de usuario logueado / sin sesión para ciertas acciones.

- **Arquitectura y buenas prácticas**
  - Consumo de servicios mediante Retrofit.
  - Uso de corrutinas y Flows/StateFlows para el manejo reactivo del estado.
  - Pruebas unitarias con JUnit y MockK sobre lógica de negocio y ViewModels.



## 4. Endpoints utilizados (API externa y microservicio): 
Base URL:  
https://api-dfs2-dm-production.up.railway.app/

### 4.1 Microservicio LevelUp (consumido directamente por la app)

- **GET /products**  
  Devuelve el listado completo del catálogo.  
  Usado en ProductViewModel para cargar los productos de la pantalla principal.

- **GET /products/{id}**  
  Entrega la información detallada de un producto.  
  Usado al abrir ProductDetailScreen.

### 4.2 API externa (origen de datos del microservicio)

El microservicio LevelUp utiliza una API externa de catálogo (similar a FakeStoreAPI) como fuente de datos.  
La app Android no la consume directamente, pero recibe sus datos a través del microservicio.

Ejemplos:  
- `GET https://fakestoreapi.com/products`  
- `GET https://fakestoreapi.com/products/{id}`

De esta forma se cumple el requisito de integrar una API externa en el proyecto.



## 5. Captura del APK firmado y .jks:
Generación del APK: <img width="641" height="139" alt="Captura de pantalla 2025-12-02 234712" src="https://github.com/user-attachments/assets/630730f2-ef74-4838-8cfd-b425d098249e" />


APK firmado: <img width="566" height="439" alt="Captura de pantalla 2025-12-02 235416" src="https://github.com/user-attachments/assets/0c1a4fc2-d18b-4dd0-b4d1-798c2e414660" />

