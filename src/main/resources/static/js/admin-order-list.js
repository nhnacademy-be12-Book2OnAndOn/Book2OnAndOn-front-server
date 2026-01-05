/**
 * 관리자 주문 리스트 - 페이징 JS
 * - 페이지 번호 표시
 * - 이전/다음 버튼
 * - 페이지 입력 이동
 */

document.addEventListener("DOMContentLoaded", () => {
    initPagination();
});

function initPagination() {
    const container = document.querySelector(".pagination-container");
    if (!container) return;

    const currentPage = Number(container.dataset.currentPage) || 1; // 1-based
    const totalPages = Number(container.dataset.totalPages) || 1;

    const pageNumbersEl = container.querySelector("#pageNumbers");
    const prevBtn = container.querySelector("#prevBtn");
    const nextBtn = container.querySelector("#nextBtn");
    const pageInput = container.querySelector("#pageInput");
    const moveBtn = container.querySelector("#moveBtn");
    const totalPageText = container.querySelector("#totalPageText");

    // 최대 페이지 출력
    totalPageText.textContent = totalPages;

    // 현재 페이지 입력 초기화
    pageInput.value = currentPage;

    // 페이지 번호 버튼 출력
    renderPageNumbers(pageNumbersEl, currentPage, totalPages);

    // 이전 버튼
    prevBtn.addEventListener("click", () => {
        if (currentPage > 1) {
            goToPage(currentPage - 1);
        }
    });

    // 다음 버튼
    nextBtn.addEventListener("click", () => {
        if (currentPage < totalPages) {
            goToPage(currentPage + 1);
        }
    });

    // 이동 버튼
    moveBtn.addEventListener("click", () => {
        const targetPage = Number(pageInput.value);
        if (targetPage >= 1 && targetPage <= totalPages) {
            goToPage(targetPage);
        } else {
            alert(`1 ~ ${totalPages} 사이의 숫자를 입력해주세요.`);
            pageInput.value = currentPage;
        }
    });
}

// 페이지 버튼 렌더링 (보이는 범위 1~5 정도)
function renderPageNumbers(container, currentPage, totalPages) {
    container.innerHTML = "";

    const maxButtons = 5; // 화면에 표시할 버튼 개수
    let startPage = Math.max(1, currentPage - Math.floor(maxButtons / 2));
    let endPage = Math.min(totalPages, startPage + maxButtons - 1);

    // startPage 조정
    startPage = Math.max(1, endPage - maxButtons + 1);

    for (let i = startPage; i <= endPage; i++) {
        const btn = document.createElement("button");
        btn.textContent = i;
        btn.className = i === currentPage ? "page-btn active" : "page-btn";
        btn.addEventListener("click", () => goToPage(i));
        container.appendChild(btn);
    }
}

// 페이지 이동 (URL 파라미터)
function goToPage(page) {
    const url = new URL(window.location.href);
    url.searchParams.set("page", page); // 1-based
    window.location.href = url.toString();
}
