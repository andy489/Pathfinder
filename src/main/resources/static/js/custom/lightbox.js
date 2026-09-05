(function () {
    const overlay = document.createElement('div');
    overlay.id = 'lightbox-overlay';
    Object.assign(overlay.style, {
        display: 'none',
        position: 'fixed',
        inset: '0',
        background: 'rgba(0,0,0,.92)',
        zIndex: '9999',
        alignItems: 'center',
        justifyContent: 'center',
        cursor: 'zoom-out',
        padding: '1rem',
    });

    const img = document.createElement('img');
    Object.assign(img.style, {
        maxWidth: '82vw',
        maxHeight: '92vh',
        objectFit: 'contain',
        borderRadius: '6px',
        boxShadow: '0 8px 40px rgba(0,0,0,.6)',
        cursor: 'default',
    });

    const close = document.createElement('button');
    close.innerHTML = '&times;';
    Object.assign(close.style, {
        position: 'absolute',
        top: '1rem',
        right: '1.25rem',
        background: 'none',
        border: 'none',
        color: '#fff',
        fontSize: '2.4rem',
        lineHeight: '1',
        cursor: 'pointer',
        fontFamily: 'inherit',
    });

    function makeArrow(direction) {
        const btn = document.createElement('button');
        btn.innerHTML = direction === 'prev' ? '&#10094;' : '&#10095;';
        Object.assign(btn.style, {
            position: 'absolute',
            top: '50%',
            transform: 'translateY(-50%)',
            [direction === 'prev' ? 'left' : 'right']: '1.25rem',
            background: 'rgba(255,255,255,.15)',
            border: 'none',
            color: '#fff',
            fontSize: '2rem',
            lineHeight: '1',
            cursor: 'pointer',
            borderRadius: '50%',
            width: '2.8rem',
            height: '2.8rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            transition: 'background .2s',
        });
        btn.addEventListener('mouseenter', function () { btn.style.background = 'rgba(255,255,255,.35)'; });
        btn.addEventListener('mouseleave', function () { btn.style.background = 'rgba(255,255,255,.15)'; });
        return btn;
    }

    const prevBtn = makeArrow('prev');
    const nextBtn = makeArrow('next');

    overlay.appendChild(img);
    overlay.appendChild(close);
    overlay.appendChild(prevBtn);
    overlay.appendChild(nextBtn);
    document.body.appendChild(overlay);

    let images = [];
    let currentIndex = 0;

    function show(index) {
        currentIndex = (index + images.length) % images.length;
        img.src = images[currentIndex].src;
        img.alt = images[currentIndex].alt || '';
        prevBtn.style.display = images.length > 1 ? 'flex' : 'none';
        nextBtn.style.display = images.length > 1 ? 'flex' : 'none';
    }

    function open(index, imgList) {
        images = imgList;
        overlay.style.display = 'flex';
        document.body.style.overflow = 'hidden';
        show(index);
    }

    function closeLightbox() {
        overlay.style.display = 'none';
        img.src = '';
        document.body.style.overflow = '';
    }

    overlay.addEventListener('click', function (e) {
        if (e.target === overlay) closeLightbox();
    });
    close.addEventListener('click', closeLightbox);
    prevBtn.addEventListener('click', function (e) { e.stopPropagation(); show(currentIndex - 1); });
    nextBtn.addEventListener('click', function (e) { e.stopPropagation(); show(currentIndex + 1); });

    document.addEventListener('keydown', function (e) {
        if (overlay.style.display === 'none') return;
        if (e.key === 'Escape') closeLightbox();
        if (e.key === 'ArrowLeft') show(currentIndex - 1);
        if (e.key === 'ArrowRight') show(currentIndex + 1);
    });

    function attachToGrid(grid) {
        const imgs = Array.from(grid.querySelectorAll('img'));
        imgs.forEach(function (el, i) {
            el.style.cursor = 'zoom-in';
            el.addEventListener('click', function () { open(i, imgs); });
        });
    }

    function init() {
        document.querySelectorAll('.photo-grid').forEach(attachToGrid);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
