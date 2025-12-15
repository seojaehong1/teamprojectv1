let orderModal;

document.addEventListener('DOMContentLoaded', function() {
    orderModal = new bootstrap.Modal(document.getElementById('orderModal'));
    loadOrders();
});

async function loadOrders() {
    try {
        const response = await fetch('/api/orders');
        const orders = await response.json();
        
        const tbody = document.getElementById('orderTableBody');
        tbody.innerHTML = '';
        
        orders.forEach(order => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${order.id}</td>
                <td>${order.productId}</td>
                <td>${order.quantity}</td>
                <td>${order.totalPrice.toLocaleString()}원</td>
                <td>${order.customerName}</td>
                <td>${order.customerEmail}</td>
                <td>${new Date(order.orderDate).toLocaleString()}</td>
                <td>${getStatusText(order.status)}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="editOrder(${order.id})">수정</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteOrder(${order.id})">삭제</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error('주문 목록을 불러오는데 실패했습니다:', error);
        alert('주문 목록을 불러오는데 실패했습니다.');
    }
}

function getStatusText(status) {
    const statusMap = {
        'PENDING': '대기',
        'PROCESSING': '처리중',
        'COMPLETED': '완료'
    };
    return statusMap[status] || status;
}

function showAddOrderModal() {
    document.getElementById('modalTitle').textContent = '주문 추가';
    document.getElementById('orderForm').reset();
    document.getElementById('orderId').value = '';
    orderModal.show();
}

async function editOrder(id) {
    try {
        const response = await fetch(`/api/orders/${id}`);
        const order = await response.json();
        
        document.getElementById('modalTitle').textContent = '주문 수정';
        document.getElementById('orderId').value = order.id;
        document.getElementById('productId').value = order.productId;
        document.getElementById('quantity').value = order.quantity;
        document.getElementById('totalPrice').value = order.totalPrice;
        document.getElementById('customerName').value = order.customerName;
        document.getElementById('customerEmail').value = order.customerEmail;
        document.getElementById('status').value = order.status;
        
        orderModal.show();
    } catch (error) {
        console.error('주문 정보를 불러오는데 실패했습니다:', error);
        alert('주문 정보를 불러오는데 실패했습니다.');
    }
}

async function saveOrder() {
    const id = document.getElementById('orderId').value;
    const order = {
        productId: parseInt(document.getElementById('productId').value),
        quantity: parseInt(document.getElementById('quantity').value),
        totalPrice: parseFloat(document.getElementById('totalPrice').value),
        customerName: document.getElementById('customerName').value,
        customerEmail: document.getElementById('customerEmail').value,
        status: document.getElementById('status').value
    };

    try {
        const url = id ? `/api/orders/${id}` : '/api/orders';
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(order)
        });

        if (!response.ok) {
            throw new Error('저장에 실패했습니다.');
        }

        orderModal.hide();
        loadOrders();
        alert('저장되었습니다.');
    } catch (error) {
        console.error('저장에 실패했습니다:', error);
        alert('저장에 실패했습니다.');
    }
}

async function deleteOrder(id) {
    if (!confirm('정말 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch(`/api/orders/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('삭제에 실패했습니다.');
        }

        loadOrders();
        alert('삭제되었습니다.');
    } catch (error) {
        console.error('삭제에 실패했습니다:', error);
        alert('삭제에 실패했습니다.');
    }
} 