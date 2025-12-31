// =================================================================
// [상수 및 전역 변수 영역]
// =================================================================
const API_BASE = {
    CART: '/cart',
    ORDER: '/orders',
    WRAP: '/wrappapers',
    TOSS_CONFIRM: '/payment/TOSS/confirm'
};

const USER_ID = window.USER_ID || null;
const TOSS_CLIENT_KEY = "test_ck_Z1aOwX7K8m1x1vJ2AgDQ8yQxzvNP";
const FIXED_DELIVERY_FEE = 3000;
const FREE_DELIVERY_THRESHOLD = 30000;

// [중요] 데이터 관리용 전역 변수
let cartData = null;
let selectedWrapData = {};
let currentBookId = null;
let userPointBalance = 0;
let wrapOptions = [];
let globalCouponList = []; // 쿠폰 상세 정보를 저장할 리스트

// 서버 사이드 렌더링(SSR) 데이터 확인
const PREPARE_DATA = typeof window !== 'undefined' ? window.__ORDER_PREPARE__ : null;

// =================================================================
// [공통 헬퍼 함수]
// =================================================================
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return decodeURIComponent(parts.pop().split(';').shift());
}

const ensureGuestId = () => {
    if (typeof window.ensureGuestId === 'function') return window.ensureGuestId();
    let gid = localStorage.getItem('uuid') || getCookie('GUEST_ID');
    if (!gid) {
        gid = `guest-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
        try { localStorage.setItem('uuid', gid); } catch (e) {}
        document.cookie = `GUEST_ID=${encodeURIComponent(gid)}; path=/; max-age=${2592000}`; // 30일
    }
    return gid;
};
const GUEST_ID = ensureGuestId();
const IS_USER = !!getCookie('accessToken');

// =================================================================
// [1. 초기화 로직]
// =================================================================
document.addEventListener('DOMContentLoaded', async () => {
    // 1. UI 초기 세팅
    setAddressList();
    setDeliveryDateOptions();

    // 2. 데이터 로드 (장바구니, 쿠폰, 포인트 등)
    await loadInitialData();

    // 3. 이벤트 리스너 연결
    setupEventListeners();

    // 4. 배송 정책 로드 (라디오 버튼 생성)
    setDeliveryPolicies();

    // 5. 초기 금액 계산 실행
    calculateFinalAmount();
});

// =================================================================
// [2. 데이터 로드 및 렌더링]
// =================================================================
async function loadInitialData() {
    try {
        const headers = { 'Content-Type': 'application/json' };
        if (IS_USER) headers['Authorization'] = `Bearer ${getCookie('accessToken')}`;
        else headers['X-Guest-Id'] = GUEST_ID;

        // A. SSR 데이터가 있는 경우 (바로구매/장바구니 이동 직후)
        if (PREPARE_DATA && PREPARE_DATA.orderItems) {
            cartData = buildCartDataFromPrepare(PREPARE_DATA.orderItems);

            // [핵심] 쿠폰 데이터를 전역 변수에 저장
            if (PREPARE_DATA.coupons && Array.isArray(PREPARE_DATA.coupons)) {
                globalCouponList = PREPARE_DATA.coupons;
                // HTML select 박스가 비어있으면 렌더링
                renderCouponOptions(globalCouponList);
            }

            if (PREPARE_DATA.currentPoint) {
                userPointBalance = PREPARE_DATA.currentPoint.currentPoint || 0;
            }
            applyPrepareAddresses(PREPARE_DATA.addresses);

            // B. CSR 데이터 로드 (페이지 새로고침 등)
        } else {
            const cartEndpoint = IS_USER ? `${API_BASE.CART}/user/items/selected` : `${API_BASE.CART}/guest/selected`;
            const [cartRes, wrapRes, pointRes] = await Promise.all([
                fetch(cartEndpoint, { headers }),
                fetch(`${API_BASE.WRAP}`, { headers }),
                IS_USER ? fetch(`/api/user/me/points/api/current`, { headers }) : Promise.resolve(null)
            ]);

            // 쿠폰 조회 (회원인 경우만)
            if (IS_USER) {
                // 주의: GET /my-coupon/usable 또는 POST /my-coupon/usable (API 스펙에 맞게 수정 필요)
                // 여기서는 안전하게 GET 요청으로 가정하고 예외 처리 추가
                try {
                    const couponRes = await fetch('/my-coupon/usable', { headers });
                    if (couponRes.ok) {
                        const couponData = await couponRes.json();
                        // Page객체인지 List인지 확인
                        globalCouponList = Array.isArray(couponData) ? couponData : (couponData.content || []);
                        renderCouponOptions(globalCouponList);
                    }
                } catch (e) {
                    console.warn("쿠폰 목록 로드 실패", e);
                }
            }

            if (cartRes.ok) cartData = await cartRes.json();
            if (wrapRes.ok) wrapOptions = await wrapRes.json();
            if (pointRes && pointRes.ok) {
                const pData = await pointRes.json();
                userPointBalance = pData.currentPoint;
            }
        }
        updatePointUI();
    } catch (error) {
        console.error("데이터 로드 실패:", error);
    }
}

// 쿠폰 옵션 렌더링
function renderCouponOptions(coupons) {
    const select = document.getElementById('couponSelect');
    if (!select) return;

    // 기존 옵션 초기화 (첫 번째 '미사용' 옵션만 남기고 삭제하거나 새로 생성)
    select.innerHTML = '<option value="0">쿠폰 미사용</option>';

    if (!Array.isArray(coupons)) return;

    coupons.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.memberCouponId; // 값은 ID로 설정

        // 화면 표시 텍스트 구성
        const unit = c.discountType === 'PERCENT' ? '%' : '원';
        opt.textContent = `${c.couponName} (${c.discountValue.toLocaleString()}${unit} 할인)`;

        select.appendChild(opt);
    });
}

// =================================================================
// [3. 금액 계산 로직 (핵심 수정)]
// =================================================================
function calculateFinalAmount() {
    if (!cartData) return;

    // 1. 상품 총액 (할인 전)
    const totalItemPrice = Number(cartData.selectedTotalPrice || 0);

    // 2. 선택된 쿠폰 및 포인트 정보 가져오기
    const selectEl = document.getElementById('couponSelect');
    const selectedCouponId = selectEl ? Number(selectEl.value) : 0;

    let couponDiscount = 0;
    let pointDiscount = Number(document.getElementById('pointDiscountAmount')?.value) || 0;

    // 3. 쿠폰 할인 계산
    // HTML 태그 파싱 대신 전역 변수(globalCouponList)에서 객체를 찾음 -> 안전함
    const selectedCoupon = globalCouponList.find(c => c.memberCouponId === selectedCouponId);

    if (selectedCoupon) {
        // 쿠폰 상세 정보 추출
        const discountValue = selectedCoupon.discountValue;
        const discountType = selectedCoupon.discountType; // "FIXED" or "PERCENT"
        const minPrice = selectedCoupon.minPrice || 0;
        const maxPrice = selectedCoupon.maxPrice || 0;

        // 타겟(도서/카테고리) 확인. null이면 빈 배열로 처리
        const targetBookIds = selectedCoupon.targetBookIds || [];
        const targetCategoryIds = selectedCoupon.targetCategoryIds || [];

        // 할인 적용 대상 금액(Base Amount) 계산
        const discountBaseAmount = calculateDiscountBaseAmount(
            cartData.items, targetBookIds, targetCategoryIds, totalItemPrice
        );

        // 최소 주문 금액 검증
        if (discountBaseAmount >= minPrice) {
            if (discountType === 'FIXED') {
                // 정액 할인
                couponDiscount = discountValue;
            } else if (discountType === 'PERCENT') {
                // 정률 할인 (원 단위 절사)
                couponDiscount = Math.floor(discountBaseAmount * (discountValue / 100));

                // 최대 할인 한도 적용
                if (maxPrice > 0 && couponDiscount > maxPrice) {
                    couponDiscount = maxPrice;
                }
            }
        } else {
            console.log(`최소 주문 금액(${minPrice}원) 미달로 쿠폰 적용 불가`);
            // 필요 시 사용자에게 알림 표시 가능
        }
    }

    // 4. 배송비 및 최종 결제 금액 계산
    const orderItems = collectOrderItems();
    const result = calculateFeesAndDiscounts(totalItemPrice, couponDiscount, pointDiscount, orderItems);

    // 5. UI 업데이트
    updateSummaryUI(totalItemPrice, result.deliveryFee, result.wrappingFee, couponDiscount, pointDiscount, result.finalAmount);
}

// 타겟 도서/카테고리에 해당하는 상품 금액 합산
function calculateDiscountBaseAmount(cartItems, targetBookIds, targetCategoryIds, totalItemAmount) {
    const hasBookTargets = Array.isArray(targetBookIds) && targetBookIds.length > 0;
    const hasCategoryTargets = Array.isArray(targetCategoryIds) && targetCategoryIds.length > 0;

    // 타겟 조건이 없으면 전체 상품이 대상
    if (!hasBookTargets && !hasCategoryTargets) {
        return totalItemAmount;
    }

    // 타겟 조건이 있으면 해당하는 상품만 필터링해서 합산
    return cartItems.reduce((sum, item) => {
        const isTargetBook = hasBookTargets && targetBookIds.includes(Number(item.bookId));
        const isTargetCategory = hasCategoryTargets && targetCategoryIds.includes(Number(item.categoryId));

        if (isTargetBook || isTargetCategory) {
            return sum + (Number(item.price) * Number(item.quantity));
        }
        return sum;
    }, 0);
}

// 배송비, 포장비, 최종 금액 계산
function calculateFeesAndDiscounts(totalItemPrice, coupon, point, items) {
    // 포장비 합산
    const wrappingFee = items.reduce((sum, i) => sum + (i.isWrapped ? i.wrappingPaperPrice : 0), 0);

    // 배송비 계산
    let deliveryFee = FIXED_DELIVERY_FEE;
    const selectedDelivery = document.querySelector('input[name="deliveryMethod"]:checked');

    if (selectedDelivery) {
        // 라디오 버튼에 저장된 데이터셋 사용
        const threshold = Number(selectedDelivery.dataset.threshold || 0);
        const fee = Number(selectedDelivery.dataset.fee || 0);

        // 무료 배송 기준 체크
        if (threshold > 0 && (totalItemPrice + wrappingFee) >= threshold) {
            deliveryFee = 0;
        } else {
            deliveryFee = fee;
        }
    } else {
        // 로딩 전 기본 로직
        if ((totalItemPrice + wrappingFee) >= FREE_DELIVERY_THRESHOLD) {
            deliveryFee = 0;
        }
    }

    // 최종 결제 금액 계산 (음수 방지)
    let finalAmount = totalItemPrice + deliveryFee + wrappingFee - coupon - point;
    finalAmount = Math.max(0, finalAmount);

    return { deliveryFee, wrappingFee, finalAmount };
}

// 화면 값 갱신
function updateSummaryUI(total, delivery, wrapping, coupon, point, finalAmt) {
    const setTxt = (id, val) => {
        const el = document.getElementById(id);
        if(el) el.textContent = val;
    };

    setTxt('summaryTotalItemPrice', `${total.toLocaleString()}원`);
    setTxt('deliveryFee', `${delivery.toLocaleString()}원`);
    setTxt('wrappingFee', `${wrapping.toLocaleString()}원`);
    setTxt('couponDiscount', `-${coupon.toLocaleString()}원`);
    setTxt('pointDiscount', `-${point.toLocaleString()}원`);
    setTxt('finalPaymentAmount', `${finalAmt.toLocaleString()}원`);
    setTxt('finalPaymentButtonText', `${finalAmt.toLocaleString()}원 결제하기`);
}

// =================================================================
// [4. 이벤트 리스너 및 UI 동작]
// =================================================================
function setupEventListeners() {
    // 쿠폰 선택 시 재계산
    document.getElementById('couponSelect')?.addEventListener('change', calculateFinalAmount);

    // 배송 방식 변경 시 재계산 (동적 요소 대응을 위해 상위 컨테이너에 이벤트 위임 권장)
    document.getElementById('deliveryMethodContainer')?.addEventListener('change', calculateFinalAmount);

    // 포인트 입력 시 재계산 및 검증
    const pointInput = document.getElementById('pointDiscountAmount');
    if (pointInput) {
        pointInput.addEventListener('input', function() {
            // 숫자만 허용
            this.value = this.value.replace(/\D/g, '');
            let val = Number(this.value);

            // 보유 포인트 초과 방지
            if (val > userPointBalance) {
                alert(`사용 가능한 최대 포인트는 ${userPointBalance.toLocaleString()}P 입니다.`);
                this.value = userPointBalance;
            }
            calculateFinalAmount();
        });
    }

    // 주소 검색 버튼
    document.querySelector('.btn-search-address')?.addEventListener('click', openPostcodeSearch);

    // 결제 요청 버튼
    document.getElementById('requestTossPayment')?.addEventListener('click', handleTossPaymentRequest);

    // 포장지 토글 및 선택 (이벤트 위임)
    const productList = document.getElementById('selectedProductList');
    if (productList) {
        productList.addEventListener('change', (e) => {
            if (e.target.classList.contains('wrap-toggle')) {
                const bookId = Number(e.target.dataset.bookId);
                const btn = e.target.closest('.item-wrap-option').querySelector('.btn-select-wrap');

                btn.disabled = !e.target.checked;

                if (e.target.checked) {
                    const titleEl = e.target.closest('.order-item-detail').querySelector('.item-title');
                    // "책제목 (1권)" 형식에서 제목만 추출
                    const title = titleEl ? titleEl.textContent.split('(')[0].trim() : '상품';
                    openWrappingModal(bookId, title);
                } else {
                    selectedWrapData[bookId] = null;
                    btn.textContent = '포장지 선택/변경';
                }
                calculateFinalAmount();
            }
        });

        productList.addEventListener('click', (e) => {
            if (e.target.classList.contains('btn-select-wrap') && !e.target.disabled) {
                const bookId = Number(e.target.dataset.bookId);
                const titleEl = e.target.closest('.order-item-detail').querySelector('.item-title');
                const title = titleEl ? titleEl.textContent.split('(')[0].trim() : '상품';
                openWrappingModal(bookId, title);
            }
        });
    }

    // 모달 닫기 (외부 클릭)
    window.onclick = (e) => { if (e.target.id === 'wrappingModal') closeModal(); };

    // 배송 메시지 직접 입력 처리
    const messageSelect = document.getElementById('deliveryMessage');
    if (messageSelect) {
        messageSelect.addEventListener('change', (e) => {
            const customInput = document.getElementById('customDeliveryMessage');
            if (e.target.value === 'direct_input') {
                customInput.style.display = 'block';
                customInput.focus();
            } else {
                customInput.style.display = 'none';
                customInput.value = '';
            }
        });
    }
}

// ---------------------------
// [배송 방식 라디오 버튼 생성]
// ---------------------------
function setDeliveryPolicies(){
    const container = document.getElementById('deliveryMethodContainer');
    if(!container) return;

    fetch('/delivery-policies')
        .then(res => res.json())
        .then(data => {
            const list = data.content || [];
            container.innerHTML = '';

            if (list.length === 0) {
                container.innerHTML = '<p>이용 가능한 배송 방식이 없습니다.</p>';
                return;
            }

            list.forEach((m, idx) => {
                const label = document.createElement('label');
                label.style.display = 'block';
                label.style.marginBottom = '5px';

                // 데이터 속성에 배송비 정보 저장
                label.innerHTML = `
                    <input type="radio" name="deliveryMethod" value="${m.deliveryPolicyId}" 
                           ${(m.default || idx === 0) ? 'checked' : ''}
                           data-fee="${m.deliveryFee}" data-threshold="${m.freeDeliveryThreshold || 0}">
                    ${m.deliveryPolicyName} (${m.deliveryFee.toLocaleString()}원)
                    ${m.freeDeliveryThreshold ? '/ ' + m.freeDeliveryThreshold.toLocaleString() + '원 이상 무료' : ''}
                `;
                container.appendChild(label);
            });
            // 로드 완료 후 재계산 (기본값 배송비 반영)
            calculateFinalAmount();
        })
        .catch(e => {
            console.error("배송 정책 로드 실패", e);
            container.innerHTML = '<p>배송 정보를 불러오는데 실패했습니다.</p>';
        });
}

// ---------------------------
// [기타 기능 함수들]
// ---------------------------
function updatePointUI() {
    const el = document.getElementById('currentPointValue');
    if (el) el.textContent = `${userPointBalance.toLocaleString()} P`;
}

function buildCartDataFromPrepare(orderItems) {
    if (!Array.isArray(orderItems)) return null;
    const items = orderItems.map(item => ({
        bookId: item.bookId,
        title: item.title,
        price: Number(item.priceSales ?? item.priceStandard ?? 0),
        quantity: Number(item.quantity || 1),
        selected: true,
        imageUrl: item.imageUrl,
        isPackable: item.isPackable !== false,
        stockCount: item.stockCount || 0,
        categoryId: item.categoryId // 백엔드 로직 동기화를 위해 필수
    }));
    const total = items.reduce((sum, i) => sum + (i.price * i.quantity), 0);
    return { items, selectedTotalPrice: total };
}

function applyPrepareAddresses(addresses) {
    if (!addresses || addresses.length === 0) return;
    const primary = addresses.find(a => a.isDefault) || addresses[0];
    const setVal = (id, val) => {
        const el = document.getElementById(id);
        if(el) el.value = val || '';
    };
    setVal('recipient', primary.recipientName);
    setVal('recipientPhoneNumber', primary.phoneNumber);
    setVal('deliveryAddress', primary.userAddress);
    setVal('deliveryAddressDetail', primary.userAddressDetail);
}

// 주소 검색 (Daum Postcode)
function openPostcodeSearch() {
    if (typeof daum === 'undefined' || !daum.Postcode) {
        alert("주소 검색 서비스를 불러오지 못했습니다.");
        return;
    }
    new daum.Postcode({
        oncomplete: function(data) {
            let addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
            document.getElementById('deliveryAddress').value = addr;
            document.getElementById('deliveryAddressDetail').focus();
        }
    }).open();
}

// 주소록 팝업 (드롭다운)
function setAddressList(){
    const wrapper = document.querySelector('.saved-address-wrapper');
    if (!wrapper) return;
    const btn = wrapper.querySelector('.saved-address-btn');
    const dropdown = wrapper.querySelector('.saved-address-dropdown');

    btn.addEventListener('click', () => {
        dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
    });

    dropdown.querySelectorAll('.saved-address-item').forEach(item => {
        item.addEventListener('click', () => {
            document.getElementById('recipient').value = item.dataset.recipient;
            document.getElementById('recipientPhoneNumber').value = item.dataset.phone;
            document.getElementById('deliveryAddress').value = item.dataset.address;
            document.getElementById('deliveryAddressDetail').value = item.dataset.detail;
            dropdown.style.display = 'none';
        });
    });

    document.addEventListener('click', (e) => {
        if (!wrapper.contains(e.target)) dropdown.style.display = 'none';
    });
}

function setDeliveryDateOptions() {
    const container = document.getElementById('deliveryDateOptions');
    if (!container) return;
    const today = new Date();
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    let count = 0, add = 1;
    container.innerHTML = '';

    while (count < 7) {
        const d = new Date(today); d.setDate(today.getDate() + add);
        // 일요일 제외 로직 (필요 시 수정)
        if (d.getDay() !== 0) {
            const ds = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
            const btn = document.createElement('button');
            btn.type = 'button'; btn.className = 'date-option-button';
            btn.innerHTML = `<span class="day-of-week">${days[d.getDay()]}</span><span>${d.getMonth()+1}/${d.getDate()}</span>`;

            btn.onclick = () => {
                document.querySelectorAll('.date-option-button').forEach(b => b.classList.remove('selected'));
                btn.classList.add('selected');
                document.getElementById('wantDeliveryDate').value = ds;
            };
            container.appendChild(btn);
            if(count === 0) btn.click(); // 첫 번째 날짜 자동 선택
            count++;
        }
        add++;
    }
}

// ---------------------------
// [포장 모달 관련]
// ---------------------------
function openWrappingModal(id, title) {
    currentBookId = id;
    document.getElementById('modalTitle').textContent = `[${title}] 포장 선택`;
    document.getElementById('wrappingModal').style.display = 'block';
    renderOptionsInModal();
}

function closeModal() {
    document.getElementById('wrappingModal').style.display = 'none';
}

function renderOptionsInModal() {
    const con = document.getElementById('wrappingOptions');
    con.innerHTML = '';
    wrapOptions.forEach(o => {
        const d = document.createElement('div');
        d.className = 'wrap-card';
        d.innerHTML = `<img src="${o.wrappingPaperPath}" alt="${o.wrappingPaperName}"><p><strong>${o.wrappingPaperName}</strong></p><p>${o.wrappingPaperPrice.toLocaleString()}원</p>`;
        d.onclick = () => {
            selectedWrapData[currentBookId] = o.wrappingPaperId;
            const btn = document.querySelector(`.order-item-detail[data-book-id="${currentBookId}"] .btn-select-wrap`);
            if(btn) btn.textContent = `${o.wrappingPaperName} (+${o.wrappingPaperPrice.toLocaleString()}원)`;
            closeModal();
            calculateFinalAmount();
        };
        con.appendChild(d);
    });
}

// ---------------------------
// [주문 데이터 수집 및 결제]
// ---------------------------
function collectOrderItems() {
    if (!cartData) return [];
    return cartData.items.map(i => ({
        bookId: i.bookId,
        quantity: i.quantity,
        isWrapped: !!selectedWrapData[i.bookId],
        wrappingPaperId: selectedWrapData[i.bookId] || null,
        wrappingPaperPrice: selectedWrapData[i.bookId] ? (wrapOptions.find(w=>w.wrappingPaperId===selectedWrapData[i.bookId])?.wrappingPaperPrice||0) : 0
    }));
}

function collectDeliveryAddress() {
    const msg = document.getElementById('deliveryMessage')?.value;
    return {
        deliveryAddress: document.getElementById('deliveryAddress')?.value,
        deliveryAddressDetail: document.getElementById('deliveryAddressDetail')?.value,
        deliveryMessage: msg === 'direct_input' ? document.getElementById('customDeliveryMessage')?.value : msg,
        recipient: document.getElementById('recipient')?.value,
        recipientPhoneNumber: document.getElementById('recipientPhoneNumber')?.value.replace(/\D/g, '')
    };
}

function validateInputs(address, orderItems) {
    if (!address.recipient || !address.recipientPhoneNumber || !address.deliveryAddress) {
        alert("배송 정보를 모두 입력해주세요."); return false;
    }
    if (!/^\d{11}$/.test(address.recipientPhoneNumber)) {
        alert("연락처는 11자리 숫자여야 합니다."); return false;
    }
    if (!document.getElementById('wantDeliveryDate')?.value) {
        alert("배송 희망일을 선택해주세요."); return false;
    }
    return true;
}

async function handleTossPaymentRequest() {
    const orderItems = collectOrderItems();
    const address = collectDeliveryAddress();

    if (!validateInputs(address, orderItems)) return;

    const deliveryRadio = document.querySelector('input[name="deliveryMethod"]:checked');
    if(!deliveryRadio) { alert("배송 방식을 선택해주세요."); return; }

    const userInfo = document.getElementById('user-info');
    const memberCouponIdVal = document.getElementById('couponSelect')?.value;
    const memberCouponId = (memberCouponIdVal && memberCouponIdVal !== "0") ? Number(memberCouponIdVal) : null;

    try {
        const res = await fetch(API_BASE.ORDER, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                orderItems,
                deliveryAddress: address,
                deliveryPolicyId: Number(deliveryRadio.value),
                wantDeliveryDate: document.getElementById('wantDeliveryDate').value,
                memberCouponId: memberCouponId,
                point: Number(document.getElementById('pointDiscountAmount').value || 0)
            })
        });

        const result = await res.json();
        if (!res.ok) throw new Error(result.message || "주문 생성 중 오류가 발생했습니다.");

        await requestTossPaymentV2(
            result.totalAmount,
            result.orderNumber,
            result.orderTitle,
            document.querySelector('input[name="paymentMethod"]:checked').value,
            userInfo ? userInfo.dataset.userName : "Guest",
            userInfo ? userInfo.dataset.userEmail : ""
        );
    } catch (e) {
        alert(e.message);
        console.error(e);
    }
}

async function requestTossPaymentV2(amount, orderId, orderName, method, customerName, customerEmail) {
    try {
        const toss = TossPayments(TOSS_CLIENT_KEY);
        const payment = toss.payment({ customerKey: IS_USER ? String(USER_ID) : TossPayments.ANONYMOUS });
        await payment.requestPayment({
            method,
            amount: { currency: "KRW", value: amount },
            orderId,
            orderName,
            customerName,
            customerEmail,
            successUrl: window.location.origin + API_BASE.TOSS_CONFIRM,
            failUrl: window.location.origin + "/fail.html"
        });
    } catch (err) {
        console.error("Toss Payment Error", err);
    }
}