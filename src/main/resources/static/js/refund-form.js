const POLICY = {
    CHANGE_OF_MIND_ROUNDTRIP_FEE: 6000,
    APPLY_FEE_REASON: 'CHANGE_OF_MIND',
    FREE_FEE_MIN_AMOUNT: null
};

window.onload = function () {
    recalcSummary();
};

function toggleRow(wrapper) {
    const checkbox = wrapper.querySelector('.item-check');
    if (!checkbox) return;
    checkbox.checked = !checkbox.checked;
    toggleRowUI(wrapper, checkbox.checked);
    recalcSummary();
}

function toggleRowFromCheckbox(checkbox) {
    const wrapper = checkbox.closest('.item-checkbox');
    toggleRowUI(wrapper, checkbox.checked);
    recalcSummary();
}

function toggleRowUI(wrapperEl, isChecked) {
    if (!wrapperEl) return;
    if (isChecked) wrapperEl.classList.add('selected');
    else wrapperEl.classList.remove('selected');
}

function recalcSummary() {
    const checks = document.querySelectorAll('.item-check');
    let selectedItemCount = 0;
    let selectedQtySum = 0;
    let selectedAmount = 0;

    checks.forEach(chk => {
        if (chk.checked) {
            selectedItemCount++;
            selectedQtySum += Number(chk.dataset.qty || 0);
            selectedAmount += Number(chk.dataset.amount || 0);
        }
    });

    const reason = document.getElementById('refundReason')?.value;
    let shippingDeduction = 0;

    if (selectedItemCount > 0 && reason === POLICY.APPLY_FEE_REASON) {
        if (typeof POLICY.FREE_FEE_MIN_AMOUNT === 'number') {
            shippingDeduction = (selectedAmount >= POLICY.FREE_FEE_MIN_AMOUNT) ? 0 : POLICY.CHANGE_OF_MIND_ROUNDTRIP_FEE;
        } else {
            shippingDeduction = POLICY.CHANGE_OF_MIND_ROUNDTRIP_FEE;
        }
    }

    const expectedRefund = Math.max(0, selectedAmount - shippingDeduction);

    document.getElementById('summaryCount').textContent = `상품 ${selectedItemCount}건 · 수량 ${selectedQtySum}개`;
    document.getElementById('summarySelectedAmount').textContent = `${selectedAmount.toLocaleString()}원`;
    document.getElementById('summaryShippingDeduction').textContent = `- ${shippingDeduction.toLocaleString()}원`;
    document.getElementById('summaryExpectedRefund').textContent = `${expectedRefund.toLocaleString()}원`;

    document.getElementById('submitBtn').disabled = (selectedItemCount === 0);

    // submit 전 hidden inputs도 갱신해 둠(실수 방지)
    rebuildHiddenInputs();
}

function rebuildHiddenInputs() {
    const hiddenBox = document.getElementById('selectedItemsHidden');
    hiddenBox.innerHTML = '';

    const checks = document.querySelectorAll('.item-check');
    let idx = 0;

    checks.forEach(chk => {
        if (!chk.checked) return;

        const orderItemId = chk.dataset.orderItemId;
        const bookId = chk.dataset.bookId;
        const qty = chk.dataset.qty;

        hiddenBox.insertAdjacentHTML('beforeend', `
      <input type="hidden" name="items[${idx}].orderItemId" value="${orderItemId}">
      <input type="hidden" name="items[${idx}].bookId" value="${bookId}">
      <input type="hidden" name="items[${idx}].quantity" value="${qty}">
    `);
        idx++;
    });
}

function goBack() {
    window.history.back();
}
