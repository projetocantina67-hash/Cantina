import { api } from '../api.js';
import { setUser } from '../auth.js';

export function renderLogin() {
    return `
        <div class="container" style="display:flex; justify-content:center; align-items:center; min-height:60vh;">
            <div style="background:white; padding:40px; border-radius:20px; box-shadow:0 10px 40px rgba(0,0,0,0.08); width:100%; max-width:400px; text-align:center; margin-bottom: 79px;">
                <div style="font-size:40px; margin-bottom:10px;">🍔</div>
                <h1 style="font-size:24px; color:#111827; margin-bottom:10px;">Bem-vindo(a) de volta!</h1>
                <p style="color:#6b7280; margin-bottom:30px;">Faça login para continuar</p>
                
                <form id="login-form" onsubmit="window.app.handleLogin(event)">
                    <div style="text-align:left; margin-bottom:20px;">
                        <label style="display:block; font-weight:600; color:#374151; margin-bottom:8px;">CPF</label>
                        <input type="text" id="login-cpf" placeholder="000.000.000-00" required
                               style="width:100%; padding:14px; border:1px solid #e5e7eb; border-radius:10px; font-size:16px;">
                    </div>
                    
                    <div style="text-align:left; margin-bottom:30px;">
                        <label style="display:block; font-weight:600; color:#374151; margin-bottom:8px;">Senha</label>
                        <input type="password" id="login-password" placeholder="Sua senha" required
                               style="width:100%; padding:14px; border:1px solid #e5e7eb; border-radius:10px; font-size:16px;">
                    </div>
                    
                    <button type="submit" class="primary-button" style="width:100%; padding:14px; font-size:16px;">
                        Entrar
                    </button>
                </form>
                
                <p style="margin-top:25px; color:#6b7280; font-size:14px;">
                    Ainda não tem conta? <a href="#" onclick="window.app.navigate('register'); return false;" style="color:var(--primary); font-weight:600; text-decoration:none;">Cadastre-se</a>
                </p>
            </div>
        </div>
    `;
}

export async function handleLoginSubmit(event) {
    event.preventDefault();
    const cpf = document.getElementById('login-cpf').value;
    const senha = document.getElementById('login-password').value;
    
    try {
        const response = await api.login(cpf, senha);
        // O response de login retorna algo como { token, usuarioId, nome, perfil }
        if (response && response.token) {
            setUser({
                id: response.usuarioId,
                nome: response.nome,
                perfil: response.perfil,
                token: response.token
            });
            window.app.showToast(`Olá, ${response.nome}!`, 'success');
            
            // Redireciona dependendo do perfil
            if (response.perfil === 'Funcionario da cantina') {
                window.app.navigate('admin');
            } else {
                window.app.navigate('home');
            }
        }
    } catch (e) {
        // Erro já é exibido no toast pelo api.js
    }
}
