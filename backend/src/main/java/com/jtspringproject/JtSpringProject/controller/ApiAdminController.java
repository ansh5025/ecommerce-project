package com.jtspringproject.JtSpringProject.controller;

import com.jtspringproject.JtSpringProject.models.Category;
import com.jtspringproject.JtSpringProject.models.Product;
import com.jtspringproject.JtSpringProject.models.User;
import com.jtspringproject.JtSpringProject.services.categoryService;
import com.jtspringproject.JtSpringProject.services.productService;
import com.jtspringproject.JtSpringProject.services.userService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class ApiAdminController {

    private final userService userService;
    private final categoryService categoryService;
    private final productService productService;

    public ApiAdminController(userService userService, categoryService categoryService, productService productService) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.productService = productService;
    }

    // --- Customers ---

    @GetMapping("/customers")
    public ResponseEntity<List<User>> getCustomers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    // --- Categories ---

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<?> addCategory(@RequestBody Map<String, String> body) {
        categoryService.addCategory(body.get("name"));
        return ResponseEntity.ok(Map.of("message", "Category added"));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable int id, @RequestBody Map<String, String> body) {
        categoryService.updateCategory(id, body.get("name"));
        return ResponseEntity.ok(Map.of("message", "Category updated"));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable int id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(Map.of("message", "Category deleted"));
    }

    // --- Products ---

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }

    @PostMapping("/products")
    public ResponseEntity<?> addProduct(@RequestBody Map<String, Object> body) {
        Product product = buildProduct(body);
        productService.addProduct(product);
        return ResponseEntity.ok(Map.of("message", "Product added"));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable int id, @RequestBody Map<String, Object> body) {
        Product product = buildProduct(body);
        productService.updateProduct(id, product);
        return ResponseEntity.ok(Map.of("message", "Product updated"));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Product deleted"));
    }

    private Product buildProduct(Map<String, Object> body) {
        int categoryId = Integer.parseInt(body.get("categoryId").toString());
        Category category = categoryService.getCategory(categoryId);
        Product p = new Product();
        p.setName((String) body.get("name"));
        p.setCategory(category);
        p.setDescription((String) body.get("description"));
        p.setPrice(Integer.parseInt(body.get("price").toString()));
        p.setWeight(Integer.parseInt(body.get("weight").toString()));
        p.setQuantity(Integer.parseInt(body.get("quantity").toString()));
        p.setImage((String) body.get("image"));
        return p;
    }
}
