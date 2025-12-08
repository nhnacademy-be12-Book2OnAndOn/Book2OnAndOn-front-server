const sidebar = document.getElementById('categorySidebar');
const overlay = document.getElementById('sidebarOverlay');

function toggleSidebar() {
    if (!sidebar || !overlay) return;
    sidebar.classList.toggle('open');
    overlay.classList.toggle('active');
}

document.addEventListener('DOMContentLoaded', () => {
    const chips = document.querySelectorAll('.chip-list .chip');
    const chipList = document.querySelector('.chip-list');
    const searchInput = document.querySelector('.global-search .input');
    const clearButton = document.querySelector('.btn-clear');
    let isDown = false;
    let startX = 0;
    let scrollLeft = 0;

    chips.forEach(chip => {
        chip.addEventListener('click', () => {
            chips.forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
        });
    });

    if (chipList) {
        chipList.addEventListener('mousedown', (e) => {
            isDown = true;
            chipList.classList.add('dragging');
            startX = e.pageX - chipList.offsetLeft;
            scrollLeft = chipList.scrollLeft;
        });

        chipList.addEventListener('mouseleave', () => {
            isDown = false;
            chipList.classList.remove('dragging');
        });

        chipList.addEventListener('mouseup', () => {
            isDown = false;
            chipList.classList.remove('dragging');
        });

        chipList.addEventListener('mousemove', (e) => {
            if (!isDown) return;
            e.preventDefault();
            const x = e.pageX - chipList.offsetLeft;
            const walk = (x - startX) * 1.2;
            chipList.scrollLeft = scrollLeft - walk;
        });
    }

    window.toggleSubcategory = function(row) {
        if (row.dataset.hasChildren !== 'true') {
            return;
        }
        const item = row.closest('.category-item');
        if (!item) return;
        item.classList.toggle('open');
        const list = item.querySelector(':scope > .subcategory-list');
        if (!list) {
            return;
        }

        if (item.classList.contains('open')) {
            list.style.maxHeight = list.scrollHeight + 'px';
            const onTransitionEnd = (event) => {
                if (event.target !== list) return;
                if (item.classList.contains('open')) {
                    list.style.maxHeight = 'none';
                }
                list.removeEventListener('transitionend', onTransitionEnd);
            };
            list.addEventListener('transitionend', onTransitionEnd);
        } else {
            if (list.style.maxHeight === 'none') {
                list.style.maxHeight = list.scrollHeight + 'px';
                requestAnimationFrame(() => {
                    list.style.maxHeight = 0;
                });
            } else {
                list.style.maxHeight = 0;
            }
        }
    };

    if (searchInput && clearButton) {
        clearButton.addEventListener('click', () => {
            searchInput.value = '';
            searchInput.focus();
        });
    }

    const hamburger = document.querySelector('.hamburger');
    const closeBtn = document.querySelector('.close-sidebar');
    if (hamburger) {
        hamburger.addEventListener('click', toggleSidebar);
    }
    if (overlay) {
        overlay.addEventListener('click', toggleSidebar);
    }
    if (closeBtn) {
        closeBtn.addEventListener('click', toggleSidebar);
    }
});

window.toggleSidebar = toggleSidebar;
