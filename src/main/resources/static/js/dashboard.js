document.addEventListener('DOMContentLoaded', () => {
    initSliders();
});

function initSliders() {
    // 화면에 있는 모든 슬라이더 요소를 찾음
    const sliders = document.querySelectorAll('.book-slider');

    sliders.forEach(slider => {
        const track = slider.querySelector('.slider-track');
        const prevBtn = slider.querySelector('.slider-prev');
        const nextBtn = slider.querySelector('.slider-next');

        // 필수 요소가 하나라도 없으면 스킵 (에러 방지)
        if (!track || !prevBtn || !nextBtn) return;

        let currentIndex = 0;
        // .book-slide 클래스 개수가 곧 전체 페이지 수 (fragments에서 5개씩 묶어서 생성함)
        const slides = slider.querySelectorAll('.book-slide');
        const totalSlides = slides.length;

        // 슬라이더 이동 및 버튼 상태 업데이트 함수
        const updateSlider = () => {
            // 인덱스만큼 왼쪽으로 100%씩 이동
            track.style.transform = `translateX(-${currentIndex * 100}%)`;

            // 첫 페이지면 이전 버튼 숨김/비활성
            if (currentIndex === 0) {
                prevBtn.style.opacity = '0.5';
                prevBtn.style.pointerEvents = 'none';
            } else {
                prevBtn.style.opacity = '1';
                prevBtn.style.pointerEvents = 'auto';
            }

            // 마지막 페이지면 다음 버튼 숨김/비활성
            if (currentIndex >= totalSlides - 1) {
                nextBtn.style.opacity = '0.5';
                nextBtn.style.pointerEvents = 'none';
            } else {
                nextBtn.style.opacity = '1';
                nextBtn.style.pointerEvents = 'auto';
            }
        };

        // 이벤트 리스너 등록
        prevBtn.addEventListener('click', (e) => {
            e.preventDefault(); // 버튼 기본 동작 방지
            if (currentIndex > 0) {
                currentIndex--;
                updateSlider();
            }
        });

        nextBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (currentIndex < totalSlides - 1) {
                currentIndex++;
                updateSlider();
            }
        });

        // 초기 상태 실행
        updateSlider();
    });
}