const API_BASE = '/user/me/points/api';
const USE_DUMMY = false;

let currentPage = 0;
let currentFilter = 'ALL';
let allHistory = [];
let totalPages = 0;

const initialHistory = window.initialPointHistory || null;
const initialTotalPages = typeof window.initialTotalPages === 'number' ? window.initialTotalPages : 0;
const initialCurrentPointValue = window.initialCurrentPointValue ?? null;

// 더미 데이터
const DUMMY_CURRENT_POINT = 25000;
const DUMMY_HISTORY = [
    {
        pointHistoryId: 1,
        changeDate: '2024-12-01 14:30:00',
        changeType: 'EARN',
        changePoint: 5000,
        balancePoint: 25000,
        changeDescription: '회원가입 포인트',
        expiryDate: '2025-12-01'
    },
    {
        pointHistoryId: 2,
        changeDate: '2024-11-30 16:20:00',
        changeType: 'USE',
        changePoint: 3000,
        balancePoint: 20000,
        changeDescription: '도서 구매 시 사용',
        expiryDate: null
    },
    {
        pointHistoryId: 3,
        changeDate: '2024-11-28 10:15:00',
        changeType: 'EARN',
        changePoint: 500,
        balancePoint: 23000,
        changeDescription: '포토 리뷰 작성 적립',
        expiryDate: '2025-11-28'
    },
    {
        pointHistoryId: 4,
        changeDate: '2024-11-25 11:45:00',
        changeType: 'EARN',
        changePoint: 1250,
        balancePoint: 22500,
        changeDescription: '도서 구매 적립 (1%)',
        expiryDate: '2025-11-25'
    },
    {
        pointHistoryId: 5,
        changeDate: '2024-11-20 09:00:00',
        changeType: 'EARN',
        changePoint: 200,
        balancePoint: 21250,
        changeDescription: '일반 리뷰 작성 적립',
        expiryDate: '2025-11-20'
    },
    {
        pointHistoryId: 6,
        changeDate: '2024-11-15 14:30:00',
        changeType: 'USE',
        changePoint: 5000,
        balancePoint: 21050,
        changeDescription: '도서 구매 시 사용',
        expiryDate: null
    },
    {
        pointHistoryId: 7,
        changeDate: '2024-11-10 10:20:00',
        changeType: 'EARN',
        changePoint: 800,
        balancePoint: 26050,
        changeDescription: '도서 구매 적립 (1%)',
        expiryDate: '2025-11-10'
    },
    {
        pointHistoryId: 8,
        changeDate: '2024-11-05 16:45:00',
        changeType: 'EARN',
        changePoint: 500,
        balancePoint: 25250,
        changeDescription: '포토 리뷰 작성 적립',
        expiryDate: '2025-11-05'
    },
    {
        pointHistoryId: 9,
        changeDate: '2024-10-28 11:00:00',
        changeType: 'USE',
        changePoint: 2000,
        balancePoint: 24750,
        changeDescription: '도서 구매 시 사용',
        expiryDate: null
    },
    {
        pointHistoryId: 10,
        changeDate: '2024-10-20 13:30:00',
        changeType: 'EARN',
        changePoint: 1350,
        balancePoint: 26750,
        changeDescription: '도서 구매 적립 (1%)',
        expiryDate: '2025-10-20'
    }
];

// 현재 포인트 조회
async function loadCurrentPoint() {
    if (initialCurrentPointValue !== null && initialCurrentPointValue !== undefined) {
        const el = document.getElementById('currentPoint');
        if (el) {
            el.textContent = initialCurrentPointValue.toLocaleString() + ' P';
        }
        return;
    }

    if (USE_DUMMY) {
        document.getElementById('currentPoint').textContent =
            DUMMY_CURRENT_POINT.toLocaleString() + ' P';

        // 이번 달 통계 계산
        const now = new Date();
        const thisMonth = now.getMonth();
        const thisYear = now.getFullYear();

        let monthlyEarned = 0;
        let monthlyUsed = 0;

        DUMMY_HISTORY.forEach(item => {
            const itemDate = new Date(item.changeDate);
            if (itemDate.getMonth() === thisMonth && itemDate.getFullYear() === thisYear) {
                if (item.changeType === 'EARN') {
                    monthlyEarned += item.changePoint;
                } else {
                    monthlyUsed += item.changePoint;
                }
            }
        });

        document.getElementById('monthlyEarned').textContent =
            monthlyEarned.toLocaleString() + ' P';
        document.getElementById('monthlyUsed').textContent =
            monthlyUsed.toLocaleString() + ' P';
        document.getElementById('expiringPoint').textContent = '1,200 P';
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/current`, { credentials: 'include' });

        if (!response.ok) throw new Error('포인트 조회 실패');

        const data = await response.json();
        document.getElementById('currentPoint').textContent =
            data.currentPoint.toLocaleString() + ' P';
    } catch (error) {
        console.error('Error:', error);
        const el = document.getElementById('currentPoint');
        if (el) {
            el.textContent = '0 P';
        }
    }
}

// 포인트 이력 조회
async function loadHistory(page = 0) {
    if (initialHistory) {
        allHistory = [...initialHistory];
        totalPages = initialTotalPages || 1;
        updateSummaryFromHistory(allHistory);
        renderPagination(totalPages);
        applyFilter();
        return;
    }

    if (USE_DUMMY) {
        allHistory = [...DUMMY_HISTORY];
        applyFilter();
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/history?page=${page}&size=10`, { credentials: 'include' });

        if (!response.ok) throw new Error('이력 조회 실패');

        const data = await response.json();
        allHistory = data.content || [];
        totalPages = data.totalPages || 0;
        updateSummaryFromHistory(allHistory);
        renderPagination(totalPages);
        applyFilter();
    } catch (error) {
        console.error('Error:', error);
        renderErrorRow('포인트 내역을 불러오지 못했습니다.');
    }
}

// 필터 적용
function applyFilter() {
    let filteredHistory = allHistory;

    if (currentFilter !== 'ALL') {
        filteredHistory = allHistory.filter(item =>
            (item.pointHistoryChange > 0 ? 'EARN' : 'USE') === currentFilter
        );
    }

    renderHistory(filteredHistory);
}

// 이력 렌더링
function renderHistory(history) {
    const tbody = document.getElementById('historyTableBody');

    if (!history || history.length === 0) {
        tbody.innerHTML = `
                    <tr>
                        <td colspan="6" class="empty-state">
                            <div class="empty-state-icon">📋</div>
                            <div>포인트 내역이 없습니다</div>
                        </td>
                    </tr>
                `;
        return;
    }

    // 페이지네이션 적용
    const start = currentPage * 10;
    const end = start + 10;
    const pageHistory = history.slice(start, end);

    tbody.innerHTML = pageHistory.map(item => {
                const isEarn = item.pointHistoryChange > 0;
                const changeAbs = Math.abs(item.pointHistoryChange);
                const createdDate = formatDate(item.pointCreatedDate);
                const expiryDate = formatDate(item.pointExpiredDate);
                return `
                <tr>
                    <td>${createdDate}</td>
                    <td>
                        <span class="status-badge ${isEarn ? 'badge-earn' : 'badge-use'}">
                            ${isEarn ? '적립' : '사용'}
                        </span>
                    </td>
                    <td>${item.pointReason || '-'}</td>
                    <td class="point-change ${isEarn ? 'point-plus' : 'point-minus'}">
                        ${isEarn ? '+' : '-'}${changeAbs.toLocaleString()} P
                    </td>
                    <td><strong>${(item.totalPoints ?? item.remainingPoint ?? 0).toLocaleString()} P</strong></td>
                    <td>${expiryDate}</td>
                </tr>
            `;
            }).join('');
}

// 페이지네이션 렌더링
function renderPagination(totalPages) {
    const pagination = document.getElementById('pagination');

    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }

    let html = '';

    // 이전 버튼
    if (currentPage > 0) {
        html += `<button class="page-btn" onclick="changePage(${currentPage - 1})">이전</button>`;
    }

    // 페이지 번호
    for (let i = 0; i < totalPages; i++) {
        html += `<button class="page-btn ${i === currentPage ? 'active' : ''}" onclick="changePage(${i})">${i + 1}</button>`;
    }

    // 다음 버튼
    if (currentPage < totalPages - 1) {
        html += `<button class="page-btn" onclick="changePage(${currentPage + 1})">다음</button>`;
    }

    pagination.innerHTML = html;
}

// 페이지 변경
function changePage(page) {
    currentPage = page;
    loadHistory(page);
}

function renderErrorRow(message) {
    const tbody = document.getElementById('historyTableBody');
    if (!tbody) return;
    tbody.innerHTML = `
        <tr>
            <td colspan="6" class="empty-state">${message}</td>
        </tr>
    `;
}

// 날짜 포맷 (YYYY-MM-DD)
function formatDate(value) {
    if (!value) return '-';
    // value가 LocalDateTime 문자열 형태라면 날짜만 잘라서 반환
    if (typeof value === 'string') {
        return value.substring(0, 10);
    }
    try {
        return new Date(value).toISOString().substring(0, 10);
    } catch (e) {
        return '-';
    }
}

// 월별/소멸 예정 요약 계산
function updateSummaryFromHistory(history) {
    if (!history || history.length === 0) {
        document.getElementById('monthlyEarned').textContent = '0 P';
        document.getElementById('monthlyUsed').textContent = '0 P';
        document.getElementById('expiringPoint').textContent = '0 P';
        return;
    }

    const now = new Date();
    const thisMonth = now.getMonth();
    const thisYear = now.getFullYear();
    const in30Days = now.getTime() + 30 * 24 * 60 * 60 * 1000;

    let earned = 0;
    let used = 0;
    let expiring = 0;

    history.forEach(item => {
        const change = item.pointHistoryChange || 0;
        const created = item.pointCreatedDate ? new Date(item.pointCreatedDate) : null;
        const expires = item.pointExpiredDate ? new Date(item.pointExpiredDate) : null;

        if (created && created.getMonth() === thisMonth && created.getFullYear() === thisYear) {
            if (change > 0) earned += change;
            if (change < 0) used += Math.abs(change);
        }

        if (expires && expires.getTime() > now.getTime() && expires.getTime() <= in30Days) {
            const remaining = item.remainingPoint ?? item.totalPoints ?? Math.max(change, 0);
            expiring += Math.max(remaining, 0);
        }
    });

    document.getElementById('monthlyEarned').textContent = earned.toLocaleString() + ' P';
    document.getElementById('monthlyUsed').textContent = used.toLocaleString() + ' P';
    document.getElementById('expiringPoint').textContent = expiring.toLocaleString() + ' P';
}

// 필터 변경
function filterHistory(filter) {
    currentFilter = filter;
    currentPage = 0;

    // 탭 활성화 상태 변경
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    event.target.classList.add('active');

    applyFilter();
}

// 페이지 로드 시 실행
loadCurrentPoint();
loadHistory();
