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


    // AI 서치 결과 반환

    const getAiSearchParams = () => {
        const params = new URLSearchParams(window.location.search);
        return {
            keyword: params.get('keyword') || '',
            categoryId: params.get('categoryId') || '',
            page: parseInt(params.get('page')) || 0,
            autoAi: params.get('autoAi') === 'true'
        };
    };

    const aiButton = document.getElementById('btn-ai-recommend');
    const aiResultContainer = document.getElementById('ai-result-container');
    const aiLoading = document.getElementById('ai-loading');
    const aiContent = document.getElementById('ai-content');

    const handleAiClick = async () => {
        const { keyword, categoryId, page } = getAiSearchParams();

        if (page > 0) {
            console.log("2페이지 이상이므로 1페이지로 이동합니다.");
            const params = new URLSearchParams(window.location.search);
            params.set('page', '0');
            params.set('autoAi', 'true');
            window.location.search = params.toString();
            return;
        }

        if (aiResultContainer) aiResultContainer.style.display = 'block';
        if (aiLoading) aiLoading.style.display = 'block';
        if (aiContent) aiContent.innerHTML = '';
        if (aiButton) aiButton.disabled = true;

        try {
            if (!keyword) throw new Error("검색어가 없습니다.");

            let url = `/books/search/ai-result?keyword=${encodeURIComponent(keyword)}`;
            if (categoryId) url += `&categoryId=${categoryId}`;

            const response = await fetch(url);

            if (response.status === 204) {
                if (aiLoading) aiLoading.style.display = 'none';
                if (aiContent) {
                    aiContent.innerHTML = `
                        <div style="text-align: center; padding: 20px; color: #555;">
                            <p>AI가 아직 분석 중입니다!</p>
                            <p style="font-size: 0.9rem; color: #888;">잠시 후 다시 눌러주세요.</p>
                        </div>`;
                }
                if (aiButton) aiButton.disabled = false;
                return;
            }

            if (response.ok) {
                const data = await response.json();
                renderAiResult(data);

                if (aiLoading) aiLoading.style.display = 'none';
                if (aiButton) aiButton.innerHTML = '<span class="sparkle"></span> 분석 완료';
            } else {
                throw new Error("Server Error: " + response.status);
            }

        } catch (error) {
            if (aiLoading) aiLoading.style.display = 'none';
            if (aiContent) aiContent.innerHTML = `<p style="color:red; text-align:center;">오류: ${error.message}</p>`;
            if (aiButton) aiButton.disabled = false;
        }
    };

    const renderAiResult = (aiData) => {
        document.querySelectorAll('.book-card.ai-highlight').forEach(card => {
            card.classList.remove('ai-highlight');
        });
        document.querySelectorAll('.card-ai-reason').forEach(el => el.remove());

        if (!aiData || aiData.length === 0) {
            if(aiContent) aiContent.innerHTML = '<p style="text-align:center;">추천 결과가 없습니다.</p>';
            return;
        }

        if (aiContent) {
            aiContent.innerHTML = `
                <div style="text-align: center; color: #2d5f3f; padding: 10px;">
                    <strong>총 ${aiData.length}권</strong>을 찾았습니다! <br>
                    <span style="font-size: 0.9em; color: #666;">(추천 도서는 목록 최상단으로 이동됩니다)</span>
                </div>`;
        }

        const bookGrid = document.querySelector('.book-grid');

        [...aiData].reverse().forEach(item => {
            const targetCard = document.querySelector(`.book-card[data-book-id="${item.id}"]`);

            if (targetCard) {
                targetCard.classList.add('ai-highlight');

                const reasonDiv = document.createElement('div');
                reasonDiv.className = 'card-ai-reason';
                reasonDiv.innerHTML = `<strong>AI 추천 사유</strong>${item.reason}`;

                const btn = targetCard.querySelector('.btn-outline-green');
                if (btn) targetCard.insertBefore(reasonDiv, btn);
                else targetCard.appendChild(reasonDiv);

                if (bookGrid) {
                    bookGrid.prepend(targetCard);
                }
            }
        });

        if (aiResultContainer) {
            aiResultContainer.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    };

    const bindAiButton = () => {
        if (aiButton) {
            aiButton.addEventListener('click', handleAiClick);
        }
        const { autoAi } = getAiSearchParams();

        if (autoAi) {
            setTimeout(() => {
                handleAiClick();
            }, 200);

            const params = new URLSearchParams(window.location.search);
            params.delete('autoAi');
            const newUrl = window.location.pathname + '?' + params.toString();
            window.history.replaceState(null, '', newUrl);
        }
    };

    syncInputsFromUrl();
    bindInputs();
    bindAiButton();

    if(layout){
        applyFilters();
    }
})();
