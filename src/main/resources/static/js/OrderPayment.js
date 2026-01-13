// --- 상수 및 전역 변수 영역 (Order & Payment 공통) ---
var API_BASE = {
    CART: '/cart',
    ORDER: '/orders',
    WRAP: '/wrappapers',
    TOSS_CONFIRM_SUCCESS: '/payment/TOSS/confirm',
    TOSS_CONFIRM_FAILURE: '/payment/TOSS/fail'
};

// const USER_ID = window.USER_ID || null;
window.USER_ID = window.USER_ID ?? null;   // 서버에서 넣었으면 유지, 없으면 null
function getUserId() {
    return window.USER_ID;
}

// const ensureGuestId = () => {
//     // 공통 ensureGuestId가 있으면 그대로 사용
//     if (typeof window.ensureGuestId === 'function') {
//         return window.ensureGuestId();
//     }
//     // 로컬 저장소/쿠키에서 우선 조회
//     let gid = localStorage.getItem('uuid') || getCookie('GUEST_ID') || getCookie('guestId');
//     if (!gid) {
//         gid = `guest-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
//     }
//     // 저장 및 쿠키 설정
//     try { localStorage.setItem('uuid', gid); } catch (e) { /* ignore */ }
//     document.cookie = `GUEST_ID=${encodeURIComponent(gid)}; path=/; max-age=${60 * 60 * 24 * 30}`;
//     document.cookie = `guestId=${encodeURIComponent(gid)}; path=/; max-age=${60 * 60 * 24 * 30}`;
//     return gid;
// };
window.ensureGuestId = window.ensureGuestId || function ensureGuestId() {
    let gid = localStorage.getItem('uuid') || getCookie('GUEST_ID') || getCookie('guestId');
    if (!gid) {
        gid = `guest-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
    }

    try { localStorage.setItem('uuid', gid); } catch (e) {}
    document.cookie = `GUEST_ID=${encodeURIComponent(gid)}; path=/; max-age=${60 * 60 * 24 * 30}`;
    document.cookie = `guestId=${encodeURIComponent(gid)}; path=/; max-age=${60 * 60 * 24 * 30}`;
    return gid;
};

const ensureGuestId = window.ensureGuestId; // 파일 내부에서 쓸 때는 이렇게 참조만 한다 (재선언 위험 줄이기)
const GUEST_ID = ensureGuestId();
const IS_USER = document.getElementById('user-info').dataset.userId;
const IS_GUEST = (typeof window.IS_GUEST === 'boolean')
    ? window.IS_GUEST
    : !IS_USER;



const TOSS_CLIENT_KEY = "test_ck_Z1aOwX7K8m1x1vJ2AgDQ8yQxzvNP";

let cartData = null;
let selectedWrapData = {};
let currentBookId = null;
let userPointBalance = 0;
// const PREPARE_DATA = typeof window !== 'undefined' ? window.__ORDER_PREPARE__ : null;

function applyPrepareAddresses(addresses) {
    if (!Array.isArray(addresses) || addresses.length === 0) return;
    const primary = addresses.find(a => a.isDefault) || addresses[0];
    if (!primary) return;
    const recipientEl = document.getElementById('recipient');
    const phoneEl = document.getElementById('recipientPhonenumber');
    const addrEl = document.getElementById('deliveryAddress');
    const addrDetailEl = document.getElementById('deliveryAddressDetail');
    if (recipientEl && primary.recipientName) recipientEl.value = primary.recipientName;
    if (phoneEl && primary.phoneNumber) phoneEl.value = primary.phoneNumber;
    if (addrEl && primary.address) addrEl.value = primary.address;
    if (addrDetailEl && primary.addressDetail) addrDetailEl.value = primary.addressDetail;
}

function buildCartDataFromPrepare(orderItems) {
    if (!Array.isArray(orderItems)) return null;
    const items = orderItems.map(item => {
        const price = Number(item.priceSales ?? item.priceStandard ?? 0);
        const qty = Number(item.quantity || 1);
        return {
            bookId: item.bookId,
            title: item.title,
            bookTitle: item.title,
            price: price,
            priceSales: price,
            originalPrice: Number(item.priceStandard || price),
            quantity: qty,
            selected: true,
            thumbnailUrl: item.imageUrl,
            isPackable: item.isPackable !== false,
            stockCount: item.stockCount || 0,
            categoryId: item.categoryId
        };
    });
    const selectedTotalPrice = items.reduce((sum, i) => sum + (i.price * i.quantity), 0);
    return {
        items,
        selectedTotalPrice,
        finalPaymentAmount: selectedTotalPrice,
        deliveryFee: 0,
        wrappingFee: 0
    };
}

// --- 1. 초기화 및 데이터 로드 ---
document.addEventListener('DOMContentLoaded', async () => {
    setAddressList();
    setDeliveryDateOptions();
    setDeliveryPolicies();
    // await loadInitialData();
    setupEventListeners();
    // calculateFinalAmount();
});


// =================================================================
// I. ORDER LOGIC (주문 상품, 배송지, 포장지 관리)
// =================================================================
function setAddressList(){
    const wrapper = document.querySelector('.saved-address-wrapper');
    if (!wrapper) return;

    const btn = wrapper.querySelector('.saved-address-btn');
    const dropdown = wrapper.querySelector('.saved-address-dropdown');

    // 버튼 클릭 시 드롭다운 토글
    btn.addEventListener('click', () => {
        dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
    });

    // 드롭다운 항목 클릭 시 input 채우기
    dropdown.querySelectorAll('.saved-address-item').forEach(item => {
        item.addEventListener('click', () => {
            document.getElementById('recipient').value = item.dataset.recipient;
            document.getElementById('recipientPhoneNumber').value = item.dataset.phone;
            document.getElementById('deliveryAddress').value = item.dataset.address;
            document.getElementById('deliveryAddressDetail').value = item.dataset.detail;
            dropdown.style.display = 'none'; // 클릭 후 드롭다운 닫기
        });
    });

    // wrapper 외부 클릭 시 드롭다운 닫기
    document.addEventListener('click', (e) => {
        if (!wrapper.contains(e.target)) {
            dropdown.style.display = 'none';
        }
    });
}



// async function loadInitialData() {
//     try {
//         const headers = { 'Content-Type': 'application/json' };
//         if (IS_USER) headers['Authorization'] = `Bearer ${getCookie('accessToken')}`;
//         else headers['X-Guest-Id'] = GUEST_ID;
//
//         // 우선 서버에서 내려준 준비 데이터가 있으면 사용
//         if (PREPARE_DATA && PREPARE_DATA.orderItems) {
//             cartData = buildCartDataFromPrepare(PREPARE_DATA.orderItems);
//             if (PREPARE_DATA.coupons) {
//                 renderCouponOptions(PREPARE_DATA.coupons);
//             }
//             if (PREPARE_DATA.currentPoint && typeof PREPARE_DATA.currentPoint.currentPoint === 'number') {
//                 userPointBalance = PREPARE_DATA.currentPoint.currentPoint;
//             }
//             if (PREPARE_DATA.addresses) {
//                 applyPrepareAddresses(PREPARE_DATA.addresses);
//             }
//         } else {
//             let cartEndpoint = IS_USER ? `${API_BASE.CART}/user/items/selected` : `${API_BASE.CART}/guest`;
//
//             let cartRes = await fetch(cartEndpoint, { headers });
//             // 사용자 토큰 만료/401 시 게스트 장바구니로 폴백
//             if (!cartRes.ok && cartRes.status === 401) {
//                 cartRes = await fetch(`${API_BASE.CART}/guest/selected`, { headers });
//             }
//
//             const [wrapRes, pointRes] = await Promise.all([
//                 fetch(`${API_BASE.WRAP}`, { headers }),
//                 IS_USER ? fetch(`/api/user/me/points/api/current`, { headers }) : Promise.resolve(null)
//             ]);
//             const couponRes = IS_USER ? await fetch('/coupons/me', { headers }) : null;
//             if (couponRes && couponRes.ok) {
//                 const coupons = await couponRes.json();
//                 renderCouponOptions(coupons);
//             }
//
//             if (cartRes.ok) cartData = await cartRes.json();
//             if (wrapRes.ok) wrapOptions = await wrapRes.json();
//             if (pointRes && pointRes.ok) {
//                 const pointData = await pointRes.json();
//                 userPointBalance = pointData.currentPoint;
//             }
//         }
//
//         // renderProductList();
//         updatePointUI();
//         calculateFinalAmount();
//     } catch (error) {
//         console.error("데이터 로드 실패:", error);
//     }
// }

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

    const pointInput = document.getElementById('pointDiscountAmount');
    const currentPointValue = document.getElementById('currentPointValue');

    if (pointInput && currentPointValue) {
        const maxPoint = Number(currentPointValue.dataset.currentPoint || 0);

        // 1. input: 숫자만 허용 (alert 없음)
        pointInput.addEventListener('input', () => {
            pointInput.value = pointInput.value.replace(/\D/g, '');
        });

        // 2. blur: 최종 검증 + alert
        pointInput.addEventListener('blur', () => {
            let value = pointInput.value;

            if (value === '') {
                pointInput.value = '0';
                return;
            }

            let inputPoint = Number(value);

            if (inputPoint > maxPoint) {
                alert(`사용 가능한 최대 포인트는 ${maxPoint.toLocaleString()}P 입니다.`);
                pointInput.value = maxPoint;
                inputPoint = maxPoint;
            }

            calculateFinalAmount();
        });

        // 3. Enter 키 → blur 트리거
        pointInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                pointInput.blur();
            }
        });
    }



    // 7. 할인 계산 이벤트 리스너 (쿠폰)
    document.getElementById('couponSelect')?.addEventListener('change', calculateFinalAmount);

    // 8. 할인 계산 이벤트 리스너 (배송 방법)
    document.getElementById('deliveryMethodOptions')
        ?.addEventListener('change', (e) => {
            if (e.target.name === 'deliveryMethod') {
                calculateFinalAmount();
            }
        });
}

let isDefault = true;
function setDeliveryPolicies(){
    const deliveryMethodContainer = document.getElementById('deliveryMethodOptions');
// API 호출 (예: /api/delivery-policies)
    fetch('/delivery-policies')
        .then(response => {
            if (!response.ok) throw new Error('배송 방식 불러오기 실패');
            return response.json();
        })
        .then(data => {
            data.forEach(method => {
                const label = document.createElement('label');
                label.style.display = 'block';
                label.style.marginBottom = '8px';

                const radio = document.createElement('input');
                radio.type = 'radio';
                radio.name = 'deliveryMethod';
                radio.dataset.deliveryPolicyId = method.deliveryPolicyId; // ID 사용
                radio.dataset.deliveryFee = method.deliveryFee || 0;
                radio.dataset.freeDeliveryThreshold = method.freeDeliveryThreshold || 0;

                if(isDefault){
                    radio.checked = true;
                    isDefault = false;
                }

                label.appendChild(radio);
                label.appendChild(document.createTextNode(
                    ` ${method.deliveryPolicyName} (${method.deliveryFee.toLocaleString()}원)` +
                    (method.freeDeliveryThreshold
                        ? ` / ${method.freeDeliveryThreshold.toLocaleString()}원 이상 무료`
                        : '')
                ));

                deliveryMethodContainer.appendChild(label);
            });
            calculateFinalAmount();
        })
        .catch(err => {
            console.error(err);
            // API 실패 시 기본 배송정책 값으로 설정
            const defaultPolicyEl = document.getElementById('delivery-policy-id');
            deliveryMethodContainer.innerHTML = '';
            if (defaultId) {
                const label = document.createElement('label');
                const radio = document.createElement('input');
                radio.type = 'radio';
                radio.name = 'deliveryMethod';
                radio.dataset.deliveryPolicyId = defaultPolicyEl.dataset.deliveryPolicyId || 1;
                radio.dataset.deliveryFee = defaultPolicyEl.dataset.deliveryFee || 0;
                radio.dataset.freeDeliveryThreshold = defaultPolicyEl.dataset.freeDeliveryThreshold || 0;
                radio.checked = true;
                label.appendChild(radio);
                label.appendChild(document.createTextNode(' 일반택배 (기본 정책 사용)'));
                deliveryMethodContainer.appendChild(label);
            } else {
                deliveryMethodContainer.textContent = '배송 방식을 불러올 수 없습니다.';
            }
        });

}

// function renderProductList() {
    // const listContainer = document.getElementById('selectedProductList');
//     if (!listContainer || !cartData) return;
//     listContainer.innerHTML = '';
//
//     cartData.items.forEach(item => {
//         const currentWrapId = selectedWrapData[item.bookId];
//         const wrapText = currentWrapId ? `선택됨: ${getWrapNameById(currentWrapId)}` : '포장지 선택/변경';
//         const totalItemPrice = (item.price * item.quantity).toLocaleString();
//
//         listContainer.innerHTML += `
//             <div class="order-item-detail" data-book-id="${item.bookId}">
//                 <div class="item-info">
//                     <span class="item-title">${item.bookTitle || item.title} (${item.quantity}권)</span>
//                     <span class="item-price">가격: ${totalItemPrice}원</span>
//                 </div>
//                 <div class="item-wrap-option">
//                     ${item.isPackable !== false ? `
//                         <label><input type="checkbox" class="wrap-toggle" data-book-id="${item.bookId}" ${currentWrapId ? 'checked' : ''}> 포장 선택</label>
//                         <button type="button" class="btn-select-wrap" data-book-id="${item.bookId}" ${currentWrapId ? '' : 'disabled'}>${wrapText}</button>
//                     ` : '<span class="non-packable">포장 불가</span>'}
//                 </div>
//             </div>`;
//     });
// }

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

// function collectOrderItems() {
//     if (!cartData || !cartData.items) return [];
//
//     return cartData.items.map(item => {
//         const container = document.querySelector(`.order-item-detail[data-book-id="${item.bookId}"]`);
//         const isWrappedCheckbox = container ? container.querySelector(`.wrap-toggle`) : null;
//         const isWrapped = isWrappedCheckbox && isWrappedCheckbox.checked;
//         const wrappingPaperId = isWrapped ? selectedWrapData[item.bookId] : null;
//
//         const wrapData = wrappingPaperId ? getWrapDataById(wrappingPaperId) : null;
//
//         return {
//             bookId: item.bookId,
//             quantity: item.quantity,
//             wrappingPaperId: wrappingPaperId,
//             isWrapped: isWrapped,
//             wrappingPaperPrice: wrapData ? wrapData.wrappingPaperPrice : 0
//         };
//     });
// }

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
        recipientPhoneNumber: document.getElementById('recipientPhoneNumber')?.value.replace(/[^0-9]/g, '')
    };
}

function collectGuestAuth() {
    // 회원이면 비회원 정보 필요 없음
    if (IS_USER) return true;

    const guestNameInput = document.getElementById('guestName');
    const guestPhoneInput = document.getElementById('guestPhoneNumber');
    const guestPasswordInput = document.getElementById('guestPassword');
    const guestPasswordConfirmInput = document.getElementById('guestPasswordConfirm');

    // 1. null 체크
    if (!guestNameInput || !guestPhoneInput || !guestPasswordInput || !guestPasswordConfirmInput) {
        console.error('비회원 입력 필드가 존재하지 않습니다.');
        alert('비회원 정보 입력 영역이 올바르지 않습니다.');
        return false;
    }

    const guestName = guestNameInput.value.trim();
    const guestPhoneNumber = guestPhoneInput.value.trim();
    const guestPassword = guestPasswordInput.value;
    const guestPasswordConfirm = guestPasswordConfirmInput.value;

    // 2. 빈 값 체크
    if (!guestName || !guestPhoneNumber || !guestPassword || !guestPasswordConfirm) {
        alert('비회원 정보를 모두 입력해주세요.');
        return false;
    }

    // 3. 전화번호 검증 (숫자 11자리)
    const phoneRegex = /^010\d{8}$/;
    if (!phoneRegex.test(guestPhoneNumber)) {
        alert('전화번호는 010으로 시작하는 11자리 숫자여야 합니다.');
        guestPhoneInput.focus();
        return false;
    }

    // 4. 비밀번호 일치 여부
    if (guestPassword !== guestPasswordConfirm) {
        console.error('비회원 주문 비밀번호 불일치');
        alert('비회원 주문 비밀번호가 일치하지 않습니다.');
        guestPasswordConfirmInput.focus();
        return false;
    }

    // 5. 정상 반환
    return {
        guestName : guestName,
        guestPhoneNumber : guestPhoneNumber,
        guestPassword : guestPassword
    };
}


function validateInputs(address, orderItems) {
    if (!address.recipient) {
        alert("수령인을 입력해주세요");
        return false;
    }

    if(!address.recipientPhoneNumber){
        alert("연락처를 입력해주세요");
        return false;
    }

    if(!address.deliveryAddress){
        alert("주소를 입력해주세요");
        return false;
    }

    if(!document.getElementById('wantDeliveryDate')?.value){
        alert("배송 희망 날짜를 입력해주세요");
        return false;
    }
    const phoneRegex = /^\d{11}$/;
    if (!phoneRegex.test(address.recipientPhoneNumber)) {
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

// 배송 희망날짜 설정
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
    let daysToAdd = 1; // 오늘 제외

    while (generatedCount < MAX_OPTIONS_TO_SHOW) {
        const currentDay = new Date(today);
        currentDay.setDate(today.getDate() + daysToAdd);

        const dayOfWeek = currentDay.getDay();

        if (dayOfWeek !== 0) { // 일요일 제외
            const dateString = `${currentDay.getFullYear()}-${String(currentDay.getMonth() + 1).padStart(2,'0')}-${String(currentDay.getDate()).padStart(2,'0')}`;
            const displayDate = `${currentDay.getMonth() + 1}/${currentDay.getDate()}`;
            const displayDay = days[dayOfWeek];

            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'date-option-button';
            button.setAttribute('data-date', dateString);
            button.innerHTML = `<span class="day-of-week">${displayDay}</span><span class="date-text">${displayDate}</span>`;

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

    // 첫 번째 버튼 선택 상태로 초기화
    const firstButton = container.querySelector('.date-option-button');
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

// 리스트 파싱 함수
function parseIdList(value) {
    if (!value || value === 'null') return [];
    try {
        return JSON.parse(value).map(Number);
    } catch {
        return [];
    }
}


function getOrderItemsFromDom() {
    return Array.from(document.querySelectorAll('.order-item-detail'))
        .map(el => {
            const quantity = Number(el.dataset.quantity);
            const priceSales = Number(el.dataset.priceSales);

            return {
                bookId: Number(el.dataset.bookId),
                categoryId: Number(el.dataset.categoryId),
                quantity: quantity,
                priceSales: priceSales,
                totalPrice: quantity * priceSales
            };
        });
}

// 선택 쿠폰 정보 반환
function getSelectedCoupon() {
    const select = document.getElementById('couponSelect');
    if (!select || select.value === '0') return null;

    const option = select.selectedOptions[0];
    if (!option) return null;

    return {
        memberCouponId: Number(option.value),

        minPrice: Number(option.dataset.couponMinPrice || 0),
        maxPrice: Number(option.dataset.couponMaxPrice || 0),
        discountValue: Number(option.dataset.couponDiscountValue || 0),
        discountType: option.dataset.couponDiscountType || null,
        targetBookIds: parseIdList(option.dataset.couponTargetBookId),
        targetCategoryIds: parseIdList(option.dataset.couponTargetCategoryId)
    };
}

// 할인 대상 금액 계산
function calculateDiscountBaseAmount(orderItems, targetBookIds, targetCategoryIds, totalItemAmount) {
    const hasBookTarget = targetBookIds && targetBookIds.length > 0;
    const hasCategoryTarget = targetCategoryIds && targetCategoryIds.length > 0;

    // 특정 도서 대상
    if (hasBookTarget && !hasCategoryTarget) {
        return orderItems
            .filter(item => targetBookIds.includes(item.bookId))
            .reduce((sum, item) => sum + item.totalPrice, 0);
    }

    // 특정 카테고리 대상
    if (!hasBookTarget && hasCategoryTarget) {
        return orderItems
            .filter(item => targetCategoryIds.includes(item.categoryId))
            .reduce((sum, item) => sum + item.totalPrice, 0);
    }

    // 전체 금액 대상
    if (!hasBookTarget && !hasCategoryTarget) {
        return totalItemAmount;
    }

    // 특정 도서 + 카테고리 대상
    return orderItems
        .filter(item =>
            targetBookIds.includes(item.bookId) ||
            targetCategoryIds.includes(item.categoryId)
        )
        .reduce((sum, item) => sum + item.totalPrice, 0);
}

// 할인 금액 반환
function calculateCouponDiscount(orderItems, totalItemAmount) {
    const coupon = getSelectedCoupon();
    if (!coupon) return 0;

    const discountBaseAmount = calculateDiscountBaseAmount(
        orderItems,
        coupon.targetBookIds,
        coupon.targetCategoryIds,
        totalItemAmount
    );


    // 최소 주문 금액 체크
    if (discountBaseAmount < coupon.minPrice) {
        alert(`최소 주문 금액 ${coupon.minPrice.toLocaleString()}원 이상부터 쿠폰 적용이 가능합니다.`);
        return 0;
    }

    // 최소 결제 금액 100원 정책
    if (discountBaseAmount - coupon.discountValue < 100) {
        alert('쿠폰 적용 후 최소 결제 금액은 100원 이상이어야 합니다.');
        return 0;
    }

    // FIXED
    if (coupon.discountType === 'FIXED') {
        return coupon.discountValue;
    }

    // RATE (%)
    let discount = Math.floor(discountBaseAmount * coupon.discountValue / 100);

    // 최대 할인 금액 제한
    return coupon.maxPrice
        ? Math.min(discount, coupon.maxPrice)
        : discount;
}

// =================================================================
// II. PAYMENT LOGIC (할인, 금액 계산, 결제 요청)
// =================================================================

// --- 금액 계산 ---
function calculateFinalAmount() {
    const orderItems = getOrderItemsFromDom();
    // if (!cartData) return;
    const totalItemPrice = orderItems.reduce((sum, item) => sum + item.totalPrice, 0);
    const couponDiscount = calculateCouponDiscount(orderItems, totalItemPrice) || 0;
    let pointDiscount = Number(document.getElementById('pointDiscountAmount')?.value) || 0;

    const result = calculateFeesAndDiscounts(totalItemPrice, couponDiscount, pointDiscount, orderItems);

    const deliveryFeeVal = Number((result.deliveryFee) || 0);
    const wrappingFeeVal = Number((result.wrappingFee) || 0);
    const finalAmountVal = Number((result.finalAmount) || 0);



    document.getElementById('summaryTotalItemPrice').textContent = `${totalItemPrice.toLocaleString()}원`;
    document.getElementById('deliveryFee').textContent = `${deliveryFeeVal.toLocaleString()}원`;
    document.getElementById('wrappingFee').textContent = `${wrappingFeeVal.toLocaleString()}원`;
    const couponEl = document.getElementById('couponDiscount');
    if (couponEl) {
        couponEl.textContent = `-${couponDiscount.toLocaleString()}원`;
    }

    const pointEl = document.getElementById('pointDiscount');
    if (pointEl) {
        pointEl.textContent = `-${pointDiscount.toLocaleString()}원`;
    }

    const finalStr = `${finalAmountVal.toLocaleString()}원`;
    document.getElementById('finalPaymentAmount').textContent = finalStr;
    document.getElementById('finalPaymentButtonText').textContent = `${finalStr} 결제하기`;
}

function calculateFeesAndDiscounts(totalItemPrice, coupon, point, items) {
    const selectedDeliveryMethodRadio = document.querySelector('input[name="deliveryMethod"]:checked');
    const setDeliveryFee = Number(selectedDeliveryMethodRadio.dataset.deliveryFee);
    const freeDeliveryThreshold = Number(selectedDeliveryMethodRadio.dataset.freeDeliveryThreshold);

    const wrappingFee = items.reduce((sum, i) => sum + (i.isWrapped ? i.wrappingPaperPrice : 0), 0);
    const deliveryFee = totalItemPrice >= freeDeliveryThreshold ? 0 : setDeliveryFee;
    return {
        deliveryFee, wrappingFee,
        finalAmount: Math.max(0, totalItemPrice + deliveryFee + wrappingFee - coupon - point)
    };
}

function getDeliveryPolicyId(){
    const deliveryPolicyId = document.getElementById('delivery-policy-id');
    return Number(deliveryPolicyId.dataset.deliveryPolicyId);
}

function getMemberCouponId(){
    const select = document.getElementById('couponSelect');
    if (!select) return null;

    const couponId = select.value;
    if (couponId === "0" || couponId === "") return null;

    return Number(couponId);
}


async function handleTossPaymentRequest() {
    const orderItems = getOrderItemsFromDom();
    const address = collectDeliveryAddress();
    if (!validateInputs(address, orderItems)) return;
    const deliveryPolicyId = getDeliveryPolicyId();
    const memberCouponId = getMemberCouponId();
    const wantDeliveryDate = document.getElementById('wantDeliveryDate')?.value;
    const pointInput = document.getElementById('pointDiscountAmount');
    const usedPoint = pointInput ? Number(pointInput.value || 0) : 0;

    const guestInfo = collectGuestAuth();

    if(!guestInfo){
        return;
    }

    let orderNumber = null;

    try {
        const headers = { 'Content-Type': 'application/json' };
        if (!IS_USER) {
            const gid = (typeof ensureGuestId === 'function') ? ensureGuestId() : null;
            if (gid) {
                headers['X-Guest-Id'] = gid;
                headers['GUEST_ID'] = gid; // 서버에서 쿠키/헤더 둘 다 참고할 가능성 대비
            }
        }

        // 1. 서버에 주문 생성
        const guest = {
            guestName: guestInfo.guestName,
            guestPhoneNumber: guestInfo.guestPhoneNumber,
            guestPassword: guestInfo.guestPassword,
            orderItems: orderItems,
            deliveryAddress: address,
            deliveryPolicyId: deliveryPolicyId,
            wantDeliveryDate: wantDeliveryDate
        }

        const user = {
            orderItems: orderItems,
            deliveryAddress: address,
            deliveryPolicyId: deliveryPolicyId,
            wantDeliveryDate: wantDeliveryDate,
            memberCouponId: memberCouponId,
            point: usedPoint,
        }

        const response = await fetch(API_BASE.ORDER, {
            method: 'POST',
            headers,
            body: JSON.stringify({
                user : user,
                guest : guest
            })
        });

        const orderResult = await response.json();
        orderNumber = orderResult.orderNumber;

        if (!response.ok) {
            let message = "주문 생성 실패";

            try {
                message = orderResult.message || message;
            } catch {}

            throw new Error(message);
        }



        //상품이 2건 이상이면 "제목 외 N건" , 1건이면 "제목"만 표시
        const orderName = orderResult.orderTitle;

        const userInfo = document.getElementById('user-info');
        const customerName = (userInfo?.dataset.userName) || '비회원';
        const customerEmail = (userInfo?.dataset.userEmail) || 'guest@example.com';

        // 2. 토스 결제창 열기
        await requestTossPaymentV2(
            orderResult.totalAmount,
            orderResult.orderNumber,
            orderName,
            document.querySelector('input[name="paymentMethod"]:checked').value,
            customerName,
            customerEmail
        );
    } catch (e) {
        console.error("결제 프로세스 오류:", e);
        try {
            await fetch(`/payment/${orderNumber}/rollback`, {
                method: "GET"
            });
            console.log("롤백 완료");
        } catch (rollbackError) {
            console.error("롤백 처리 실패:", rollbackError);
        }


        alert("주문 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
}

// [Toss Payment V2 Logic] V2 연쇄 호출 구조
async function requestTossPaymentV2(amount, orderId, orderName, method, customerName, customerEmail) {
    const toss = TossPayments(TOSS_CLIENT_KEY);
    const payment = toss.payment({ customerKey: IS_USER ? String(getUserId()) : TossPayments.ANONYMOUS });
    await payment.requestPayment({
        method: method,
        amount: { currency: "KRW", value: amount },
        orderId: orderId,
        orderName: orderName,
        customerName: customerName,
        customerEmail: customerEmail,
        successUrl: window.location.origin + API_BASE.TOSS_CONFIRM_SUCCESS,
        failUrl: window.location.origin + API_BASE.TOSS_CONFIRM_FAILURE
    });
}
