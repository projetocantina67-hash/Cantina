import { api } from './api.js';
import { getUser } from './auth.js';
import { getCart, clearCart } from './cart.js';
import { formatPrice, showToast } from './utils.js';

let ordersCache = [];

export async function submitOrder() {
    const user = getUser();
    const cart = getCart();
    if (!user || cart.length === 0) return;

    const horarioSelecionado = document.getElementById('checkout-horario')?.value || '15min';
    const horarioEstimadoRetirada = resolverHorario(horarioSelecionado);

    const radioSelecionado = document.querySelector('input[name="pagamento"]:checked');
    const formaPagamento = radioSelecionado ? radioSelecionado.value : 'PIX';

    const pedidoData = {
        horarioEstimadoRetirada,
        formaPagamento,
        itens: cart.map(item => ({ produtoId: item.id, quantidade: item.quantity }))
    };

    await api.criarPedido(user.id, pedidoData);
    showToast('Agendamento realizado com sucesso!', 'success');
    clearCart();
    window.app.navigate('orders');
}

/**
 * Converte a opção do select de horário em um datetime ISO válido.
 */
function resolverHorario(opcao) {
    const agora = new Date();
    switch (opcao) {
        case 'manha': {
            const d = new Date(agora);
            d.setHours(10, 0, 0, 0);
            if (d <= agora) d.setDate(d.getDate() + 1);
            return d.toISOString();
        }
        case 'almoco': {
            const d = new Date(agora);
            d.setHours(12, 0, 0, 0);
            if (d <= agora) d.setDate(d.getDate() + 1);
            return d.toISOString();
        }
        case 'tarde': {
            const d = new Date(agora);
            d.setHours(17, 0, 0, 0);
            if (d <= agora) d.setDate(d.getDate() + 1);
            return d.toISOString();
        }
        case '15min':
        default:
            return new Date(Date.now() + 15 * 60000).toISOString();
    }
}

export async function renderOrdersPage() {
    const user = getUser();
    if (!user) {
        return `<div class="container center-title"><h1 class="section-title">Meus Pedidos</h1><p>Faça login para visualizar.</p><button class="primary-button" onclick="window.app.navigate('login')" style="margin-top:20px;">Fazer Login</button></div>`;
    }

    try {
        const pedidos = await api.getMeusPedidos(user.id);
        ordersCache = Array.isArray(pedidos) ? pedidos : [];

        if (!ordersCache || ordersCache.length === 0) {
            return `<div class="container"><h1 class="section-title">Meus Pedidos</h1><p>Você ainda não fez nenhum agendamento.</p></div>`;
        }

        return `
            <div class="container">
                <h1 class="section-title">Meus Pedidos</h1>
                <div class="products">
                    ${ordersCache.map(p => {
                        const isCancelado = p.status === 'CANCELADO';
                        const isPago = p.pago === true;
                        
                        let statusColor = '#0369a1';
                        let statusBg = '#e0f2fe';
                        if (p.status === 'PENDENTE' || p.status === 'RECEBIDO') { statusBg = '#fef3c7'; statusColor = '#92400e'; }
                        else if (p.status === 'EM_PREPARO') { statusBg = '#e0f2fe'; statusColor = '#0369a1'; }
                        else if (p.status === 'PRONTO') { statusBg = '#dcfce7'; statusColor = '#15803d'; }
                        else if (p.status === 'CANCELADO') { statusBg = '#fee2e2'; statusColor = '#b91c1c'; }
                        else if (p.status === 'FINALIZADO') { statusBg = '#f3f4f6'; statusColor = '#4b5563'; }

                        const podeGerenciar = !isCancelado && p.status !== 'FINALIZADO';

                        return `
                            <div class="product-card" style="padding:24px; display:flex; flex-direction:column; justify-content:space-between; height:100%;">
                                <div>
                                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
                                        <h3 style="font-size:20px; margin:0;">Pedido #${p.id}</h3>
                                        <span style="background:${statusBg}; color:${statusColor}; padding:6px 14px; border-radius:20px; font-size:12px; font-weight:800; text-transform:uppercase;">${p.status}</span>
                                    </div>
                                    <p style="color:#6b7280; font-size:14px; margin-bottom:8px;">
                                        <i class="fa-regular fa-clock"></i> <strong>Retirada:</strong> ${new Date(p.horarioEstimadoRetirada).toLocaleString('pt-BR')}
                                    </p>
                                    <p style="color:#6b7280; font-size:14px; margin-bottom:8px;">
                                        <i class="fa-solid fa-receipt"></i> <strong>Pagamento:</strong> 
                                        ${isPago 
                                            ? '<span style="color:#166534; font-weight:700;">🟢 Pago</span>' 
                                            : '<span style="color:#d97706; font-weight:700;">🟡 Pendente (PIX)</span>'}
                                    </p>
                                    <div style="margin-top:15px; font-size:22px; font-weight:900; color:var(--primary);">
                                        R$ ${formatPrice(p.valorTotal)}
                                    </div>
                                </div>
                                ${podeGerenciar ? `
                                    <button class="primary-button" style="margin-top:20px; width:100%; padding:12px; font-size:14px; display:flex; align-items:center; justify-content:center; gap:8px;" onclick="window.app.openOrderModal(${p.id})">
                                        <i class="fa-solid fa-sliders"></i> Gerenciar Pedido (Pagar / Cancelar)
                                    </button>
                                ` : ''}
                            </div>
                        `;
                    }).join('')}
                </div>
            </div>
            <div id="order-modal-root"></div>
        `;
    } catch (e) {
        return '<div class="container"><p>Erro ao carregar pedidos.</p></div>';
    }
}

export function openOrderModal(pedidoId) {
    const pedido = ordersCache.find(p => p.id === pedidoId);
    if (!pedido) {
        showToast('Pedido não encontrado.', 'error');
        return;
    }

    const modalRoot = document.getElementById('order-modal-root') || document.body;
    const isPago = pedido.pago === true;

    modalRoot.innerHTML = `
        <div class="modal-overlay" onclick="if(event.target === this) window.app.closeOrderModal()">
            <div class="modal-content">
                <button class="modal-close-btn" onclick="window.app.closeOrderModal()"><i class="fa-solid fa-xmark"></i></button>
                <div class="modal-header">
                    <h2 class="modal-title">Gerenciar Pedido #${pedido.id}</h2>
                    <p class="modal-subtitle">Valor Total: <strong style="color:var(--primary);">R$ ${formatPrice(pedido.valorTotal)}</strong></p>
                </div>

                <div id="modal-actions-step">
                    <div class="action-cards-grid">
                        <div class="action-card action-card-pay" onclick="window.app.showPixSection(${pedido.id})">
                            <div class="action-card-icon"><i class="fa-solid fa-qrcode"></i></div>
                            <div class="action-card-title">Pagar com PIX</div>
                            <div class="action-card-desc">${isPago ? '🟢 Pagamento Confirmado' : 'Gerar QR Code ou Copia e Cola'}</div>
                        </div>
                        <div class="action-card action-card-cancel" onclick="window.app.cancelUserOrder(${pedido.id})">
                            <div class="action-card-icon"><i class="fa-solid fa-ban"></i></div>
                            <div class="action-card-title">Cancelar Pedido</div>
                            <div class="action-card-desc">Desistir do pedido e liberar o estoque</div>
                        </div>
                    </div>
                </div>

                <div id="pix-section" class="hidden">
                    <div class="pix-container">
                        <div style="font-weight:800; color:#1e293b; font-size:16px; margin-bottom:5px;">
                            <i class="fa-solid fa-qrcode" style="color:var(--primary);"></i> Pagamento via PIX
                        </div>
                        <p style="font-size:13px; color:#64748b; margin-bottom:10px;">Escaneie o QR Code abaixo com seu app de banco ou copie o código:</p>
                        
                        <div class="pix-qr-box">
                            <svg width="140" height="140" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <rect width="100" height="100" fill="white"/>
                                <path d="M10 10H40V40H10V10ZM20 20V30H30V20H20Z" fill="#1E293B"/>
                                <path d="M60 10H90V40H60V10ZM70 20V30H80V20H70Z" fill="#1E293B"/>
                                <path d="M10 60H40V90H10V60ZM20 70V80H30V70H20Z" fill="#1E293B"/>
                                <rect x="45" y="45" width="10" height="10" fill="#6D28D9"/>
                                <rect x="60" y="60" width="15" height="15" fill="#1E293B"/>
                                <rect x="75" y="75" width="15" height="15" fill="#6D28D9"/>
                                <rect x="50" y="20" width="8" height="20" fill="#1E293B"/>
                                <rect x="20" y="50" width="20" height="8" fill="#1E293B"/>
                            </svg>
                        </div>

                        <div class="pix-key-input-wrapper">
                            <input type="text" id="pix-copy-paste-input" readonly value="00020126580014BR.GOV.BCB.PIX0136cantinasenai-pix-chave-efemera-0987655204000053039865405${formatPrice(pedido.valorTotal).replace(',', '')}5802BR5915CANTINA_SENAI6009SAO_PAULO62070503***6304">
                            <button class="small-button" onclick="window.app.copyPixKey()"><i class="fa-regular fa-copy"></i> Copiar</button>
                        </div>

                        <div style="margin-top:20px; display:flex; gap:10px;">
                            <button class="primary-button" style="flex:1; padding:12px; font-size:14px; background:#166534;" onclick="window.app.confirmPixPayment(${pedido.id})">
                                <i class="fa-solid fa-check"></i> Já Paguei / Confirmar
                            </button>
                            <button class="small-button" style="background:#e2e8f0; color:#334155;" onclick="document.getElementById('pix-section').classList.add('hidden'); document.getElementById('modal-actions-step').classList.remove('hidden');">
                                Voltar
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
}

export function closeOrderModal() {
    const modalRoot = document.getElementById('order-modal-root');
    if (modalRoot) modalRoot.innerHTML = '';
}

export function showPixSection(pedidoId) {
    const actionsStep = document.getElementById('modal-actions-step');
    const pixSection = document.getElementById('pix-section');
    if (actionsStep && pixSection) {
        actionsStep.classList.add('hidden');
        pixSection.classList.remove('hidden');
    }
}

export function copyPixKey() {
    const input = document.getElementById('pix-copy-paste-input');
    if (input) {
        input.select();
        navigator.clipboard?.writeText(input.value);
        showToast('Chave PIX copiada para a área de transferência!', 'success');
    }
}

export async function confirmPixPayment(pedidoId) {
    try {
        await api.marcarPagamentoPedido(pedidoId, true);
        showToast(`Pagamento do Pedido #${pedidoId} confirmado!`, 'success');
        closeOrderModal();
        window.app.render();
    } catch(e) {
        showToast('Erro ao confirmar pagamento.', 'error');
    }
}

export async function cancelUserOrder(pedidoId) {
    if (!confirm(`Tem certeza de que deseja cancelar o Pedido #${pedidoId}? Esta ação não pode ser desfeita.`)) {
        return;
    }

    try {
        await api.cancelarPedido(pedidoId);
        showToast(`Pedido #${pedidoId} foi cancelado com sucesso!`, 'success');
        closeOrderModal();
        window.app.render();
    } catch (e) {
        showToast('Erro ao cancelar o pedido.', 'error');
    }
}