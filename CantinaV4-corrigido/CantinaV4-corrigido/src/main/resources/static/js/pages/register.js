import { api } from '../api.js';

export function renderRegister() {
    return `
        <div class="container" style="display:flex; justify-content:center; align-items:center; min-height:80vh; padding: 20px 0;">
            <div style="background:white; padding:40px; border-radius:20px; box-shadow:0 10px 40px rgba(0,0,0,0.08); width:100%; max-width:500px; text-align:center;">
                <div style="font-size:40px; margin-bottom:10px;">🍔</div>
                <h1 style="font-size:24px; color:#111827; margin-bottom:10px;">Crie sua conta</h1>
                <p style="color:#6b7280; margin-bottom:30px;">Preencha os dados abaixo para se cadastrar</p>
                
                <form id="register-form" onsubmit="window.app.handleRegister(event)">
                    
                    <div style="text-align:left; margin-bottom:15px;">
                        <label style="display:block; font-weight:600; color:#374151; margin-bottom:8px;">Nome Completo</label>
                        <input type="text" id="reg-nome" placeholder="João da Silva" required minlength="3"
                               style="width:100%; padding:12px; border:1px solid #e5e7eb; border-radius:10px; font-size:16px;">
                    </div>

                    <div style="display:flex; gap:15px; margin-bottom:15px;">
                        <div style="text-align:left; flex:1;">
                            <label style="display:block; font-weight:600; color:#374151; margin-bottom:8px;">CPF</label>
                            <input type="text" id="reg-cpf" placeholder="000.000.000-00" required
                                   style="width:100%; padding:12px; border:1px solid #e5e7eb; border-radius:10px; font-size:16px;">
                        </div>
                        <div style="text-align:left; flex:1;">
                            <label style="display:block; font-weight:600; color:#374151; margin-bottom:8px;">Telefone</label>
                            <input type="text" id="reg-telefone" placeholder="11999999999" required
                                   style="width:100%; padding:12px; border:1px solid #e5e7eb; border-radius:10px; font-size:16px;">
                        </div>
                    </div>

                    <div style="text-align:left; margin-bottom:15px;">
                        <label style="display:block; font-weight:600; color:#374151; margin-bottom:8px;">Email</label>
                        <input type="email" id="reg-email" placeholder="seu@email.com" required
                               style="width:100%; padding:12px; border:1px solid #e5e7eb; border-radius:10px; font-size:16px;">
                    </div>
                    
                    <div style="text-align:left; margin-bottom:15px;">
                        <label style="display:block; font-weight:600; color:#374151; margin-bottom:8px;">Perfil</label>
                        <select id="reg-perfil" required style="width:100%; padding:12px; border:1px solid #e5e7eb; border-radius:10px; font-size:16px; background:white;">
                            <option value="Aluno">Aluno</option>
                            <option value="Professor">Professor</option>
                            <option value="Secretaria">Secretaria</option>
                        </select>
                    </div>
                    
                    <div style="text-align:left; margin-bottom:25px;">
                        <label style="display:block; font-weight:600; color:#374151; margin-bottom:8px;">Senha</label>
                        <input type="password" id="reg-senha" placeholder="Mínimo 8 caracteres" required minlength="8"
                               style="width:100%; padding:12px; border:1px solid #e5e7eb; border-radius:10px; font-size:16px;">
                        <small style="color:#6b7280; font-size:12px; display:block; margin-top:5px;">Deve ter letra maiúscula, minúscula e número.</small>
                    </div>
                    
                    <button type="submit" class="primary-button" style="width:100%; padding:14px; font-size:16px;">
                        Cadastrar
                    </button>
                </form>
                
                <p style="margin-top:25px; color:#6b7280; font-size:14px;">
                    Já tem uma conta? <a href="#" onclick="window.app.navigate('login'); return false;" style="color:var(--primary); font-weight:600; text-decoration:none;">Faça Login</a>
                </p>
            </div>
        </div>
    `;
}

export async function handleRegisterSubmit(event) {
    event.preventDefault();
    
    // Pega os valores e remove máscaras (CPF e Telefone apenas números)
    const data = {
        nome: document.getElementById('reg-nome').value.trim(),
        cpf: document.getElementById('reg-cpf').value.replace(/\D/g, ''),
        email: document.getElementById('reg-email').value.trim(),
        telefone: document.getElementById('reg-telefone').value.replace(/\D/g, ''),
        perfil: document.getElementById('reg-perfil').value,
        senha: document.getElementById('reg-senha').value
    };
    
    try {
        await api.cadastrarUsuario(data);
        window.app.showToast('Cadastro realizado com sucesso! Faça o login.', 'success');
        window.app.navigate('login');
    } catch (e) {
        // Erro já tratado pelo api.js
    }
}
