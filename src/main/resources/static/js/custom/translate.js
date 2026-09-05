/* translate.js — Translation proxy via /api/translate (LibreTranslate backend) */

const TRANSLATE_PROXY_URL = '/api/translate';

function getCsrfHeaders() {
    const token  = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    return (token && header) ? { [header]: token } : {};
}

/* Translates text to the given target language code */
window.translateTo = async function (text, targetLang) {
    const res = await fetch(TRANSLATE_PROXY_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...getCsrfHeaders() },
        body: JSON.stringify({ q: text, target: targetLang })
    });
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const data = await res.json();
    if (!data.translatedText) throw new Error('No translation returned');
    return data.translatedText;
};

/* Convenience wrapper — kept for backwards compat with comments.js */
window.translateToEnglish = function (text) {
    return window.translateTo(text, 'en');
};

/* ---------- Description panel (route-details.html) ---------- */
(function () {
    const btn    = document.getElementById('translate-desc-btn');
    const descEl = document.getElementById('route-description');
    if (!btn || !descEl) return;

    const labelTranslate   = btn.dataset.translateLabel   || 'Translate to English';
    const labelTranslating = btn.dataset.translatingLabel || 'Translating...';
    const labelOriginal    = btn.dataset.showOriginalLabel || 'Show original';
    const btnSpan          = btn.querySelector('span');

    let originalText = null;
    let translated   = false;

    /* Called by auto-translate when it translates the description element */
    window._descAutoTranslated = function (original) {
        originalText = original;
        translated = true;
        btnSpan.textContent = labelOriginal;
    };

    btn.addEventListener('click', async function () {
        if (translated) {
            descEl.textContent = originalText;
            btnSpan.textContent = labelTranslate;
            translated = false;
            return;
        }

        btn.disabled = true;
        btnSpan.textContent = labelTranslating;

        try {
            if (originalText === null) originalText = descEl.textContent;
            const result = await window.translateToEnglish(originalText);
            descEl.textContent = result;
            btnSpan.textContent = labelOriginal;
            translated = true;
        } catch {
            btnSpan.textContent = labelTranslate;
        } finally {
            btn.disabled = false;
        }
    });
})();

/* ---------- Auto-translate route content for all non-Bulgarian locales ---------- */
(function () {
    const localeMeta = document.querySelector('meta[name="page-locale"]');
    if (!localeMeta) return;
    const locale = localeMeta.content;
    /* Data is stored in Bulgarian — skip auto-translate only when locale is 'bg' */
    if (!locale || locale === 'bg') return;

    /* Map Spring locale codes to LibreTranslate language codes */
    const LOCALE_MAP = { 'zh': 'zh-Hans' };
    const targetLang = LOCALE_MAP[locale] || locale || 'en';

    /* Translate a single element, storing the original text */
    async function translateElement(el) {
        if (el.dataset.translated) return;
        const original = el.textContent.trim();
        if (!original) return;
        try {
            const result = await window.translateTo(original, targetLang);
            el.dataset.original = original;
            el.textContent = result;
            el.dataset.translated = '1';
            /* Notify description button if this was the route description */
            if (el.id === 'route-description' && typeof window._descAutoTranslated === 'function') {
                window._descAutoTranslated(original);
            }
        } catch {
            /* silently skip on error */
        }
    }

    /* All pages — translate any element with data-translatable */
    document.querySelectorAll('[data-translatable]').forEach(translateElement);
})();
