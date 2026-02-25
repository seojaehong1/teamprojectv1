let productModal;

document.addEventListener('DOMContentLoaded', function() {
    productModal = new bootstrap.Modal(document.getElementById('productModal'));
    loadProducts();
});

async function loadProducts() {
    try {
        const response = await fetch('/api/products');
        
        if (!response.ok) {
            console.error('상품 목록을 불러오는데 실패했습니다. Status:', response.status);
            const errorText = await response.text();
            console.error('Error response:', errorText);
            alert('상품 목록을 불러오는데 실패했습니다. (Status: ' + response.status + ')');
            return;
        }
        
        const products = await response.json();
        
        // products가 배열인지 확인
        if (!Array.isArray(products)) {
            console.error('상품 목록이 배열 형식이 아닙니다:', products);
            alert('상품 목록 형식이 올바르지 않습니다.');
            return;
        }
        
        const tbody = document.getElementById('productTableBody');
        tbody.innerHTML = '';
        
        if (products.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center">등록된 상품이 없습니다.</td></tr>';
            return;
        }
        
        products.forEach(product => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${product.id}</td>
                <td>${product.name}</td>
                <td>${product.description || ''}</td>
                <td>${(product.price || 0).toLocaleString()}원</td>
                <td>${product.stock || 0}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="editProduct('${product.id}')">수정</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteProduct('${product.id}')">삭제</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error('상품 목록을 불러오는데 실패했습니다:', error);
        alert('상품 목록을 불러오는데 실패했습니다: ' + error.message);
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
        
        if (!response.ok) {
            console.error('상품 정보를 불러오는데 실패했습니다. Status:', response.status);
            alert('상품 정보를 불러오는데 실패했습니다. (Status: ' + response.status + ')');
            return;
        }
        
        const product = await response.json();
        
        document.getElementById('modalTitle').textContent = '상품 수정';
        document.getElementById('productId').value = product.id;
        document.getElementById('productName').value = product.name;
        document.getElementById('productDescription').value = product.description || '';
        document.getElementById('productPrice').value = product.price;
        document.getElementById('productStock').value = product.stock || 0;
        
        productModal.show();
    } catch (error) {
        console.error('상품 정보를 불러오는데 실패했습니다:', error);
        alert('상품 정보를 불러오는데 실패했습니다: ' + error.message);
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
            console.error('삭제에 실패했습니다. Status:', response.status);
            const errorText = await response.text();
            console.error('Error response:', errorText);
            alert('삭제에 실패했습니다. (Status: ' + response.status + ')');
            return;
        }

        loadProducts();
        alert('삭제되었습니다.');
    } catch (error) {
        console.error('삭제에 실패했습니다:', error);
        alert('삭제에 실패했습니다: ' + error.message);
    }
} 