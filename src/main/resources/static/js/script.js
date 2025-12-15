// 1. 사이드바 토글 함수 (전역 범위로 이동)
function toggleSidebar() {
    const sidebar = document.getElementById('categorySidebar');
    const overlay = document.getElementById('sidebarOverlay');

    if (sidebar && overlay) {
        sidebar.classList.toggle('open');
        overlay.classList.toggle('active');
    } else {
        console.warn("사이드바 요소를 찾을 수 없습니다.");
    }
}

// 2. 하위 카테고리 토글 함수 (전역 범위로 이동)
function toggleSubcategory(row) {
    if (row.dataset.hasChildren !== 'true') {
        return;
    }

    const item = row.closest('.category-item');
    if (!item) return;

    // 클래스 토글 (open 클래스가 CSS에서 + / - 모양을 바꿈)
    item.classList.toggle('open');

    // 슬라이드 애니메이션 처리
    const list = item.querySelector(':scope > .subcategory-list');
    if (!list) return;

    if (item.classList.contains('open')) {
        list.style.display = 'block'; // 먼저 보이게 하고
        list.style.maxHeight = list.scrollHeight + 'px'; // 높이를 늘림

        // 애니메이션 끝나면 max-height 해제 (내용물 가려짐 방지)
        const onTransitionEnd = (event) => {
            if (event.target !== list) return;
            if (item.classList.contains('open')) {
                list.style.maxHeight = 'none';
            }
            list.removeEventListener('transitionend', onTransitionEnd);
        };
        list.addEventListener('transitionend', onTransitionEnd);

    } else {
        // 닫을 때는 현재 높이를 숫자로 세팅하고 바로 0으로 줄임
        list.style.maxHeight = list.scrollHeight + 'px';
        requestAnimationFrame(() => {
            list.style.maxHeight = '0';
        });
    }
}

// 전역에서 접근 가능하도록 window에 바인딩 (다른 템플릿에서도 안전하게 호출)
window.toggleSidebar = window.toggleSidebar || toggleSidebar;
window.toggleSubcategory = window.toggleSubcategory || toggleSubcategory;

// 3. 이벤트 리스너 연결 (DOM 로드 후 실행)
document.addEventListener('DOMContentLoaded', () => {
    // 햄버거 버튼 & 닫기 버튼 연결
    const hamburger = document.querySelector('.hamburger');
    const closeBtn = document.querySelector('.close-sidebar');
    const overlay = document.getElementById('sidebarOverlay');

    if (hamburger) hamburger.addEventListener('click', toggleSidebar);
    if (closeBtn) closeBtn.addEventListener('click', toggleSidebar);
    if (overlay) overlay.addEventListener('click', toggleSidebar);

    // 검색창 X 버튼 기능
    const searchInput = document.querySelector('.global-search .input');
    const clearButton = document.querySelector('.btn-clear');

    if (searchInput && clearButton) {
        clearButton.addEventListener('click', () => {
            searchInput.value = '';
            searchInput.focus();
        });
    }
});
