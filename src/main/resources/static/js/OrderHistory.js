const API_BASE = '/orders';

const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
};

const ACCESS_TOKEN = getCookie('accessToken');
const IS_MEMBER_LOGGED_IN =
    (typeof window !== 'undefined' && window.IS_MEMBER_LOGGED_IN !== undefined)
        ? Boolean(window.IS_MEMBER_LOGGED_IN) || Boolean(ACCESS_TOKEN)
        : Boolean(ACCESS_TOKEN);
const USE_SERVER_PAGING =
    (typeof window !== 'undefined' && window.USE_SERVER_PAGING !== undefined)
        ? Boolean(window.USE_SERVER_PAGING)
        : false;
// const USER_ID =
//     (typeof window !== 'undefined' && window.USER_ID !== undefined)
//         ? window.USER_ID
//         : null;
function getUserId() {
    return (typeof window !== 'undefined' && window.USER_ID !== undefined) ? window.USER_ID : null;
}

// 백엔드 Enum 및 UI 표시용 상태명
const ORDER_STATUS = {
    PENDING: "주문 대기",
    SHIPPING: "배송중",
    DELIVERED: "배송 완료",
    CANCELED: "주문 취소",
    COMPLETED: "주문 완료",
    RETURN_REQUESTED: "반품 신청",
    RETURN_COMPLETED: "반품 완료"
};

const RETURN_REASON = {
    CHANGE_OF_MIND: "단순 변심",
    PRODUCT_DEFECT: "상품 불량",
    WRONG_DELIVERY: "배송 문제",
    OTHER: "기타"
};

let currentOrderDetail = null;
let memberOrders = Array.isArray(window.INITIAL_ORDERS) ? window.INITIAL_ORDERS : [];

document.addEventListener('DOMContentLoaded', () => {
    try {
        initializeView();
    } catch (e) {
        console.error('initializeView error', e);
    }
    try {
        setupEventListeners();
    } catch (e) {
        console.error('setupEventListeners error', e);
    }
    try {
        setupModalListeners();
    } catch (e) {
        console.error('setupModalListeners error', e);
    }
    try {
        initializeFilterForm();
    } catch (e) {
        console.error('initializeFilterForm error', e);
    }
});

// --- 초기화 및 UI 제어 ---
function initializeView() {
    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get('orderId');
    const mode = urlParams.get('mode');
    const isLoggedIn = IS_MEMBER_LOGGED_IN && mode !== 'guest';

    if (orderId) {
        fetchOrderDetail(orderId, isLoggedIn ? 'MEMBER_MODE' : 'GUEST_MODE');
    } else if (isLoggedIn) {
        showMemberHistory();
        fetchMemberOrders();
    } else {
        showGuestLookupForm();
    }
}

function setupEventListeners() {
    document.getElementById('guestLookupForm')?.addEventListener('submit', handleGuestLookup);
    document.getElementById('backToHistory')?.addEventListener('click', () => {
        IS_MEMBER_LOGGED_IN ? showMemberHistory() : showGuestLookupForm();
    });

    // 주문 목록 클릭 시 상세 보기
    document.getElementById('orderList')?.addEventListener('click', (e) => {
        const orderItem = e.target.closest('.order-item');
        if (orderItem) fetchOrderDetail(orderItem.dataset.orderId, 'MEMBER_MODE');
    });

    document.getElementById('sortOrderSelect')?.addEventListener('change', (e) => {
        const filtered = filterOrders(memberOrders, getCurrentFilters());
        sortOrdersAndRender(e.target.value, filtered);
    });

    document.getElementById('orderFilterForm')?.addEventListener('submit', handleOrderFiltering);
    document.getElementById('filterResetButton')?.addEventListener('click', initializeFilterForm);
    document.getElementById('inlineSearchButton')?.addEventListener('click', () => {
        document.getElementById('orderFilterForm')?.dispatchEvent(new Event('submit', {
            cancelable: true,
            bubbles: true
        }));
    });

    // 상세페이지 체크박스 금액 실시간 업데이트
    document.getElementById('orderDetailContent')?.addEventListener('change', (e) => {
        if (e.target.classList.contains('item-checkbox')) updateSelectedAmount();
    });
}

// --- 상태별 색상 반환 함수 ---
function getStatusColor(status) {
    switch (status) {
        case ORDER_STATUS.DELIVERED:
            return 'green';
        case ORDER_STATUS.CANCELED:
            return 'red';
        case ORDER_STATUS.RETURN_REQUESTED:
            return 'orange';
        case ORDER_STATUS.SHIPPING:
            return '#333';
        default:
            return '#333';
    }
}

/*
 * --- 주문 목록 렌더링 (이미지 스타일 복구) ---
 */
function renderOrderList(orders) {
    const container = document.getElementById('orderList');
    if (!container) return;
    container.innerHTML = orders.length ? '' : '<p id="noOrdersMessage">주문 내역이 없습니다.</p>';

    orders.forEach(o => {
        const statusText = ORDER_STATUS[o.orderStatus] || ORDER_STATUS[o.status] || o.orderStatus || '상태 미정';
        const createdAt = o.orderDateTime || o.date;
        const dateText = createdAt ? formatDate(createdAt) : '';
        const title = o.orderTitle || (o.items && o.items[0]?.name) || '주문 상품';
        container.innerHTML += `
            <div class="order-item" data-order-id="${o.orderNumber || o.orderId}" style="cursor:pointer; border:1px solid #ddd; padding:15px; margin-bottom:10px; border-radius:8px;">
                <div class="order-info">
                    <strong>주문 번호: ${o.orderNumber || o.orderId || ''}</strong> ${dateText ? `(${dateText})` : ''}<br>
                    총 금액: ${(o.totalAmount ?? o.total ?? 0).toLocaleString()}원 | 상태: <span style="font-weight:bold; color:${getStatusColor(statusText)};">${statusText}</span><br>
                    상품: ${title}
                </div>
                <button class="btn-primary" style="margin-top:10px;">상세 보기</button>
            </div>`;
    });
}

/*
 * --- 주문 상세 정보 렌더링 (이미지 내용 복구) ---
 */
async function fetchOrderDetail(orderId, mode) {
    try {
        const headers = {'Content-Type': 'application/json'};
        if (mode === 'MEMBER_MODE') {
            headers['Authorization'] = `Bearer ${getCookie('accessToken')}`;
        }

        const response = await fetch(`${API_BASE}/${orderId}`, {
            method: 'GET',
            headers: headers
        });

        if (!response.ok) throw new Error("상세 정보 로드 실패");

        currentOrderDetail = await response.json();
        renderOrderDetailUI(currentOrderDetail, mode);
    } catch (error) {
        console.error("Error:", error);
        alert("주문 정보를 불러올 수 없습니다.");
    }
}

// UI 렌더링 로직 분리
function renderOrderDetailUI(detail, mode) {
    const container = document.getElementById('orderDetailContent');
    const topActionContainer = document.getElementById('actionButtons');
    if (topActionContainer) topActionContainer.innerHTML = '';

    let itemsHtml = '';
    const statusKey = (detail.orderStatus || detail.status || '').toString();
    const statusLabel = ORDER_STATUS[statusKey] || detail.status || detail.orderStatus || '';
    const allowReview = statusKey === 'DELIVERED' || statusKey === 'COMPLETED'
        || statusLabel === ORDER_STATUS.DELIVERED || statusLabel === ORDER_STATUS.COMPLETED;
    const items = Array.isArray(detail.orderItems) ? detail.orderItems : (detail.items || []);
    const isCheckable = [ORDER_STATUS.PENDING, ORDER_STATUS.SHIPPING, ORDER_STATUS.DELIVERED]
        .includes(detail.status) || ['PENDING', 'SHIPPING', 'DELIVERED'].includes(statusKey);
    items.forEach(item => {
        const itemName = item.bookTitle || item.name || '상품';
        const itemPrice = Number(item.unitPrice ?? item.price ?? 0);
        const itemQuantity = Math.max(1, Number(item.orderItemQuantity ?? item.quantity ?? 1));
        const itemId = item.orderItemId || item.id || '';
        const hasBookId = item.bookId !== undefined && item.bookId !== null;
        const reviewBtn = allowReview && hasBookId
            ? `<a class="btn-secondary" style="margin-left:8px; white-space:nowrap;"
                  href="/books/${item.bookId}?tab=reviews#reviewForm">리뷰 작성</a>`
            : '';

        for (let i = 0; i < itemQuantity; i++) {
            itemsHtml += `
                <div class="order-item-detail" style="display:flex; align-items:center; gap:10px; margin-bottom:10px; padding:10px; background:#fcfcfc; border-left:3px solid #ddd;">
                ${isCheckable ?
                `<input type="checkbox" class="item-checkbox" data-id="${item.orderItemId || item.id}" data-price="${item.price}" data-name="${item.name}">`
                : ''}
                <span style="flex:1; min-width:0; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${item.name} (1권) - ${item.price.toLocaleString()}원 ${item.isWrapped ? `(포장 옵션: ${item.wrapName})` : ''}</span>
                ${reviewBtn}
            </div>`;
        }
    });

    container.innerHTML = `
        <h3 style="border-left:5px solid #4A7C59; padding-left:10px; color:#4A7C59;">#${detail.orderNumber || detail.id} 주문 상세 내역</h3>
        <p><strong>주문 일자:</strong> ${detail.orderDate || detail.date}</p>
        <p><strong>주문 상태:</strong> <span style="font-weight:bold; color:${getStatusColor(detail.status)};">${detail.status}</span></p>
        <h4 style="margin-top:20px;">배송 정보</h4>
        <p><strong>수령인:</strong> ${detail.recipient}</p>
        <p><strong>주소:</strong> ${detail.address} ${detail.addressDetail || ''}</p>
        <h4 style="margin-top:20px;">상품 목록</h4>
        <div id="itemList">${itemsHtml}</div>
        <div style="margin-top:15px; padding:10px; background:#f9f9f9; text-align:right;">
            선택된 취소/반품 금액: <strong id="selectedAmount" style="color:red;">0</strong>원
        </div>
        <h3 style="margin-top:20px; text-align:right;">최종 결제 금액: ${detail.totalAmount?.toLocaleString() || detail.total?.toLocaleString()}원</h3>
    `;

    renderBottomButtons(detail, mode);
    showOrderDetail();
}

function renderBottomButtons(detail, mode) {
    const section = document.getElementById('orderDetailSection');
    const statusKey = (detail.orderStatus || detail.status || '').toString();
    const isPending = statusKey === 'PENDING' || detail.status === ORDER_STATUS.PENDING;
    const isDelivered = statusKey === 'DELIVERED' || detail.status === ORDER_STATUS.DELIVERED;
    const isShipping = statusKey === 'SHIPPING' || detail.status === ORDER_STATUS.SHIPPING;

    // 기존에 생성된 하단 버튼 컨테이너가 있다면 제거 (중복 방지)
    const oldBtnContainer = document.querySelector('.detail-bottom-actions');
    if (oldBtnContainer) oldBtnContainer.remove();

    // 새 버튼 컨테이너 생성
    const btnContainer = document.createElement('div');
    btnContainer.className = 'detail-bottom-actions';
    btnContainer.style.marginTop = '20px';
    btnContainer.style.display = 'flex';
    btnContainer.style.gap = '10px';

    // 1. 목록으로 돌아가기 버튼 (기존 HTML에 있는 버튼은 숨기고 새로 생성)
    const originalBackBtn = document.getElementById('backToHistory');
    if (originalBackBtn) originalBackBtn.classList.add('hidden');

    const newBackBtn = document.createElement('button');
    newBackBtn.className = 'btn-secondary';
    newBackBtn.textContent = mode === 'GUEST_MODE' ? '다른 주문 조회하기' : '목록으로 돌아가기';
    newBackBtn.onclick = () => {
        IS_MEMBER_LOGGED_IN ? showMemberHistory() : showGuestLookupForm();
    };
    btnContainer.appendChild(newBackBtn);

    // 2. 주문 상태에 따른 액션 버튼 추가 (오른쪽 배치)
    if (isPending) {
        const cancelBtn = document.createElement('button');
        cancelBtn.className = 'btn-primary';
        cancelBtn.style.backgroundColor = '#e74c3c'; // 취소 버튼 강조색
        cancelBtn.textContent = '선택 항목 취소';
        cancelBtn.onclick = () => showModal('cancel', detail);
        btnContainer.appendChild(cancelBtn);
    } else if (isDelivered || isShipping) {
        const returnBtn = document.createElement('button');
        returnBtn.className = 'btn-primary';
        returnBtn.textContent = '선택 항목 반품';
        returnBtn.onclick = () => showModal('return', detail);
        btnContainer.appendChild(returnBtn);
    }

    // 상세 섹션의 가장 마지막에 버튼 바 추가
    section.appendChild(btnContainer);
}

function updateSelectedAmount() {
    let total = 0;
    document.querySelectorAll('.item-checkbox:checked').forEach(cb => {
        total += parseInt(cb.dataset.price);
    });
    const display = document.getElementById('selectedAmount');
    if (display) display.textContent = total.toLocaleString();
}

/*
 * --- 모달 및 공통 기능 (기존 유지) ---
 */
function showModal(actionType, detail) {
    const selected = document.querySelectorAll('.item-checkbox:checked');
    if (selected.length === 0) return alert('항목을 선택해주세요.');

    const modal = document.getElementById('actionModal');
    const amount = Array.from(selected).reduce((sum, cb) => sum + parseInt(cb.dataset.price), 0);

    document.getElementById('modalTitle').textContent = actionType === 'cancel' ? '부분 취소' : '부분 반품';
    document.getElementById('modalOrderId').textContent = detail.id;
    document.getElementById('modalAmount').textContent = amount.toLocaleString();

    const reasonGroup = document.getElementById('reasonGroup');
    reasonGroup.innerHTML = '';
    if (actionType === 'cancel') {
        reasonGroup.innerHTML = '<label>취소 사유</label><textarea id="actionReason" style="width:100%;" rows="3"></textarea>';
    } else {
        let options = Object.entries(RETURN_REASON).map(([k, v]) => `<option value="${k}">${v}</option>`).join('');
        reasonGroup.innerHTML = `
            <label>반품 사유</label>
            <select id="returnReason" style="width:100%; margin-bottom:10px;">${options}</select>
            <textarea id="actionReason" style="width:100%;" placeholder="상세 사유"></textarea>`;
    }

    document.getElementById('confirmActionButton').onclick = () => handleActionRequest(actionType, detail, amount);
    modal.classList.remove('hidden');
}

// 사용자가 취소 or 반품 클릭했을때 실행
async function handleActionRequest(type, detail, amount) {
    const selectedCheckboxes = document.querySelectorAll('.item-checkbox:checked');
    const itemIds = Array.from(selectedCheckboxes).map(cb => cb.dataset.id);
    const reason = document.getElementById('actionReason')?.value || "";
    const returnReason = document.getElementById('returnReason')?.value || null;

    try {
        const response = await fetch(`${API_BASE}/${detail.id}/${type}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${getCookie('accessToken')}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                orderItemIdList: itemIds,
                reason: reason,
                returnReasonCode: returnReason,
                cancelAmount: amount
            })
        });

        if (!response.ok) throw new Error("요청 처리 실패");

        alert(`성공적으로 처리되었습니다.\n금액: ${amount.toLocaleString()}원`);
        hideModal();
        location.reload(); // 데이터 갱신을 위해 페이지 새로고침
    } catch (error) {
        console.error("Error:", error);
        alert("요청 중 오류가 발생했습니다.");
    }
}

// (취소/환불)모달창 숨기기
function hideModal() {
    document.getElementById('actionModal').classList.add('hidden');
}

function setupModalListeners() {
    document.querySelector('#actionModal .close-button')?.addEventListener('click', hideModal);
}

/*
 * --- 주문 내역에 가이드 추가 ---
 */

// 모든 섹션 숨기기 함수 수정
function hideAllSections() {
    const guestLookupSection = document.getElementById('guestLookupSection');
    const memberHistorySection = document.getElementById('memberHistorySection');
    const orderDetailSection = document.getElementById('orderDetailSection');

    guestLookupSection?.classList.add('hidden');
    memberHistorySection?.classList.add('hidden');
    orderDetailSection?.classList.add('hidden');

    // 가이드 섹션도 일단 전체 숨김.
    const guideSection = document.querySelector('.order-guide-section');
    if (guideSection) guideSection.classList.add('hidden');
}

// 비회원 조회 폼 보여주기
function showGuestLookupForm() {
    hideAllSections();
    const guestLookupSection = document.getElementById('guestLookupSection');
    guestLookupSection?.classList.remove('hidden');

    // 가이드 표시
    const guideSection = document.querySelector('.order-guide-section');
    if (guideSection) guideSection.classList.remove('hidden');
}

// 회원 주문 목록 보여주기
function showMemberHistory() {
    hideAllSections();
    const memberHistorySection = document.getElementById('memberHistorySection');
    memberHistorySection?.classList.remove('hidden');

    // 가이드 표시
    const guideSection = document.querySelector('.order-guide-section');
    if (guideSection) guideSection.classList.remove('hidden');
}

// 주문 상세 정보 보여주기 (여기서는 가이드 표시 x)
function showOrderDetail() {
    hideAllSections();
    const orderDetailSection = document.getElementById('orderDetailSection');
    orderDetailSection?.classList.remove('hidden');

    // 상세 페이지에서는 가이드를 숨김
    const guideSection = document.querySelector('.order-guide-section');
    if (guideSection) guideSection.classList.add('hidden');
}

/*
 * --- 검색 및 정렬 기능 (기존 유지) ---
 */
function initializeFilterForm() {
    const currentYear = new Date().getFullYear();
    const filterYear = document.getElementById('filterYear');
    const filterMonth = document.getElementById('filterMonth');
    const filterStatus = document.getElementById('filterStatus');
    if (!filterYear || !filterMonth || !filterStatus) return;
    filterYear.innerHTML = '<option value="all">전체보기</option>';
    for (let y = currentYear; y >= currentYear - 3; y--) filterYear.innerHTML += `<option value="${y}">${y}년</option>`;
    filterMonth.innerHTML = '<option value="all">전체보기</option>';
    for (let m = 1; m <= 12; m++) filterMonth.innerHTML += `<option value="${String(m).padStart(2, '0')}">${m}월</option>`;
    filterStatus.innerHTML = '<option value="all">전체보기</option>';
    Object.keys(ORDER_STATUS).forEach(k => filterStatus.innerHTML += `<option value="${k}">${ORDER_STATUS[k]}</option>`);
}

async function fetchMemberOrders() {
    // 서버에서 타임리프로 내려준 초기 데이터 사용 (페이지네이션 여부와 무관하게 클라이언트 필터에 활용)
    memberOrders = Array.isArray(window.INITIAL_ORDERS)
        ? window.INITIAL_ORDERS.filter(o => (o.orderStatus || o.status) !== 'PENDING')
        : [];
    sortOrdersAndRender('latest', memberOrders);
}

function sortOrdersAndRender(sortType, orders) {
    const sorted = [...(orders || memberOrders)].sort((a, b) =>
        sortType === 'latest'
            ? new Date(b.orderDateTime || b.date) - new Date(a.orderDateTime || a.date)
            : new Date(a.orderDateTime || a.date) - new Date(b.orderDateTime || b.date));
    renderOrderList(sorted);
}

// 필터 폼 제출 시 실행
async function handleOrderFiltering(e) {
    e.preventDefault(); // 페이지 새로고침 방지

    const filters = getCurrentFilters();
    const filtered = filterOrders(memberOrders, filters);
    sortOrdersAndRender(document.getElementById('sortOrderSelect')?.value || 'latest', filtered);
}

// 화면의 (년도, 월, 상태, 키워드) <- 현재 사용자가 선택하거나 입력한 값들 수집하여 하나 객체로 반환
function getCurrentFilters() {
    return {
        year: document.getElementById('filterYear').value,
        month: document.getElementById('filterMonth').value,
        status: document.getElementById('filterStatus').value,
        searchType: document.getElementById('searchType').value,
        keyword: document.getElementById('searchKeyword').value
            .trim()
    };
}

// 실제 데이터 필터링 로직
function filterOrders(orders, f) {
    const keyword = (f.keyword || '').toLowerCase();
    return (orders || []).filter(o => {
        const d = new Date(o.orderDateTime || o.date || '');
        if (f.year !== 'all' && String(d.getFullYear()) !== f.year) return false;
        if (f.month !== 'all' && String(d.getMonth() + 1).padStart(2, '0') !== f.month) return false;

        if (f.status && f.status !== 'all') {
            const statusKey = o.orderStatus || o.status;
            if (statusKey !== f.status) return false;
        }

        if (keyword) {
            if (f.searchType === 'orderItemName') {
                const title = (o.orderTitle || '').toLowerCase();
                if (!title.includes(keyword)) return false;
            } else if (f.searchType === 'orderNumber') {
                const num = (o.orderNumber || o.orderId || '').toString().toLowerCase();
                if (!num.includes(keyword)) return false;
            } else if (f.searchType === 'recipientName') {
                const recipient = (o.recipient || '').toLowerCase();
                if (!recipient.includes(keyword)) return false;
            }
        }
        return true;
    });
}

function formatDate(value) {
    try {
        const d = new Date(value);
        if (Number.isNaN(d.getTime())) return '';
        const yy = d.getFullYear();
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        const hh = String(d.getHours()).padStart(2, '0');
        const mi = String(d.getMinutes()).padStart(2, '0');
        return `${yy}-${mm}-${dd} ${hh}:${mi}`;
    } catch (e) {
        return '';
    }
}

// 비회원이 주문 정보를 입력하고 조회 버튼 눌렀을때 실행
async function handleGuestLookup(e) {
    e.preventDefault();
    const orderNumber = document.getElementById('guestOrderId').value;
    const name = document.getElementById('guestOrderer').value;
    const password = document.getElementById('guestPassword').value;

    try {
        const response = await fetch(`${API_BASE}/guest/lookup`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({orderNumber, name, password})
        });

        if (!response.ok) throw new Error("주문 정보를 찾을 수 없습니다.");

        const orderDetail = await response.json();
        // 조회 성공하면 상세 UI를 렌더링
        renderOrderDetailUI(orderDetail, 'GUEST_MODE');
    } catch (error) {
        alert(error.message);
    }
}
