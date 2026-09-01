import { getProducts, renderProductCard } from '../products.js';
import { CATEGORIES } from '../config.js';
import { formatPrice } from '../utils.js';

export function renderMenu(selectedCategory = 'Todos') {
    const products = getProducts();
    const filteredProducts = selectedCategory === 'Todos'
        ? products
        : products.filter(p => p.category === selectedCategory);

    return `
        <div class="container menu-page">
            <h1 class="section-title">Nosso Menu</h1>

            <!-- ===== PRATOS FIXOS DA CANTINA ===== -->
            <div class="cantina-top-grid">
                
                <!-- Card Tradicional -->
                <div class="cantina-top-card" style="background:#F9F6F0;" onclick="window.app.openProduct(101)">
                    <img src="/images/tradicional.jpg" alt="Tradicional" class="cantina-top-img" onerror="this.onerror=null; this.src='/images/tradicional.jpg';">
                    <div class="cantina-top-body">
                        <h3 class="cantina-top-title" style="color:#4A2C10;">Tradicional</h3>
                        <p class="cantina-top-desc" style="color:#4A2C10;">Filé de Frango e Calabresa</p>
                        <p class="cantina-top-price"><span>R$</span>25,00</p>
                    </div>
                </div>

                <!-- Card Do Dia -->
                <div class="cantina-top-card" style="background:#2C1B10;" onclick="window.app.openProduct(102)">
                    <img src="/images/dodia.jpg" alt="Do Dia" class="cantina-top-img" onerror="this.onerror=null; this.src='/images/tradicional.jpg';">
                    <div class="cantina-top-body">
                        <h3 class="cantina-top-title" style="color:#FFFFFF;">Do Dia</h3>
                        <p class="cantina-top-desc" style="color:#FFFFFF;">Filé de Frango ou Labareda</p>
                        <p class="cantina-top-price"><span>R$</span>28,00</p>
                    </div>
                </div>

            </div>

            <!-- ===== PRATOS DO DIA (Segunda a Sábado) ===== -->
            <h2 class="cantina-section-title">Pratos do Dia <i class="fa-solid fa-utensils" style="color:#DA8A18; font-size: 20px;"></i></h2>
            
            <div class="cantina-day-grid">

                <!-- Segunda -->
                <div class="cantina-day-card" onclick="window.app.openProduct(103)">
                    <div class="cantina-day-header" style="background:#E65100;">Segunda</div>
                    <img src="/images/segunda.jpg" alt="Picadinho" class="cantina-day-img" onerror="this.onerror=null; this.src='/images/tradicional.jpg';">
                    <div class="cantina-day-body">
                        <h3 class="cantina-day-title">Picadinho de Carne com Legumes</h3>
                    </div>
                </div>

                <!-- Terça -->
                <div class="cantina-day-card" onclick="window.app.openProduct(104)">
                    <div class="cantina-day-header" style="background:#F57F17;">Terça-feira</div>
                    <img src="/images/terca.jpg" alt="Strogonoff" class="cantina-day-img" onerror="this.onerror=null; this.src='/images/tradicional.jpg';">
                    <div class="cantina-day-body">
                        <h3 class="cantina-day-title">Strogonoff com Batata Palha</h3>
                    </div>
                </div>

                <!-- Quarta -->
                <div class="cantina-day-card" onclick="window.app.openProduct(105)">
                    <div class="cantina-day-header" style="background:#388E3C;">Quarta</div>
                    <img src="/images/quarta.jpg" alt="Feijoada" class="cantina-day-img" onerror="this.onerror=null; this.src='/images/tradicional.jpg';">
                    <div class="cantina-day-body">
                        <h3 class="cantina-day-title">Feijoada</h3>
                    </div>
                </div>

                <!-- Quinta -->
                <div class="cantina-day-card" onclick="window.app.openProduct(106)">
                    <div class="cantina-day-header" style="background:#E64A19;">Quinta-feira</div>
                    <img src="/images/quinta.jpg" alt="Massa" class="cantina-day-img" onerror="this.onerror=null; this.src='/images/tradicional.jpg';">
                    <div class="cantina-day-body">
                        <h3 class="cantina-day-title">Massa com Almôndegas</h3>
                    </div>
                </div>

                <!-- Sexta -->
                <div class="cantina-day-card" onclick="window.app.openProduct(107)">
                    <div class="cantina-day-header" style="background:#1976D2;">Sexta-feira</div>
                    <img src="/images/sexta.jpg" alt="Peixe" class="cantina-day-img" onerror="this.onerror=null; this.src='/images/tradicional.jpg';">
                    <div class="cantina-day-body">
                        <h3 class="cantina-day-title">Peixe com Purê</h3>
                    </div>
                </div>

                <!-- Sábado -->
                <div class="cantina-day-card" onclick="window.app.openProduct(108)">
                    <div class="cantina-day-header" style="background:#7B1FA2;">Sábado</div>
                    <img src="/images/sabado.jpg" alt="Feijoada Sábado" class="cantina-day-img" onerror="this.onerror=null; this.src='/images/tradicional.jpg';">
                    <div class="cantina-day-body">
                        <h3 class="cantina-day-title">Feijoada</h3>
                    </div>
                </div>

            </div>

            <!-- ===== FILTRO E PRODUTOS DO SISTEMA ===== -->
            <h2 class="section-title">Nosso Cardápio</h2>
            
            <div class="category-filter">
                <button class="filter-button ${selectedCategory === 'Todos' ? 'active' : ''}" onclick="window.app.filterCategory('Todos')">
                    Todos
                </button>
                ${CATEGORIES.map(category => `
                    <button class="filter-button ${selectedCategory === category.name ? 'active' : ''}" onclick="window.app.filterCategory('${category.name}')">
                        ${category.icon} ${category.label}
                    </button>
                `).join('')}
            </div>

            <div class="products">
                ${filteredProducts.length ? filteredProducts.map(renderProductCard).join('') : '<p>Nenhum produto encontrado nesta categoria.</p>'}
            </div>
        </div>
    `;
}

export function renderProduct(product, productQuantity = 1) {
    if (!product) return '<div class="container"><p>Produto não encontrado.</p></div>';

    const total = product.price * productQuantity;
    const allProducts = getProducts();
    const relatedProducts = allProducts.filter(p => p.id !== product.id).slice(0, 3);

    return `
        <div class="product-page">
            <button class="back-button" onclick="window.app.navigate('menu')">
                <i class="fa-solid fa-arrow-left"></i> Voltar ao Cardápio
            </button>
            
            <div class="product-detail-card">
                <div class="product-detail-media">
                    <span class="product-detail-tag">${product.category || 'Prato Especial'}</span>
                    <img class="product-detail-image" src="${product.image}" alt="${product.name}" onerror="this.onerror=null; this.src='/images/tradicional.jpg';">
                </div>
                
                <div class="product-detail-info">
                    <h1 class="product-detail-title">${product.name}</h1>
                    <p class="product-detail-desc">${product.description}</p>
                    
                    <div class="product-features-grid">
                        <div class="product-feature-item">
                            <div class="product-feature-icon">🍽️</div>
                            <span>Comida Caseira</span>
                        </div>
                        <div class="product-feature-item">
                            <div class="product-feature-icon">🌿</div>
                            <span>Ingredientes Frescos</span>
                        </div>
                        <div class="product-feature-item">
                            <div class="product-feature-icon">❤️</div>
                            <span>Feito na Hora</span>
                        </div>
                    </div>

                    <div class="product-price-section">
                        <span class="product-price-label">Preço unitário:</span>
                        <span class="product-price-val">R$ ${formatPrice(product.price)}</span>
                    </div>
                    
                    <div class="quantity-control-wrapper">
                        <span class="quantity-control-label">Quantidade:</span>
                        <div class="quantity-btn-group">
                            <button class="quantity-btn" onclick="window.app.updateProductQuantity(-1)">-</button>
                            <span class="quantity-val">${productQuantity}</span>
                            <button class="quantity-btn" onclick="window.app.updateProductQuantity(1)">+</button>
                        </div>
                    </div>
                    
                    <button class="product-add-cart-btn" onclick="window.app.addToCartAndReturn()">
                        <i class="fa-solid fa-cart-shopping"></i> Adicionar ao Carrinho • R$ ${formatPrice(total)}
                    </button>
                </div>
            </div>

            ${relatedProducts.length ? `
                <div class="related-products-container">
                    <h2 class="section-title">Outras Opções da Cantina</h2>
                    <div class="products">
                        ${relatedProducts.map(renderProductCard).join('')}
                    </div>
                </div>
            ` : ''}
        </div>
    `;
}