package my.foodon.pizzamania.cart;

import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.models.Pizza;

public class CartManager {
    private static CartManager instance;
    private final List<CartItem> cartItems = new ArrayList<>();

    // Removes ONE line item (this pizza + size). If you want to only decrement, use decrement below.
    public void removeFromCart(Pizza pizza, Pizza.PizzaSize size) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.pizza.getName().equals(pizza.getName()) && item.size == size) {
                cartItems.remove(i);
                return;
            }
        }
    }

    // Optional helpers
    public void decrementQuantity(Pizza pizza, Pizza.PizzaSize size) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.pizza.getName().equals(pizza.getName()) && item.size == size) {
                if (item.quantity > 1) {
                    item.quantity--;
                } else {
                    cartItems.remove(i);
                }
                return;
            }
        }
    }

    public void incrementQuantity(Pizza pizza, Pizza.PizzaSize size) {
        for (CartItem item : cartItems) {
            if (item.pizza.getName().equals(pizza.getName()) && item.size == size) {
                item.quantity++;
                return;
            }
        }
        cartItems.add(new CartItem(pizza, size)); // if not found, add new line
    }


    // Cart item inner class
    public static class CartItem {
        public final Pizza pizza;
        public final Pizza.PizzaSize size;
        public int quantity;

        public CartItem(Pizza pizza, Pizza.PizzaSize size) {
            this.pizza = pizza;
            this.size = size;
            this.quantity = 1;
        }
    }

    private CartManager() { }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Pizza pizza, Pizza.PizzaSize size) {
        // Check if already in cart, increment
        for (CartItem item : cartItems) {
            if (item.pizza.getName().equals(pizza.getName()) && item.size == size) {
                item.quantity++;
                return;
            }
        }
        cartItems.add(new CartItem(pizza, size));
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public void clearCart() {
        cartItems.clear();
    }
}
