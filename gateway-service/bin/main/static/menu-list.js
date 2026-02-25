// static/menu-list.js (수정 완료)

let menuDetailModalInstance = null;
let currentMenuBasePrice = 0;
// 🌟🌟🌟 전역 optionsContainer 제거 (주석 처리 또는 삭제)
// const optionsContainer = document.getElementById('optionsContainer');


// ==========================================================
// 1. 유틸리티 함수
// ==========================================================

function formatNumber(number) {
    if (typeof number !== 'number' || isNaN(number)) {
        return '0';
    }
    return number.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

/**
 * 서버에서 받은 옵션 데이터를 HTML로 변환하여 컨테이너에 삽입합니다.
 * @param {Object} optionsByGroup - { 그룹명: [OptionDto, ...] } 형태의 객체
 */
function renderOptions(optionsByGroup) {
    // 🌟🌟🌟 함수 내부에서 요소를 다시 찾습니다. 🌟🌟🌟
    const container = document.getElementById('optionsContainer');
    if (!container) return; // 요소가 없으면 종료

    container.innerHTML = ''; // 기존 옵션 제거

    for (const groupName in optionsByGroup) {
        if (optionsByGroup.hasOwnProperty(groupName)) {
            const options = optionsByGroup[groupName];

            // 옵션 그룹 헤더
            let html = `<div class="mb-4">
                            <h6 class="fw-bold border-bottom pb-1">${groupName}</h6>`;

            // 개별 옵션 루프
            options.forEach(option => {
                // 1. 가격 데이터를 숫자로 확실히 변환
                const optionPrice = Number(option.optionPrice) || 0;

                // 2. 가격 표시 문자열 생성 로직 수정: optionPrice를 직접 사용
                let priceText;

                if (optionPrice > 0) {
                    // 가격이 0원 초과일 경우: +600원 형태로 표시
                    priceText = `+${formatNumber(optionPrice)}원`;
                } else if (option.optionName === '텀블러 이용' || option.optionName === '시럽 제외') {
                    // 텀블러 이용, 시럽 제외 등 0원인데 표시해야 하는 경우
                    priceText = '(0원)';
                } else {
                    // 그 외 (안전 장치)
                    priceText = '(0원)';
                }

                html += `<div class="form-check ps-0">
                            <input class="form-check-input option-input" 
                                   type="checkbox" 
                                   name="${groupName}" 
                                   id="option_${option.optionId}" 
                                   value="${option.optionId}"
                                   data-price-delta="${optionPrice}"  
                                   onchange="updateTotalPriceDisplay()">
                            <label class="form-check-label w-100 d-flex justify-content-between align-items-center" 
                                   for="option_${option.optionId}">
                                <span>${option.optionName}</span>
                                <span class="text-success fw-bold">${priceText}</span> 
                            </label>
                        </div>`;
            });

            html += `</div>`;
            container.insertAdjacentHTML('beforeend', html);
        }
    }
}


// ==========================================================
// 2. 모달 제어 및 AJAX 함수
// ==========================================================

async function openMenuDetailModal(element) {

    const menuCode = element.getAttribute('data-menu-code');
    const menuName = element.querySelector('.card-title').innerText;
    const basePriceText = element.querySelector('.card-text').innerText;

    currentMenuBasePrice = parseInt(basePriceText.replace(/[^0-9]/g, ''));

    if (!menuDetailModalInstance) {
        // 모달 인스턴스 초기화
        menuDetailModalInstance = new bootstrap.Modal(document.getElementById('menuDetailModal'));
    }

    // 1. 모달 기본 정보 채우기
    document.getElementById('modalMenuImage').src = `/images/${menuCode}.jpg`;
    document.getElementById('modalMenuName').innerText = menuName;
    document.getElementById('modalBasePrice').innerText = basePriceText;
    document.getElementById('modalQuantity').value = 1;
    document.getElementById('modalTotalPrice').setAttribute('data-base-price', currentMenuBasePrice);

    // 2. AJAX 요청으로 해당 메뉴의 옵션 그룹 데이터 로드
    try {
        const response = await fetch(`/api/menu/${menuCode}/options`);
        if (!response.ok) {
            // 4xx, 5xx 에러 처리
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const optionsData = await response.json();

        // 3. 옵션 동적 렌더링
        renderOptions(optionsData);

    } catch (error) {
        console.error("옵션 로드 중 오류 발생:", error);
        // 🌟🌟🌟 함수 내부에서 요소를 다시 찾습니다. 🌟🌟🌟
        const container = document.getElementById('optionsContainer');
        if (container) {
            container.innerHTML = `<p class="text-danger">옵션 정보를 불러오는 데 실패했습니다. (${error.message})</p>`;
        }
    }

    // 4. 모달 표시
    updateTotalPriceDisplay();
    menuDetailModalInstance.show();
}

/**
 * 수량 변경 버튼 ( + / - ) 클릭 핸들러
 */
function changeQuantity(delta) {
    const quantityInput = document.getElementById('modalQuantity');
    let quantity = parseInt(quantityInput.value);
    quantity = Math.max(1, quantity + delta);
    quantityInput.value = quantity;
    updateTotalPriceDisplay();
}

/**
 * 옵션 선택, 수량 변경 시 총 주문 금액을 계산하고 표시합니다.
 */
function updateTotalPriceDisplay() {
    let totalPrice = currentMenuBasePrice;

    document.querySelectorAll('.option-input:checked').forEach(checkbox => {
        // data-price-delta에 저장된 옵션 가격을 가져와 합산
        const priceDelta = parseInt(checkbox.getAttribute('data-price-delta')) || 0;
        totalPrice += priceDelta;
    });

    const quantity = parseInt(document.getElementById('modalQuantity').value) || 1;
    totalPrice *= quantity;

    document.getElementById('modalTotalPrice').innerText = formatNumber(totalPrice) + '원';
}


// ==========================================================
// 3. 주문/장바구니 핸들러
// ==========================================================

function addToCart() {
    alert("장바구니에 추가되었습니다! (총 금액: " + document.getElementById('modalTotalPrice').innerText + ")");
}

function placeOrder() {
    alert("바로 주문 요청! (총 금액: " + document.getElementById('modalTotalPrice').innerText + ")");
}