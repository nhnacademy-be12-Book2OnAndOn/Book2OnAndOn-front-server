/**
 * 쿠폰 정책 리스트 필터링 (프론트엔드 전용)
 */
function filterTable() {
    // 1. 선택된 값 가져오기
    const selectedType = document.getElementById('filterType').value;
    const selectedStatus = document.getElementById('filterStatus').value;

    // 2. 모든 테이블 행 가져오기
    const rows = document.querySelectorAll('#policyTable tbody tr');

    // 3. 각 행 검사
    rows.forEach(row => {
        // th:data-* 로 넣어둔 값 읽기
        const rowType = row.getAttribute('data-type');
        const rowStatus = row.getAttribute('data-status');

        let isShow = true;

        // 타입 비교 (ALL이 아니고 값이 다르면 숨김)
        if (selectedType !== 'ALL' && rowType !== selectedType) {
            isShow = false;
        }

        // 상태 비교
        if (selectedStatus !== 'ALL' && rowStatus !== selectedStatus) {
            isShow = false;
        }

        // 4. 표시/숨김 적용
        if (isShow) {
            row.style.display = ''; // 보이기 (기본값)
        } else {
            row.style.display = 'none'; // 숨기기
        }
    });
}

function resetFilter() {
    document.getElementById('filterType').value = 'ALL';
    document.getElementById('filterStatus').value = 'ALL';
    filterTable(); // 초기화 후 다시 실행해서 모두 보여줌
}