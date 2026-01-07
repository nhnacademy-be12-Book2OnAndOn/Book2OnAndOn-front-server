/**
 * 관리자 주문 상세 - 상태 변경 JS
 * - 주문 상태 변경
 * - 주문 상품 상태 변경
 * - 주문 취소
 */

document.addEventListener("DOMContentLoaded", () => {
    bindOrderStatusUpdate();
    bindOrderItemStatusUpdate();
});

/* ===============================
   주문 전체 상태 변경
================================ */
function bindOrderStatusUpdate() {
    const box = document.querySelector(".order-status-box");
    if (!box) return;

    const orderNumber = box.dataset.orderNumber;
    const select = box.querySelector(".order-status-select");
    const button = box.querySelector(".btn-order-update");

    console.log(select);
    button.addEventListener("click", async () => {
        const newStatus = select.value;

        if (!confirm(`주문 상태를 '${newStatus}' 로 변경하시겠습니까?`)) {
            return;
        }

        try {
            await fetch(`/admin/orders/${orderNumber}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    orderStatus: newStatus
                })
            });

            alert("주문 상태가 변경되었습니다.");
            location.reload();
        } catch (e) {
            console.error(e);
            alert("주문 상태 변경에 실패했습니다.");
        }
    });
}

/* ===============================
   주문 상품 상태 변경
================================ */
function bindOrderItemStatusUpdate() {
    document.querySelectorAll(".order-item-status-form").forEach(form => {
        const orderItemId = form.dataset.orderItemId;
        const select = form.querySelector(".order-item-status-select");
        const button = form.querySelector(".btn-item-update");

        // 주문번호는 URL에서 추출
        const orderNumber = getOrderNumberFromPath();

        button.addEventListener("click", async () => {
            const newStatus = select.value;

            if (!confirm(`상품 상태를 '${newStatus}' 로 변경하시겠습니까?`)) {
                return;
            }

            try {
                await fetch(`/admin/orders/${orderNumber}/order-items`, {
                    method: "PATCH",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        orderItemId: Number(orderItemId),
                        orderItemStatus: newStatus
                    })
                });

                alert("상품 상태가 변경되었습니다.");
                location.reload();
            } catch (e) {
                console.error(e);
                alert("상품 상태 변경에 실패했습니다.");
            }
        });
    });
}


/* ===============================
   공통 유틸
================================ */
function getOrderNumberFromPath() {
    const paths = location.pathname.split("/");
    return paths[paths.length - 1];
}
