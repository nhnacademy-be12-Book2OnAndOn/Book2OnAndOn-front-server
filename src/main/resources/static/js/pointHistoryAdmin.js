const API_BASE = '/admin/api/points';
const USE_DUMMY = false;

let currentUserId = null;
let currentPage = 0;
let totalPages = 0;

// 더미 데이터
const DUMMY_CURRENT_POINT = 15000;
const DUMMY_HISTORY = [
    {
        pointHistoryId: 1,
        changeDate: '2024-12-01 14:30:00',
        changeType: 'EARN',
        changePoint: 5000,
        balancePoint: 15000,
        changeDescription: '회원가입 포인트',
        expiryDate: '2025-12-01'
    },
    {
        pointHistoryId: 2,
        changeDate: '2024-11-28 10:15:00',
        changeType: 'EARN',
        changePoint: 500,
        balancePoint: 10000,
        changeDescription: '포토 리뷰 작성',
        expiryDate: '2025-11-28'
    },
    {
        pointHistoryId: 3,
        changeDate: '2024-11-25 16:20:00',
        changeType: 'USE',
        changePoint: 3000,
        balancePoint: 9500,
        changeDescription: '주문 시 사용',
        expiryDate: null
    },
    {
        pointHistoryId: 4,
        changeDate: '2024-11-20 11:45:00',
        changeType: 'EARN',
        changePoint: 12500,
        balancePoint: 12500,
        changeDescription: '도서 구매 적립 (1%)',
        expiryDate: '2025-11-20'
    },
    {
        pointHistoryId: 5,
        changeDate: '2024-11-15 09:00:00',
        changeType: 'EARN',
        changePoint: 200,
        balancePoint: 200,
        changeDescription: '리뷰 작성',
        expiryDate: '2025-11-15'
    }
];

// 사용자 검색
async function searchUser(event) {
    event.preventDefault();
    const userId = parseInt(document.getElementById('userIdInput').value);

    if (!userId || userId < 1) {
        alert('올바른 사용자 ID를 입력하세요.');
        return;
    }

    currentUserId = userId;
    currentPage = 0;

    await loadCurrentPoint();
    await loadHistory(0);

    document.getElementById('pointInfoSection').style.display = 'block';
}

// 현재 포인트 조회
async function loadCurrentPoint() {
    if (USE_DUMMY) {
        document.getElementById('currentUserId').textContent = currentUserId;
        document.getElementById('currentPoint').textContent = DUMMY_CURRENT_POINT.toLocaleString() + ' P';
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/current?userId=${currentUserId}`, {
            credentials: 'include'
        });
        if (response.status === 401) {
            alert('로그인이 필요합니다.');
            location.href = '/login';
            return;
        }
        if (!response.ok) {
            const msg = await safeText(response);
            throw new Error(`포인트 조회 실패 (${response.status}) ${msg}`);
        }

        const data = await response.json();
        document.getElementById('currentUserId').textContent = currentUserId;
        document.getElementById('currentPoint').textContent = data.currentPoint.toLocaleString() + ' P';
    } catch (error) {
        console.error('Error:', error);
        alert('포인트 조회에 실패했습니다.');
    }
}

// 포인트 이력 조회
async function loadHistory(page) {
    if (USE_DUMMY) {
        renderHistory(DUMMY_HISTORY);
        renderPagination(1, 5);
        return;
    }

    try {
        const response = await fetch(`${API_BASE}?userId=${currentUserId}&page=${page}&size=10`, {
            credentials: 'include'
        });
        if (response.status === 401) {
            alert('로그인이 필요합니다.');
            location.href = '/login';
            return;
        }
        if (!response.ok) {
            const msg = await safeText(response);
            throw new Error(`이력 조회 실패 (${response.status}) ${msg}`);
        }

        const data = await response.json();
        renderHistory(data.content);
        renderPagination(data.totalPages, data.totalElements);
    } catch (error) {
        console.error('Error:', error);
        alert('이력 조회에 실패했습니다.');
    }
}

// 이력 렌더링
function renderHistory(history) {
    const tbody = document.getElementById('historyTableBody');

    if (!history || history.length === 0) {
        tbody.innerHTML = `
                    <tr>
                        <td colspan="6" class="empty-state">
                            <div class="empty-state-icon">📋</div>
                            <div>포인트 이력이 없습니다</div>
                        </td>
                    </tr>
            `;
        return;
    }

    tbody.innerHTML = history.map(item => {
        const delta = typeof item.pointHistoryChange === 'number' ? item.pointHistoryChange : 0;
        const balance = typeof item.totalPoints === 'number' ? item.totalPoints : 0;
        const changeType = delta >= 0 ? 'EARN' : 'USE';
        const changeDate = item.pointCreatedDate || '';
        const description = item.pointReason || '';
        const expiry = item.pointExpiredDate || '-';

        return `
                <tr>
                    <td>${changeDate}</td>
                    <td>${changeType === 'EARN' ? '적립' : '사용'}</td>
                    <td class="point-change ${changeType === 'EARN' ? 'point-plus' : 'point-minus'}">
                        ${changeType === 'EARN' ? '+' : '-'}${Math.abs(delta).toLocaleString()} P
                    </td>
                    <td><strong>${balance.toLocaleString()} P</strong></td>
                    <td>${description}</td>
                    <td>${expiry}</td>
                </tr>
            `;
    }).join('');
}

// 페이지네이션 렌더링
function renderPagination(totalPages, totalElements) {
    const pagination = document.getElementById('pagination');

    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }

    const prevDisabled = currentPage === 0 ? 'disabled' : '';
    const nextDisabled = currentPage + 1 >= totalPages ? 'disabled' : '';
    pagination.innerHTML = `
        <a class="page-link ${prevDisabled}" data-page="${currentPage - 1}">이전</a>
        <span class="page-info">${currentPage + 1} / ${totalPages}</span>
        <a class="page-link ${nextDisabled}" data-page="${currentPage + 1}">다음</a>
    `;
    pagination.querySelectorAll('.page-link').forEach(link => {
        if (link.classList.contains('disabled')) return;
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetPage = Number(link.getAttribute('data-page'));
            changePage(targetPage);
        });
    });
}

// 페이지 변경
function changePage(page) {
    currentPage = page;
    loadHistory(page);
}

// 포인트 조정 모달 열기
function openAdjustModal(type) {
    document.getElementById('adjustModalTitle').textContent =
        type === 'EARN' ? '포인트 지급' : '포인트 차감';

    document.getElementById(type === 'EARN' ? 'typeEarn' : 'typeUse').checked = true;
    document.getElementById('adjustPoint').value = '';
    document.getElementById('adjustReason').value = '';
    document.getElementById('useTypeGroup').style.display = type === 'USE' ? 'block' : 'none';

    document.getElementById('adjustModal').classList.add('active');
}

// 포인트 조정 모달 닫기
function closeAdjustModal() {
    document.getElementById('adjustModal').classList.remove('active');
}

// 포인트 조정 제출
async function submitAdjust(event) {
    event.preventDefault();

    const type = document.querySelector('input[name="adjustType"]:checked').value;
    const point = parseInt(document.getElementById('adjustPoint').value);
    const reason = document.getElementById('adjustReason').value;
    const useType = document.getElementById('useType')?.value || null;

    if (!currentUserId) {
        alert('사용자 ID를 먼저 조회하세요.');
        return;
    }

    if (!Number.isInteger(point) || point < 1) {
        alert('포인트는 1 이상이어야 합니다.');
        return;
    }

    if (USE_DUMMY) {
        alert(`${type === 'EARN' ? '지급' : '차감'} 완료: ${point.toLocaleString()}P\n사유: ${reason}`);
        closeAdjustModal();
        loadCurrentPoint();
        loadHistory(currentPage);
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/adjust`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify({
                userId: currentUserId,
                changeType: type,
                amount: point,
                memo: reason,
                useType: type === 'USE' ? useType : null
            })
        });

        if (response.status === 401) {
            alert('로그인이 필요합니다.');
            location.href = '/login';
            return;
        }
        if (!response.ok) {
            const msg = await safeText(response);
            throw new Error(`포인트 조정 실패 (${response.status}) ${msg}`);
        }

        alert('포인트 조정이 완료되었습니다.');
        closeAdjustModal();
        loadCurrentPoint();
        loadHistory(currentPage);
    } catch (error) {
        console.error('Error:', error);
        alert('포인트 조정에 실패했습니다.');
    }
}

// 만료 포인트 처리
async function expirePoints() {
    if (!confirm('만료 기한이 지난 포인트를 처리하시겠습니까?')) return;

    if (USE_DUMMY) {
        alert('만료 포인트 처리가 완료되었습니다.');
        loadCurrentPoint();
        loadHistory(currentPage);
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/expire`, {
            method: 'POST',
            credentials: 'include'
        });
        if (response.status === 501) {
            alert('만료 포인트 처리 API가 아직 준비되지 않았습니다.');
            return;
        }
        if (response.status === 401) {
            alert('로그인이 필요합니다.');
            location.href = '/login';
            return;
        }
        if (!response.ok) {
            const msg = await safeText(response);
            throw new Error(`만료 처리 실패 (${response.status}) ${msg}`);
        }

        alert('만료 포인트 처리가 완료되었습니다.');
        loadCurrentPoint();
        loadHistory(currentPage);
    } catch (error) {
        console.error('Error:', error);
        alert('만료 처리에 실패했습니다.');
    }
}

// 모달 외부 클릭 시 닫기
window.onclick = function (event) {
    const adjustModal = document.getElementById('adjustModal');
    if (event.target === adjustModal) {
        closeAdjustModal();
    }
}

async function safeText(response) {
    try {
        return await response.text();
    } catch (_) {
        return '';
    }
}
