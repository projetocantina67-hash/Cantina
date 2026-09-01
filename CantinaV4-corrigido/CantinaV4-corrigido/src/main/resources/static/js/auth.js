export function getUser() {
    return JSON.parse(localStorage.getItem('cantina_user'));
}

export function setUser(user) {
    if (user) localStorage.setItem('cantina_user', JSON.stringify(user));
    else localStorage.removeItem('cantina_user');
    if (window.app && typeof window.app.updateHeaderUI === 'function') {
        window.app.updateHeaderUI();
    }
}

export function logout() {
    setUser(null);
    window.app.showToast('Você saiu da conta', 'success');
}