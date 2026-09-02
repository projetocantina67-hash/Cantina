import { renderHome } from './pages/home.js';
import { renderMenu, renderProduct } from './pages/menu.js';
import { renderCheckout } from './pages/checkout.js';
import { renderLogin, handleLoginSubmit } from './pages/login.js';
import { renderRegister, handleRegisterSubmit } from './pages/register.js';
import { renderAdmin, updateOrderStatus, togglePaymentStatus, cancelOrder, handleResetStock, switchAdminTab,
    abrirModalProduto, fecharModalAdmin, salvarProdutoAdmin, desativarProdutoAdmin, ativarProdutoAdmin,
    abrirModalFuncionario, cadastrarFuncionarioAdmin, desativarUsuarioAdmin } from './pages/admin.js';
import { renderOrdersPage, submitOrder, openOrderModal, closeOrderModal, showPixSection, copyPixKey, confirmPixPayment, cancelUserOrder } from './orders.js';
import { getUser, logout } from './auth.js';
import { loadProducts, setCategory, getCategory, getProducts } from './products.js';
import { getCart, addToCart, removeFromCart, updateQuantity } from './cart.js';
import { showToast, formatPrice } from './utils.js';

class App {
    constructor() {
        this.currentPage = 'home';
        this.selectedProduct = null;
        this.productQuantity = 1;
        this.init();
    }

    async init() {
        await loadProducts(); // Carrega produtos do backend na inicialização
        this.updateHeaderUI();
        this.updateCartUI();
        this.navigate(this.currentPage);

        // Listener para popstate (voltar do navegador)
        window.addEventListener('popstate', (e) => {
            if (e.state && e.state.page) this.navigate(e.state.page, false);
        });
    }

    async render() {
        const appDiv = document.getElementById('app');
        let html = '';

        switch(this.currentPage) {
            case 'home': html = renderHome(); break;
            case 'menu': html = renderMenu(getCategory()); break;
            case 'product': html = renderProduct(this.selectedProduct, this.productQuantity); break;
            case 'checkout': html = renderCheckout(); break;
            case 'login': html = renderLogin(); break;
            case 'register': html = renderRegister(); break;
            case 'admin': html = await renderAdmin(); break;
            case 'orders': html = await renderOrdersPage(); break;
            default: html = renderHome();
        }

        appDiv.innerHTML = html;
        this.updateNavigationClasses();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    navigate(page, pushState = true) {
        this.currentPage = page;
        if (pushState) history.pushState({ page }, '', `/#${page}`);
        this.render();
    }

    updateNavigationClasses() {
        document.querySelectorAll('.nav button').forEach(btn => btn.classList.remove('active'));
        if (this.currentPage === 'home') document.getElementById('nav-home')?.classList.add('active');
        if (this.currentPage === 'menu' || this.currentPage === 'product') document.getElementById('nav-menu')?.classList.add('active');
        if (this.currentPage === 'orders') document.getElementById('nav-orders')?.classList.add('active');
    }

    // --- Header & Auth UI ---
    updateHeaderUI() {
        const userArea = document.getElementById('user-area');
        const user = getUser();

        if (user) {
            let adminBtn = '';
            if (user.perfil === 'Funcionario da cantina') {
                adminBtn = `<button onclick="window.app.navigate('admin')" style="background:var(--primary); color:white; border:none; padding:8px 15px; border-radius:20px; font-weight:bold; font-size:14px; cursor:pointer; margin-right:10px;">Painel</button>`;
            }

            userArea.innerHTML = `
                ${adminBtn}
                <div style="display:flex; align-items:center; gap:10px; cursor:pointer;" onclick="window.app.handleLogout()">
                    <div style="width:40px; height:40px; background:#e5e7eb; border-radius:50%; display:flex; align-items:center; justify-content:center; font-weight:bold; color:#4b5563;">
                        ${user.nome.charAt(0).toUpperCase()}
                    </div>
                    <div class="profile-name-block">
                        <span style="font-size:14px; font-weight:600; display:block;">${user.nome}</span>
                        <span style="font-size:12px; color:#6b7280;">Sair</span>
                    </div>
                </div>
            `;
        } else {
            userArea.innerHTML = `<button onclick="window.app.navigate('login')" class="primary-button" style="padding:8px 20px;">Entrar</button>`;
        }
    }

    handleLogout() {
        if(confirm('Deseja realmente sair?')) {
            logout();
            this.updateHeaderUI();
            this.navigate('home');
        }
    }

    handleLogin(event) {
        handleLoginSubmit(event).then(() => {
            this.updateHeaderUI();
        });
    }

    handleRegister(event) {
        handleRegisterSubmit(event);
    }

    // --- Menu & Products ---
    filterCategory(cat) {
        setCategory(cat);
        this.navigate('menu');
    }

    openProduct(id) {
        this.selectedProduct = getProducts().find(p => p.id === id);
        this.productQuantity = 1;
        this.navigate('product');
    }

    updateProductQuantity(delta) {
        this.productQuantity = Math.max(1, this.productQuantity + delta);
        this.render(); // Re-render product page
    }

    // --- Cart UI ---
    toggleCart() {
        const cartSidebar = document.getElementById('cart-sidebar');
        const cartOverlay = document.getElementById('cart-overlay');
        cartSidebar.classList.toggle('open');
        cartOverlay.classList.toggle('hidden');
    }

    updateCartUI() {
        const cart = getCart();
        const count = cart.reduce((sum, item) => sum + item.quantity, 0);

        const countBadge = document.getElementById('cart-count');
        document.getElementById('cart-title-count').textContent = count;

        if (count > 0) {
            countBadge.textContent = count;
            countBadge.classList.remove('hidden');
        } else {
            countBadge.classList.add('hidden');
        }

        const cartContent = document.getElementById('cart-content');
        if (cart.length === 0) {
            cartContent.innerHTML = `<div class="empty-cart"><div class="empty-cart-icon">🛒</div><p>Seu carrinho está vazio</p><button class="primary-button" onclick="window.app.toggleCart(); window.app.navigate('menu')">Explorar Menu</button></div>`;
            return;
        }

        const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

        let html = '<div class="cart-items">';
        cart.forEach(item => {
            const imgSrc = item.image || '/images/tradicional.jpg';
            html += `
                <div class="cart-item">
                    <img src="${imgSrc}" alt="${item.name}" onerror="this.onerror=null; this.src='/images/tradicional.jpg';">
                    <div class="cart-item-details">
                        <h4>${item.name}</h4>
                        <span class="price">R$ ${formatPrice(item.price * item.quantity)}</span>
                        <div class="quantity-controls">
                            <button onclick="window.app.updateCartQuantity(${item.id}, -1)">-</button>
                            <span>${item.quantity}</span>
                            <button onclick="window.app.updateCartQuantity(${item.id}, 1)">+</button>
                            <button onclick="window.app.removeFromCart(${item.id})" style="margin-left:10px; color:#ef4444; border:none; background:transparent; cursor:pointer;" aria-label="Remover"><i class="fa-solid fa-trash"></i></button>
                        </div>
                    </div>
                </div>
            `;
        });
        html += `</div>
            <div class="cart-footer">
                <div class="cart-total"><span>Total</span><span>R$ ${formatPrice(total)}</span></div>
                <button class="primary-button" onclick="window.app.toggleCart(); window.app.navigate('checkout')">Finalizar Pedido</button>
            </div>
        `;
        cartContent.innerHTML = html;
    }

    addToCartAndReturn() {
        addToCart(this.selectedProduct, this.productQuantity);
        this.navigate('menu');
    }

    addDirectlyToCart(id) {
        const product = getProducts().find(p => p.id === id);
        if (product) {
            addToCart(product, 1);
        } else {
            showToast('Produto indisponível.', 'error');
        }
    }

    updateCartQuantity(id, delta) { updateQuantity(id, delta); }
    removeFromCart(id) { removeFromCart(id); }

    // --- Order Management Modal Exports ---
    openOrderModal(id) { openOrderModal(id); }
    closeOrderModal() { closeOrderModal(); }
    showPixSection(id) { showPixSection(id); }
    copyPixKey() { copyPixKey(); }
    confirmPixPayment(id) { confirmPixPayment(id); }
    cancelUserOrder(id) { cancelUserOrder(id); }

    // --- Utils Export ---
    showToast(msg, type) { showToast(msg, type); }
    submitOrder() { submitOrder(); }
    updateOrderStatus(id, status) { updateOrderStatus(id, status); }
    togglePaymentStatus(id, pago) { togglePaymentStatus(id, pago); }
    cancelOrder(id) { cancelOrder(id); }
    handleResetStock() { handleResetStock(); }
    switchAdminTab(tab) { switchAdminTab(tab); }

    // --- Utils Export ---
    showToast(msg, type) { showToast(msg, type); }
    submitOrder() { submitOrder(); }
    updateOrderStatus(id, status) { updateOrderStatus(id, status); }
    togglePaymentStatus(id, pago) { togglePaymentStatus(id, pago); }
    cancelOrder(id) { cancelOrder(id); }
    handleResetStock() { handleResetStock(); }
    switchAdminTab(tab) { switchAdminTab(tab); }

    // --- Gestão de Produtos (Painel) ---
    abrirModalProduto(id) { abrirModalProduto(id); }
    fecharModalAdmin() { fecharModalAdmin(); }
    salvarProdutoAdmin(event) { salvarProdutoAdmin(event); }
    desativarProdutoAdmin(id) { desativarProdutoAdmin(id); }
    ativarProdutoAdmin(id) { ativarProdutoAdmin(id); }

    // --- Gestão de Funcionários (Painel) ---
    abrirModalFuncionario() { abrirModalFuncionario(); }
    cadastrarFuncionarioAdmin(event) { cadastrarFuncionarioAdmin(event); }
    desativarUsuarioAdmin(id) { desativarUsuarioAdmin(id); }
}

// Global hook
window.app = new App();