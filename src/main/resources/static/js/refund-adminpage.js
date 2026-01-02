let currentPage = 0;
let totalPages = 1;
let pageSize = 20;
let refundData = [];

// 페이지 로드 시 초기화
window.onload = function() {
    loadRefunds();
    updateStats();
};

async function loadRefunds() {
    try {
        const params = new URLSearchParams({
            status: document.getElementById('filterStatus').value,
            startDate: document.getElementById('filterStartDate').value,
            endDate: document.getElementById('filterEndDate').value,
            orderNumber: document.getElementById('filterOrderNumber').value,
            userKeyword: document.getElementById('filterUserKeyword').value,
            includeGuest: document.getElementById('filterIncludeGuest').value,
            page: currentPage,
            size: pageSize
        });

        // API 호출 예시
        // const response = await fetch(`/admin/refunds?${params}`, {
        //     headers: {
        //         'Authorization': 'Bearer ' + adminToken
        //     }
        // });
        // const data = await response.json();
        // refundData = data.content;
        // totalPages = data.totalPages;

        // 임시 데이터
        refundData = [
            {
                refundId: 1,
                orderNumber: '2025123456789',
                userName: '홍길동',
                userType: '회원',
                userId: 1,
                items: [
                    { name: '클린 코드', quantity: 2, price: 33000 },
                    { name: '리팩터링', quantity: 1, price: 35000 }
                ],
                totalAmount: 101000,
                refundReason: '상품 불량',
                detailReason: '책에 얼룩이 있습니다',
                requestDate: '2024-12-15',
                status: 0,
                statusText: '처리 대기'
            },
            {
                refundId: 2,
                orderNumber: '2025123456788',
                userName: '비회원',
                userType: '비회원',
                items: [
                    { name: '이펙티브 자바', quantity: 1, price: 36000 }
                ],
                totalAmount: 36000,
                reason: '오배송',
                detailReason: '다른 상품이 왔습니다',
                requestDate: '2024-12-14',
                status: 1,
                statusText: '승인됨'
            },
            {
                refundId: 3,
                orderNumber: '2025123456787',
                userName: '김철수',
                userType: '회원',
                userId: 2,
                items: [
                    { name: 'Design Patterns', quantity: 1, price: 40000 }
                ],
                totalAmount: 40000,
                reason: '단순 변심',
                detailReason: '필요 없어졌습니다',
                requestDate: '2024-12-13',
                status: 2,
                statusText: '거부됨',
                adminMemo: '반품 기한 초과'
            }
        ];

        totalPages = 1; // 임시
        renderTable();

    } catch (error) {
        console.error('데이터 로드 실패:', error);
        alert('반품 목록을 불러오는데 실패했습니다.');
    }
}

function renderTable() {
    const tbody = document.getElementById('refundTableBody');
    tbody.innerHTML = '';

    if (refundData.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align: center; padding: 40px; color: #666;">조회된 반품 내역이 없습니다.</td></tr>';
        return;
    }

    refundData.forEach(refund => {
        const statusClass = getStatusClass(refund.status);
        const itemsText = refund.items.length > 1
            ? `${refund.items[0].name} 외 ${refund.items.length - 1}건`
            : refund.items[0].name;

        const row = document.createElement('tr');
        row.innerHTML = `
                    <td>${refund.refundId}</td>
                    <td>${refund.orderNumber}</td>
                    <td>${refund.userName}<br><small style="color: #666;">(${refund.userType})</small></td>
                    <td>${itemsText}</td>
                    <td><strong>${refund.totalAmount.toLocaleString()}원</strong></td>
                    <td>${refund.reason}</td>
                    <td>${refund.requestDate}</td>
                    <td><span class="status-badge ${statusClass}">${refund.statusText}</span></td>
                    <td>
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-info" onclick="viewDetail(${refund.refundId})">상세</button>
                            ${refund.status === 0 ? `
                                <button class="btn btn-sm btn-success" onclick="approveRefund(${refund.refundId})">승인</button>
                                <button class="btn btn-sm btn-danger" onclick="rejectRefund(${refund.refundId})">거부</button>
                            ` : ''}
                        </div>
                    </td>
                `;
        tbody.appendChild(row);
    });

    document.getElementById('currentPage').textContent = currentPage + 1;
    document.getElementById('totalPages').textContent = totalPages;
    document.getElementById('btnPrev').disabled = currentPage === 0;
    document.getElementById('btnNext').disabled = currentPage === totalPages - 1;
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

async function updateStats() {
    // 실제로는 API로 통계 조회
    // const stats = await fetch('/admin/refunds/stats');
    document.getElementById('statPending').textContent = '5';
    document.getElementById('statApproved').textContent = '12';
    document.getElementById('statRejected').textContent = '3';
    document.getElementById('statCompleted').textContent = '8';
}

function searchRefunds() {
    currentPage = 0;
    loadRefunds();
}

function resetFilters() {
    document.getElementById('filterStatus').value = '';
    document.getElementById('filterStartDate').value = '';
    document.getElementById('filterEndDate').value = '';
    document.getElementById('filterOrderNumber').value = '';
    document.getElementById('filterUserKeyword').value = '';
    document.getElementById('filterIncludeGuest').value = 'true';
    searchRefunds();
}

function changePage(delta) {
    const newPage = currentPage + delta;
    if (newPage >= 0 && newPage < totalPages) {
        currentPage = newPage;
        loadRefunds();
    }
}

function viewDetail(refundId) {
    const refund = refundData.find(r => r.refundId === refundId);
    if (!refund) return;

    const modalBody = document.getElementById('modalBody');
    const itemsHtml = refund.items.map(item => `
                <div class="item-card">
                    <h5>${item.name}</h5>
                    <div class="item-info-row">
                        <span>수량: ${item.quantity}개</span>
                        <span>단가: ${item.price.toLocaleString()}원</span>
                        <span>소계: ${(item.price * item.quantity).toLocaleString()}원</span>
                    </div>
                </div>
            `).join('');

    modalBody.innerHTML = `
                <div class="detail-section">
                    <h4>기본 정보</h4>
                    <div class="detail-grid">
                        <div class="detail-item">
                            <label>반품 ID</label>
                            <span>${refund.refundId}</span>
                        </div>
                        <div class="detail-item">
                            <label>주문번호</label>
                            <span>${refund.orderNumber}</span>
                        </div>
                        <div class="detail-item">
                            <label>신청자</label>
                            <span>${refund.userName} (${refund.userType})</span>
                        </div>
                        <div class="detail-item">
                            <label>신청일</label>
                            <span>${refund.requestDate}</span>
                        </div>
                        <div class="detail-item">
                            <label>현재 상태</label>
                            <span class="status-badge ${getStatusClass(refund.status)}">${refund.statusText}</span>
                        </div>
                    </div>
                </div>

                <div class="detail-section">
                    <h4>반품 상품 목록</h4>
                    <div class="items-list">
                        ${itemsHtml}
                    </div>
                    <div class="detail-item" style="margin-top: 15px; background: #e8f5e9;">
                        <label>총 반품 금액</label>
                        <span style="color: #4A7C59; font-size: 1.2em;">${refund.totalAmount.toLocaleString()}원</span>
                    </div>
                </div>

                <div class="detail-section">
                    <h4>반품 사유</h4>
                    <div class="detail-item">
                        <label>사유 구분</label>
                        <span>${refund.reason}</span>
                    </div>
                    <div class="detail-item" style="margin-top: 10px;">
                        <label>상세 내용</label>
                        <span>${refund.detailReason}</span>
                    </div>
                </div>

                ${refund.adminMemo ? `
                    <div class="detail-section">
                        <h4>관리자 메모</h4>
                        <div class="detail-item">
                            <span>${refund.adminMemo}</span>
                        </div>
                    </div>
                ` : ''}

                ${refund.status === 0 ? `
                    <div class="status-update-form">
                        <h4>상태 변경</h4>
                        <form onsubmit="updateRefundStatus(event, ${refund.refundId})">
                            <div class="form-group">
                                <label>처리 상태</label>
                                <select id="updateStatus" required>
                                    <option value="">선택하세요</option>
                                    <option value="1">승인</option>
                                    <option value="2">거부</option>
                                    <option value="3">완료</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>처리 메모</label>
                                <textarea id="updateMemo" placeholder="처리 사유나 메모를 입력하세요 (선택사항)"></textarea>
                            </div>
                            <button type="submit" class="btn btn-primary" style="width: 100%;">상태 변경</button>
                        </form>
                    </div>
                ` : ''}
            `;

    document.getElementById('detailModal').classList.add('show');
}

function closeModal() {
    document.getElementById('detailModal').classList.remove('show');
}

async function approveRefund(refundId) {
    if (!confirm('이 반품을 승인하시겠습니까?')) return;

    try {
        // const response = await fetch(`/admin/refunds/${refundId}`, {
        //     method: 'PATCH',
        //     headers: {
        //         'Content-Type': 'application/json',
        //         'Authorization': 'Bearer ' + adminToken
        //     },
        //     body: JSON.stringify({
        //         refundStatus: 1
        //     })
        // });

        alert('반품이 승인되었습니다.');
        loadRefunds();
        updateStats();

    } catch (error) {
        alert('처리 중 오류가 발생했습니다.');
        console.error(error);
    }
}

async function rejectRefund(refundId) {
    const reason = prompt('거부 사유를 입력해주세요:');
    if (!reason || !reason.trim()) return;

    try {
        // const response = await fetch(`/admin/refunds/${refundId}`, {
        //     method: 'PATCH',
        //     headers: {
        //         'Content-Type': 'application/json',
        //         'Authorization': 'Bearer ' + adminToken
        //     },
        //     body: JSON.stringify({
        //         refundStatus: 2,
        //         adminMemo: reason
        //     })
        // });

        alert('반품이 거부되었습니다.');
        loadRefunds();
        updateStats();

    } catch (error) {
        alert('처리 중 오류가 발생했습니다.');
        console.error(error);
    }
}

async function updateRefundStatus(event, refundId) {
    event.preventDefault();

    const status = document.getElementById('updateStatus').value;
    const memo = document.getElementById('updateMemo').value;

    if (!status) {
        alert('처리 상태를 선택해주세요.');
        return;
    }

    try {
        // const response = await fetch(`/admin/refunds/${refundId}`, {
        //     method: 'PATCH',
        //     headers: {
        //         'Content-Type': 'application/json',
        //         'Authorization': 'Bearer ' + adminToken
        //     },
        //     body: JSON.stringify({
        //         refundStatus: parseInt(status),
        //         adminMemo: memo
        //     })
        // });

        alert('상태가 변경되었습니다.');
        closeModal();
        loadRefunds();
        updateStats();

    } catch (error) {
        alert('처리 중 오류가 발생했습니다.');
        console.error(error);
    }
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('detailModal');
    if (event.target === modal) {
        closeModal();
    }
}