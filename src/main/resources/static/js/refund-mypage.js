let currentPage = 1;
let totalPages = 1;
let pageSize = 10;
let allRefunds = [];
let filteredRefunds = [];

// 페이지 로드 시 데이터 로드
window.onload = function() {
    loadMyRefunds();
};

async function loadMyRefunds() {
    try {
        // API 호출 예시
        // const response = await fetch(`/api/orders/refunds/list?page=${currentPage - 1}&size=${pageSize}`, {
        //     headers: {
        //         'Authorization': 'Bearer ' + token
        //     }
        // });
        // const data = await response.json();
        // allRefunds = data.content;
        // totalPages = data.totalPages;

        // 임시 데이터
        allRefunds = [
            {
                refundId: 1,
                orderNumber: '2025123456789',
                orderId: 101,
                productName: '클린 코드',
                quantity: 2,
                amount: 66000,
                reason: '상품 불량',
                detailReason: '책에 얼룩이 있습니다',
                requestDate: '2025-12-24',
                status: 0,
                statusText: '처리중'
            },
            {
                refundId: 2,
                orderNumber: '2025123456788',
                orderId: 102,
                productName: '리팩터링',
                quantity: 1,
                amount: 35000,
                reason: '단순 변심',
                detailReason: '필요 없어졌습니다',
                requestDate: '2025-11-10',
                status: 1,
                statusText: '승인됨',
                approvedDate: '2025-11-11'
            },
            {
                refundId: 3,
                orderNumber: '2025123456787',
                orderId: 103,
                productName: '이펙티브 자바',
                quantity: 1,
                amount: 36000,
                reason: '오배송',
                detailReason: '다른 상품이 배송되었습니다',
                requestDate: '2025-02-25',
                status: 3,
                statusText: '완료',
                completedDate: '2025-02-28'
            },
            {
                refundId: 4,
                orderNumber: '2025123456786',
                orderId: 104,
                productName: 'Design Patterns',
                quantity: 1,
                amount: 40000,
                reason: '상품 불량',
                detailReason: '책 표지가 찢어져 있습니다',
                requestDate: '2024-11-20',
                status: 2,
                statusText: '거부됨',
                rejectedReason: '반품 기한이 지났습니다'
            }
        ];

        filteredRefunds = [...allRefunds];
        totalPages = Math.ceil(filteredRefunds.length / pageSize);
        renderRefunds();

    } catch (error) {
        console.error('데이터 로드 실패:', error);
        showEmptyState('반품 내역을 불러오는데 실패했습니다.');
    }
}

function renderRefunds() {
    const container = document.getElementById('refundListContainer');

    if (filteredRefunds.length === 0) {
        showEmptyState('반품 내역이 없습니다.');
        return;
    }

    const start = (currentPage - 1) * pageSize;
    const end = start + pageSize;
    const pageRefunds = filteredRefunds.slice(start, end);

    container.innerHTML = '<div class="refund-list">' +
        pageRefunds.map(refund => createRefundCard(refund)).join('') +
        '</div>';

    document.getElementById('currentPage').textContent = currentPage;
    document.getElementById('totalPages').textContent = totalPages;

    // 페이지네이션 버튼 활성화/비활성화
    document.getElementById('btnPrev').disabled = currentPage === 1;
    document.getElementById('btnNext').disabled = currentPage === totalPages;
}

function createRefundCard(refund) {
    const statusClass = getStatusClass(refund.status);

    return `
                <div class="refund-item">
                    <div class="refund-header">
                        <div>
                            <div class="refund-id">반품 #${refund.refundId}</div>
                            <div class="refund-date">주문번호: ${refund.orderNumber}</div>
                            <div class="refund-date">신청일: ${refund.requestDate}</div>
                        </div>
                        <span class="status-badge ${statusClass}">${refund.statusText}</span>
                    </div>

                    <div class="refund-body">
                        <div class="info-item">
                            <span class="info-label">상품명</span>
                            <span class="info-value">${refund.productName}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">수량</span>
                            <span class="info-value">${refund.quantity}개</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">반품 금액</span>
                            <span class="info-value">${refund.amount.toLocaleString()}원</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">반품 사유</span>
                            <span class="info-value">${refund.reason}</span>
                        </div>
                    </div>

                    <div class="refund-reason">
                        <span class="info-label">상세 사유</span>
                        <div class="info-value">${refund.detailReason}</div>
                    </div>

                    <div class="action-buttons">
                        <button class="btn btn-sm btn-outline" onclick="viewDetail(${refund.refundId})">상세보기</button>
                        ${refund.status === 0 ? `
                            <button class="btn btn-sm btn-danger" onclick="cancelRefund(${refund.refundId})">신청 취소</button>
                        ` : ''}
                        ${refund.status === 2 ? `
                            <button class="btn btn-sm btn-secondary" onclick="viewRejectionReason(${refund.refundId})">거부 사유</button>
                        ` : ''}
                    </div>
                </div>
            `;
}

function getStatusClass(status) {
    const classes = {
        0: 'status-pending',
        1: 'status-approved',
        2: 'status-rejected',
        3: 'status-completed'
    };
    return classes[status] || 'status-pending';
}

function showEmptyState(message) {
    const container = document.getElementById('refundListContainer');
    container.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">📭</div>
                    <h3>${message}</h3>
                    <p>아직 반품 신청 내역이 없습니다.<br>주문 내역에서 반품을 신청하실 수 있습니다.</p>
                    <a href="refund-form.html" class="btn">반품 신청하기</a>
                </div>
            `;
}

function filterRefunds() {
    const status = document.getElementById('filterStatus').value;

    if (status === '') {
        filteredRefunds = [...allRefunds];
    } else {
        filteredRefunds = allRefunds.filter(r => r.status === parseInt(status));
    }

    currentPage = 1;
    totalPages = Math.ceil(filteredRefunds.length / pageSize);
    renderRefunds();
}

function filterByPeriod() {
    const period = document.getElementById('filterPeriod').value;

    if (period === 'all') {
        filteredRefunds = [...allRefunds];
    } else {
        const months = parseInt(period);
        const cutoffDate = new Date();
        cutoffDate.setMonth(cutoffDate.getMonth() - months);

        filteredRefunds = allRefunds.filter(r => {
            const refundDate = new Date(r.requestDate);
            return refundDate >= cutoffDate;
        });
    }

    currentPage = 1;
    totalPages = Math.ceil(filteredRefunds.length / pageSize);
    renderRefunds();
}

function searchByOrder() {
    const keyword = document.getElementById('searchOrder').value.toLowerCase();

    if (keyword === '') {
        filteredRefunds = [...allRefunds];
    } else {
        filteredRefunds = allRefunds.filter(r =>
            r.orderNumber.toLowerCase().includes(keyword)
        );
    }

    currentPage = 1;
    totalPages = Math.ceil(filteredRefunds.length / pageSize);
    renderRefunds();
}

function changePage(delta) {
    const newPage = currentPage + delta;
    if (newPage >= 1 && newPage <= totalPages) {
        currentPage = newPage;
        renderRefunds();
    }
}

function viewDetail(refundId) {
    const refund = allRefunds.find(r => r.refundId === refundId);
    if (!refund) return;

    const modalBody = document.getElementById('modalBody');
    modalBody.innerHTML = `
                <div class="refund-reason" style="margin-bottom: 20px;">
                    <span class="info-label">반품 상태</span>
                    <div style="margin-top: 10px;">
                        <span class="status-badge ${getStatusClass(refund.status)}">${refund.statusText}</span>
                    </div>
                </div>

                <div class="refund-body" style="margin-bottom: 20px;">
                    <div class="info-item">
                        <span class="info-label">반품 ID</span>
                        <span class="info-value">#${refund.refundId}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">주문번호</span>
                        <span class="info-value">${refund.orderNumber}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">신청일</span>
                        <span class="info-value">${refund.requestDate}</span>
                    </div>
                    ${refund.approvedDate ? `
                        <div class="info-item">
                            <span class="info-label">승인일</span>
                            <span class="info-value">${refund.approvedDate}</span>
                        </div>
                    ` : ''}
                    ${refund.completedDate ? `
                        <div class="info-item">
                            <span class="info-label">완료일</span>
                            <span class="info-value">${refund.completedDate}</span>
                        </div>
                    ` : ''}
                </div>

                <div class="refund-body" style="margin-bottom: 20px;">
                    <div class="info-item">
                        <span class="info-label">상품명</span>
                        <span class="info-value">${refund.productName}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">수량</span>
                        <span class="info-value">${refund.quantity}개</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">반품 금액</span>
                        <span class="info-value">${refund.amount.toLocaleString()}원</span>
                    </div>
                </div>

                <div class="refund-reason" style="margin-bottom: 20px;">
                    <span class="info-label">반품 사유</span>
                    <div class="info-value" style="margin-top: 8px;">${refund.reason}</div>
                </div>

                <div class="refund-reason">
                    <span class="info-label">상세 사유</span>
                    <div class="info-value" style="margin-top: 8px;">${refund.detailReason}</div>
                </div>

                ${refund.rejectedReason ? `
                    <div class="refund-reason" style="background: #f8d7da; border: 1px solid #f5c6cb;">
                        <span class="info-label" style="color: #721c24;">거부 사유</span>
                        <div class="info-value" style="margin-top: 8px; color: #721c24;">${refund.rejectedReason}</div>
                    </div>
                ` : ''}
            `;

    document.getElementById('detailModal').classList.add('show');
}

async function cancelRefund(refundId) {
    if (!confirm('반품 신청을 취소하시겠습니까?')) return;

    try {
        // const response = await fetch(`/api/orders/${orderId}/refunds/${refundId}/cancel`, {
        //     method: 'POST',
        //     headers: {
        //         'Authorization': 'Bearer ' + token
        //     }
        // });

        alert('반품 신청이 취소되었습니다.');
        loadMyRefunds();

    } catch (error) {
        alert('취소 처리 중 오류가 발생했습니다.');
        console.error(error);
    }
}

function viewRejectionReason(refundId) {
    const refund = allRefunds.find(r => r.refundId === refundId);
    if (!refund || !refund.rejectedReason) return;

    alert(`거부 사유:\n\n${refund.rejectedReason}`);
}

function closeModal() {
    document.getElementById('detailModal').classList.remove('show');
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('detailModal');
    if (event.target === modal) {
        closeModal();
    }
}