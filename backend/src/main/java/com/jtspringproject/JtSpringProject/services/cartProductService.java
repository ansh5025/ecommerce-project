package com.jtspringproject.JtSpringProject.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jtspringproject.JtSpringProject.dao.cartProductDao;
import com.jtspringproject.JtSpringProject.models.CartProduct;

@Service
public class cartProductService {
    private final cartProductDao cartProductDao;

    public cartProductService(cartProductDao cartProductDao) {
        this.cartProductDao = cartProductDao;
    }

    public CartProduct addCartProduct(CartProduct cartProduct) {
        return this.cartProductDao.addCartProduct(cartProduct);
    }

    public CartProduct getCartProduct(int cartId, int productId) {
        return this.cartProductDao.getCartProduct(cartId, productId);
    }

    public List<CartProduct> getCartProductsByCartId(int cartId) {
        return this.cartProductDao.getCartProductsByCartId(cartId);
    }

    public void deleteCartProduct(CartProduct cartProduct) {
        this.cartProductDao.deleteCartProduct(cartProduct);
    }
}
