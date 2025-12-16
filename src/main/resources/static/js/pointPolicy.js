const API_BASE = '/admin/point-policies/api';
const USE_DUMMY = false; // 더미 데이터 사용 여부

// 더미 데이터
const DUMMY_POLICIES = [
    { pointPolicyId: 1, pointPolicyName: 'SIGNUP', pointAddPoint: 5000, pointIsActive: true },
    { pointPolicyId: 2, pointPolicyName: 'ORDER', pointAddPoint: 0, pointIsActive: true },
    { pointPolicyId: 3, pointPolicyName: 'REVIEW', pointAddPoint: 200, pointIsActive: true },
    { pointPolicyId: 4, pointPolicyName: 'REVIEW_PHOTO', pointAddPoint: 500, pointIsActive: false }
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

    if (Array.isArray(window.__initialPolicies) && window.__initialPolicies.length > 0) {
        policies = window.__initialPolicies;
        renderPolicies();
        return;
    }

    // 실제 API 호출
    try {
        const response = await fetch(API_BASE, { method: 'GET', credentials: 'include' });

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

    tbody.innerHTML = policies.map(policy => {
        const id = policy.pointPolicyId ?? policy.policyId ?? '';
        const idStr = id === null || id === undefined ? '' : String(id);
        const name = policy.pointPolicyName ?? policy.policyName ?? '정책명 없음';
        const fixedPoint = policy.pointAddPoint ?? policy.accrualPoint ?? null;
        const isActive = policy.pointIsActive ?? policy.isActive ?? false;
        const accrualRateText = '-'; // 현재 DTO에 비율 필드 없음
        const accrualPointText = typeof fixedPoint === 'number' ? `${fixedPoint.toLocaleString()}P` : '-';

        return `
                <tr>
                    <td>${idStr || '-'}</td>
                    <td><strong>${name}</strong></td>
                    <td>${accrualRateText}</td>
                    <td>${accrualPointText}</td>
                    <td>
                        <span class="status-badge ${isActive ? 'status-active' : 'status-inactive'}">
                            ${isActive ? '활성' : '비활성'}
                        </span>
                    </td>
                    <td>
                        <div class="action-buttons">
                            <button class="btn btn-primary btn-small" onclick="openEditModal('${idStr}')">
                                수정
                            </button>
                            <button class="btn btn-secondary btn-small" onclick="openActiveModal('${idStr}')">
                                ${isActive ? '비활성화' : '활성화'}
                            </button>
                        </div>
                    </td>
                </tr>
            `;
    }).join('');
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
    const targetId = policyId !== null && policyId !== undefined ? String(policyId) : '';
    const policy = policies.find(p => String(p.pointPolicyId ?? p.policyId) === targetId);
    if (!policy) {
        console.warn('정책을 찾을 수 없습니다.', policyId, policies);
        return;
    }

    currentEditingPolicy = policy;

    document.getElementById('editPolicyId').value = policy.pointPolicyId ?? policy.policyId ?? '';
    document.getElementById('editPolicyName').value = policy.pointPolicyName ?? policy.policyName ?? '';
    document.getElementById('editAccrualRate').value = 0; // 비율 필드 없음
    document.getElementById('editAccrualPoint').value = policy.pointAddPoint ?? policy.accrualPoint ?? 0;

    document.getElementById('editModal').classList.add('show');
}

// 수정 모달 닫기
function closeEditModal() {
    document.getElementById('editModal').classList.remove('show');
    currentEditingPolicy = null;
}

// 수정 제출
async function submitEdit(event) {
    event.preventDefault();

    if (!currentEditingPolicy) return;

    const accrualPoint = parseInt(document.getElementById('editAccrualPoint').value);

    if (isNaN(accrualPoint) || accrualPoint < 0) {
        alert('고정 포인트는 0 이상이어야 합니다.');
        return;
    }

    if (USE_DUMMY) {
        // 더미 데이터 업데이트
        const policyIndex = policies.findIndex(p =>
            String(p.pointPolicyId ?? p.policyId) === String(currentEditingPolicy.pointPolicyId ?? currentEditingPolicy.policyId)
        );
        if (policyIndex !== -1) {
            policies[policyIndex].pointAddPoint = accrualPoint || null;
        }
        alert('정책이 성공적으로 수정되었습니다.');
        closeEditModal();
        renderPolicies();
        return;
    }

    // 실제 API 호출
    try {
        const response = await fetch(`${API_BASE}/${currentEditingPolicy.pointPolicyId ?? currentEditingPolicy.policyId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify({
                pointAddPoint: accrualPoint
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
    const targetId = policyId !== null && policyId !== undefined ? String(policyId) : '';
    const policy = policies.find(p => String(p.pointPolicyId ?? p.policyId) === targetId);
    if (!policy) {
        console.warn('정책을 찾을 수 없습니다.', policyId, policies);
        return;
    }

    currentEditingPolicy = policy;

    document.getElementById('activePolicyName').textContent = policy.pointPolicyName ?? policy.policyName ?? '';
    document.getElementById('activeToggle').checked = policy.pointIsActive ?? policy.isActive ?? false;
    syncActiveLabel();

    document.getElementById('activeModal').classList.add('show');
}

// 활성화 상태 변경 모달 닫기
function closeActiveModal() {
    document.getElementById('activeModal').classList.remove('show');
    currentEditingPolicy = null;
}

function syncActiveLabel() {
    const label = document.getElementById('activeStateLabel');
    const toggle = document.getElementById('activeToggle');
    if (!label || !toggle) return;
    label.textContent = toggle.checked ? '활성' : '비활성';
}

// 활성화 상태 변경 제출
async function submitActiveChange() {
    if (!currentEditingPolicy) return;

    const isActive = document.getElementById('activeToggle').checked;

    if (USE_DUMMY) {
        // 더미 데이터 업데이트
        const policyIndex = policies.findIndex(p =>
            String(p.pointPolicyId ?? p.policyId) === String(currentEditingPolicy.pointPolicyId ?? currentEditingPolicy.policyId)
        );
        if (policyIndex !== -1) {
            policies[policyIndex].pointIsActive = isActive;
        }
        alert('정책 상태가 성공적으로 변경되었습니다.');
        closeActiveModal();
        renderPolicies();
        return;
    }

    // 실제 API 호출
    try {
        const response = await fetch(`${API_BASE}/${currentEditingPolicy.pointPolicyId ?? currentEditingPolicy.policyId}/active`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
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
