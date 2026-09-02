export function getCart() {
    return JSON.parse(localStorage.getItem('cantina_cart')) || [];
}

export function saveCart(cart) {
    localStorage.setItem('cantina_cart', JSON.stringify(cart));
    window.app.updateCartUI();
}

export function addToCart(product, quantity = 1) {
    const cart = getCart();
    const existing = cart.find(item => item.id === product.id);
    if (existing) existing.quantity += quantity;
    else cart.push({ ...product, quantity });
    saveCart(cart);
    window.app.showToast('Adicionado ao carrinho!', 'success');
}

export function removeFromCart(productId) {
    const cart = getCart().filter(item => item.id !== productId);
    saveCart(cart);
}

export function updateQuantity(productId, delta) {
    const cart = getCart();
    const item = cart.find(i => i.id === productId);
    if (item) {
        item.quantity = Math.max(1, item.quantity + delta);
        saveCart(cart);
    }
}

export function clearCart() {
    saveCart([]);
}

export function getCartTotal() {
    return getCart().reduce((sum, item) => sum + (item.price * item.quantity), 0);
}