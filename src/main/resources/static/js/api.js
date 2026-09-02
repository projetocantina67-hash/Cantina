import { API_URL } from './config.js';
import { toggleLoading, showToast } from './utils.js';

async function fetchAPI(endpoint, options = {}) {
    toggleLoading(true);
    try {
        const user = JSON.parse(localStorage.getItem('cantina_user'));
        const authHeaders = user?.token ? { 'Authorization': `Bearer ${user.token}` } : {};
        const response = await fetch(`${API_URL}${endpoint}`, {
            ...options,
            headers: { 'Content-Type': 'application/json', ...authHeaders, ...options.headers }
        });
        if (!response.ok) {
            let errorMsg = `Erro: ${response.status}`;
            try {
                const errData = await response.json();
                // O Spring Boot validation errors
                if (errData.campos) {
                    errorMsg = Object.values(errData.campos).join(', ');
                } else if (errData.errors) {
                    errorMsg = errData.errors[0].defaultMessage || errData.errors;
                } else if (errData.message) {
                    errorMsg = errData.message;
                } else if (errData.erro) {
                    errorMsg = errData.erro;
                } else if (Array.isArray(errData) && errData[0].mensagem) {
                    errorMsg = errData[0].mensagem;
                }
            } catch(e) {}
            throw new Error(errorMsg);
        }
        if (response.status === 204) return null;
        return await response.json();
    } catch (error) {
        showToast(error.message, 'error');
        throw error;
    } finally {
        toggleLoading(false);
    }
}

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

export const api = {
    // --- Autenticação ---
    login: async (cpf, senha) => await fetchAPI('/auth/login', { method: 'POST', body: JSON.stringify({ cpf, senha }) }),

    // --- Produtos ---
    getProdutos: async () => {
        const data = await fetchAPI('/produtos');
        return (data.content || []).map(p => ({
            id: p.id, name: p.nome, description: p.descricao || 'Sem descrição',
            price: p.preco, 
            image: p.imagem || IMAGE_MAP[p.id] || null,
            category: p.categoriaProduto || 'Geral', rating: 4.8
        }));
    },
    buscarProdutoPorId: async (id) => await fetchAPI(`/produtos/${id}`),
    cadastrarProduto: async (data) => await fetchAPI('/produtos', { method: 'POST', body: JSON.stringify(data) }),
    atualizarProduto: async (id, data) => await fetchAPI(`/produtos/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    desativarProduto: async (id) => await fetchAPI(`/produtos/${id}`, { method: 'DELETE' }),
    ativarProduto: async (id) => await fetchAPI(`/produtos/${id}/ativar`, { method: 'PATCH' }),
    resetarEstoque: async () => await fetchAPI('/produtos/resetar-estoque', { method: 'POST' }),

    // --- Usuários ---
    cadastrarUsuario: async (data) => await fetchAPI('/usuarios', { method: 'POST', body: JSON.stringify(data) }),
    buscarUsuarioPorId: async (id) => await fetchAPI(`/usuarios/${id}`),

    // --- Pedidos ---
    criarPedido: async (usuarioId, data) => await fetchAPI(`/usuarios/${usuarioId}/pedidos`, { method: 'POST', body: JSON.stringify(data) }),
    getMeusPedidos: async (usuarioId) => await fetchAPI(`/usuarios/${usuarioId}/pedidos`),
    listarPedidosPorStatus: async (status) => await fetchAPI(`/pedidos?status=${status}&size=500`),
    atualizarStatusPedido: async (id, status) => await fetchAPI(`/pedidos/${id}/status?status=${status}`, { method: 'PATCH' }),
    marcarPagamentoPedido: async (id, pago) => await fetchAPI(`/pedidos/${id}/pagamento?pago=${pago}`, { method: 'PATCH' }),
    cancelarPedido: async (id) => await fetchAPI(`/pedidos/${id}/cancelar`, { method: 'PATCH' }),
    listarProdutosAdmin: async () => await fetchAPI('/produtos/todos'),
    listarUsuarios: async () => await fetchAPI('/usuarios'),
    desativarUsuario: async (id) => await fetchAPI(`/usuarios/${id}`, { method: 'DELETE' }),

    // --- Itens de Pedido ---
    adicionarItemPedido: async (pedidoId, data) => await fetchAPI(`/pedidos/${pedidoId}/itens`, { method: 'POST', body: JSON.stringify(data) }),
    atualizarItemPedido: async (id, data) => await fetchAPI(`/itens/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    excluirItemPedido: async (id) => await fetchAPI(`/itens/${id}`, { method: 'DELETE' })
};