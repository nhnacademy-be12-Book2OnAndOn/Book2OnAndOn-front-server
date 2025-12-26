// --- 상수 및 전역 변수 영역 (Order & Payment 공통) ---
const API_BASE = {
    CART: '/cart',
    ORDER: '/orders', // Mock 환경에서 서버 통신 없이 사용
    WRAP: '/wrappapers',
    TOSS_CONFIRM: '/payment/TOSS/confirm'
};

const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
};

const USER_ID = window.USER_ID || null;
const ensureGuestId = () => {
    // 공통 ensureGuestId가 있으면 그대로 사용
    if (typeof window.ensureGuestId === 'function') {
        return window.ensureGuestId();
    }
    // 로컬 저장소/쿠키에서 우선 조회
    let gid = localStorage.getItem('uuid') || getCookie('GUEST_ID') || getCookie('guestId');
    if (!gid) {
        gid = `guest-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
    }
    // 저장 및 쿠키 설정
    try { localStorage.setItem('uuid', gid); } catch (e) { /* ignore */ }
    document.cookie = `GUEST_ID=${encodeURIComponent(gid)}; path=/; max-age=${60 * 60 * 24 * 30}`;
    document.cookie = `guestId=${encodeURIComponent(gid)}; path=/; max-age=${60 * 60 * 24 * 30}`;
    return gid;
};
const GUEST_ID = ensureGuestId();
const IS_USER = !!getCookie('accessToken');



const TOSS_CLIENT_KEY = "test_ck_Z1aOwX7K8m1x1vJ2AgDQ8yQxzvNP";
const FIXED_DELIVERY_FEE = 3000;
const FREE_DELIVERY_THRESHOLD = 30000;

let cartData = null;
let wrapOptions = [];
let selectedWrapData = {};
let currentBookId = null;
let userPointBalance = 0;

// --- 1. 초기화 및 데이터 로드 ---
document.addEventListener('DOMContentLoaded', async () => {
    setDeliveryDateOptions();
    await loadInitialData();
    setupEventListeners();
    calculateFinalAmount();
});


// =================================================================
// I. ORDER LOGIC (주문 상품, 배송지, 포장지 관리)
// =================================================================

async function loadInitialData() {
    try {
        const headers = { 'Content-Type': 'application/json' };
        if (IS_USER) headers['Authorization'] = `Bearer ${getCookie('accessToken')}`;
        else headers['X-Guest-Id'] = GUEST_ID;

        const cartEndpoint = IS_USER ? `${API_BASE.CART}/user/items/selected` : `${API_BASE.CART}/guest/selected`;

        const [cartRes, wrapRes, pointRes] = await Promise.all([
            fetch(cartEndpoint, { headers }),
            fetch(`${API_BASE.WRAP}`, { headers }),
            IS_USER ? fetch(`/api/user/me/points/api/current`, { headers }) : Promise.resolve(null)
        ]);
        const couponRes = IS_USER ? await fetch('/coupons/me', { headers }) : null;
        if (couponRes && couponRes.ok) {
            const coupons = await couponRes.json();
            renderCouponOptions(coupons);
        }

        if (cartRes.ok) cartData = await cartRes.json();
        if (wrapRes.ok) wrapOptions = await wrapRes.json();
        if (pointRes && pointRes.ok) {
            const pointData = await pointRes.json();
            userPointBalance = pointData.currentPoint;
        }

        renderProductList();
        updatePointUI();
        calculateFinalAmount();
    } catch (error) {
        console.error("데이터 로드 실패:", error);
    }
}

function renderCouponOptions(coupons) {
    const select = document.getElementById('couponSelect');
    coupons.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.discountAmount;
        opt.textContent = `${c.couponName} (-${c.discountAmount.toLocaleString()}원)`;
        select.appendChild(opt);
    });
}

function updatePointUI() {
    const el = document.getElementById('currentPointValue');
    if (el) el.textContent = `${userPointBalance.toLocaleString()} P`;
}

function setupEventListeners() {

    // 1. 배송 메시지 동적 입력 로직 (Order)
    const messageSelect = document.getElementById('deliveryMessage');
    const customInput = document.getElementById('customDeliveryMessage');
    if (messageSelect) {
        messageSelect.addEventListener('change', (e) => {
            if (e.target.value === 'direct_input') {
                customInput.style.display = 'block';
                customInput.focus();
            } else {
                customInput.style.display = 'none';
                customInput.value = '';
            }
        });
    }

    // 2. 주소 검색 버튼 이벤트 설정 (Order)
    document.querySelector('.btn-search-address')?.addEventListener('click', openPostcodeSearch);

    // 3. 최종 결제 버튼 이벤트 설정 (Payment)
    document.getElementById('requestTossPayment')?.addEventListener('click', handleTossPaymentRequest);

    // 4. 포장 토글 및 버튼 활성화 리스너 (Order)
    setupWrapToggleListeners();

    // 5. 포장지 선택 버튼 클릭 이벤트 (모달 오픈)
    document.getElementById('selectedProductList')?.addEventListener('click', (e) => {
        if (e.target.classList.contains('btn-select-wrap')) {
            const bookId = Number(e.target.getAttribute('data-book-id'));

            const itemTitleFull = e.target.closest('.order-item-detail')
                .querySelector('.item-title').textContent;
            const itemTitle = itemTitleFull.substring(0, itemTitleFull.lastIndexOf('(')).trim();

            if (!e.target.disabled) {
                openWrappingModal(bookId, itemTitle);
            }
        }
    });

    // 6. 모달 닫기 버튼 및 외부 클릭 설정 (Order)
    document.querySelector('#wrappingModal .close-button')?.addEventListener('click', closeModal);
    window.addEventListener('click', (e) => {
        if (e.target === document.getElementById('wrappingModal')) {
            closeModal();
        }
    });

    // 7. 할인 계산 이벤트 리스너 (Payment)
    document.getElementById('couponSelect')?.addEventListener('change', calculateFinalAmount);
    document.getElementById('pointDiscountAmount')?.addEventListener('blur', (e) => {
        let val = Number(e.target.value);
        if(val > userPointBalance){
            alert('최대 ${userPointBalance.toLocaleString()}P까지 사용 가능합니다.');
            e.target.value = userPointBalance;
        }
        if(val < 0){
            e.target.value = 0;
        }
        calculateFinalAmount();
    });
}

function renderProductList() {
    const listContainer = document.getElementById('selectedProductList');
    if (!listContainer || !cartData) return;
    listContainer.innerHTML = '';

    cartData.items.forEach(item => {
        const currentWrapId = selectedWrapData[item.bookId];
        const wrapText = currentWrapId ? `선택됨: ${getWrapNameById(currentWrapId)}` : '포장지 선택/변경';
        const totalItemPrice = (item.price * item.quantity).toLocaleString();

        listContainer.innerHTML += `
            <div class="order-item-detail" data-book-id="${item.bookId}">
                <div class="item-info">
                    <span class="item-title">${item.bookTitle || item.title} (${item.quantity}권)</span>
                    <span class="item-price">가격: ${totalItemPrice}원</span>
                </div>
                <div class="item-wrap-option">
                    ${item.isPackable !== false ? `
                        <label><input type="checkbox" class="wrap-toggle" data-book-id="${item.bookId}" ${currentWrapId ? 'checked' : ''}> 포장 선택</label>
                        <button type="button" class="btn-select-wrap" data-book-id="${item.bookId}" ${currentWrapId ? '' : 'disabled'}>${wrapText}</button>
                    ` : '<span class="non-packable">포장 불가</span>'}
                </div>
            </div>`;
    });
}

function getWrapNameById(id) {
    const wrap = wrapOptions.find(opt => opt.wrappingPaperId === id);
    return wrap ? wrap.wrappingPaperName : '선택됨';
}

function getWrapDataById(id) {
    return wrapOptions.find(opt => opt.wrappingPaperId === id);
}

// 포장지 버튼
function setupWrapToggleListeners() {
    document.getElementById('selectedProductList')?.addEventListener('change', (e) => {
        if (e.target.classList.contains('wrap-toggle')) {
            const bookId = Number(e.target.getAttribute('data-book-id'));
            const selectButton = e.target.closest('.item-wrap-option').querySelector('.btn-select-wrap');

            selectButton.disabled = !e.target.checked;

            if (e.target.checked) {
                const itemTitleFull = e.target.closest('.order-item-detail')
                    .querySelector('.item-title').textContent;
                const itemTitle = itemTitleFull.substring(0, itemTitleFull.lastIndexOf('(')).trim();

                if (!selectedWrapData[bookId]) {
                    openWrappingModal(bookId, itemTitle);
                }
            } else {
                selectedWrapData[bookId] = null;
                selectButton.textContent = '포장지 선택/변경';
            }
            calculateFinalAmount();
        }
    });
}

// 포장지 옵션
function openWrappingModal(bookId, bookTitle) {
    currentBookId = bookId;
    const modalElement = document.getElementById('wrappingModal');
    if (!modalElement) {
        console.error("Fatal Error: wrappingModal 요소를 찾을 수 없습니다.");
        return;
    }

    document.getElementById('modalTitle').textContent = `[${bookTitle}] 포장 옵션 선택`;
    renderOptionsInModal();
    modalElement.style.display = 'block';

    const currentSelection = selectedWrapData[bookId];
    document.querySelectorAll('.wrap-card').forEach(c => c.classList.remove('selected'));
    if (currentSelection) {
        const selectedCard = document.querySelector(`.wrap-card[data-wrap-id="${currentSelection}"]`);
        if (selectedCard) selectedCard.classList.add('selected');
        document.getElementById('confirmWrapButton').disabled = false;
    } else {
        document.getElementById('confirmWrapButton').disabled = true;
    }
}

function closeModal() {
    document.getElementById('wrappingModal').style.display = 'none';
}

function renderOptionsInModal() {
    const optionsContainer = document.getElementById('wrappingOptions');
    if (!optionsContainer) return;
    optionsContainer.innerHTML = '';

    wrapOptions.forEach(option => {
        const card = document.createElement('div');
        card.className = 'wrap-card';
        card.setAttribute('data-wrap-id', option.wrappingPaperId);
        card.innerHTML = `
            <img src="${option.wrappingPaperPath}" alt="${option.wrappingPaperName}">
            <p><strong>${option.wrappingPaperName}</strong></p>
            <p>${option.wrappingPaperPrice.toLocaleString()}원</p>
        `;
        card.addEventListener('click', () => {
            handleOptionSelection(card, option);
        });
        optionsContainer.appendChild(card);
    });
}

function handleOptionSelection(selectedCard, wrapData) {
    document.querySelectorAll('.wrap-card').forEach(c => c.classList.remove('selected'));
    selectedCard.classList.add('selected');
    selectedWrapData[currentBookId] = wrapData.wrappingPaperId;

    const confirmButton = document.getElementById('confirmWrapButton');
    confirmButton.disabled = false;
    confirmButton.onclick = () => {
        finalizeWrapSelection(currentBookId, wrapData);
    };
}

function finalizeWrapSelection(bookId, wrapData) {
    closeModal();
    const selectButton = document.querySelector(`.order-item-detail[data-book-id="${bookId}"] .btn-select-wrap`);
    if (selectButton) {
        selectButton.textContent = `${wrapData.wrappingPaperName} (+${wrapData.wrappingPaperPrice.toLocaleString()}원) 선택 완료`;
    }
    calculateFinalAmount();
}

function collectOrderItems() {
    if (!cartData || !cartData.items) return [];

    return cartData.items.map(item => {
        const container = document.querySelector(`.order-item-detail[data-book-id="${item.bookId}"]`);
        const isWrappedCheckbox = container ? container.querySelector(`.wrap-toggle`) : null;
        const isWrapped = isWrappedCheckbox && isWrappedCheckbox.checked;
        const wrappingPaperId = isWrapped ? selectedWrapData[item.bookId] : null;

        const wrapData = wrappingPaperId ? getWrapDataById(wrappingPaperId) : null;

        return {
            bookId: item.bookId,
            quantity: item.quantity,
            wrappingPaperId: wrappingPaperId,
            isWrapped: isWrapped,
            wrappingPaperPrice: wrapData ? wrapData.wrappingPaperPrice : 0
        };
    });
}

function collectDeliveryAddress() {
    let deliveryMessage = document.getElementById('deliveryMessage')?.value;

    if (deliveryMessage === 'direct_input') {
        deliveryMessage = document.getElementById('customDeliveryMessage')?.value || '요청사항 없음';
    }

    return {
        deliveryAddress: document.getElementById('deliveryAddress')?.value,
        deliveryAddressDetail: document.getElementById('deliveryAddressDetail')?.value,
        deliveryMessage: deliveryMessage,
        recipient: document.getElementById('recipient')?.value,
        recipientPhonenumber: document.getElementById('recipientPhonenumber')?.value.replace(/[^0-9]/g, '')
    };
}

function validateInputs(address, orderItems) {
    if (!address.recipient || !address.recipientPhonenumber || !address.deliveryAddress || !document.getElementById('wantDeliveryDate')?.value) {
        alert('수령인 정보, 주소, 연락처, 희망 배송일을 모두 입력해주세요.');
        return false;
    }
    const phoneRegex = /^\d{11}$/;
    if (!phoneRegex.test(address.recipientPhonenumber)) {
        alert('연락처 형식이 올바르지 않습니다. 11자리 숫자로 입력해주세요.');
        return false;
    }
    for (const item of orderItems) {
        if (item.isWrapped && !item.wrappingPaperId) {
            alert(`도서 ID ${item.bookId}에 대해 포장을 선택했지만, 포장지 종류를 선택하지 않았습니다.`);
            return false;
        }
    }
    return true;
}

// 배송 희망날짜
function setDeliveryDateOptions() {
    const container = document.getElementById('deliveryDateOptions');
    if (!container) return;
    container.innerHTML = '';

    const today = new Date();
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    const MAX_OPTIONS_TO_SHOW = 7;

    const setHiddenDate = (dateString) => {
        const hiddenInput = document.getElementById('wantDeliveryDate');
        if (hiddenInput) {
            hiddenInput.value = dateString;
            hiddenInput.dispatchEvent(new Event('change'));
        }
    };

    let generatedCount = 0;
    let daysToAdd = 0;

    while (generatedCount < MAX_OPTIONS_TO_SHOW) {
        const currentDay = new Date(today);
        currentDay.setDate(today.getDate() + daysToAdd);

        const dayOfWeek = currentDay.getDay();

        if (dayOfWeek !== 0) { // 일요일이 아니면 버튼 생성
            const dateString = `${currentDay.getFullYear()}-${String(currentDay.getMonth() + 1).padStart(2, '0')}-${String(currentDay.getDate()).padStart(2, '0')}`;
            const displayDay = days[dayOfWeek];
            const displayDate = `${currentDay.getMonth() + 1}/${currentDay.getDate()}`;

            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'date-option-button';
            button.setAttribute('data-date', dateString);

            let dayTextDisplay = displayDay;
            if (generatedCount === 0) {
                dayTextDisplay = '오늘';
            } else if (generatedCount === 1) {
                dayTextDisplay = '내일';
            }

            button.innerHTML = `<span class="day-of-week">${dayTextDisplay} (${displayDay})</span><span class="date-text">${displayDate}</span>`;

            button.addEventListener('click', () => {
                document.querySelectorAll('.date-option-button').forEach(btn => btn.classList.remove('selected'));
                button.classList.add('selected');
                setHiddenDate(dateString);
            });

            container.appendChild(button);
            generatedCount++;
        }

        daysToAdd++;
    }

    const firstButton = document.querySelector('.date-option-button');
    if (firstButton) {
        firstButton.click();
    }
}

// 주소 검색
function openPostcodeSearch() {
    if (typeof daum === 'undefined' || !daum.Postcode) {
        alert("Daum Postcode SDK가 로드되지 않았습니다. HTML 스크립트 태그를 확인해주세요.");
        return;
    }
    new daum.Postcode({
        oncomplete: function(data) {
            let addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
            document.getElementById('deliveryAddress').value = addr;
            document.getElementById('deliveryAddressDetail').focus();
        },
        width : '100%',
        height : '100%'
    }).open();
}


// =================================================================
// II. PAYMENT LOGIC (할인, 금액 계산, 결제 요청)
// =================================================================

// --- 금액 계산 ---
function calculateFinalAmount() {
    if (!cartData) return;
    const totalItemPrice = cartData.selectedTotalPrice || 0;
    const couponDiscount = Number(document.getElementById('couponSelect')?.value) || 0;
    let pointDiscount = Number(document.getElementById('pointDiscountAmount')?.value) || 0;

    pointDiscount = Math.min(pointDiscount, userPointBalance);
    const orderItems = collectOrderItems();
    const result = calculateFeesAndDiscounts(totalItemPrice, couponDiscount, pointDiscount, orderItems);

    document.getElementById('summaryTotalItemPrice').textContent = `${totalItemPrice.toLocaleString()}원`;
    document.getElementById('deliveryFee').textContent = `${result.deliveryFee.toLocaleString()}원`;
    document.getElementById('wrappingFee').textContent = `${result.wrappingFee.toLocaleString()}원`;
    document.getElementById('couponDiscount').textContent = `-${couponDiscount.toLocaleString()}원`;
    document.getElementById('pointDiscount').textContent = `-${pointDiscount.toLocaleString()}원`;

    const finalStr = `${result.finalAmount.toLocaleString()}원`;
    document.getElementById('finalPaymentAmount').textContent = finalStr;
    document.getElementById('finalPaymentButtonText').textContent = `${finalStr} 결제하기`;
}

function calculateFeesAndDiscounts(totalItemPrice, coupon, point, items) {
    const wrappingFee = items.reduce((sum, i) => sum + (i.isWrapped ? (i.wrappingPaperPrice * i.quantity) : 0), 0);
    const deliveryFee = (totalItemPrice - coupon) >= FREE_DELIVERY_THRESHOLD ? 0 : FIXED_DELIVERY_FEE;
    return {
        deliveryFee, wrappingFee,
        finalAmount: Math.max(0, totalItemPrice + deliveryFee + wrappingFee - coupon - point)
    };
}

async function handleTossPaymentRequest() {
    const orderItems = collectOrderItems();
    const address = collectDeliveryAddress();
    if (!validateInputs(address, orderItems)) return;

    try {
        const headers = { 'Content-Type': 'application/json' };
        if (IS_USER) headers['Authorization'] = `Bearer ${getCookie('accessToken')}`;
        else headers['X-Guest-Id'] = GUEST_ID;

        // 1. 서버에 주문 생성
        const response = await fetch(API_BASE.ORDER, {
            method: 'POST',
            headers,
            body: JSON.stringify({
                orderItems,
                ...address,
                couponDiscount: Number(document.getElementById('couponSelect').value),
                pointUsage: Number(document.getElementById('pointDiscountAmount').value),
                wantDeliveryDate: document.getElementById('wantDeliveryDate').value
            })
        });

        if(!response.ok){
            throw new Error("주문 서버 생성 실패");
        }
        const orderResult = await response.json();

        // 토스 결제창에 표시할 주문명 생성
        const firstItem = cartData.items[0];
        const firstTitle = (firstItem.bookTitle || firstItem.title).split('(')[0].trim();

        //상품이 2건 이상이면 "제목 외 N건" , 1건이면 "제목"만 표시
        const orderName = cartData.items.length > 1 ? `${firstTitle} 외 ${cartData.items.length - 1}건` : firstTitle;

        // 2. 토스 결제창 열기
        await requestTossPaymentV2(
            orderResult.totalAmount,
            orderResult.orderNumber,
            `${cartData.items[0].bookTitle} 외`,
            document.querySelector('input[name="paymentMethod"]:checked').value,
            address.recipient,
            'user@example.com'
        );
    } catch (e) {
        console.error("결제 프로세스 오류:", e);
        alert("주문 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
}

// [Toss Payment V2 Logic] V2 연쇄 호출 구조
async function requestTossPaymentV2(amount, orderId, orderName, method, customerName, customerEmail) {
    const toss = TossPayments(TOSS_CLIENT_KEY);
    const payment = toss.payment({ customerKey: IS_USER ? String(USER_ID) : TossPayments.ANONYMOUS });
    await payment.requestPayment({
        method, amount: { currency: "KRW", value: amount },
        orderId, orderName, customerName, customerEmail,
        successUrl: window.location.origin + API_BASE.TOSS_CONFIRM,
        failUrl: window.location.origin + "/fail.html"
    });
}
