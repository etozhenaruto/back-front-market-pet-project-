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

        if (!response.ok) {
            const contentType = response.headers.get('content-type');
            let errorMessage = response.statusText;
            
            if (contentType && contentType.includes('application/json')) {
                try {
                    const errorData = await response.json();
                    errorMessage = errorData.message || errorMessage;
                } catch (e) {
                    errorMessage = await response.text() || errorMessage;
                }
            } else {
                errorMessage = await response.text() || errorMessage;
            }
            
            console.error('API Error:', response.status, errorMessage);
            throw new Error(errorMessage);
        }

        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }

        return [];
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
