const API_BASE = '/admin/point-policies';
const userId = localStorage.getItem('userId') || '1'; // 관리자 ID
const USE_DUMMY = false; // 더미 데이터 사용 여부

// 더미 데이터
const DUMMY_POLICIES = [
    {
        policyId: 1,
        policyName: 'SIGNUP',
        accrualRate: null,
        accrualPoint: 5000,
        isActive: true
    },
    {
        policyId: 2,
        policyName: 'ORDER',
        accrualRate: 1.0,
        accrualPoint: null,
        isActive: true
    },
    {
        policyId: 3,
        policyName: 'REVIEW',
        accrualRate: null,
        accrualPoint: 200,
        isActive: true
    },
    {
        policyId: 4,
        policyName: 'REVIEW_PHOTO',
        accrualRate: null,
        accrualPoint: 500,
        isActive: true
    }
];

let policies = [];
let currentEditingPolicy = null;

// 페이지 로드 시 정책 목록 불러오기
async function loadPolicies() {
    if (USE_DUMMY) {
        // 더미 데이터 사용
        policies = [...DUMMY_POLICIES];
        renderPolicies();
        return;
    }

    // 실제 API 호출
    try {
        const response = await fetch(API_BASE, {
            method: 'GET',
            headers: {
                'X-USER-ID': userId
            }
        });

        if (!response.ok) {
            throw new Error('정책 목록을 불러오는데 실패했습니다.');
        }

        policies = await response.json();
        renderPolicies();
    } catch (error) {
        console.error('Error:', error);
        alert('정책 목록을 불러오는데 실패했습니다.');
        renderEmptyState('정책 목록을 불러오는데 실패했습니다.');
    }
}

// 정책 목록 렌더링
function renderPolicies() {
    const tbody = document.getElementById('policiesTableBody');

    if (policies.length === 0) {
        renderEmptyState('등록된 정책이 없습니다.');
        return;
    }

    tbody.innerHTML = policies.map(policy => `
                <tr>
                    <td>${policy.policyId}</td>
                    <td><strong>${policy.policyName}</strong></td>
                    <td>${policy.accrualRate !== null ? policy.accrualRate + '%' : '-'}</td>
                    <td>${policy.accrualPoint !== null ? policy.accrualPoint.toLocaleString() + 'P' : '-'}</td>
                    <td>
                        <span class="status-badge ${policy.isActive ? 'status-active' : 'status-inactive'}">
                            ${policy.isActive ? '활성' : '비활성'}
                        </span>
                    </td>
                    <td>
                        <div class="action-buttons">
                            <button class="btn btn-primary btn-small" onclick="openEditModal(${policy.policyId})">
                                수정
                            </button>
                            <button class="btn btn-secondary btn-small" onclick="openActiveModal(${policy.policyId})">
                                ${policy.isActive ? '비활성화' : '활성화'}
                            </button>
                        </div>
                    </td>
                </tr>
            `).join('');
}

// 빈 상태 렌더링
function renderEmptyState(message) {
    const tbody = document.getElementById('policiesTableBody');
    tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="empty-state">
                        <div class="empty-state-icon">📋</div>
                        <div>${message}</div>
                    </td>
                </tr>
            `;
}

// 수정 모달 열기
function openEditModal(policyId) {
    const policy = policies.find(p => p.policyId === policyId);
    if (!policy) return;

    currentEditingPolicy = policy;

    document.getElementById('editPolicyId').value = policy.policyId;
    document.getElementById('editPolicyName').value = policy.policyName;
    document.getElementById('editAccrualRate').value = policy.accrualRate || 0;
    document.getElementById('editAccrualPoint').value = policy.accrualPoint || 0;

    document.getElementById('editModal').classList.add('active');
}

// 수정 모달 닫기
function closeEditModal() {
    document.getElementById('editModal').classList.remove('active');
    currentEditingPolicy = null;
}

// 수정 제출
async function submitEdit(event) {
    event.preventDefault();

    if (!currentEditingPolicy) return;

    const accrualRate = parseFloat(document.getElementById('editAccrualRate').value);
    const accrualPoint = parseInt(document.getElementById('editAccrualPoint').value);

    if (accrualRate < 0 || accrualRate > 100) {
        alert('적립 비율은 0-100 사이의 값이어야 합니다.');
        return;
    }

    if (accrualPoint < 0) {
        alert('고정 포인트는 0 이상이어야 합니다.');
        return;
    }

    if (USE_DUMMY) {
        // 더미 데이터 업데이트
        const policyIndex = policies.findIndex(p => p.policyId === currentEditingPolicy.policyId);
        if (policyIndex !== -1) {
            policies[policyIndex].accrualRate = accrualRate || null;
            policies[policyIndex].accrualPoint = accrualPoint || null;
        }
        alert('정책이 성공적으로 수정되었습니다.');
        closeEditModal();
        renderPolicies();
        return;
    }

    // 실제 API 호출
    try {
        const response = await fetch(`${API_BASE}/${currentEditingPolicy.policyId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-USER-ID': userId
            },
            body: JSON.stringify({
                accrualRate: accrualRate,
                accrualPoint: accrualPoint
            })
        });

        if (!response.ok) {
            throw new Error('정책 수정에 실패했습니다.');
        }

        alert('정책이 성공적으로 수정되었습니다.');
        closeEditModal();
        loadPolicies();
    } catch (error) {
        console.error('Error:', error);
        alert('정책 수정에 실패했습니다.');
    }
}

// 활성화 상태 변경 모달 열기
function openActiveModal(policyId) {
    const policy = policies.find(p => p.policyId === policyId);
    if (!policy) return;

    currentEditingPolicy = policy;

    document.getElementById('activePolicyName').textContent = policy.policyName;
    document.getElementById('activeToggle').checked = policy.isActive;

    document.getElementById('activeModal').classList.add('active');
}

// 활성화 상태 변경 모달 닫기
function closeActiveModal() {
    document.getElementById('activeModal').classList.remove('active');
    currentEditingPolicy = null;
}

// 활성화 상태 변경 제출
async function submitActiveChange() {
    if (!currentEditingPolicy) return;

    const isActive = document.getElementById('activeToggle').checked;

    if (USE_DUMMY) {
        // 더미 데이터 업데이트
        const policyIndex = policies.findIndex(p => p.policyId === currentEditingPolicy.policyId);
        if (policyIndex !== -1) {
            policies[policyIndex].isActive = isActive;
        }
        alert('정책 상태가 성공적으로 변경되었습니다.');
        closeActiveModal();
        renderPolicies();
        return;
    }

    // 실제 API 호출
    try {
        const response = await fetch(`${API_BASE}/${currentEditingPolicy.policyId}/active`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'X-USER-ID': userId
            },
            body: JSON.stringify({
                isActive: isActive
            })
        });

        if (!response.ok) {
            throw new Error('정책 상태 변경에 실패했습니다.');
        }

        alert('정책 상태가 성공적으로 변경되었습니다.');
        closeActiveModal();
        loadPolicies();
    } catch (error) {
        console.error('Error:', error);
        alert('정책 상태 변경에 실패했습니다.');
    }
}

// 모달 외부 클릭 시 닫기
window.onclick = function (event) {
    const editModal = document.getElementById('editModal');
    const activeModal = document.getElementById('activeModal');

    if (event.target === editModal) {
        closeEditModal();
    }
    if (event.target === activeModal) {
        closeActiveModal();
    }
}

// 페이지 로드 시 실행
loadPolicies();