// API 설정
const API_CONFIG = {
    // 상대 경로 사용 (모든 환경에서 동작)
    baseURL: '',  // 상대 경로 사용

    // API 엔드포인트
    endpoints: {
        // Auth
        login: '/api/auth/login',
        register: '/api/auth/register',
        logout: '/api/auth/logout',
        findUserId: '/api/auth/find-userid',
        findPassword: '/api/auth/find-password',
        emailSend: '/api/auth/email/send-code',
        emailVerify: '/api/auth/email/verify-code',

        // User
        userProfile: '/api/users/profile',

        // Admin - Users
        adminUsers: '/api/admin/users',

        // Admin - Notices
        adminNotices: '/api/admin/notices',

        // Admin - Inquiries
        adminInquiries: '/api/admin/inquiries',

        // Admin - Products
        adminProducts: '/api/admin/products',

        // Boards
        boards: '/api/boards',

        // Comments
        comments: '/api/comments',

        // Products
        products: '/api/products',

        // Orders
        orders: '/api/orders'
    }
};

// API 호출 헬퍼 함수
function getApiUrl(endpoint, params = {}) {
    let url = API_CONFIG.baseURL + endpoint;

    // URL 파라미터 추가
    const queryString = Object.keys(params)
        .filter(key => params[key] !== null && params[key] !== undefined)
        .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
        .join('&');

    if (queryString) {
        url += (url.includes('?') ? '&' : '?') + queryString;
    }

    return url;
}

// Authorization 헤더 가져오기
function getAuthHeader() {
    const token = localStorage.getItem('accessToken');
    return token ? `Bearer ${token}` : null;
}

// 인증이 필요한 fetch 요청
function authenticatedFetch(url, options = {}) {
    const authHeader = getAuthHeader();

    if (!authHeader) {
        console.error('인증 토큰이 없습니다.');
        window.location.href = '/login';
        return Promise.reject(new Error('인증이 필요합니다.'));
    }

    const headers = {
        'Content-Type': 'application/json',
        'Authorization': authHeader,
        ...(options.headers || {})
    };

    return fetch(url, {
        ...options,
        headers: headers
    });
}
