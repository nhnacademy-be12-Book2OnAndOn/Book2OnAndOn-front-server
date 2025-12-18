(() => {
    const layout = document.querySelector('.search-layout');
    if (!layout) return;

    const priceInputs = Array.from(document.querySelectorAll('input[name="priceRange"]'));
    const ratingInputs = Array.from(document.querySelectorAll('input[name="ratingRange"]'));
    const resetButton = document.querySelector('.filter-reset');
    const cards = Array.from(document.querySelectorAll('.book-card[data-price]'));

    const getFiltersFromUrl = () => {
        const params = new URLSearchParams(window.location.search);
        return {
            priceRange: params.get('priceRange') || 'all',
            ratingRange: params.get('ratingRange') || 'all'
        };
    };

    const updateUrl = (filters) => {
        const params = new URLSearchParams(window.location.search);
        if (filters.priceRange && filters.priceRange !== 'all') {
            params.set('priceRange', filters.priceRange);
        } else {
            params.delete('priceRange');
        }
        if (filters.ratingRange && filters.ratingRange !== 'all') {
            params.set('ratingRange', filters.ratingRange);
        } else {
            params.delete('ratingRange');
        }
        const query = params.toString();
        const nextUrl = query ? `${window.location.pathname}?${query}` : window.location.pathname;
        window.history.replaceState(null, '', nextUrl);
    };

    const matchPrice = (price, range) => {
        if (range === 'all') return true;
        if (range === 'under-15000') return price < 15000;
        if (range === '15000-25000') return price >= 15000 && price <= 25000;
        if (range === 'over-25000') return price > 25000;
        return true;
    };

    const matchRating = (rating, range) => {
        if (range === 'all') return true;
        const min = Number(range);
        if (Number.isNaN(min)) return true;
        return rating >= min;
    };

    const applyFilters = () => {
        const selectedPrice = priceInputs.find(input => input.checked)?.value || 'all';
        const selectedRating = ratingInputs.find(input => input.checked)?.value || 'all';

        cards.forEach(card => {
            const price = Number(card.dataset.price) || 0;
            const rating = Number(card.dataset.rating) || 0;
            const visible = matchPrice(price, selectedPrice) && matchRating(rating, selectedRating);
            card.classList.toggle('is-hidden', !visible);
        });

        updateUrl({ priceRange: selectedPrice, ratingRange: selectedRating });
    };

    const syncInputsFromUrl = () => {
        const { priceRange, ratingRange } = getFiltersFromUrl();
        priceInputs.forEach(input => {
            input.checked = input.value === priceRange;
        });
        ratingInputs.forEach(input => {
            input.checked = input.value === ratingRange;
        });
    };

    const bindInputs = () => {
        priceInputs.forEach(input => {
            input.addEventListener('change', applyFilters);
        });
        ratingInputs.forEach(input => {
            input.addEventListener('change', applyFilters);
        });
        if (resetButton) {
            resetButton.addEventListener('click', () => {
                const priceAll = priceInputs.find(input => input.value === 'all');
                const ratingAll = ratingInputs.find(input => input.value === 'all');
                if (priceAll) priceAll.checked = true;
                if (ratingAll) ratingAll.checked = true;
                applyFilters();
            });
        }
    };

    syncInputsFromUrl();
    bindInputs();
    applyFilters();
})();
