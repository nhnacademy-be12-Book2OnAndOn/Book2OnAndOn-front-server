// ============================
// 기본 설정
// ============================

// 쿠키 읽기 유틸
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop().split(';').shift();
    }
    return null;
}

const userId = localStorage.getItem('userId');

// accessToken 기준으로 회원/비회원 판단
const accessToken = getCookie('accessToken');
let uuid = localStorage.getItem('uuid');

const isGuest = !accessToken;
const API_BASE = '/cart';
const USE_DUMMY = false;

// 더미 아이템 (개발용)
const DUMMY_ITEMS = [
    {
        bookId: 1,
        title: '샘플 도서',
        thumbnailUrl: '',
        originalPrice: 15000,
        price: 15000,
        stockCount: 3,
        saleEnded: false,
        deleted: false,
        hidden: false,
        quantity: 1,
        selected: true
    }
];

let cartItems = USE_DUMMY ? [...DUMMY_ITEMS] : [];

// 서버 응답 전체를 담아둘 상태 (배송비, 최종 결제금액 포함)
let cartSummaryData = null;

// ============================
// 공통 헤더 생성 유틸
// ============================

function buildAuthHeaders(baseHeaders = {}) {
    const headers = {...baseHeaders};

    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    } else {
        headers['X-Guest-Id'] = uuid;
    }
    return headers;
}


// ============================
// 서버에서 장바구니 조회
// ============================

async function loadCartFromServer() {
    try {
        let url;
        const baseHeaders = {
            'Content-Type': 'application/json',
        };

        if (accessToken) {
            // 회원 장바구니 조회: GET /cart/user
            url = `${API_BASE}/user`;
        } else {
            url = `${API_BASE}/guest`;
        }

        const headers = buildAuthHeaders(baseHeaders);

        const response = await fetch(url, {
            method: 'GET',
            headers
        });

        if (!response.ok) {
            console.error('장바구니 조회 실패', response.status);
            return;
        }

        const data = await response.json(); // CartItemsResponseDto 구조
        cartSummaryData = data;
        cartItems = data.items || [];

        renderCart();
    } catch (e) {
        console.error('장바구니 조회 중 오류', e);
    }
}

// ============================
// 렌더링
// ============================

function renderCart() {
    const cartContent = document.getElementById('cartContent');
    const cartSummary = document.getElementById('cartSummary');

    if (!cartItems || cartItems.length === 0) {
        cartContent.innerHTML = `
      <div class="empty-cart">
        <!-- TODO: 장바구니 아이콘 변경 -->
        <div class="empty-cart-icon">🛒</div>
        <h2>장바구니가 비어있습니다</h2>
        <p>원하는 책을 담아보세요!</p>
      </div>
    `;
        cartSummary.style.display = 'none';
        return;
    }

    cartSummary.style.display = 'block';

    cartContent.innerHTML = `
    <div class="cart-items">
      ${cartItems.map(item => {
        const isUnavailable = item.deleted || item.hidden || item.saleEnded;
        const isOutOfStock = item.stockCount === 0;
        const isLowStock = item.stockCount > 0 && item.stockCount <= 5;
        const hasDiscount = item.originalPrice > item.price;
        const discountRate = hasDiscount ? Math.round((1 - item.price / item.originalPrice) * 100) : 0;

        return `
          <div class="cart-item ${isUnavailable ? 'item-unavailable-overlay' : ''}">
            <div class="item-checkbox">
              <input type="checkbox"
                ${item.selected ? 'checked' : ''}
                ${isUnavailable || isOutOfStock ? 'disabled' : ''}
                onchange="toggleItem(${item.bookId})">
            </div>
            <div class="item-image">
              ${item.thumbnailUrl
            ? `<img src="${item.thumbnailUrl}" alt="${item.title}">`
            : '책 이미지'}
            </div>
            <div class="item-details">
              <div class="item-title">${item.title}</div>
              <div class="item-meta">
                ${isOutOfStock
            ? '<span class="item-badge badge-stock out">품절</span>'
            : isLowStock
                ? `<span class="item-badge badge-stock low">재고 ${item.stockCount}개</span>`
                : `<span class="item-badge badge-stock">재고 ${item.stockCount}개</span>`
        }
                ${item.saleEnded ? '<span class="item-badge badge-sale">판매종료</span>' : ''}
                ${item.deleted ? '<span class="item-badge badge-unavailable">삭제된 상품</span>' : ''}
                ${item.hidden ? '<span class="item-badge badge-unavailable">숨김 상품</span>' : ''}
                ${hasDiscount && !isUnavailable ? `<span class="item-badge badge-discount">${discountRate}% 할인</span>` : ''}
              </div>
              <div class="item-price-section">
                ${hasDiscount ? `<span class="item-original-price">${item.originalPrice.toLocaleString()}원</span>` : ''}
                <span class="item-price">${item.price.toLocaleString()}원</span>
                ${hasDiscount ? `<span class="item-discount-rate">${discountRate}%↓</span>` : ''}
              </div>
            </div>
            <div class="item-controls">
              <div class="quantity-control">
                <button class="quantity-btn"
                  onclick="updateQuantity(${item.bookId}, ${item.quantity - 1})"
                  ${isUnavailable || isOutOfStock ? 'disabled' : ''}>-</button>
                <div class="quantity-display">${item.quantity}</div>
                <button class="quantity-btn"
                  onclick="updateQuantity(${item.bookId}, ${item.quantity + 1})"
                  ${isUnavailable || isOutOfStock || item.quantity >= item.stockCount ? 'disabled' : ''}>+</button>
              </div>
              <div class="item-total">${(item.price * item.quantity).toLocaleString()}원</div>
              <button class="btn-remove" onclick="removeItem(${item.bookId})">삭제</button>
            </div>
          </div>
        `;
    }).join('')}
    </div>
  `;

    updateSummary();
    updateSelectAllCheckbox();
}

function updateSummary() {
    const subtotalElem = document.getElementById('subtotal');
    const shippingElem = document.getElementById('shipping');
    const totalElem = document.getElementById('total');

    // 실제 서버 데이터 기반
    if (!cartSummaryData) {
        subtotalElem.textContent = '0원';
        shippingElem.textContent = '0원';
        totalElem.textContent = '0원';
        return;
    }

    const selectedTotalPrice = cartSummaryData.selectedTotalPrice || 0;
    const deliveryFee = cartSummaryData.deliveryFee || 0;
    const finalPaymentAmount = cartSummaryData.finalPaymentAmount || 0;

    subtotalElem.textContent = selectedTotalPrice.toLocaleString() + '원';
    shippingElem.textContent =
        deliveryFee === 0 ? '무료' : deliveryFee.toLocaleString() + '원';
    totalElem.textContent = finalPaymentAmount.toLocaleString() + '원';
}

function updateSelectAllCheckbox() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const availableItems = cartItems.filter(item =>
        !item.deleted && !item.hidden && !item.saleEnded && item.stockCount > 0
    );
    const allSelected = availableItems.length > 0 && availableItems.every(item => item.selected);
    selectAllCheckbox.checked = allSelected;
}

// ============================
// 액션
// ============================

async function toggleSelectAll() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const selectAll = selectAllCheckbox.checked;

    // 실제 API 모드
    try {
        let url;
        const body = JSON.stringify({selected: selectAll});
        const baseHeaders = {
            'Content-Type': 'application/json'
        };

        if (accessToken) {
            // 회원: PATCH /cart/user/items/select-all
            url = `${API_BASE}/user/items/select-all`;
        } else {
            // 비회원: PATCH /cart/guest/items/select-all
            url = `${API_BASE}/guest/items/select-all`;
        }

        const headers = buildAuthHeaders(baseHeaders);

        const res = await fetch(url, {
            method: 'PATCH',
            headers,
            body
        });

        if (!res.ok) {
            console.error('전체 선택/해제 실패', res.status);
            alert('전체 선택/해제 중 문제가 발생했습니다.');
            return;
        }

        await loadCartFromServer();
    } catch (e) {
        console.error('전체 선택/해제 중 오류', e);
        alert('전체 선택/해제 중 오류가 발생했습니다.');
    }
}

async function toggleItem(bookId) {
    const item = cartItems.find(i => i.bookId === bookId);
    if (!item) return;

    // 실제 API 모드
    try {
        let url;
        const nextSelected = !item.selected;

        const body = JSON.stringify({
            bookId: bookId,
            selected: nextSelected
        });

        const baseHeaders = {
            'Content-Type': 'application/json'
        };

        if (accessToken) {
            // 회원: PATCH /cart/user/items/select
            url = `${API_BASE}/user/items/select`;
        } else {
            // 비회원: PATCH /cart/guest/items/select
            url = `${API_BASE}/guest/items/select`;
        }

        const headers = buildAuthHeaders(baseHeaders);

        const res = await fetch(url, {
            method: 'PATCH',
            headers,
            body
        });

        if (!res.ok) {
            console.error('선택/해제 실패', res.status);
            alert('선택/해제 중 문제가 발생했습니다.');
            return;
        }

        await loadCartFromServer();
    } catch (e) {
        console.error('선택/해제 중 오류', e);
        alert('선택/해제 중 오류가 발생했습니다.');
    }
}

async function updateQuantity(bookId, newQuantity) {
    const item = cartItems.find(i => i.bookId === bookId);
    if (!item) return;

    // 간단한 프론트 유효성 검사
    if (newQuantity < 1 || newQuantity > item.stockCount) return;

    // 실제 API 모드
    try {
        let url;
        const body = JSON.stringify({
            bookId: bookId,
            quantity: newQuantity
        });

        const baseHeaders = {
            'Content-Type': 'application/json'
        };

        if (accessToken) {
            // 회원: PATCH /cart/user/items/quantity
            url = `${API_BASE}/user/items/quantity`;
        } else {
            // 비회원: PATCH /cart/guest/items/quantity
            url = `${API_BASE}/guest/items/quantity`;
        }

        const headers = buildAuthHeaders(baseHeaders);

        const res = await fetch(url, {
            method: 'PATCH',
            headers,
            body
        });

        if (!res.ok) {
            console.error('수량 변경 실패', res.status);
            alert('수량 변경 중 문제가 발생했습니다.');
            return;
        }

        await loadCartFromServer();
    } catch (e) {
        console.error('수량 변경 중 오류', e);
        alert('수량 변경 중 오류가 발생했습니다.');
    }
}

async function removeItem(bookId) {
    if (!confirm('이 상품을 삭제하시겠습니까?')) return;

    // 실제 API 모드
    try {
        let url;
        const baseHeaders = {
            'Content-Type': 'application/json'
        };

        if (accessToken) {
            // 회원: DELETE /cart/user/items/{bookId}
            url = `${API_BASE}/user/items/${bookId}`;
        } else {
            // 비회원: DELETE /cart/guest/items/{bookId}
            url = `${API_BASE}/guest/items/${bookId}`;
        }

        const headers = buildAuthHeaders(baseHeaders);

        const res = await fetch(url, {
            method: 'DELETE',
            headers
        });

        if (!res.ok) {
            console.error('상품 삭제 실패', res.status);
            alert('상품 삭제 중 문제가 발생했습니다.');
            return;
        }

        await loadCartFromServer();
    } catch (e) {
        console.error('상품 삭제 중 오류', e);
        alert('상품 삭제 중 오류가 발생했습니다.');
    }
}

async function deleteSelected() {
    const selectedItems = cartItems.filter(item => item.selected);
    if (selectedItems.length === 0) {
        alert('선택된 상품이 없습니다.');
        return;
    }

    if (!confirm(`선택한 ${selectedItems.length}개 상품을 삭제하시겠습니까?`)) return;

    // 실제 API 모드
    try {
        let url;
        const baseHeaders = {
            'Content-Type': 'application/json'
        };

        if (accessToken) {
            // 회원: DELETE /cart/user/items/selected
            url = `${API_BASE}/user/items/selected`;
        } else {
            // 비회원: DELETE /cart/guest/items/selected
            url = `${API_BASE}/guest/items/selected`;
        }

        const headers = buildAuthHeaders(baseHeaders);

        const res = await fetch(url, {
            method: 'DELETE',
            headers
        });

        if (!res.ok) {
            console.error('선택 삭제 실패', res.status);
            alert('선택 삭제 중 문제가 발생했습니다.');
            return;
        }

        await loadCartFromServer();
    } catch (e) {
        console.error('선택 삭제 중 오류', e);
        alert('선택 삭제 중 오류가 발생했습니다.');
    }
}

async function clearCart() {
    if (cartItems.length === 0) {
        alert('장바구니가 비어있습니다.');
        return;
    }

    if (!confirm('장바구니를 전체 삭제하시겠습니까?')) return;

    // 실제 API 모드
    try {
        let url;
        const baseHeaders = {
            'Content-Type': 'application/json'
        };

        if (accessToken) {
            // 회원: DELETE /cart/user/items
            url = `${API_BASE}/user/items`;
        } else {
            // 비회원: DELETE /cart/guest/items
            url = `${API_BASE}/guest/items`;
        }

        const headers = buildAuthHeaders(baseHeaders);

        const res = await fetch(url, {
            method: 'DELETE',
            headers
        });

        if (!res.ok) {
            console.error('전체 삭제 실패', res.status);
            alert('전체 삭제 중 문제가 발생했습니다.');
            return;
        }

        await loadCartFromServer();
    } catch (e) {
        console.error('전체 삭제 중 오류', e);
        alert('전체 삭제 중 오류가 발생했습니다.');
    }
}

function checkout() {
    const selectedItems = cartItems.filter(item =>
        item.selected &&
        !item.deleted &&
        !item.hidden &&
        !item.saleEnded &&
        item.stockCount > 0
    );

    if (selectedItems.length === 0) {
        alert('주문할 수 있는 상품을 선택해주세요.');
        return;
    }

    const total = selectedItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
    alert(`${selectedItems.length}개 상품 / 총 ${total.toLocaleString()}원\n주문 페이지로 이동합니다.`);

    // 실제 주문 페이지로 이동하는 로직은 나중에 연결
    // window.location.href = '/order';
}

async function initCartPage() {

    await loadCartFromServer(); // 기존 장바구니 렌더링

    // 로그인 상태 + uuid가 있는 경우에만 merge-status 조회
    if (userId && uuid) {
        await checkMergeStatusAndMaybeOpenModal();
    }
}

async function checkMergeStatusAndMaybeOpenModal() {
    try {
        const baseHeaders = {
            'Content-Type': 'application/json'
        };
        const headers = buildAuthHeaders(baseHeaders);

        const res = await fetch('/cart/user/merge-status', {
            method: 'GET',
            headers
        });

        if (!res.ok) {
            console.error('merge-status 조회 실패', res.status);
            return;
        }

        const data = await res.json(); // CartMergeStatusResponseDto

        // 1) 게스트 카트가 아예 없으면 아무것도 안 함
        if (!data.hasGuestCart) {
            return;
        }

        // 2) 게스트 O + 회원 X → 자동 병합
        if (data.hasGuestCart && !data.hasUserCart) {
            // 자동 병합 후 간단 안내만 띄우고 끝
            await mergeGuestCart(true); // true = autoMergeFlag 정도로
            return;
        }

        // 3) 게스트 O + 회원 O → 모달 띄워서 선택형 병합
        if (data.hasGuestCart && data.hasUserCart) {
            openMergeModal(data.guestItemCount);
        }
    } catch (e) {
        console.error('merge-status 조회 중 오류', e);
    }
}


function openMergeModal(guestItemCount) {
    const confirmMerge = confirm(
        `비회원 장바구니에 ${guestItemCount}개의 상품이 있습니다.\n` +
        `현재 회원 장바구니와 병합하시겠습니까?`
    );

    if (confirmMerge) {
        mergeGuestCart();
    } else {
        // 정책에 따라:
        // 1) 그냥 아무것도 안 하기 (게스트 카트 유지)
        // 2) 게스트 카트 바로 삭제
        //   fetch('/cart/user/guest-clear', ...) 같은 API 만들어서 처리
        // fetch('/cart/guest/items', {
        //     method: 'DELETE',
        //     headers: {
        //         'Content-Type': 'application/json',
        //         'X-Guest-Id': uuid
        //     }
        // })
        //     .then(res => {
        //         if (!res.ok) {
        //             console.error('게스트 장바구니 삭제 실패', res.status);
        //             return;
        //         }
        //         // 필요하다면 uuid 정리
        //         // localStorage.removeItem('uuid');
        //         // uuid = null;
        //
        //         // 화면 다시 로딩
        //         loadCartFromServer();
        //     })
        //     .catch(e => {
        //         console.error('게스트 장바구니 삭제 중 오류', e);
        //     });
    }
}

async function mergeGuestCart(isAuto = false) {
    try {
        const baseHeaders = {
            'Content-Type': 'application/json'
        };
        const headers = buildAuthHeaders(baseHeaders);

        const res = await fetch('/cart/user/merge', {
            method: 'POST',
            headers
        });

        if (!res.ok) {
            alert('장바구니 병합 중 오류가 발생했습니다.');
            return;
        }

        const mergeResult = await res.json();

        // 병합 성공 시 uuid 정리할지 정책에 따라 선택
        // if (mergeResult.mergeSucceeded) {
        //     localStorage.removeItem('uuid');
        //     uuid = null;
        // }

        if (isAuto) {
            // 자동 병합 케이스라면 살짝 안내 한 줄 정도
            alert('비회원 장바구니를 회원 장바구니로 자동 병합했습니다.');
        } else {
            // 모달에서 사용자가 "예"를 누른 병합 케이스
            alert('장바구니 병합이 완료되었습니다.');
        }

        await loadCartFromServer();
    } catch (e) {
        console.error('merge 호출 중 오류', e);
        alert('장바구니 병합 중 오류가 발생했습니다.');
    }
}

async function initCartPage() {
    if (USE_DUMMY) {
        renderCart();
        return;
    }

    await loadCartFromServer(); // 기존 장바구니 렌더링

    // 로그인 상태 + uuid가 있는 경우에만 merge-status 조회
    if (userId && uuid) {
        await checkMergeStatusAndMaybeOpenModal();
    }
}

async function checkMergeStatusAndMaybeOpenModal() {
    try {
        const res = await fetch('/cart/user/merge-status', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-User-Id': userId,
                'X-Guest-Id': uuid
            }
        });

        if (!res.ok) {
            console.error('merge-status 조회 실패', res.status);
            return;
        }

        const data = await res.json(); // CartMergeStatusResponseDto

        // 1) 게스트 카트가 아예 없으면 아무것도 안 함
        if (!data.hasGuestCart) {
            return;
        }

        // 2) 게스트 O + 회원 X → 자동 병합
        if (data.hasGuestCart && !data.hasUserCart) {
            // 자동 병합 후 간단 안내만 띄우고 끝
            await mergeGuestCart(true); // true = autoMergeFlag 정도로
            return;
        }

        // 3) 게스트 O + 회원 O → 모달 띄워서 선택형 병합
        if (data.hasGuestCart && data.hasUserCart) {
            openMergeModal(data.guestItemCount);
        }
    } catch (e) {
        console.error('merge-status 조회 중 오류', e);
    }
}


function openMergeModal(guestItemCount) {
    const confirmMerge = confirm(
        `비회원 장바구니에 ${guestItemCount}개의 상품이 있습니다.\n` +
        `현재 회원 장바구니와 병합하시겠습니까?`
    );

    if (confirmMerge) {
        mergeGuestCart();
    } else {
        // 정책에 따라:
        // 1) 그냥 아무것도 안 하기 (게스트 카트 유지)
        // 2) 게스트 카트 바로 삭제
        //   fetch('/cart/user/guest-clear', ...) 같은 API 만들어서 처리
    }
}

async function mergeGuestCart(isAuto = false) {
    try {
        const res = await fetch('/cart/user/merge', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-User-Id': userId,
                'X-Guest-Id': uuid
            }
        });

        if (!res.ok) {
            alert('장바구니 병합 중 오류가 발생했습니다.');
            return;
        }

        const mergeResult = await res.json();

        // 병합 성공 시 uuid 정리할지 정책에 따라 선택
        // if (mergeResult.mergeSucceeded) {
        //     localStorage.removeItem('uuid');
        //     uuid = null;
        // }

        if (isAuto) {
            // 자동 병합 케이스라면 살짝 안내 한 줄 정도
            alert('비회원 장바구니를 회원 장바구니로 자동 병합했습니다.');
        } else {
            // 모달에서 사용자가 "예"를 누른 병합 케이스
            alert('장바구니 병합이 완료되었습니다.');
        }

        await loadCartFromServer();
    } catch (e) {
        console.error('merge 호출 중 오류', e);
        alert('장바구니 병합 중 오류가 발생했습니다.');
    }
}


// ============================
// 초기화
// ============================

initCartPage();
