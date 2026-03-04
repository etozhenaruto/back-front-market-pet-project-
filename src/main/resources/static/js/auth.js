/**
 * Модуль аутентификации и управления сессией.
 */

const AUTH_STORAGE_KEY = 'beerTestShopUser';

/**
 * Сохранить информацию о пользователе.
 */
function saveUser(user) {
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(user));
}

/**
 * Получить текущего пользователя.
 */
function getCurrentUserFromStorage() {
    const user = localStorage.getItem(AUTH_STORAGE_KEY);
    return user ? JSON.parse(user) : null;
}

/**
 * Очистить информацию о пользователе.
 */
function clearUser() {
    localStorage.removeItem(AUTH_STORAGE_KEY);
}

/**
 * Проверить, авторизован ли пользователь.
 */
function isAuthenticated() {
    return getCurrentUserFromStorage() !== null;
}

/**
 * Проверить, является ли пользователь администратором.
 */
function isAdmin() {
    const user = getCurrentUserFromStorage();
    return user && user.role === 'ADMIN';
}

/**
 * Обновить меню навигации на всех страницах.
 */
function updateNavigation() {
    const user = getCurrentUserFromStorage();
    const navContainer = document.getElementById('nav-auth-container');
    
    if (!navContainer) return;
    
    if (user) {
        navContainer.innerHTML = `
            <span class="navbar-text me-3" data-testid="user-greeting">
                Привет, <strong>${escapeHtml(user.username)}</strong> (${user.role})
            </span>
            <button class="btn btn-outline-danger btn-sm" data-testid="btn-logout" onclick="handleLogout()">
                Выйти
            </button>
        `;
        
        // Показать админ-ссылку если админ
        const adminLink = document.getElementById('admin-link');
        if (adminLink) {
            adminLink.style.display = user.role === 'ADMIN' ? 'block' : 'none';
        }
    } else {
        navContainer.innerHTML = `
            <a class="btn btn-outline-primary btn-sm" href="/login.html" data-testid="btn-login-nav">
                Войти
            </a>
        `;
    }
}

/**
 * Обработчик выхода.
 */
async function handleLogout() {
    try {
        await logout();
    } catch (error) {
        console.error('Logout error:', error);
    }
    
    clearUser();
    window.location.href = '/';
}

/**
 * Требовать аутентификацию. Перенаправить на логин если не авторизован.
 */
function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.pathname);
        return false;
    }
    return true;
}

/**
 * Требовать роль администратора.
 */
function requireAdmin() {
    if (!isAdmin()) {
        alert('Доступ запрещён. Требуется роль администратора.');
        window.location.href = '/';
        return false;
    }
    return true;
}

/**
 * Экранирование HTML для защиты от XSS.
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Инициализация навигации при загрузке страницы.
 */
document.addEventListener('DOMContentLoaded', function() {
    updateNavigation();
});
