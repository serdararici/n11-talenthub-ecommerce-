import { createContext, useContext, useState } from 'react';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const [cart, setCart] = useState(() => {
    try { return JSON.parse(localStorage.getItem('n11_cart')) || { items: [] }; } catch { return { items: [] }; }
  });
  const [wishlist, setWishlist] = useState(() => {
    try { return JSON.parse(localStorage.getItem('n11_wishlist')) || []; } catch { return []; }
  });

  const persist = (newCart) => {
    setCart(newCart);
    localStorage.setItem('n11_cart', JSON.stringify(newCart));
  };

  const addToCart = (product, qty = 1) => {
    setCart(prev => {
      const items = [...prev.items];
      const idx = items.findIndex(i => i.productId === product.id);
      if (idx >= 0) items[idx] = { ...items[idx], quantity: items[idx].quantity + qty };
      else items.push({ productId: product.id, quantity: qty, product });
      const next = { items };
      localStorage.setItem('n11_cart', JSON.stringify(next));
      return next;
    });
  };

  const updateCart = (productId, quantity) => {
    setCart(prev => {
      const items = prev.items
        .map(i => i.productId === productId ? { ...i, quantity } : i)
        .filter(i => i.quantity > 0);
      const next = { items };
      localStorage.setItem('n11_cart', JSON.stringify(next));
      return next;
    });
  };

  const removeFromCart = (productId) => {
    setCart(prev => {
      const items = prev.items.filter(i => i.productId !== productId);
      const next = { items };
      localStorage.setItem('n11_cart', JSON.stringify(next));
      return next;
    });
  };

  const clearCart = () => {
    const empty = { items: [] };
    setCart(empty);
    localStorage.setItem('n11_cart', JSON.stringify(empty));
  };

  const toggleWishlist = (productId) => {
    setWishlist(prev => {
      const next = prev.includes(productId)
        ? prev.filter(id => id !== productId)
        : [...prev, productId];
      localStorage.setItem('n11_wishlist', JSON.stringify(next));
      return next;
    });
  };

  const cartCount = cart.items.reduce((s, i) => s + i.quantity, 0);
  const cartTotal = cart.items.reduce((s, i) => s + (i.product?.price || 0) * i.quantity, 0);

  return (
    <CartContext.Provider value={{
      cart, addToCart, updateCart, removeFromCart, clearCart,
      cartCount, cartTotal,
      wishlist, toggleWishlist,
    }}>
      {children}
    </CartContext.Provider>
  );
}

export const useCart = () => useContext(CartContext);
