package com.jtspringproject.JtSpringProject.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jtspringproject.JtSpringProject.models.Cart;
import com.jtspringproject.JtSpringProject.models.CartProduct;
import com.jtspringproject.JtSpringProject.models.Product;
import com.jtspringproject.JtSpringProject.models.User;
import com.jtspringproject.JtSpringProject.services.cartProductService;
import com.jtspringproject.JtSpringProject.services.cartService;
import com.jtspringproject.JtSpringProject.services.productService;
import com.jtspringproject.JtSpringProject.services.userService;

@RestController
@RequestMapping("/api")
public class ApiUserController {

    private final userService userService;
    private final productService productService;
    private final cartService cartService;
    private final cartProductService cartProductService;

    public ApiUserController(userService userService, productService productService,
                             cartService cartService, cartProductService cartProductService) {
        this.userService = userService;
        this.productService = productService;
        this.cartService = cartService;
        this.cartProductService = cartProductService;
    }

    // --- Products ---

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(@PathVariable int id) {
        Product p = productService.getProduct(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    // --- Registration ---

    @PostMapping("/users/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userService.checkUserExists(user.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", user.getUsername() + " is already taken"));
        }
        user.setRole("ROLE_NORMAL");
        userService.addUser(user);
        return ResponseEntity.ok(Map.of("message", "Registration successful"));
    }

    // --- Profile ---

    @GetMapping("/users/profile")
    public ResponseEntity<?> getProfile() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "address", user.getAddress() == null ? "" : user.getAddress()
        ));
    }

    @PutMapping("/users/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));

        User updated = userService.updateUserProfile(
                user.getId(),
                body.get("username"),
                body.get("email"),
                body.get("password"),
                body.get("address")
        );
        if (updated == null) return ResponseEntity.internalServerError().body(Map.of("message", "Update failed"));
        return ResponseEntity.ok(Map.of("message", "Profile updated"));
    }

    // --- Cart ---

    @GetMapping("/cart")
    public ResponseEntity<?> getCart() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));

        Cart cart = cartService.getCartByCustomerId(user.getId());
        if (cart == null) return ResponseEntity.ok(Collections.emptyList());
        return ResponseEntity.ok(cartProductService.getCartProductsByCartId(cart.getId()));
    }

    @Transactional
    @PostMapping("/cart/add")
    public ResponseEntity<?> addToCart(@RequestParam int productId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));

        Product product = productService.getProduct(productId);
        if (product == null) return ResponseEntity.badRequest().body(Map.of("message", "Product not found"));

        Cart cart = getOrCreateCart(user);
        CartProduct existing = cartProductService.getCartProduct(cart.getId(), product.getId());
        if (existing == null) {
            cartProductService.addCartProduct(new CartProduct(cart, product));
        }
        return ResponseEntity.ok(Map.of("message", "Added to cart"));
    }

    @DeleteMapping("/cart/remove")
    public ResponseEntity<?> removeFromCart(@RequestParam int productId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));

        Cart cart = cartService.getCartByCustomerId(user.getId());
        if (cart != null) {
            CartProduct cp = cartProductService.getCartProduct(cart.getId(), productId);
            if (cp != null) cartProductService.deleteCartProduct(cp);
        }
        return ResponseEntity.ok(Map.of("message", "Removed from cart"));
    }

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username);
    }

    private Cart getOrCreateCart(User user) {
        Cart cart = cartService.getCartByCustomerId(user.getId());
        if (cart == null) {
            cart = new Cart();
            cart.setCustomer(user);
            cartService.addCart(cart);
        }
        return cart;
    }
}
