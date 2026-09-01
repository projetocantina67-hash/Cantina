import { api } from './api.js';
import { CATEGORIES } from './config.js';
import { formatPrice } from './utils.js';

const predefinedProducts = [
    { id: 101, name: 'Tradicional', description: 'Filé de frango grelhado com calabresa ao molho especial da cantina', price: 25.00, image: '/images/tradicional.jpg', category: 'Prato Feito', rating: 4.8, tempoPreparoMinutos: 15 },
    { id: 102, name: 'Do Dia', description: 'Filé de frango ou labareda temperados com os condimentos da casa', price: 28.00, image: '/images/dodia.jpg', category: 'Prato Feito', rating: 4.8, tempoPreparoMinutos: 15 },
    { id: 103, name: 'Picadinho de Carne com Legumes', description: 'Picadinho de carne bovina com legumes frescos da estação', price: 22.00, image: '/images/segunda.jpg', category: 'Prato do Dia', rating: 4.5, tempoPreparoMinutos: 20 },
    { id: 104, name: 'Strogonoff com Batata Palha', description: 'Clássico strogonoff de frango com batata palha crocante', price: 24.00, image: '/images/terca.jpg', category: 'Prato do Dia', rating: 4.7, tempoPreparoMinutos: 20 },
    { id: 105, name: 'Feijoada', description: 'Feijoada completa com arroz, couve, farofa e laranja', price: 26.00, image: '/images/quarta.jpg', category: 'Prato do Dia', rating: 4.9, tempoPreparoMinutos: 20 },
    { id: 106, name: 'Massa com Almôndegas', description: 'Massa ao molho de tomate caseiro com almôndegas grelhadas', price: 23.00, image: '/images/quinta.jpg', category: 'Prato do Dia', rating: 4.6, tempoPreparoMinutos: 20 },
    { id: 107, name: 'Peixe com Purê', description: 'Filé de peixe grelhado servido com purê de batatas cremoso', price: 27.00, image: '/images/sexta.jpg', category: 'Prato do Dia', rating: 4.8, tempoPreparoMinutos: 20 },
    { id: 108, name: 'Feijoada Especial de Sábado', description: 'Feijoada especial de sábado com todos os acompanhamentos tradicionais', price: 30.00, image: '/images/sabado.jpg', category: 'Prato do Dia', rating: 4.9, tempoPreparoMinutos: 25 },
];

let productsData = [...predefinedProducts];
let selectedCategory = 'Todos';

const IMAGE_MAP = {
    101: '/images/tradicional.jpg',
    102: '/images/dodia.jpg',
    103: '/images/segunda.jpg',
    104: '/images/terca.jpg',
    105: '/images/quarta.jpg',
    106: '/images/quinta.jpg',
    107: '/images/sexta.jpg',
    108: '/images/sabado.jpg'
};

export function getProductImage(p) {
    if (!p) return '/images/tradicional.jpg';
    if (p.id && IMAGE_MAP[p.id]) return IMAGE_MAP[p.id];
    if (p.imagem && !p.imagem.includes('loremflickr') && p.imagem !== '/images/tradicional.jpg') return p.imagem;
    if (p.image && !p.image.includes('loremflickr') && p.image !== '/images/tradicional.jpg') return p.image;
    
    const name = (p.nome || p.name || '').toLowerCase();
    const desc = (p.descricao || p.description || '').toLowerCase();
    const cat = (p.categoriaProduto || p.categoria || p.category || '').toLowerCase();
    
    if (name.includes('tradicional')) return '/images/tradicional.jpg';
    if (name.includes('dia')) return '/images/dodia.jpg';
    if (name.includes('picadinho') || name.includes('segunda')) return '/images/segunda.jpg';
    if (name.includes('strogonoff') || name.includes('terça') || name.includes('terca')) return '/images/terca.jpg';
    if (name.includes('feijoada') && (name.includes('sábado') || name.includes('sabado'))) return '/images/sabado.jpg';
    if (name.includes('feijoada')) return '/images/quarta.jpg';
    if (name.includes('massa') || name.includes('almôndegas') || name.includes('almondegas') || name.includes('quinta')) return '/images/quinta.jpg';
    if (name.includes('peixe') || name.includes('purê') || name.includes('pure') || name.includes('sexta')) return '/images/sexta.jpg';
    
    // Novas correspondências
    if (name.includes('coca') || desc.includes('coca')) return '/images/coca.png';
    if (name.includes('guaraná') || name.includes('guarana') || desc.includes('guaraná') || desc.includes('guarana') || name.includes('refrigerante')) return '/images/guarana.png';
    if (name.includes('bolo') || desc.includes('bolo')) return '/images/bolo.jpeg';
    if (name.includes('brownie') || desc.includes('brownie')) return '/images/brownie.jpeg';
    if (name.includes('salada') || name.includes('fruta') || desc.includes('fruta')) return '/images/salada_de_fruta.jpeg';
    
    // Fallbacks para salgados
    if (name.includes('salgado') || name.includes('coxinha') || name.includes('pastel') || name.includes('esfiha') || name.includes('kibe') || name.includes('bauru') || name.includes('hambúrguer') || name.includes('hamburguer') || cat === 'salgado') return '/images/salgado.jpeg';
    
    // Fallbacks para doces
    if (name.includes('croissant') || name.includes('doce') || cat === 'doce') return '/images/capa_doce.jpeg';

    return '/images/tradicional.jpg';
}

export async function loadProducts() {
    try {
        const apiProds = await api.getProdutos();
        if (Array.isArray(apiProds) && apiProds.length > 0) {
            productsData = apiProds.map(p => {
                const fallback = predefinedProducts.find(pre => pre.id === p.id) || {};
                return {
                    id: p.id,
                    name: p.nome || p.name || fallback.name || 'Produto',
                    description: p.descricao || p.description || fallback.description || '',
                    price: p.preco !== undefined ? p.preco : (p.price !== undefined ? p.price : fallback.price),
                    image: getProductImage(p) || fallback.image || '/images/tradicional.jpg',
                    category: p.categoriaProduto || p.categoria || p.category || fallback.category || 'Prato Feito',
                    rating: fallback.rating || 4.8,
                    tempoPreparoMinutos: p.tempoPreparoMinutos || fallback.tempoPreparoMinutos || 15
                };
            });
        } else {
            productsData = [...predefinedProducts];
        }
    } catch(e) {
        productsData = [...predefinedProducts];
    }
    
    const existingIds = new Set(productsData.map(p => p.id));
    const toAdd = predefinedProducts.filter(p => !existingIds.has(p.id));
    productsData = [...productsData, ...toAdd];
}

export function getProducts() {
    if (!selectedCategory || selectedCategory === 'Todos') return productsData;
    const normSelected = selectedCategory.toLowerCase().replace(/_/g, '').replace(/\s+/g, '');
    
    return productsData.filter(p => {
        if (!p.category) return false;
        const normProductCat = p.category.toLowerCase().replace(/_/g, '').replace(/\s+/g, '');
        if (normSelected === 'pratofeito' && (normProductCat === 'pratofeito' || normProductCat === 'pratododia')) {
            return true;
        }
        return normProductCat === normSelected;
    });
}

export function setCategory(cat) {
    selectedCategory = cat;
}

export function renderProductCard(product) {
    return `
        <div class="product-card" style="display: flex; flex-direction: column; height: 100%;">
            <div style="background-color: var(--primary); color: white; padding: 12px; text-align: center; font-weight: bold; font-size: 14px; text-transform: uppercase; letter-spacing: 1px;">
                ${product.category || 'Geral'}
            </div>
            <img class="product-image" src="${product.image}" alt="${product.name}" loading="lazy" onerror="this.onerror=null; this.src='/images/tradicional.jpg';">
            <div class="product-body" style="flex: 1; display: flex; flex-direction: column;">
                <div class="rating" style="color:#facc15; margin-bottom:10px;"><i class="fa-solid fa-star"></i> <span style="color:#6b7280">${product.rating}</span></div>
                <h3 style="font-size:20px; margin-bottom:8px;">${product.name}</h3>
                <p style="color:#6b7280; font-size:14px; line-height:1.5; min-height:42px; margin-bottom:20px; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden;">${product.description}</p>
                <div style="display:flex; align-items:center; justify-content:space-between; gap:15px; margin-top: auto;">
                    <span style="color:var(--primary); font-size:22px; font-weight:800;">R$ ${formatPrice(product.price)}</span>
                    <button class="small-button" onclick="window.app.openProduct(${product.id})">Ver Mais</button>
                </div>
            </div>
        </div>
    `;
}

export function renderCategories() {
    return CATEGORIES.map(cat => `
        <button class="category-card" onclick="window.app.filterCategory('${cat.name}')">
            <div class="category-icon">${cat.icon}</div>
            <h3>${cat.label}</h3>
        </button>
    `).join('');
}