package cl.levelup.productservice.controller;

import cl.levelup.productservice.model.dto.MessageResponse;
import cl.levelup.productservice.model.dto.ResenaRequest;
import cl.levelup.productservice.model.Product;
import cl.levelup.productservice.model.Resena;
import cl.levelup.productservice.service.ProductService;
import cl.levelup.productservice.service.ResenaService;
import cl.levelup.productservice.storage.SupabaseStorageService;
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
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @Autowired
    private ResenaService resenaService;

    @GetMapping("/health")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(new MessageResponse("Ok"));
    }

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

    @GetMapping("/{productCode}")
    public ResponseEntity<?> getProduct(@PathVariable String productCode) {

        Product existing = productService.findByCodigo(productCode);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Product doesn't exists"));
        }

        // 🔥 Traemos reseñas enriquecidas con datos del usuario
        List<Resena> resenasEnriquecidas =
                resenaService.getResenasEnriquecidas(productCode);

        existing.setResenas(resenasEnriquecidas);

        return ResponseEntity.ok(existing);
    }

    // ================== SUBIR IMAGEN DE PRODUCTO ==================

    @PostMapping(
            path = "/{productCode}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadProductImage(
            @PathVariable("productCode") String productCode,
            @RequestPart("file") MultipartFile file,
            @RequestParam("categoria") String categoria,
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

    // ==============================================================

    @PostMapping("/")
    public ResponseEntity<MessageResponse> addProduct(@RequestBody Product productRequest) {
        System.out.println("Producto recibido: " + productRequest);
        try {
            Product existing = productService.findByCodigo(productRequest.getCodigo());
            if (productRequest.getId() <= 0 && existing == null) {

                String baseUrl = "http://levelup.ddns.net:8000/storage/v1/object/public/levelup_files/products/";
                String categoryPath = productRequest.getCategoria() + "/";

                String imagen = productRequest.getImagenUrl();

                if (imagen != null && !imagen.isEmpty()) {
                    // Si ya viene como URL completa (Supabase u otra), la dejamos tal cual
                    if (imagen.startsWith("http://") || imagen.startsWith("https://")) {
                        // no cambiamos nada
                    } else if (!imagen.startsWith(baseUrl + categoryPath)) {
                        // Si es solo el nombre de archivo, armamos la URL
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

    @DeleteMapping("/{productId}")
    public ResponseEntity<MessageResponse> deleteProduct(@PathVariable String productId) {
        if (productId == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("Product id not found"));
        }

        try {
            // productId en tu caso es el codigo del producto
            Product existing = productService.findByCodigo(productId);

            if (existing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Product doesn't exist"));
            }

            // 1) Si tiene imagen, la eliminamos de Supabase Storage
            String imagenUrl = existing.getImagenUrl();
            if (imagenUrl != null && !imagenUrl.isBlank()) {
                supabaseStorageService.deleteByPublicUrl(imagenUrl);
            }

            // 2) Eliminamos el producto de la BD
            productService.delete(productId);

            return ResponseEntity.ok(new MessageResponse("Product deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error deleting product: " + e.getMessage()));
        }
    }

    @PutMapping("/{productCode}")
    public ResponseEntity<MessageResponse> updateProduct(
            @PathVariable String productCode,
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
                // ya es URL completa
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

    // ================== RESEÑAS ==================

    // Obtener todas las reseñas de un producto
    @GetMapping("/{productCode}/resenas")
    public ResponseEntity<?> getResenasByProduct(@PathVariable String productCode) {
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

    // Crear nueva reseña
    @PostMapping("/{productCode}/resenas")
    public ResponseEntity<?> addResena(
            @PathVariable String productCode,
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

    // Actualizar reseña
    @PutMapping("/{productCode}/resenas/{resenaId}")
    public ResponseEntity<?> updateResena(
            @PathVariable String productCode,
            @PathVariable Long resenaId,
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

    // Eliminar reseña
    @DeleteMapping("/{productCode}/resenas/{resenaId}")
    public ResponseEntity<?> deleteResena(
            @PathVariable String productCode,
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
