package cl.levelup.productservice.controller;

import cl.levelup.productservice.model.dto.MessageResponse;
import cl.levelup.productservice.model.dto.ResenaRequest;
import cl.levelup.productservice.model.Product;
import cl.levelup.productservice.model.Resena;
import cl.levelup.productservice.service.ProductService;
import cl.levelup.productservice.service.ResenaService;
import cl.levelup.productservice.storage.SupabaseStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "APIs para gestión de productos, imágenes y reseñas")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @Autowired
    private ResenaService resenaService;

    @Operation(summary = "Health check", description = "Verifica el estado del servicio de productos")
    @ApiResponse(responseCode = "200", description = "Servicio funcionando correctamente")
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(new MessageResponse("Ok"));
    }

    @Operation(
            summary = "Obtener todos los productos",
            description = "Retorna lista de todos los productos ordenados por código"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente"),
            @ApiResponse(responseCode = "204", description = "No hay productos disponibles")
    })
    @GetMapping("/")
    public ResponseEntity<?> getAllProducts() {
        List<Product> products = productService.findAll()
                .stream()
                .sorted(Comparator.comparing(Product::getCodigo))
                .collect(Collectors.toList());
        return products.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(products);
    }

    @Operation(
            summary = "Obtener producto por código",
            description = "Retorna un producto específico con sus reseñas enriquecidas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{productCode}")
    public ResponseEntity<?> getProduct(
            @Parameter(description = "Código único del producto", example = "PROD-001", required = true)
            @PathVariable String productCode) {

        Product existing = productService.findByCodigo(productCode);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Product doesn't exists"));
        }

        List<Resena> resenasEnriquecidas = resenaService.getResenasEnriquecidas(productCode);
        existing.setResenas(resenasEnriquecidas);

        return ResponseEntity.ok(existing);
    }

    @Operation(
            summary = "Subir imagen de producto",
            description = "Sube una imagen para un producto específico a Supabase Storage"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagen subida exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error al subir la imagen")
    })
    @PostMapping(
            path = "/{productCode}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadProductImage(
            @Parameter(description = "Código del producto", required = true)
            @PathVariable("productCode") String productCode,
            @Parameter(
                    description = "Archivo de imagen",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Categoría del producto", example = "electronics", required = true)
            @RequestParam("categoria") String categoria,
            @Parameter(description = "Nombre del producto", example = "Laptop Gaming", required = true)
            @RequestParam("nombreProducto") String nombreProducto
    ) {
        try {
            String publicUrl = supabaseStorageService.uploadProductImage(categoria, nombreProducto, file);

            Map<String, String> body = new HashMap<>();
            body.put("publicUrl", publicUrl);

            return ResponseEntity.ok(body);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al subir imagen de producto: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Agregar nuevo producto",
            description = "Crea un nuevo producto en el catálogo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto agregado exitosamente"),
            @ApiResponse(responseCode = "409", description = "El producto ya existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/")
    public ResponseEntity<MessageResponse> addProduct(
            @Parameter(description = "Datos del producto a crear", required = true)
            @RequestBody Product productRequest) {
        System.out.println("Producto recibido: " + productRequest);
        try {
            Product existing = productService.findByCodigo(productRequest.getCodigo());
            if (productRequest.getId() <= 0 && existing == null) {

                String baseUrl = "http://levelup.ddns.net:8000/storage/v1/object/public/levelup_files/products/";
                String categoryPath = productRequest.getCategoria() + "/";

                String imagen = productRequest.getImagenUrl();

                if (imagen != null && !imagen.isEmpty()) {
                    if (imagen.startsWith("http://") || imagen.startsWith("https://")) {
                        // Ya es una URL completa, no hacer nada
                    } else if (!imagen.startsWith(baseUrl + categoryPath)) {
                        imagen = baseUrl + categoryPath + imagen;
                    }
                    productRequest.setImagenUrl(imagen);
                }

                productService.add(productRequest);
                return ResponseEntity.ok(new MessageResponse("Product added successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new MessageResponse("Product already exists"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error interno al agregar producto"));
        }
    }

    @Operation(
            summary = "Eliminar producto",
            description = "Elimina un producto y su imagen asociada de Supabase Storage"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "ID de producto no proporcionado"),
            @ApiResponse(responseCode = "500", description = "Error al eliminar el producto")
    })
    @DeleteMapping("/{productId}")
    public ResponseEntity<MessageResponse> deleteProduct(
            @Parameter(description = "Código del producto a eliminar", example = "PROD-001", required = true)
            @PathVariable String productId) {
        if (productId == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("Product id not found"));
        }

        try {
            Product existing = productService.findByCodigo(productId);

            if (existing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Product doesn't exist"));
            }

            String imagenUrl = existing.getImagenUrl();
            if (imagenUrl != null && !imagenUrl.isBlank()) {
                supabaseStorageService.deleteByPublicUrl(imagenUrl);
            }

            productService.delete(productId);

            return ResponseEntity.ok(new MessageResponse("Product deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error deleting product: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Actualizar producto",
            description = "Actualiza un producto existente (actualización completa o parcial)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PutMapping("/{productCode}")
    public ResponseEntity<MessageResponse> updateProduct(
            @Parameter(description = "Código del producto a actualizar", required = true)
            @PathVariable String productCode,
            @Parameter(description = "Datos actualizados del producto", required = true)
            @RequestBody Product productRequest) {

        Product existing = productService.findByCodigo(productCode);

        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Product doesn't exist"));
        }

        String baseUrl = "http://levelup.ddns.net:8000/storage/v1/object/public/levelup_files/products/";
        String categoryPath = productRequest.getCategoria() + "/";

        if (productRequest.getImagenUrl() != null && !productRequest.getImagenUrl().isEmpty()) {
            String imagen = productRequest.getImagenUrl();

            if (imagen.startsWith("http://") || imagen.startsWith("https://")) {
                // Ya es una URL completa, no hacer nada
            } else if (!imagen.startsWith(baseUrl + categoryPath)) {
                productRequest.setImagenUrl(baseUrl + categoryPath + imagen);
            } else {
                productRequest.setImagenUrl(imagen);
            }
        }

        boolean isPartial = productService.isPartialUpdate(productRequest);

        if (isPartial) {
            productService.partialUpdate(existing, productRequest);
            return ResponseEntity.ok(new MessageResponse("Product partially updated"));
        } else {
            productService.update(existing, productRequest);
            return ResponseEntity.ok(new MessageResponse("Product fully updated"));
        }
    }

    // ============ RESEÑAS ============

    @Operation(
            summary = "Obtener reseñas de producto",
            description = "Retorna todas las reseñas de un producto específico"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reseñas obtenida"),
            @ApiResponse(responseCode = "204", description = "No hay reseñas para este producto"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{productCode}/resenas")
    public ResponseEntity<?> getResenasByProduct(
            @Parameter(description = "Código del producto", example = "PROD-001", required = true)
            @PathVariable String productCode) {
        Product existing = productService.findByCodigo(productCode);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Product doesn't exists"));
        }

        List<Resena> resenas = resenaService.getResenasByProductCode(productCode);
        return resenas.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(resenas);
    }

    @Operation(
            summary = "Agregar reseña",
            description = "Crea una nueva reseña para un producto"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reseña creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de reseña inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/{productCode}/resenas")
    public ResponseEntity<?> addResena(
            @Parameter(description = "Código del producto", required = true)
            @PathVariable String productCode,
            @Parameter(description = "Datos de la reseña", required = true)
            @RequestBody ResenaRequest request
    ) {
        try {
            Resena created = resenaService.addResena(productCode, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al crear reseña"));
        }
    }

    @Operation(
            summary = "Actualizar reseña",
            description = "Actualiza una reseña existente"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reseña actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de reseña inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{productCode}/resenas/{resenaId}")
    public ResponseEntity<?> updateResena(
            @Parameter(description = "Código del producto", required = true)
            @PathVariable String productCode,
            @Parameter(description = "ID de la reseña", example = "1", required = true)
            @PathVariable Long resenaId,
            @Parameter(description = "Datos actualizados de la reseña", required = true)
            @RequestBody ResenaRequest request
    ) {
        try {
            Resena updated = resenaService.updateResena(productCode, resenaId, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al actualizar reseña"));
        }
    }

    @Operation(
            summary = "Eliminar reseña",
            description = "Elimina una reseña específica"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reseña eliminada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Reseña no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{productCode}/resenas/{resenaId}")
    public ResponseEntity<?> deleteResena(
            @Parameter(description = "Código del producto", required = true)
            @PathVariable String productCode,
            @Parameter(description = "ID de la reseña", example = "1", required = true)
            @PathVariable Long resenaId
    ) {
        try {
            resenaService.deleteResena(productCode, resenaId);
            return ResponseEntity.ok(new MessageResponse("Reseña eliminada correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al eliminar reseña"));
        }
    }
}