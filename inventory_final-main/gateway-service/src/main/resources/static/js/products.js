let productModal;

document.addEventListener('DOMContentLoaded', function() {
    productModal = new bootstrap.Modal(document.getElementById('productModal'));
    loadProducts();
});

async function loadProducts() {
    try {
        const response = await fetch('/api/products');
        const products = await response.json();
        
        const tbody = document.getElementById('productTableBody');
        tbody.innerHTML = '';
        
        products.forEach(product => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${product.id}</td>
                <td>${product.name}</td>
                <td>${product.description || ''}</td>
                <td>${product.price.toLocaleString()}원</td>
                <td>${product.stock}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="editProduct(${product.id})">수정</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteProduct(${product.id})">삭제</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error('상품 목록을 불러오는데 실패했습니다:', error);
        alert('상품 목록을 불러오는데 실패했습니다.');
    }
}

function showAddProductModal() {
    document.getElementById('modalTitle').textContent = '상품 추가';
    document.getElementById('productForm').reset();
    document.getElementById('productId').value = '';
    productModal.show();
}

async function editProduct(id) {
    try {
        const response = await fetch(`/api/products/${id}`);
        const product = await response.json();
        
        document.getElementById('modalTitle').textContent = '상품 수정';
        document.getElementById('productId').value = product.id;
        document.getElementById('productName').value = product.name;
        document.getElementById('productDescription').value = product.description || '';
        document.getElementById('productPrice').value = product.price;
        document.getElementById('productStock').value = product.stock;
        
        productModal.show();
    } catch (error) {
        console.error('상품 정보를 불러오는데 실패했습니다:', error);
        alert('상품 정보를 불러오는데 실패했습니다.');
    }
}

async function saveProduct() {
    const id = document.getElementById('productId').value;
    const product = {
        name: document.getElementById('productName').value,
        description: document.getElementById('productDescription').value,
        price: parseFloat(document.getElementById('productPrice').value),
        stock: parseInt(document.getElementById('productStock').value)
    };

    try {
        const url = id ? `/api/products/${id}` : '/api/products';
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(product)
        });

        if (!response.ok) {
            throw new Error('저장에 실패했습니다.');
        }

        productModal.hide();
        loadProducts();
        alert('저장되었습니다.');
    } catch (error) {
        console.error('저장에 실패했습니다:', error);
        alert('저장에 실패했습니다.');
    }
}

async function deleteProduct(id) {
    if (!confirm('정말 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch(`/api/products/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('삭제에 실패했습니다.');
        }

        loadProducts();
        alert('삭제되었습니다.');
    } catch (error) {
        console.error('삭제에 실패했습니다:', error);
        alert('삭제에 실패했습니다.');
    }
} 