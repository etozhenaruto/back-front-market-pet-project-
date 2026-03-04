/**
 * API модуль для взаимодействия с бэкендом.
 */
const API_BASE_URL = '/api/v1';

/**
 * Выполнить fetch-запрос с обработкой ошибок.
 */
async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const defaultHeaders = {
        'Content-Type': 'application/json',
    };

    const config = {
        ...options,
        headers: {
            ...defaultHeaders,
            ...(options.headers || {}),
        },
    };

    try {
        const response = await fetch(url, config);
        
        // Обработка 401 - неавторизован
        if (response.status === 401) {
            window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.pathname);
            return null;
        }
        
        // Обработка 403 - доступ запрещён
        if (response.status === 403) {
            alert('Доступ запрещён. Недостаточно прав для выполнения операции.');
            return null;
        }
        
        // Пустой ответ (204 No Content)
        if (response.status === 204) {
            return null;
        }
        
        const data = await response.json();
        
        if (!response.ok) {
            console.error('API Error:', data);
            throw new Error(data.message || 'Ошибка при выполнении запроса');
        }
        
        return data;
    } catch (error) {
        console.error('Request failed:', error);
        throw error;
    }
}

// ==================== Products API ====================

async function getProducts() {
    return apiRequest('/products');
}

async function getProductById(id) {
    return apiRequest(`/products/${id}`);
}

async function updateProduct(id, productData) {
    return apiRequest(`/products/${id}`, {
        method: 'PUT',
        body: JSON.stringify(productData),
    });
}

// ==================== Cart API ====================

async function getCart() {
    return apiRequest('/cart');
}

async function addToCart(productId, quantity) {
    return apiRequest('/cart/add', {
        method: 'POST',
        body: JSON.stringify({ productId, quantity }),
    });
}

async function removeFromCart(productId) {
    return apiRequest(`/cart/remove?productId=${productId}`, {
        method: 'DELETE',
    });
}

async function clearCart() {
    return apiRequest('/cart/clear', {
        method: 'DELETE',
    });
}

// ==================== Feedback API ====================

async function getFeedback() {
    return apiRequest('/feedback');
}

async function createFeedback(feedbackData) {
    return apiRequest('/feedback', {
        method: 'POST',
        body: JSON.stringify(feedbackData),
    });
}

async function updateFeedback(id, feedbackData) {
    return apiRequest(`/feedback/${id}`, {
        method: 'PUT',
        body: JSON.stringify(feedbackData),
    });
}

async function deleteFeedback(id) {
    return apiRequest(`/feedback/${id}`, {
        method: 'DELETE',
    });
}

// ==================== Admin API ====================

async function adminGetProducts() {
    return apiRequest('/admin/products');
}

async function adminCreateProduct(productData) {
    return apiRequest('/admin/products', {
        method: 'POST',
        body: JSON.stringify(productData),
    });
}

async function adminUpdateProduct(id, productData) {
    return apiRequest(`/admin/products/${id}`, {
        method: 'PUT',
        body: JSON.stringify(productData),
    });
}

async function adminDeleteProduct(id) {
    return apiRequest(`/admin/products/${id}`, {
        method: 'DELETE',
    });
}

async function adminGetFeedback() {
    return apiRequest('/admin/feedback');
}

async function adminDeleteFeedback(id) {
    return apiRequest(`/admin/feedback/${id}`, {
        method: 'DELETE',
    });
}

// ==================== Auth API ====================

async function login(username, password) {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
    });
    
    if (response.status === 401) {
        const data = await response.json();
        throw new Error(data.message || 'Неверный логин или пароль');
    }
    
    if (!response.ok) {
        throw new Error('Ошибка аутентификации');
    }
    
    return response.json();
}

async function logout() {
    return apiRequest('/auth/logout', {
        method: 'POST',
    });
}

async function getCurrentUser() {
    try {
        return await apiRequest('/auth/me');
    } catch (error) {
        return null;
    }
}
