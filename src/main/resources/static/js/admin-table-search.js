(function () {
    function initSearch(input) {
        const targetSelector = input.getAttribute('data-search-target');
        if (!targetSelector) return;
        const table = document.querySelector(targetSelector);
        if (!table) return;
        const tbody = table.querySelector('tbody');
        if (!tbody) return;

        const getEmptyRow = () => {
            let emptyRow = tbody.querySelector('.search-empty-row');
            if (!emptyRow) {
                const colCount = table.querySelectorAll('thead th').length || 1;
                emptyRow = document.createElement('tr');
                emptyRow.className = 'search-empty-row';
                emptyRow.innerHTML = `
                    <td colspan="${colCount}" class="empty-state">
                        <div class="empty-state-icon">📋</div>
                        <div>검색 결과가 없습니다.</div>
                    </td>
                `;
                tbody.appendChild(emptyRow);
            }
            return emptyRow;
        };

        const applyFilter = () => {
            const query = input.value.trim().toLowerCase();
            const rows = Array.from(tbody.querySelectorAll('tr')).filter(
                (row) => !row.classList.contains('search-empty-row')
            );

            let visibleCount = 0;
            rows.forEach((row) => {
                if (!query) {
                    row.style.display = '';
                    visibleCount += 1;
                    return;
                }
                const text = row.innerText.toLowerCase();
                const match = text.includes(query);
                row.style.display = match ? '' : 'none';
                if (match) visibleCount += 1;
            });

            const emptyRow = getEmptyRow();
            if (!query) {
                emptyRow.style.display = 'none';
            } else {
                emptyRow.style.display = visibleCount === 0 ? '' : 'none';
            }
        };

        input.addEventListener('input', applyFilter);
        applyFilter();
        input._applyAdminTableSearch = applyFilter;
    }

    function initAll() {
        document.querySelectorAll('input[data-search-target]').forEach(initSearch);
    }

    window.applyAdminTableSearch = function () {
        document.querySelectorAll('input[data-search-target]').forEach((input) => {
            if (typeof input._applyAdminTableSearch === 'function') {
                input._applyAdminTableSearch();
            }
        });
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAll);
    } else {
        initAll();
    }
})();
