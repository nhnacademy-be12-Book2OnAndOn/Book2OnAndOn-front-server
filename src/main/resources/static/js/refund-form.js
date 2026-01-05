const ORDER_API_BASE = '/orders';

// orderItemId -> unitPrice
let UNIT_PRICE_MAP = new Map();

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

async function loadUnitPriceMap() {
    const form = document.getElementById('refundForm');
    const orderId = form?.dataset?.orderId;

    console.log('[REFUND_FORM_JS] data-order-id =', orderId);
    console.log('[REFUND_FORM_JS] fetch url =', `/orders/${orderId}`);

    if (!orderId) return;

    const headers = { 'Content-Type': 'application/json' };

    // 회원/비회원 모두 대응: 이미 프로젝트에서 쿠키로 토큰 쓰고 있으니 그대로 사용
    const accessToken = getCookie('accessToken');
    if (accessToken) headers['Authorization'] = `Bearer ${accessToken}`;

    // 게스트 토큰을 쿠키로 쓰는 구조면 이것도 추가 가능
    const guestOrderToken = getCookie('guestOrderToken');
    if (guestOrderToken) headers['X-Guest-Order-Token'] = guestOrderToken;

    const res = await fetch(`${ORDER_API_BASE}/${orderId}`, {
        method: 'GET',
        headers,
        credentials: 'include' // 쿠키 기반이면 이게 안전
    });

    if (!res.ok) {
        console.warn('[REFUND_FORM_JS] order detail fetch failed:', res.status);
        return;
    }

    const detail = await res.json();

    // detail.orderItems (또는 detail.items) 구조 둘 다 대응
    const items = Array.isArray(detail.orderItems) ? detail.orderItems : (detail.items || []);
    items.forEach(it => {
        const orderItemId = it.orderItemId ?? it.id; // 너 코드에 두 케이스가 섞여있었음
        const unitPrice = Number(it.unitPrice ?? it.price ?? 0);

        if (orderItemId != null && unitPrice > 0) {
            UNIT_PRICE_MAP.set(String(orderItemId), unitPrice);
        }
    });

    // 체크박스에 data-unit-price를 주입해두면 이후 계산이 단순해짐
    document.querySelectorAll('.item-check').forEach(chk => {
        const oid = chk.dataset.orderItemId;
        const p = UNIT_PRICE_MAP.get(String(oid)) ?? 0;
        chk.dataset.unitPrice = String(p);
    });
}

const POLICY = {
    CHANGE_OF_MIND_DELIVERY_FEE: 3000,
    APPLY_FEE_REASON: 'CHANGE_OF_MIND',
    FREE_FEE_MIN_AMOUNT: null
};

window.onload = async function () {
    console.log("[REFUND_FORM_JS] loaded");
    console.log("[REFUND_FORM_JS] checkbox count =", document.querySelectorAll('.item-check').length);
    console.log("[REFUND_FORM_JS] itemsList html length =", document.getElementById('itemsList')?.innerHTML?.length);

    await loadUnitPriceMap();  // 가격 먼저 로드
    recalcSummary();           // 그 다음 계산
};

function toggleRow(wrapper) {
    const checkbox = wrapper.querySelector('.item-check');
    if (!checkbox) return;
    checkbox.checked = !checkbox.checked;
    toggleRowUI(wrapper, checkbox.checked);
    recalcSummary();
}

function toggleRowFromCheckbox(checkbox) {
    const wrapper = checkbox.closest('.item-checkbox');
    toggleRowUI(wrapper, checkbox.checked);
    recalcSummary();
}

function toggleRowUI(wrapperEl, isChecked) {
    if (!wrapperEl) return;
    if (isChecked) wrapperEl.classList.add('selected');
    else wrapperEl.classList.remove('selected');
}

function recalcSummary() {
    const checks = document.querySelectorAll('.item-check');
    let selectedItemCount = 0;
    let selectedQtySum = 0;
    let selectedAmount = 0;

    checks.forEach(chk => {
        if (!chk.checked) return;

        selectedItemCount++;

        const qty = Number(chk.dataset.qty || 0);
        const unitPrice = Number(chk.dataset.unitPrice || 0);

        selectedQtySum += qty;
        selectedAmount += (unitPrice * qty);
    });

    const reason = document.getElementById('refundReason')?.value;
    let shippingDeduction = 0;

    if (selectedItemCount > 0 && reason === POLICY.APPLY_FEE_REASON) {
        shippingDeduction = POLICY.CHANGE_OF_MIND_DELIVERY_FEE;
    }

    const expectedRefund = Math.max(0, selectedAmount - shippingDeduction);

    document.getElementById('summaryCount').textContent = `상품 ${selectedItemCount}건 · 수량 ${selectedQtySum}개`;
    document.getElementById('summarySelectedAmount').textContent = `${selectedAmount.toLocaleString()}원`;
    document.getElementById('summaryShippingDeduction').textContent = `- ${shippingDeduction.toLocaleString()}원`;
    document.getElementById('summaryExpectedRefund').textContent = `${expectedRefund.toLocaleString()}원`;

    document.getElementById('submitBtn').disabled = (selectedItemCount === 0);

    rebuildHiddenInputs();
}


function rebuildHiddenInputs() {
    const hiddenBox = document.getElementById('selectedItemsHidden');
    hiddenBox.innerHTML = '';

    const checks = document.querySelectorAll('.item-check');
    let idx = 0;

    checks.forEach(chk => {
        if (!chk.checked) return;

        const orderItemId = chk.dataset.orderItemId;
        const qty = chk.dataset.qty;

        hiddenBox.insertAdjacentHTML('beforeend', `
      <input type="hidden" name="refundItems[${idx}].orderItemId" value="${orderItemId}">
      <input type="hidden" name="refundItems[${idx}].refundQuantity" value="${qty}">
    `);
        idx++;
    });
}

function goBack() {
    window.history.back();
}
