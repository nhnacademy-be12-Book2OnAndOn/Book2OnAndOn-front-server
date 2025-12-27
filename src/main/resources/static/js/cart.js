// ============================
// 기본 설정
// ============================
// 브라우저에 실제로 들어있는 쿠키 상태를 로그로 까보고 싶어서 추가함. 지워도 ok
function dumpCookies() {
    const cookieStr = document.cookie || '';
    const cookies = cookieStr.split(';').map(v => v.trim()).filter(Boolean);

    const map = {};
    for (const c of cookies) {
        const idx = c.indexOf('=');
        const k = idx >= 0 ? c.slice(0, idx) : c;
        const v = idx >= 0 ? decodeURIComponent(c.slice(idx + 1)) : '';
        map[k] = v;
    }
    console.log('[cart] cookies(map)=', map);
    console.log('[cart] cookies(raw)=', cookieStr);
    return map;
}

// 있으면 Authorization 붙이고, 없으면 아무 것도 안 붙임
// -> bearer 문제때문에 혹시 몰라 추가
function buildAuthHeaders(base = {}) {
    const token = getCookie('accessToken');
    if (!token) return base;
    const auth = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    return { ...base, 'Authorization': auth };
}

// 비회원 요청에 필요한 guest 식별 헤더를 일관되게 생성
// -> 비회원 장바구니 유실 방지용
function buildGuestHeaders(base = {}) {
    const gid = ensureGuestId();
    return { ...base, 'X-Guest-Id': gid };
}

// 회원 -> 비회원 fallback 전략을 캡슐화
async function fetchUserThenGuest(userUrl, guestUrl, userOpts, guestOpts) {
    let res = await fetch(userUrl, userOpts);
    console.log('[cart][user attempt]', userUrl, 'status=', res.status, 'redirected=', res.redirected);

    // 프론트보다 서버 응답을 우선 신뢰 (토큰 만료/재발급/쿠키 불일치 시 판단 오류 등을 방지)
    // 토큰이 있어도 만료면 401 → guest
    // 토큰이 없는데 세션 살아있으면 200 → user
    if (res.status === 401 || res.status === 403) {
        res = await fetch(guestUrl, guestOpts);
        return { res, mode: 'guest' };
    }
    // 병합 여부 판단, UI 분기를 위해 mode 추가
    return { res, mode: 'user' };
}

// 쿠키 읽기 유틸
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop().split(';').shift();
    }
    return null;
}

function setCookie(name, value, days) {
    const date = new Date();
    date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
    document.cookie = `${name}=${encodeURIComponent(value)}; path=/; expires=${date.toUTCString()}`;
}

function ensureGuestId() {
    if (typeof window.ensureGuestId === 'function' && window.ensureGuestId !== ensureGuestId) {
        return window.ensureGuestId();
    }
    let gid = localStorage.getItem('uuid') || getCookie('GUEST_ID') || getCookie('guestId');
    if (!gid) {
        gid = `guest-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
    }
    try { localStorage.setItem('uuid', gid); } catch (e) { /* ignore storage errors */ }
    setCookie('GUEST_ID', gid, 30);
    setCookie('guestId', gid, 30);
    // 다음 호출에서도 동일 함수가 재사용되도록 글로벌에 바인딩
    window.ensureGuestId = window.ensureGuestId || ensureGuestId;
    return gid;
}

const userId = localStorage.getItem('userId');

// accessToken 기준으로 회원/비회원 판단
// const accessToken = getCookie('accessToken');
// let uuid = ensureGuestId();
// const isGuest = !accessToken;
// -> 이는 스크립트 로드 시점의 쿠키 상태를 고정하는 방식.
// but 페이지가 살아 있는 동안 accessToken 상태는 계속 변함.
// 따라서 전역 const는 이 변화를 절대 따라가지 못함.


const API_BASE = '/cart';
const USE_DUMMY = false;


// 더미 아이템 (개발용)
const DUMMY_ITEMS = [
    {
        bookId: 1,
        title: '샘플 도서',
        thumbnailUrl: '',
        originalPrice: 15000,
        salePrice: 15000, // book-service, cart-service의 dto필드명 통일
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
// 서버에서 장바구니 조회
// ============================

async function loadCartFromServer() {
    try {
        const baseHeaders = {'Content-Type': 'application/json'};

        // 요청 옵션을 “회원용/비회원용”으로 분리
        const userOpts = { method: 'GET', headers: buildAuthHeaders(baseHeaders), credentials: 'include' };
        const guestOpts = { method: 'GET', headers: buildGuestHeaders(baseHeaders), credentials: 'include' };

        const { res, mode } = await fetchUserThenGuest(
            `${API_BASE}/user`,
            `${API_BASE}/guest`,
            userOpts,
            guestOpts
        );

        if (!res.ok) {
            console.error('장바구니 조회 실패', res.status);
            return;
        }

        const data = await res.json();
        cartSummaryData = data;
        cartItems = data.items || [];

        renderCart();
        updateHeaderCartBadgeFromSummary(); // 헤더 뱃지 동기화

        // 쿠키를 읽지 않고도 "로그인 상태"를 확정할 수 있도록 함
        if (mode === 'user') {
            const gid = ensureGuestId();
            if (gid) await checkMergeStatusAndMaybeOpenModal(gid);
        }
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
        const hasDiscount = item.originalPrice > item.salePrice;
        const discountRate = hasDiscount ? Math.round((1 - item.salePrice / item.originalPrice) * 100) : 0;

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
                <span class="item-price">${item.salePrice.toLocaleString()}원</span>
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
              <div class="item-total">${(item.salePrice * item.quantity).toLocaleString()}원</div>
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
    const selectAll = document.getElementById('selectAll').checked;
    const body = JSON.stringify({ selected: selectAll });

    const baseHeaders = { 'Content-Type': 'application/json' };

    // const userOpts = { method: 'PATCH', headers: baseHeaders, body, credentials: 'include' };
    const userOpts = { method: 'PATCH', headers: buildAuthHeaders(baseHeaders), body, credentials: 'include' };
    const guestOpts = { method: 'PATCH', headers: buildGuestHeaders(baseHeaders), body, credentials: 'include' };

    const { res } = await fetchUserThenGuest(
        `${API_BASE}/user/items/select-all`,
        `${API_BASE}/guest/items/select-all`,
        userOpts,
        guestOpts
    );

    if (!res.ok) {
        console.error('전체 선택/해제 실패', res.status);
        alert('전체 선택/해제 중 문제가 발생했습니다.');
        return;
    }

    await loadCartFromServer();
}

async function toggleItem(bookId) {
    const item = cartItems.find(i => i.bookId === bookId);
    if (!item) return;

    const body = JSON.stringify({ bookId, selected: !item.selected });
    const baseHeaders = { 'Content-Type': 'application/json' };

    // const userOpts = { method: 'PATCH', headers: baseHeaders, body, credentials: 'include' };
    const userOpts = { method: 'PATCH', headers: buildAuthHeaders(baseHeaders), body, credentials: 'include' };
    const guestOpts = { method: 'PATCH', headers: buildGuestHeaders(baseHeaders), body, credentials: 'include' };

    const { res } = await fetchUserThenGuest(
        `${API_BASE}/user/items/select`,
        `${API_BASE}/guest/items/select`,
        userOpts,
        guestOpts
    );

    if (!res.ok) {
        console.error('선택/해제 실패', res.status);
        alert('선택/해제 중 문제가 발생했습니다.');
        return;
    }

    await loadCartFromServer();
}


async function updateQuantity(bookId, newQuantity) {
    const item = cartItems.find(i => i.bookId === bookId);
    if (!item) return;
    if (newQuantity < 1 || newQuantity > item.stockCount) return;

    const body = JSON.stringify({ bookId, quantity: newQuantity });
    const baseHeaders = { 'Content-Type': 'application/json' };

    // const userOpts = { method: 'PATCH', headers: baseHeaders, body, credentials: 'include' };
    const userOpts = { method: 'PATCH', headers: buildAuthHeaders(baseHeaders), body, credentials: 'include' };
    const guestOpts = { method: 'PATCH', headers: buildGuestHeaders(baseHeaders), body, credentials: 'include' };

    const { res } = await fetchUserThenGuest(
        `${API_BASE}/user/items/quantity`,
        `${API_BASE}/guest/items/quantity`,
        userOpts,
        guestOpts
    );

    if (!res.ok) {
        console.error('수량 변경 실패', res.status);
        alert('수량 변경 중 문제가 발생했습니다.');
        return;
    }

    await loadCartFromServer();
}

async function removeItem(bookId) {
    if (!confirm('이 상품을 삭제하시겠습니까?')) return;

    const baseHeaders = { 'Content-Type': 'application/json' };
    // const userOpts = { method: 'DELETE', headers: baseHeaders, credentials: 'include' };
    const userOpts = { method: 'DELETE', headers: buildAuthHeaders(baseHeaders), credentials: 'include' };
    const guestOpts = { method: 'DELETE', headers: buildGuestHeaders(baseHeaders), credentials: 'include' };

    const { res } = await fetchUserThenGuest(
        `${API_BASE}/user/items/${bookId}`,
        `${API_BASE}/guest/items/${bookId}`,
        userOpts,
        guestOpts
    );

    if (!res.ok) {
        console.error('상품 삭제 실패', res.status);
        alert('상품 삭제 중 문제가 발생했습니다.');
        return;
    }

    await loadCartFromServer();
}

async function deleteSelected() {
    const selectedItems = cartItems.filter(item => item.selected);
    if (selectedItems.length === 0) {
        alert('선택된 상품이 없습니다.');
        return;
    }
    if (!confirm(`선택한 ${selectedItems.length}개 상품을 삭제하시겠습니까?`)) return;

    const baseHeaders = { 'Content-Type': 'application/json' };
    // const userOpts = { method: 'DELETE', headers: baseHeaders, credentials: 'include' };
    const userOpts = { method: 'DELETE', headers: buildAuthHeaders(baseHeaders), credentials: 'include' };

    const guestOpts = { method: 'DELETE', headers: buildGuestHeaders(baseHeaders), credentials: 'include' };

    const { res } = await fetchUserThenGuest(
        `${API_BASE}/user/items/selected`,
        `${API_BASE}/guest/items/selected`,
        userOpts,
        guestOpts
    );

    if (!res.ok) {
        console.error('선택 삭제 실패', res.status);
        alert('선택 삭제 중 문제가 발생했습니다.');
        return;
    }

    await loadCartFromServer();
}


async function clearCart() {
    if (cartItems.length === 0) { alert('장바구니가 비어있습니다.'); return; }
    if (!confirm('장바구니를 전체 삭제하시겠습니까?')) return;

    const baseHeaders = { 'Content-Type': 'application/json' };
    // const userOpts = { method: 'DELETE', headers: baseHeaders, credentials: 'include' };
    const userOpts = { method: 'DELETE', headers: buildAuthHeaders(baseHeaders), credentials: 'include' };
    const guestOpts = { method: 'DELETE', headers: buildGuestHeaders(baseHeaders), credentials: 'include' };

    const { res } = await fetchUserThenGuest(
        `${API_BASE}/user/items`,
        `${API_BASE}/guest/items`,
        userOpts,
        guestOpts
    );

    if (!res.ok) {
        console.error('전체 삭제 실패', res.status);
        alert('전체 삭제 중 문제가 발생했습니다.');
        return;
    }

    await loadCartFromServer();
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
    console.log("[cart] document.cookie =", document.cookie);
    console.log("[cart] API_BASE =", API_BASE);
    // const cookies = dumpCookies();
    // console.log("[cart] localStorage.userId =", localStorage.getItem('userId'));
    // console.log("[cart] guestId(localStorage.uuid) =", localStorage.getItem('uuid'));
    // console.log("[cart] guestId(cookie.GUEST_ID) =", cookies.GUEST_ID);
    // console.log("[cart] guestId(cookie.guestId) =", cookies.guestId);

    await loadCartFromServer();

    // merge-status 로직을 유지하려면 guestId를 여기서만 구해서 사용
    // const gid = ensureGuestId();
    // if (userId && gid) {
    //     await checkMergeStatusAndMaybeOpenModal(gid);
    // }
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
    const gid = ensureGuestId();
    const res = await fetch('/cart/user/merge', {
        method: 'POST',
        headers: { 'X-Guest-Id': gid },
        credentials: 'include'
    });

    if (!res.ok) throw new Error('장바구니 병합 중 오류가 발생했습니다.');

    alert(isAuto ? '비회원 장바구니를 회원 장바구니로 자동 병합했습니다.' : '장바구니 병합이 완료되었습니다.');
    await loadCartFromServer();
}


async function checkMergeStatusAndMaybeOpenModal(gid) {
    try {
        const res  = await fetch('/cart/user/merge-status', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                // 'X-User-Id': userId,
                'X-Guest-Id': gid
            },
            credentials: 'include'
        });
        console.log('[merge-status] status=', res.status);

        if (!res.ok) {
            console.error('merge-status 조회 실패', res.status);
            return;
        }

        const data = await res.json(); // CartMergeStatusResponseDto
        console.log('[merge-status] data=', data);

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

function updateHeaderCartBadgeFromSummary() {
    const badge = document.querySelector('[data-cart-count]');
    if (!badge) return;

    // 서버 DTO가 내려주는 totalQuantity / totalItemCount 중 원하는 기준 선택
    const count = (cartSummaryData && Number(cartSummaryData.totalItemCount)) || 0;

    badge.textContent = String(count);
    badge.style.display = count > 0 ? 'inline-flex' : 'none';
}

// ============================
// 초기화
// ============================

initCartPage();
