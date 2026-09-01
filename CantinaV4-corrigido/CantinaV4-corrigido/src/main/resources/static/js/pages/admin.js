import { api } from '../api.js';
import { formatPrice } from '../utils.js';
import { getUser } from '../auth.js';

let currentOrders = {
    'PENDENTE': [],
    'EM_PREPARO': [],
    'PRONTO': [],
    'ENTREGUE': [],
    'CANCELADO': []
};

let currentProdutos = [];
let currentUsuarios = [];
let produtoEditando = null;

const CATEGORIA_OPTIONS = [
    { value: 'SALGADO', label: 'Salgado' },
    { value: 'DOCE', label: 'Doce' },
    { value: 'BEBIDA', label: 'Bebida' },
    { value: 'PRATO_FEITO', label: 'Prato Feito' },
];

export async function loadAdminProdutos() {
    try {
        const data = await api.listarProdutosAdmin();
        currentProdutos = data?.content || [];
    } catch (e) { console.error("Erro ao carregar produtos:", e); }
}

export async function loadAdminUsuarios() {
    try {
        const data = await api.listarUsuarios();
        currentUsuarios = data?.content || [];
    } catch (e) { console.error("Erro ao carregar usuários:", e); }
}

let activeTab = 'kanban'; // 'kanban' ou 'historico'

export async function loadAdminOrders() {
    try {
        const [pendentes, emPreparo, prontos, entregues, cancelados] = await Promise.all([
            api.listarPedidosPorStatus('PENDENTE'),
            api.listarPedidosPorStatus('EM_PREPARO'),
            api.listarPedidosPorStatus('PRONTO'),
            api.listarPedidosPorStatus('ENTREGUE'),
            api.listarPedidosPorStatus('CANCELADO')
        ]);
        
        currentOrders.PENDENTE = pendentes?.content || [];
        currentOrders.EM_PREPARO = emPreparo?.content || [];
        currentOrders.PRONTO = prontos?.content || [];
        currentOrders.ENTREGUE = entregues?.content || [];
        currentOrders.CANCELADO = cancelados?.content || [];
    } catch (e) {
        console.error("Erro ao carregar pedidos no Painel da Cantina:", e);
    }
}

export async function renderAdmin() {
    const user = getUser();
    if (!user || user.perfil !== 'Funcionario da cantina') {
        return `
            <div class="container center-title" style="padding:60px 20px;">
                <div style="font-size:48px; margin-bottom:15px;">🔒</div>
                <h1 class="section-title">Acesso Restrito ao Pessoal da Cantina</h1>
                <p style="color:#6b7280; max-width:500px; margin:0 auto 20px;">
                    Este painel é de uso exclusivo dos funcionários autorizados para gerenciamento do fluxo de pedidos.
                </p>
                <button class="primary-button" onclick="window.app.navigate('login')">Fazer Login como Funcionário</button>
            </div>
        `;
    }

    await Promise.all([loadAdminOrders(), loadAdminProdutos(), loadAdminUsuarios()]);

    const totalAtivos = currentOrders.PENDENTE.length + currentOrders.EM_PREPARO.length + currentOrders.PRONTO.length;

    return `
        <div class="container" style="max-width:1400px; padding-top:20px; padding-bottom:40px;">
            
            <!-- CABEÇALHO DO PAINEL DA CANTINA -->
            <div style="background:white; border-radius:16px; padding:20px 25px; margin-bottom:25px; box-shadow:0 4px 20px rgba(0,0,0,0.06); display:flex; flex-wrap:wrap; justify-content:space-between; align-items:center; gap:15px;">
                <div>
                    <div style="display:flex; align-items:center; gap:10px;">
                        <span style="background:var(--primary); color:white; padding:6px 12px; border-radius:8px; font-weight:800; font-size:12px; letter-spacing:1px;">EQUIPE CANTINA</span>
                        <h1 style="margin:0; font-size:24px; color:#111827;">Painel de Operações & Pedidos</h1>
                    </div>
                    <p style="margin:5px 0 0 0; color:#6b7280; font-size:14px;">
                        Atendente: <strong>${user.nome}</strong> • Pedidos ativos no momento: <strong>${totalAtivos}</strong>
                    </p>
                </div>

                <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap;">
                    <button onclick="window.app.render()" style="background:#f3f4f6; color:#374151; border:1px solid #d1d5db; padding:10px 16px; border-radius:10px; font-weight:600; cursor:pointer; display:flex; align-items:center; gap:8px;">
                        <i class="fa-solid fa-rotate-right"></i> Atualizar Pedidos
                    </button>

                    <button onclick="window.app.handleResetStock()" style="background:#fee2e2; color:#991b1b; border:1px solid #fca5a5; padding:10px 16px; border-radius:10px; font-weight:600; cursor:pointer; display:flex; align-items:center; gap:8px;">
                        <i class="fa-solid fa-boxes-stacked"></i> Resetar Estoque Diário
                    </button>
                </div>
            </div>

            <!-- SELETOR DE ABA (KANBAN VS HISTÓRICO) -->
                        <!-- SELETOR DE ABA -->
                <div style="display:flex; gap:10px; margin-bottom:20px; flex-wrap:wrap;">
                <button onclick="window.app.switchAdminTab('kanban')" style="padding:10px 20px; border-radius:10px; font-weight:700; border:none; cursor:pointer; ${activeTab === 'kanban' ? 'background:var(--primary); color:white;' : 'background:#e5e7eb; color:#4b5563;'}">
                    <i class="fa-solid fa-list-check"></i> Fila de Produção (${totalAtivos})
                </button>
                <button onclick="window.app.switchAdminTab('historico')" style="padding:10px 20px; border-radius:10px; font-weight:700; border:none; cursor:pointer; ${activeTab === 'historico' ? 'background:var(--primary); color:white;' : 'background:#e5e7eb; color:#4b5563;'}">
                    <i class="fa-solid fa-clock-rotate-left"></i> Histórico (${currentOrders.ENTREGUE.length + currentOrders.CANCELADO.length})
                </button>
                <button onclick="window.app.switchAdminTab('produtos')" style="padding:10px 20px; border-radius:10px; font-weight:700; border:none; cursor:pointer; ${activeTab === 'produtos' ? 'background:var(--primary); color:white;' : 'background:#e5e7eb; color:#4b5563;'}">
                    <i class="fa-solid fa-box"></i> Produtos (${currentProdutos.length})
                </button>
                <button onclick="window.app.switchAdminTab('funcionarios')" style="padding:10px 20px; border-radius:10px; font-weight:700; border:none; cursor:pointer; ${activeTab === 'funcionarios' ? 'background:var(--primary); color:white;' : 'background:#e5e7eb; color:#4b5563;'}">
                    <i class="fa-solid fa-users"></i> Equipe (${currentUsuarios.length})
                </button>
            </div>

            ${activeTab === 'kanban' ? renderKanbanView()
        : activeTab === 'historico' ? renderHistoricoView()
            : activeTab === 'produtos' ? renderProdutosView()
                : renderFuncionariosView()}

        </div>
    `;
}

function renderKanbanView() {
    return `
        <!-- COLUNAS KANBAN DE PEDIDOS -->
        <div style="display:grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap:20px; align-items:start;">
            
            <!-- COLUNA 1: PENDENTES -->
            <div style="background:#f9fafb; border:2px solid #fef3c7; border-radius:16px; padding:18px;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:15px; padding-bottom:10px; border-bottom:2px solid #fde68a;">
                    <span style="font-weight:700; font-size:16px; color:#92400e; display:flex; align-items:center; gap:8px;">
                        <i class="fa-solid fa-clock" style="color:#f59e0b;"></i> 1. PENDENTES (Aguardando)
                    </span>
                    <span style="background:#fef3c7; color:#92400e; padding:4px 12px; border-radius:20px; font-weight:800; font-size:13px;">
                        ${currentOrders.PENDENTE.length}
                    </span>
                </div>
                <div style="display:flex; flex-direction:column; gap:15px;">
                    ${currentOrders.PENDENTE.length > 0 
                        ? currentOrders.PENDENTE.map(renderKanbanCard).join('') 
                        : '<p style="text-align:center; color:#9ca3af; padding:20px 0; font-size:14px;">Nenhum pedido pendente.</p>'}
                </div>
            </div>

            <!-- COLUNA 2: EM PREPARO -->
            <div style="background:#f9fafb; border:2px solid #fed7aa; border-radius:16px; padding:18px;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:15px; padding-bottom:10px; border-bottom:2px solid #fdba74;">
                    <span style="font-weight:700; font-size:16px; color:#9a3412; display:flex; align-items:center; gap:8px;">
                        <i class="fa-solid fa-fire-burner" style="color:#ea580c;"></i> 2. EM PREPARO (Cozinha)
                    </span>
                    <span style="background:#ffedd5; color:#9a3412; padding:4px 12px; border-radius:20px; font-weight:800; font-size:13px;">
                        ${currentOrders.EM_PREPARO.length}
                    </span>
                </div>
                <div style="display:flex; flex-direction:column; gap:15px;">
                    ${currentOrders.EM_PREPARO.length > 0 
                        ? currentOrders.EM_PREPARO.map(renderKanbanCard).join('') 
                        : '<p style="text-align:center; color:#9ca3af; padding:20px 0; font-size:14px;">Cozinha sem pedidos no momento.</p>'}
                </div>
            </div>

            <!-- COLUNA 3: PRONTOS -->
            <div style="background:#f9fafb; border:2px solid #bbf7d0; border-radius:16px; padding:18px;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:15px; padding-bottom:10px; border-bottom:2px solid #86efac;">
                    <span style="font-weight:700; font-size:16px; color:#166534; display:flex; align-items:center; gap:8px;">
                        <i class="fa-solid fa-bell" style="color:#16a34a;"></i> 3. PRONTOS (No Balcão)
                    </span>
                    <span style="background:#dcfce7; color:#166534; padding:4px 12px; border-radius:20px; font-weight:800; font-size:13px;">
                        ${currentOrders.PRONTO.length}
                    </span>
                </div>
                <div style="display:flex; flex-direction:column; gap:15px;">
                    ${currentOrders.PRONTO.length > 0 
                        ? currentOrders.PRONTO.map(renderKanbanCard).join('') 
                        : '<p style="text-align:center; color:#9ca3af; padding:20px 0; font-size:14px;">Nenhum pedido aguardando retirada.</p>'}
                </div>
            </div>

        </div>
    `;
}

function renderKanbanCard(order) {
    const time = new Date(order.horarioEstimadoRetirada).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    
    let nextStatus = '';
    let btnText = '';
    let btnColor = 'var(--primary)';

    if (order.status === 'PENDENTE') {
        nextStatus = 'EM_PREPARO';
        btnText = '▶ Iniciar Preparo';
        btnColor = '#d97706';
    } else if (order.status === 'EM_PREPARO') {
        nextStatus = 'PRONTO';
        btnText = '✓ Marcar como Pronto';
        btnColor = '#16a34a';
    } else if (order.status === 'PRONTO') {
        nextStatus = 'ENTREGUE';
        btnText = '🎉 Entregar ao Cliente';
        btnColor = '#2563eb';
    }

    return `
        <div style="background:white; border-radius:12px; padding:16px; border:1px solid #e5e7eb; box-shadow:0 2px 8px rgba(0,0,0,0.04);">
            <!-- HEADER DO CARD -->
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
                <span style="font-weight:900; font-size:16px; color:#111827;">Pedido #${order.id}</span>
                <span style="font-size:13px; font-weight:700; color:#4b5563; background:#f3f4f6; padding:3px 8px; border-radius:6px;">
                    <i class="fa-regular fa-clock"></i> Retirar às ${time}
                </span>
            </div>

            <!-- LISTA DE ITENS DO PEDIDO -->
            <div style="margin-bottom:12px; padding:10px; background:#f9fafb; border-radius:8px; font-size:13px; color:#374151;">
                ${(order.itens || []).map(i => `
                    <div style="display:flex; justify-content:space-between; padding:2px 0;">
                        <span><strong>${i.quantidade}x</strong> ${i.produtoNome}</span>
                        <span style="color:#6b7280;">R$ ${formatPrice(i.precoUnitario * i.quantidade)}</span>
                    </div>
                `).join('')}
            </div>

            <!-- PAGAMENTO & TOTAL -->
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; font-size:13px;">
                <div>
                    <span style="font-weight:700; color:#111827;">R$ ${formatPrice(order.valorTotal)}</span>
                    <span style="color:#9ca3af; font-size:11px; margin-left:4px;">(${order.formaPagamento || 'PIX'})</span>
                </div>

                <!-- BOTÃO INTERATIVO DE PAGAMENTO -->
                <button onclick="window.app.togglePaymentStatus(${order.id}, ${order.pago})" 
                        title="Clique para alterar status de pagamento"
                        style="border:none; cursor:pointer; font-size:11px; font-weight:800; padding:4px 10px; border-radius:12px; transition:all 0.2s; ${order.pago ? 'background:#d1fae5; color:#065f46;' : 'background:#fee2e2; color:#991b1b;'}">
                    ${order.pago ? '<i class="fa-solid fa-check"></i> PAGO' : '<i class="fa-solid fa-triangle-exclamation"></i> NÃO PAGO (Marcar)'}
                </button>
            </div>

            <!-- AÇÕES PRINCIPAIS DO PEDIDO -->
            <div style="display:flex; gap:8px;">
                <button onclick="window.app.updateOrderStatus(${order.id}, '${nextStatus}')" 
                        style="flex:1; padding:10px; background:${btnColor}; color:white; border:none; border-radius:8px; font-weight:700; font-size:13px; cursor:pointer; transition:opacity 0.2s;">
                    ${btnText}
                </button>
                
                ${order.status === 'PENDENTE' ? `
                    <button onclick="window.app.cancelOrder(${order.id})" 
                            title="Cancelar Pedido"
                            style="padding:10px 12px; background:#f3f4f6; color:#ef4444; border:1px solid #fee2e2; border-radius:8px; font-weight:700; cursor:pointer;">
                        <i class="fa-solid fa-xmark"></i>
                    </button>
                ` : ''}
            </div>
        </div>
    `;
}

function renderProdutosView() {
    return `
        <div style="background:white; border-radius:16px; padding:20px; border:1px solid #e5e7eb;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; flex-wrap:wrap; gap:10px;">
                <h3 style="margin:0; color:#111827;"><i class="fa-solid fa-box"></i> Gestão de Produtos (${currentProdutos.length})</h3>
                <button class="primary-button" style="padding:10px 18px; font-size:14px;" onclick="window.app.abrirModalProduto()">
                    <i class="fa-solid fa-plus"></i> Novo Produto
                </button>
            </div>
            <div style="display:flex; flex-direction:column; gap:10px;">
                ${currentProdutos.length > 0 ? currentProdutos.map(renderProdutoRow).join('') : '<p style="text-align:center; color:#9ca3af; padding:20px 0;">Nenhum produto cadastrado.</p>'}
            </div>
        </div>
        <div id="admin-modal-root"></div>
    `;
}

function renderProdutoRow(produto) {
    return `
        <div style="display:flex; justify-content:space-between; align-items:center; padding:14px; border:1px solid #f3f4f6; border-radius:10px; gap:15px; flex-wrap:wrap; ${!produto.ativo ? 'opacity:0.6; background:#f9fafb;' : ''}">
            <div style="flex:1; min-width:200px;">
                <div style="display:flex; align-items:center; gap:8px;">
                    <strong style="font-size:15px; color:#111827;">${produto.nome}</strong>
                    <span style="font-size:11px; font-weight:700; padding:2px 8px; border-radius:10px; ${produto.ativo ? 'background:#dcfce7; color:#166534;' : 'background:#fee2e2; color:#991b1b;'}">
                        ${produto.ativo ? 'ATIVO' : 'INATIVO'}
                    </span>
                </div>
                <p style="margin:4px 0 0; font-size:13px; color:#6b7280;">
                    R$ ${formatPrice(produto.preco)} • ${produto.categoriaProduto} • Estoque hoje: ${produto.quantidadeDisponivelHoje}/${produto.quantidadePadraoDiaria}
                </p>
            </div>
            <div style="display:flex; gap:8px;">
                <button onclick="window.app.abrirModalProduto(${produto.id})" style="padding:8px 12px; background:#f3f4f6; color:#374151; border:1px solid #d1d5db; border-radius:8px; font-weight:600; cursor:pointer;" title="Editar">
                    <i class="fa-solid fa-pen"></i>
                </button>
                ${produto.ativo
        ? `<button onclick="window.app.desativarProdutoAdmin(${produto.id})" style="padding:8px 12px; background:#fee2e2; color:#991b1b; border:1px solid #fca5a5; border-radius:8px; font-weight:600; cursor:pointer;" title="Desativar"><i class="fa-solid fa-ban"></i></button>`
        : `<button onclick="window.app.ativarProdutoAdmin(${produto.id})" style="padding:8px 12px; background:#dcfce7; color:#166534; border:1px solid #86efac; border-radius:8px; font-weight:600; cursor:pointer;" title="Reativar"><i class="fa-solid fa-rotate-left"></i></button>`
    }
            </div>
        </div>
    `;
}

function renderFuncionariosView() {
    return `
        <div style="background:white; border-radius:16px; padding:20px; border:1px solid #e5e7eb;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; flex-wrap:wrap; gap:10px;">
                <h3 style="margin:0; color:#111827;"><i class="fa-solid fa-users"></i> Equipe & Usuários (${currentUsuarios.length})</h3>
                <button class="primary-button" style="padding:10px 18px; font-size:14px;" onclick="window.app.abrirModalFuncionario()">
                    <i class="fa-solid fa-user-plus"></i> Novo Funcionário
                </button>
            </div>
            <div style="display:flex; flex-direction:column; gap:10px;">
                ${currentUsuarios.length > 0 ? currentUsuarios.map(renderUsuarioRow).join('') : '<p style="text-align:center; color:#9ca3af; padding:20px 0;">Nenhum usuário encontrado.</p>'}
            </div>
        </div>
        <div id="admin-modal-root"></div>
    `;
}

function renderUsuarioRow(usuario) {
    const isFuncionario = usuario.perfil === 'Funcionario da cantina';
    return `
        <div style="display:flex; justify-content:space-between; align-items:center; padding:14px; border:1px solid #f3f4f6; border-radius:10px; gap:15px; flex-wrap:wrap;">
            <div style="flex:1; min-width:200px;">
                <div style="display:flex; align-items:center; gap:8px;">
                    <strong style="font-size:15px; color:#111827;">${usuario.nome}</strong>
                    <span style="font-size:11px; font-weight:700; padding:2px 8px; border-radius:10px; ${isFuncionario ? 'background:#ede9fe; color:#5b21b6;' : 'background:#e5e7eb; color:#4b5563;'}">${usuario.perfil}</span>
                </div>
                <p style="margin:4px 0 0; font-size:13px; color:#6b7280;">${usuario.email} • ${usuario.telefone}</p>
            </div>
            <button onclick="window.app.desativarUsuarioAdmin(${usuario.id})" style="padding:8px 12px; background:#fee2e2; color:#991b1b; border:1px solid #fca5a5; border-radius:8px; font-weight:600; cursor:pointer;" title="Desativar acesso">
                <i class="fa-solid fa-user-slash"></i>
            </button>
        </div>
    `;
}

function renderHistoricoView() {
    return `
        <div style="display:grid; grid-template-columns: 1fr 1fr; gap:20px;">
            <!-- ENTREGUES -->
            <div style="background:white; border-radius:16px; padding:20px; border:1px solid #e5e7eb;">
                <h3 style="margin-top:0; color:#166534; display:flex; align-items:center; gap:8px;">
                    <i class="fa-solid fa-circle-check"></i> Entregues Recentemente (${currentOrders.ENTREGUE.length})
                </h3>
                <div style="display:flex; flex-direction:column; gap:10px; max-height:500px; overflow-y:auto;">
                    ${currentOrders.ENTREGUE.length > 0 ? currentOrders.ENTREGUE.map(renderHistoricoItem).join('') : '<p style="color:#9ca3af;">Nenhum pedido entregue no histórico recente.</p>'}
                </div>
            </div>

            <!-- CANCELADOS -->
            <div style="background:white; border-radius:16px; padding:20px; border:1px solid #e5e7eb;">
                <h3 style="margin-top:0; color:#991b1b; display:flex; align-items:center; gap:8px;">
                    <i class="fa-solid fa-ban"></i> Pedidos Cancelados (${currentOrders.CANCELADO.length})
                </h3>
                <div style="display:flex; flex-direction:column; gap:10px; max-height:500px; overflow-y:auto;">
                    ${currentOrders.CANCELADO.length > 0 ? currentOrders.CANCELADO.map(renderHistoricoItem).join('') : '<p style="color:#9ca3af;">Nenhum pedido cancelado.</p>'}
                </div>
            </div>
        </div>
    `;
}

function renderHistoricoItem(order) {
    return `
        <div style="padding:12px; border:1px solid #f3f4f6; border-radius:8px; display:flex; justify-content:space-between; align-items:center; font-size:13px;">
            <div>
                <strong>Pedido #${order.id}</strong> • R$ ${formatPrice(order.valorTotal)} (${order.formaPagamento})
                <div style="color:#6b7280; font-size:12px;">${(order.itens || []).map(i => `${i.quantidade}x ${i.produtoNome}`).join(', ')}</div>
            </div>
            <span style="font-weight:700; padding:3px 8px; border-radius:6px; ${order.status === 'ENTREGUE' ? 'background:#dcfce7; color:#166534;' : 'background:#fee2e2; color:#991b1b;'}">
                ${order.status}
            </span>
        </div>
    `;
}

// =========================================================================
// AÇÕES DO FUNCIONÁRIO INTEGRADAS COM REST BACKEND
// =========================================================================

export function switchAdminTab(tab) {
    activeTab = tab;
    window.app.render();
}

export async function updateOrderStatus(id, newStatus) {
    try {
        await api.atualizarStatusPedido(id, newStatus);
        window.app.showToast(`Pedido #${id} atualizado para ${newStatus}!`, 'success');
        window.app.render();
    } catch (e) {
        // Trato no api.js
    }
}

export async function togglePaymentStatus(id, currentPagoState) {
    try {
        const novoStatusPago = !currentPagoState;
        await api.marcarPagamentoPedido(id, novoStatusPago);
        window.app.showToast(`Pagamento do Pedido #${id} alterado para ${novoStatusPago ? 'PAGO' : 'NÃO PAGO'}!`, 'success');
        window.app.render();
    } catch (e) {
        // Trato no api.js
    }
}

export async function cancelOrder(id) {
    if (confirm(`Tem certeza que deseja CANCELAR o pedido #${id}?`)) {
        try {
            await api.atualizarStatusPedido(id, 'CANCELADO');
            window.app.showToast(`Pedido #${id} foi cancelado.`, 'warning');
            window.app.render();
        } catch (e) {}
    }
}

export async function handleResetStock() {
    if (confirm('Atenção: Deseja redefinir o estoque diário de TODOS os produtos ativos para a quantidade padrão?')) {
        try {
            await api.resetarEstoque();
            window.app.showToast('Estoque diário redefinido com sucesso!', 'success');
            window.app.render();
        } catch (e) {}
    }
}

export function abrirModalProduto(produtoId) {
    produtoEditando = produtoId ? currentProdutos.find(p => p.id === produtoId) : null;
    const p = produtoEditando || {};
    const modalRoot = document.getElementById('admin-modal-root') || document.body;

    modalRoot.innerHTML = `
        <div class="modal-overlay" onclick="if(event.target === this) window.app.fecharModalAdmin()">
            <div class="modal-content">
                <button class="modal-close-btn" onclick="window.app.fecharModalAdmin()"><i class="fa-solid fa-xmark"></i></button>
                <div class="modal-header"><h2 class="modal-title">${produtoEditando ? 'Editar Produto' : 'Novo Produto'}</h2></div>
                <form id="produto-form" onsubmit="window.app.salvarProdutoAdmin(event)">
                    <div style="text-align:left; margin-bottom:15px;">
                        <label style="display:block; font-weight:600; margin-bottom:6px; font-size:14px;">Nome</label>
                        <input type="text" id="produto-nome" required minlength="3" maxlength="100" value="${p.nome || ''}" style="width:100%; padding:10px; border:1px solid #e5e7eb; border-radius:8px; font-size:14px;">
                    </div>
                    <div style="text-align:left; margin-bottom:15px;">
                        <label style="display:block; font-weight:600; margin-bottom:6px; font-size:14px;">Descrição</label>
                        <textarea id="produto-descricao" required minlength="10" maxlength="500" rows="3" style="width:100%; padding:10px; border:1px solid #e5e7eb; border-radius:8px; font-size:14px; resize:vertical;">${p.descricao || ''}</textarea>
                    </div>
                    <div style="display:flex; gap:12px; margin-bottom:15px;">
                        <div style="text-align:left; flex:1;">
                            <label style="display:block; font-weight:600; margin-bottom:6px; font-size:14px;">Preço (R$)</label>
                            <input type="number" id="produto-preco" required min="0.01" step="0.01" value="${p.preco ?? ''}" style="width:100%; padding:10px; border:1px solid #e5e7eb; border-radius:8px; font-size:14px;">
                        </div>
                        <div style="text-align:left; flex:1;">
                            <label style="display:block; font-weight:600; margin-bottom:6px; font-size:14px;">Preparo (min)</label>
                            <input type="number" id="produto-tempo" required min="1" max="480" value="${p.tempoPreparoMinutos ?? ''}" style="width:100%; padding:10px; border:1px solid #e5e7eb; border-radius:8px; font-size:14px;">
                        </div>
                    </div>
                    <div style="display:flex; gap:12px; margin-bottom:15px;">
                        <div style="text-align:left; flex:1;">
                            <label style="display:block; font-weight:600; margin-bottom:6px; font-size:14px;">Qtd. padrão diária</label>
                            <input type="number" id="produto-qtd" required min="0" max="10000" value="${p.quantidadePadraoDiaria ?? ''}" style="width:100%; padding:10px; border:1px solid #e5e7eb; border-radius:8px; font-size:14px;">
                        </div>
                        <div style="text-align:left; flex:1;">
                            <label style="display:block; font-weight:600; margin-bottom:6px; font-size:14px;">Categoria</label>
                            <select id="produto-categoria" required style="width:100%; padding:10px; border:1px solid #e5e7eb; border-radius:8px; font-size:14px; background:white;">
                                ${CATEGORIA_OPTIONS.map(c => `<option value="${c.value}" ${p.categoriaProduto === c.value ? 'selected' : ''}>${c.label}</option>`).join('')}
                            </select>
                        </div>
                    </div>
                    <button type="submit" class="primary-button" style="width:100%; padding:12px; font-size:15px;">${produtoEditando ? 'Salvar Alterações' : 'Cadastrar Produto'}</button>
                </form>
            </div>
        </div>
    `;
}

export function fecharModalAdmin() {
    const modalRoot = document.getElementById('admin-modal-root');
    if (modalRoot) modalRoot.innerHTML = '';
    produtoEditando = null;
}

export async function salvarProdutoAdmin(event) {
    event.preventDefault();
    const data = {
        nome: document.getElementById('produto-nome').value.trim(),
        descricao: document.getElementById('produto-descricao').value.trim(),
        preco: parseFloat(document.getElementById('produto-preco').value),
        tempoPreparoMinutos: parseInt(document.getElementById('produto-tempo').value, 10),
        quantidadePadraoDiaria: parseInt(document.getElementById('produto-qtd').value, 10),
        categoriaProduto: document.getElementById('produto-categoria').value
    };
    try {
        if (produtoEditando) {
            await api.atualizarProduto(produtoEditando.id, data);
            window.app.showToast('Produto atualizado com sucesso!', 'success');
        } else {
            await api.cadastrarProduto(data);
            window.app.showToast('Produto cadastrado com sucesso!', 'success');
        }
        fecharModalAdmin();
        window.app.render();
    } catch (e) { /* erro já tratado no api.js */ }
}

export async function desativarProdutoAdmin(id) {
    if (!confirm('Desativar este produto? Ele deixará de aparecer no cardápio.')) return;
    try {
        await api.desativarProduto(id);
        window.app.showToast('Produto desativado.', 'warning');
        window.app.render();
    } catch (e) {}
}

export async function ativarProdutoAdmin(id) {
    try {
        await api.ativarProduto(id);
        window.app.showToast('Produto reativado com sucesso!', 'success');
        window.app.render();
    } catch (e) {}
}

export function abrirModalFuncionario() {
    const modalRoot = document.getElementById('admin-modal-root') || document.body;
    modalRoot.innerHTML = `
        <div class="modal-overlay" onclick="if(event.target === this) window.app.fecharModalAdmin()">
            <div class="modal-content">
                <button class="modal-close-btn" onclick="window.app.fecharModalAdmin()"><i class="fa-solid fa-xmark"></i></button>
                <div class="modal-header">
                    <h2 class="modal-title">Novo Funcionário</h2>
                    <p class="modal-subtitle">A conta já é criada com o perfil de Funcionário da Cantina.</p>
                </div>
                <form id="funcionario-form" onsubmit="window.app.cadastrarFuncionarioAdmin(event)">
                    <div style="text-align:left; margin-bottom:15px;">
                        <label style="display:block; font-weight:600; margin-bottom:6px; font-size:14px;">Nome Completo</label>
                        <input type="text" id="func-nome" required minlength="3" maxlength="100" style="width:100%; padding:10px; border:1px solid #e5e7eb; border-radius:8px; font-size:14px;">
                    </div>
                    <div style="display:flex; gap:12px; margin-bottom:15px;">
                        <div style="text-align:left; flex:1;">
                            <label style="display:block; font-weight:600; margin-bottom:6px; font-size:14px;">CPF</label>
                            <input type="text" id="func-cpf" required style="width:100%; padding:10px; border:1px solid #e5e7eb; border-radius:8px; font-size:14px;">
                        </div>
                        <div style="text-align:left; flex:1;">
                            <label style="display:block; font-weight:600; margin-bottom:6px; font-size:14px;">Telefone</label>
                            <input type="text" id="func-telefone" required style="width:100%; padding:10px; border:1px solid #e5e7eb; border-radius:8px; font-size:14px;">
                        </div>
                    </div>
                    <div style="text-align:left; margin-bottom:15px;">
                        <label style="display:block; font-weight:600; margin-bottom:6px; font-size:14px;">Email</label>
                        <input type="email" id="func-email" required style="width:100%; padding:10px; border:1px solid #e5e7eb; border-radius:8px; font-size:14px;">
                    </div>
                    <div style="text-align:left; margin-bottom:20px;">
                        <label style="display:block; font-weight:600; margin-bottom:6px; font-size:14px;">Senha Temporária</label>
                        <input type="password" id="func-senha" required minlength="8" style="width:100%; padding:10px; border:1px solid #e5e7eb; border-radius:8px; font-size:14px;">
                        <small style="color:#6b7280; font-size:12px; display:block; margin-top:5px;">Maiúscula, minúscula e número.</small>
                    </div>
                    <button type="submit" class="primary-button" style="width:100%; padding:12px; font-size:15px;">Cadastrar Funcionário</button>
                </form>
            </div>
        </div>
    `;
}

export async function cadastrarFuncionarioAdmin(event) {
    event.preventDefault();
    const data = {
        nome: document.getElementById('func-nome').value.trim(),
        cpf: document.getElementById('func-cpf').value.replace(/\D/g, ''),
        email: document.getElementById('func-email').value.trim(),
        telefone: document.getElementById('func-telefone').value.replace(/\D/g, ''),
        perfil: 'Funcionario da cantina',
        senha: document.getElementById('func-senha').value
    };
    try {
        await api.cadastrarUsuario(data);
        window.app.showToast('Funcionário cadastrado com sucesso!', 'success');
        fecharModalAdmin();
        window.app.render();
    } catch (e) { /* erro já tratado no api.js */ }
}

export async function desativarUsuarioAdmin(id) {
    if (!confirm('Desativar o acesso deste usuário?')) return;
    try {
        await api.desativarUsuario(id);
        window.app.showToast('Acesso desativado.', 'warning');
        window.app.render();
    } catch (e) {}
}