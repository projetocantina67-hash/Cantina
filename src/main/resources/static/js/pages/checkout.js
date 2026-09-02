import { getCart, getCartTotal } from '../cart.js';
import { formatPrice } from '../utils.js';
import { getUser } from '../auth.js';

export function renderCheckout() {
    const cart = getCart();
    const user = getUser();
    
    if (!user) {
        return `
            <div class="container center-title">
                <h1 class="section-title">Finalizar Pedido</h1>
                <p style="margin-bottom:20px;">Você precisa estar logado para agendar um pedido na cantina.</p>
                <button class="primary-button" onclick="window.app.navigate('login')">Fazer Login ou Cadastro</button>
            </div>
        `;
    }

    if (cart.length === 0) {
        return `
            <div class="container center-title">
                <h1 class="section-title">Seu carrinho está vazio</h1>
                <button class="primary-button" onclick="window.app.navigate('menu')" style="margin-top:20px;">Ver Cardápio</button>
            </div>
        `;
    }

    return `
        <div class="container">
            <h1 class="section-title">Finalizar Pedido</h1>
            
            <div class="checkout-grid" style="display:grid; grid-template-columns: 2fr 1fr; gap:30px;">
                
                <div class="checkout-form">
                    <div style="background:white; padding:25px; border-radius:15px; box-shadow:0 10px 30px rgba(0,0,0,0.05); margin-bottom:20px;">
                        <h2 style="margin-bottom:20px; font-size:1.3rem;">Dados para Retirada</h2>
                        
                        <div class="form-group" style="margin-bottom:15px;">
                            <label style="display:block; margin-bottom:8px; font-weight:600; color:#374151;">Nome (Usuário Logado)</label>
                            <input type="text" value="${user.nome || 'Usuário'}" disabled style="width:100%; padding:12px; border:1px solid #e5e7eb; border-radius:8px; background:#f9fafb;">
                        </div>
                        
                        <div class="form-group" style="margin-bottom:15px;">
                            <label style="display:block; margin-bottom:8px; font-weight:600; color:#374151;">Horário Estimado de Retirada</label>
                        <select id="checkout-horario" style="width:100%; padding:12px; border:1px solid #e5e7eb; border-radius:8px;">
                                <option value="15min">Daqui a 15 minutos</option>
                                <option value="manha">Intervalo da Manhã (~10:00)</option>
                                <option value="almoco">Horário do Almoço (~12:00)</option>
                                <option value="tarde">Intervalo da Tarde (~17:00)</option>
                            </select>
                        </div>
                    </div>
                    
                    <div style="background:white; padding:25px; border-radius:15px; box-shadow:0 10px 30px rgba(0,0,0,0.05);">
                        <h2 style="margin-bottom:20px; font-size:1.3rem;">Forma de Pagamento</h2>
                        <div style="display:grid; grid-template-columns:1fr 1fr; gap:15px;">
                            <label style="border:2px solid var(--primary); padding:15px; border-radius:10px; cursor:pointer; text-align:center;">
                                <input type="radio" name="pagamento" value="PIX" checked style="display:none;">
                                <div style="font-size:24px; color:var(--primary);"><i class="fa-brands fa-pix"></i></div>
                                <div style="font-weight:600; margin-top:5px;">PIX</div>
                            </label>
                            <label style="border:1px solid #e5e7eb; padding:15px; border-radius:10px; cursor:pointer; text-align:center;">
                                <input type="radio" name="pagamento" value="DINHEIRO" style="display:none;">
                                <div style="font-size:24px; color:#6b7280;"><i class="fa-solid fa-money-bill"></i></div>
                                <div style="font-weight:600; margin-top:5px; color:#6b7280;">Na Retirada</div>
                            </label>
                        </div>
                    </div>
                </div>

                <div class="checkout-summary" style="background:white; padding:25px; border-radius:15px; box-shadow:0 10px 30px rgba(0,0,0,0.05); align-self:start;">
                    <h2 style="margin-bottom:20px; font-size:1.3rem;">Resumo do Pedido</h2>
                    
                    <div style="border-bottom:1px solid #e5e7eb; padding-bottom:15px; margin-bottom:15px;">
                        ${cart.map(item => `
                            <div style="display:flex; justify-content:space-between; margin-bottom:10px; color:#4b5563;">
                                <span>${item.quantity}x ${item.name}</span>
                                <span style="font-weight:600;">R$ ${formatPrice(item.price * item.quantity)}</span>
                            </div>
                        `).join('')}
                    </div>
                    
                    <div style="display:flex; justify-content:space-between; margin-bottom:25px; font-size:1.2rem; font-weight:800; color:#111827;">
                        <span>Total</span>
                        <span>R$ ${formatPrice(getCartTotal())}</span>
                    </div>
                    
                    <button class="primary-button" style="width:100%;" onclick="window.app.submitOrder()">
                        Confirmar Agendamento
                    </button>
                </div>
                
            </div>
        </div>
    `;
}
