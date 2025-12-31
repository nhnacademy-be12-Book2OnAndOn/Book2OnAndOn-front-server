// ============================================================
// 0) 상수 / 전역 상태
// ============================================================
const API_BASE = '/cart';
const USE_DUMMY = false;

const DUMMY_ITEMS = [
    {
        bookId: 1,
        title: '샘플 도서',
        thumbnailUrl: '',
        originalPrice: 15000,
        salePrice: 15000,
        stockCount: 3,
        saleEnded: false,
        deleted: false,
        hidden: false,
        quantity: 1,
        selected: true
    }
];

let cartItems = USE_DUMMY ? [...DUMMY_ITEMS] : [];
let cartSummaryData = null;
let cartMode = 'guest'; // user | guest | error

// 동시 로드 보호 / merge 체크
let mergeChecked = false;
let loadSeq = 0;

// 타이머들
let syncTimer = null;
let syncDelay = 250;

let selectionReloadTimer = null;
let reloadTimer = null;

// 수량 디바운스
const qtyTimers = new Map();      // bookId -> timerId
const qtyPending = new Map();     // bookId -> lastQuantity
const qtyFallback = new Map();    // bookId -> firstPrev (롤백 기준)

const userId = localStorage.getItem('userId');


// ============================================================
// 1) 디버그 / 쿠키 유틸
// ============================================================
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

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}

function setCookie(name, value, days) {
    const date = new Date();
    date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
    document.cookie = `${name}=${encodeURIComponent(value)}; path=/; expires=${date.toUTCString()}`;
}


// ============================================================
// 2) GuestId / Header 빌더
// ============================================================
function ensureGuestId() {
    if (typeof window.ensureGuestId === 'function' && window.ensureGuestId !== ensureGuestId) {
        return window.ensureGuestId();
    }

    let gid = localStorage.getItem('uuid') || getCookie('GUEST_ID') || getCookie('guestId');
    if (!gid) gid = `guest-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;

    try {
        localStorage.setItem('uuid', gid);
    } catch (e) { /* ignore */
    }

    setCookie('GUEST_ID', gid, 30);
    setCookie('guestId', gid, 30);

    window.ensureGuestId = window.ensureGuestId || ensureGuestId;
    return gid;
}

function buildAuthHeaders(base = {}) {
    const token = getCookie('accessToken');
    if (!token) return base;
    const auth = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    return {...base, 'Authorization': auth};
}

function buildGuestHeaders(base = {}) {
    const gid = ensureGuestId();
    return {...base, 'X-Guest-Id': gid};
}


// ============================================================
// 3) 네트워크 공통 (회원 -> 비회원 fallback)
// ============================================================
async function fetchUserThenGuest(userUrl, guestUrl, userOpts, guestOpts) {
    let res = await fetch(userUrl, userOpts);
    console.log('[cart][user attempt]', userUrl, 'status=', res.status, 'redirected=', res.redirected);

    if (res.status === 401 || res.status === 403) {
        res = await fetch(guestUrl, guestOpts);
        return {res, mode: 'guest'};
    }

    if (res.status >= 500) {
        alert('장바구니 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요.');
        return {res, mode: 'error'};
    }

    return {res, mode: 'user'};
}


// ============================================================
// 4) 서버 동기화/디바운스
// ============================================================
function scheduleServerSync(delay = syncDelay) {
    clearTimeout(syncTimer);
    syncTimer = setTimeout(() => loadCartFromServer(), delay);
}

function scheduleSelectionReload() {
    clearTimeout(selectionReloadTimer);
    selectionReloadTimer = setTimeout(() => loadCartFromServer(), 200);
}

function scheduleReload() {
    clearTimeout(reloadTimer);
    reloadTimer = setTimeout(() => loadCartFromServer(), 300);
}


// ============================================================
// 5) 서버에서 장바구니 조회
// ============================================================
async function loadCartFromServer() {
    const mySeq = ++loadSeq;

    try {
        const baseHeaders = {'Content-Type': 'application/json'};

        const userOpts = {method: 'GET', headers: buildAuthHeaders(baseHeaders), credentials: 'include'};
        const guestOpts = {method: 'GET', headers: buildGuestHeaders(baseHeaders), credentials: 'include'};

        const {res, mode} = await fetchUserThenGuest(
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
        if (mySeq !== loadSeq) return; // 최신 요청이 아니면 폐기

        cartSummaryData = data;
        cartItems = data.items || [];
        cartMode = mode;

        renderCart();
        updateHeaderCartBadgeFromSummary();

        if (mode === 'user' && !mergeChecked) {
            mergeChecked = true;
            const gid = ensureGuestId();
            if (gid) await checkMergeStatusAndMaybeOpenModal(gid);
        }
    } catch (e) {
        console.error('장바구니 조회 중 오류', e);
    }
}


// ============================================================
// 6) 렌더링 / 계산
// ============================================================
function computeLocalSelectedSubtotal() {
    return cartItems
        .filter(i =>
            i.selected &&
            !i.deleted && !i.hidden && !i.saleEnded &&
            i.stockCount > 0
        )
        .reduce((sum, i) => sum + (i.salePrice * i.quantity), 0);
}

function updateSummary() {
    const totalElem = document.getElementById('total');

    const localSubtotal = computeLocalSelectedSubtotal();

    const finalPaymentAmount = (cartSummaryData && cartSummaryData.finalPaymentAmount) != null
        ? cartSummaryData.finalPaymentAmount
        : localSubtotal;

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

function renderCart() {
    const cartContent = document.getElementById('cartContent');
    const cartSummary = document.getElementById('cartSummary');

    if (!cartItems || cartItems.length === 0) {
        cartContent.innerHTML = `
      <div class="empty-cart">
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

            <a class="item-image item-link" href="/books/${item.bookId}" aria-label="${item.title} 상세로 이동">
              ${item.thumbnailUrl ? `<img src="${item.thumbnailUrl}" alt="${item.title}">` : '책 이미지'}
            </a>

            <div class="item-details">
              <a class="item-title item-link" href="/books/${item.bookId}">${item.title}</a>

              <div class="item-meta">
                ${isOutOfStock
            ? '<span class="item-badge badge-stock out">품절</span>'
            : isLowStock
                ? `<span class="item-badge badge-stock low">재고 ${item.stockCount}개</span>`
                : `<span class="item-badge badge-stock">재고 ${item.stockCount}개</span>`}
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
                  onclick="changeQuantity(${item.bookId}, -1)"
                  ${isUnavailable || isOutOfStock ? 'disabled' : ''}>-</button>

                <div class="quantity-display">${item.quantity}</div>

                <button class="quantity-btn"
                  onclick="changeQuantity(${item.bookId}, +1)"
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


// ============================================================
// 7) 액션: 선택(체크박스)
// ============================================================
async function toggleSelectAll() {
    const selectAllChecked = document.getElementById('selectAll').checked;
    const prevSelectedMap = new Map(cartItems.map(i => [i.bookId, i.selected]));

    for (const item of cartItems) {
        const available = !item.deleted && !item.hidden && !item.saleEnded && item.stockCount > 0;
        if (available) item.selected = selectAllChecked;
    }
    renderCart();

    const body = JSON.stringify({selected: selectAllChecked});
    const baseHeaders = {'Content-Type': 'application/json'};

    const userOpts = {method: 'PATCH', headers: buildAuthHeaders(baseHeaders), body, credentials: 'include'};
    const guestOpts = {method: 'PATCH', headers: buildGuestHeaders(baseHeaders), body, credentials: 'include'};

    const {res} = await fetchUserThenGuest(
        `${API_BASE}/user/items/select-all`,
        `${API_BASE}/guest/items/select-all`,
        userOpts,
        guestOpts
    );

    if (!res.ok) {
        for (const item of cartItems) item.selected = prevSelectedMap.get(item.bookId) ?? item.selected;
        renderCart();
        alert('전체 선택/해제 중 문제가 발생했습니다.');
        return;
    }

    scheduleServerSync(200);
}

async function toggleItem(bookId) {
    const index = cartItems.findIndex(i => i.bookId === bookId);
    if (index < 0) return;

    const item = cartItems[index];
    const available = !item.deleted && !item.hidden && !item.saleEnded && item.stockCount > 0;
    if (!available) return;

    const prev = item.selected;
    const next = !prev;

    item.selected = next;
    renderCart();

    const body = JSON.stringify({bookId, selected: next});
    const baseHeaders = {'Content-Type': 'application/json'};

    const userOpts = {method: 'PATCH', headers: buildAuthHeaders(baseHeaders), body, credentials: 'include'};
    const guestOpts = {method: 'PATCH', headers: buildGuestHeaders(baseHeaders), body, credentials: 'include'};

    const {res} = await fetchUserThenGuest(
        `${API_BASE}/user/items/select`,
        `${API_BASE}/guest/items/select`,
        userOpts,
        guestOpts
    );

    if (!res.ok) {
        item.selected = prev;
        renderCart();
        alert('선택/해제 중 문제가 발생했습니다.');
        return;
    }

    scheduleServerSync(200);
}


// ============================================================
// 8) 액션: 수량 변경(디바운스)
// ============================================================
function changeQuantity(bookId, delta) {
    const index = cartItems.findIndex(i => i.bookId === bookId);
    if (index < 0) return;

    const current = cartItems[index].quantity;
    const next = current + delta;
    updateQuantity(bookId, next);
}

function updateQuantity(bookId, newQuantity) {
    const index = cartItems.findIndex(i => i.bookId === bookId);
    if (index < 0) return;

    const stock = cartItems[index].stockCount;
    if (newQuantity < 1 || newQuantity > stock) return;

    if (!qtyTimers.has(bookId)) {
        qtyFallback.set(bookId, cartItems[index].quantity);
    }

    cartItems[index].quantity = newQuantity;
    renderCart();

    qtyPending.set(bookId, newQuantity);

    if (qtyTimers.has(bookId)) clearTimeout(qtyTimers.get(bookId));

    qtyTimers.set(bookId, setTimeout(async () => {
        const finalQty = qtyPending.get(bookId);
        const prev = qtyFallback.get(bookId);

        qtyPending.delete(bookId);
        qtyFallback.delete(bookId);
        qtyTimers.delete(bookId);

        const ok = await sendQuantityPatch(bookId, finalQty, prev);
        if (ok) scheduleServerSync(300);
    }, 250));
}

async function sendQuantityPatch(bookId, quantity, fallbackPrev) {
    const body = JSON.stringify({bookId, quantity});
    const baseHeaders = {'Content-Type': 'application/json'};

    const userOpts = {method: 'PATCH', headers: buildAuthHeaders(baseHeaders), body, credentials: 'include'};
    const guestOpts = {method: 'PATCH', headers: buildGuestHeaders(baseHeaders), body, credentials: 'include'};

    const {res} = await fetchUserThenGuest(
        `${API_BASE}/user/items/quantity`,
        `${API_BASE}/guest/items/quantity`,
        userOpts,
        guestOpts
    );

    if (!res.ok) {
        const index = cartItems.findIndex(i => i.bookId === bookId);
        if (index >= 0) {
            cartItems[index].quantity = fallbackPrev;
            renderCart();
        }
        alert('수량 변경 중 문제가 발생했습니다.');
        return false;
    }
    return true;
}


// ============================================================
// 9) 액션: 삭제
// ============================================================
async function removeItem(bookId) {
    if (!confirm('이 상품을 삭제하시겠습니까?')) return;

    const baseHeaders = {'Content-Type': 'application/json'};
    const userOpts = {method: 'DELETE', headers: buildAuthHeaders(baseHeaders), credentials: 'include'};
    const guestOpts = {method: 'DELETE', headers: buildGuestHeaders(baseHeaders), credentials: 'include'};

    const {res} = await fetchUserThenGuest(
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

    const baseHeaders = {'Content-Type': 'application/json'};
    const userOpts = {method: 'DELETE', headers: buildAuthHeaders(baseHeaders), credentials: 'include'};
    const guestOpts = {method: 'DELETE', headers: buildGuestHeaders(baseHeaders), credentials: 'include'};

    const {res} = await fetchUserThenGuest(
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
    if (cartItems.length === 0) {
        alert('장바구니가 비어있습니다.');
        return;
    }
    if (!confirm('장바구니를 전체 삭제하시겠습니까?')) return;

    const baseHeaders = {'Content-Type': 'application/json'};
    const userOpts = {method: 'DELETE', headers: buildAuthHeaders(baseHeaders), credentials: 'include'};
    const guestOpts = {method: 'DELETE', headers: buildGuestHeaders(baseHeaders), credentials: 'include'};

    const {res} = await fetchUserThenGuest(
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


// ============================================================
// 10) 주문 준비: /orders/prepare 폼 submit
// ============================================================
function createHidden(name, value) {
    const input = document.createElement("input");
    input.type = "hidden";
    input.name = name;
    input.value = String(value);
    return input;
}

function submitOrderPrepare(selectedItems) {
    const form = document.createElement("form");
    form.method = "POST";
    form.action = "/orders/prepare";

    selectedItems.forEach((item, idx) => {
        form.appendChild(createHidden(`bookItems[${idx}].bookId`, item.bookId));
        form.appendChild(createHidden(`bookItems[${idx}].quantity`, Number(item.quantity) || 0));
    });

    document.body.appendChild(form);
    form.submit();
    form.remove();
}

function submitGuestOrderPrepare(selectedItems) {
    const form = document.createElement("form");
    form.method = "POST";
    form.action = "/orders/guest/prepare";

    selectedItems.forEach((item, idx) => {
        form.appendChild(createHidden(`bookItems[${idx}].bookId`, item.bookId));
        form.appendChild(createHidden(`bookItems[${idx}].quantity`, Number(item.quantity) || 0));
    });

    document.body.appendChild(form);
    form.submit();
    form.remove();
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

    const total = selectedItems.reduce(
        (sum, item) => sum + (Number(item.salePrice) || 0) * (Number(item.quantity) || 0),
        0
    );

    if (cartMode === 'guest') {
        alert(`${selectedItems.length}개 상품 / 총 ${total.toLocaleString()}원\n비회원 결제 페이지로 이동합니다.`);
        submitGuestOrderPrepare(selectedItems)
    } else if (cartMode === 'user') {
        alert(`${selectedItems.length}개 상품 / 총 ${total.toLocaleString()}원\n주문 준비로 이동합니다.`);
        submitOrderPrepare(selectedItems);
    } else {
        alert('장바구니 상태를 확인할 수 없습니다. 잠시 후 다시 시도해주세요.');
    }
}


// ============================================================
// 11) 병합(merge) 관련
// ============================================================
function openMergeModal(guestItemCount) {
    const confirmMerge = confirm(
        `비회원 장바구니에 ${guestItemCount}개의 상품이 있습니다.\n` +
        `현재 회원 장바구니와 병합하시겠습니까?`
    );
    if (confirmMerge) mergeGuestCart();
}

async function mergeGuestCart(isAuto = false) {
    const gid = ensureGuestId();
    const res = await fetch('/cart/user/merge', {
        method: 'POST',
        headers: {'X-Guest-Id': gid},
        credentials: 'include'
    });

    if (!res.ok) throw new Error('장바구니 병합 중 오류가 발생했습니다.');

    alert(isAuto ? '비회원 장바구니를 회원 장바구니로 자동 병합했습니다.' : '장바구니 병합이 완료되었습니다.');
    await loadCartFromServer();
}

async function checkMergeStatusAndMaybeOpenModal(gid) {
    try {
        const res = await fetch('/cart/user/merge-status', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-Guest-Id': gid
            },
            credentials: 'include'
        });

        console.log('[merge-status] status=', res.status);

        // 백엔드가 머지 상태 없으면 404를 줄 수 있음 → 조용히 종료
        if (res.status === 404) return;
        if (!res.ok) return;

        const data = await res.json();
        console.log('[merge-status] data=', data);

        if (!data.hasGuestCart) return;

        if (data.hasGuestCart && !data.hasUserCart) {
            await mergeGuestCart(true);
            return;
        }

        if (data.hasGuestCart && data.hasUserCart) {
            openMergeModal(data.guestItemCount);
        }
    } catch (e) {
        console.error('merge-status 조회 중 오류', e);
    }
}


// ============================================================
// 12) 헤더 뱃지
// ============================================================
function updateHeaderCartBadgeFromSummary() {
    const badge = document.querySelector('[data-cart-count]');
    if (!badge) return;

    const count = (cartSummaryData && Number(cartSummaryData.totalItemCount)) || 0;
    badge.textContent = String(count);
    badge.style.display = count > 0 ? 'inline-flex' : 'none';
}


// ============================================================
// 13) 초기화
// ============================================================
async function initCartPage() {
    console.log("[cart] document.cookie =", document.cookie);
    console.log("[cart] API_BASE =", API_BASE);

    await loadCartFromServer();
}

initCartPage();
