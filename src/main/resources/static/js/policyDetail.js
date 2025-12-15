/**
 * 쿠폰 정책 상세 페이지 스크립트
 * - 모달 제어 및 수량 입력 토글
 */

// 1. 체크박스 상태에 따라 입력창 제어
function toggleQuantityInput() {
    const isUnlimited = document.getElementById('unlimitedCheck').checked;
    const quantityInput = document.getElementById('quantity');

    if (isUnlimited) {
        // 무제한 체크 시: 입력창 비우고, 비활성화(disabled) -> 서버로 값 전송 안 됨 (null)
        quantityInput.value = '';
        quantityInput.disabled = true;
        quantityInput.placeholder = "무제한 발행 모드";
        quantityInput.style.backgroundColor = "#f0f0f0"; // 회색 처리
    } else {
        // 체크 해제 시: 입력창 활성화
        quantityInput.disabled = false;
        quantityInput.placeholder = "수량을 입력하세요 (예: 100)";
        quantityInput.style.backgroundColor = "white";
        quantityInput.focus();
    }
}

// 2. 모달 열기 (초기화 포함)
function openCouponModal() {
    document.getElementById('couponModal').classList.add('show');

    // 상태 초기화: 무제한 체크 해제, 입력창 활성화
    document.getElementById('unlimitedCheck').checked = false;
    toggleQuantityInput();
}

// 3. 모달 닫기
function closeCouponModal() {
    document.getElementById('couponModal').classList.remove('show');
}

// 4. 배경 클릭 시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('couponModal');
    if (event.target == modal) {
        closeCouponModal();
    }
}
