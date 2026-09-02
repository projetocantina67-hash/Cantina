import { renderCategories, getProducts, renderProductCard } from '../products.js';

export function renderHome() {
    const products = getProducts().slice(0, 4); // Pega os 4 primeiros produtos como destaque

    const pratosDoDia = [
        { nome: 'Feijoada Especial', preco: '35,00' },
        { nome: 'Picadinho de carne com legumes', preco: '28,00' },
        { nome: 'Strogonoff de Frango', preco: '28,00' },
        { nome: 'Feijoada', preco: '30,00' },
        { nome: 'Massa ao Sugo com Polpetone', preco: '26,00' },
        { nome: 'Peixe Grelhado com Purê', preco: '32,00' },
        { nome: 'Feijoada Completa', preco: '35,00' }
    ];
    const diaAtual = new Date().getDay();
    const pratoAtual = pratosDoDia[diaAtual];

    return `
        <section class="hero">
            <div class="hero-container">
                <div>
                    <h1>Monte sua refeição perfeita.</h1>
                    <p>Experimente os melhores sabores da cantina em poucos minutos. Comida caseira fresca e ingredientes selecionados.</p>
                    <button class="primary-button" onclick="window.app.navigate('menu')">Pedir Agora</button>
                </div>
                <div class="hero-image" style="position: relative;">
                    <img src="/images/tradicional.jpg" alt="Comida deliciosa da cantina" onerror="this.onerror=null; this.src='/images/tradicional.jpg';">
                    
                    <!-- Prato do Dia (Floating Card) -->
                    <div style="position: absolute; top: 30px; left: -30px; background: #f8fafc; padding: 15px 20px; border-radius: 16px; box-shadow: 0 10px 30px rgba(0,0,0,0.15); display: flex; align-items: center; gap: 15px; z-index: 10;">
                        <div style="background: #e0e7ff; color: #4f46e5; width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px;">
                            <i class="fa-solid fa-utensils"></i>
                        </div>
                        <div>
                            <p style="font-size: 10px; color: #64748b; font-weight: 700; margin: 0 0 4px 0; letter-spacing: 0.5px;">HOJE NA CANTINA</p>
                            <p style="font-size: 15px; color: #0f172a; font-weight: 800; margin: 0 0 4px 0; max-width: 140px; line-height: 1.2;">${pratoAtual.nome}</p>
                            <p style="font-size: 15px; color: #f59e0b; font-weight: 800; margin: 0;">R$ ${pratoAtual.preco}</p>
                        </div>
                    </div>

                </div>
            </div>
        </section>

      

        <section class="container">
            <h2 class="section-title">Destaques do Cardápio</h2>
            <div class="products">
                ${products.length ? products.map(renderProductCard).join("") : '<p>Nenhum produto em destaque.</p>'}
            </div>
        </section>

        <!-- SEÇÃO PRATOS DA CANTINA -->
        <section class="section">
            <div class="container">
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

                <!-- Banner de Benefícios da Cantina -->
                <div style="display:flex; justify-content:space-around; align-items:center; background:var(--primary); color:white; padding:20px; border-radius:15px; margin-top:30px; flex-wrap:wrap; gap:15px;">
                    <div style="display:flex; align-items:center; gap:10px;">
                        <span style="font-size:24px;">🍽️</span>
                        <div>
                            <p style="font-weight:700; margin:0; font-size:14px;">Comida Caseira</p>
                            <p style="margin:0; font-size:12px; opacity:0.85;">Feita com carinho</p>
                        </div>
                    </div>
                    <div style="display:flex; align-items:center; gap:10px;">
                        <span style="font-size:24px;">🌿</span>
                        <div>
                            <p style="font-weight:700; margin:0; font-size:14px;">Ingredientes Selecionados</p>
                            <p style="margin:0; font-size:12px; opacity:0.85;">Qualidade garantida</p>
                        </div>
                    </div>
                    <div style="display:flex; align-items:center; gap:10px;">
                        <span style="font-size:24px;">❤️</span>
                        <div>
                            <p style="font-weight:700; margin:0; font-size:14px;">Sabor que Acolhe</p>
                            <p style="margin:0; font-size:12px; opacity:0.85;">Cantina SESI SENAI</p>
                        </div>
                    </div>
                </div>

            </div>
        </section>

        <section class="how-it-works">
            <div class="container">
                <h2 class="section-title center-title">Como Funciona</h2>
                <div class="steps">
                    <div class="step" data-step="1">
                        <div class="step-icon">📱</div>
                        <h3>Escolha</h3>
                        <p>Navegue pelo nosso cardápio e escolha seus pratos favoritos.</p>
                    </div>
                    <div class="step" data-step="2">
                        <div class="step-icon">🛒</div>
                        <h3>Peça</h3>
                        <p>Adicione ao carrinho e finalize seu pedido em segundos.</p>
                    </div>
                    <div class="step" data-step="3">
                        <div class="step-icon">🏃</div>
                        <h3>Retire</h3>
                        <p>Passe na cantina e retire seu pedido sem filas.</p>
                    </div>
                </div>
            </div>
        </section>
    `;
}