package cl.levelup.productservice.controller;

import cl.levelup.productservice.controller.response.MessageResponse;
import cl.levelup.productservice.model.Product;
import cl.levelup.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/health")
    public ResponseEntity<MessageResponse> health() {
        return ResponseEntity.ok(new MessageResponse("Ok"));
    }

    @GetMapping("/")
    public ResponseEntity<?> getAllProducts() {

        List<Product> products = productService.findAll();
        return products.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(products);
    }

    @GetMapping("/{productCode}")
    public ResponseEntity<?> getProduct(@PathVariable String productCode) {
        Product existing = productService.findByCodigo(productCode);
        if (existing != null) {
            return ResponseEntity.ok(existing);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Product doesn't exists"));
        }
    }

    @PostMapping("/")
    public ResponseEntity<MessageResponse> addProduct(@RequestBody Product productRequest) {
        Product existing = productService.findByCodigo(productRequest.getCodigo());
        if (productRequest.getId() <= 0 && existing == null) {
            String imgUrl = "http://levelup.ddns.net:8000/storage/v1/object/public/levelup_files/products/" + productRequest.getCategoria() + "/" + productRequest.getImagenUrl();
            productRequest.setImagenUrl(imgUrl);
            productService.add(productRequest);
            return ResponseEntity.ok(new MessageResponse("Product added successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("Product already exists"));
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<MessageResponse> deleteProduct(@PathVariable int productId) {
        if (productId <= 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("Product id not found"));
        } else {
            productService.delete(productId);
            return ResponseEntity.ok(new MessageResponse("Product deleted successfully"));
        }
    }

    // Añadir update

    @PutMapping("/{productCode}")
    public ResponseEntity<MessageResponse> updateProduct(
            @PathVariable String productCode,
            @RequestBody Product productRequest) {

        Product existing = productService.findByCodigo(productCode);

        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Product doesn't exist"));
        }

        if (productRequest.getImagenUrl() != null) {
            String imgUrl = "http://levelup.ddns.net:8000/storage/v1/object/public/levelup_files/products/" + productRequest.getCategoria() + "/" + productRequest.getImagenUrl();
            productRequest.setImagenUrl(imgUrl);
        }

        productService.update(existing, productRequest);

        return ResponseEntity.ok(new MessageResponse("Product updated successfully"));
    }

}
