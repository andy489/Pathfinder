const routeId = document.getElementById('curr-route-id')?.value
const commentForm = document.getElementById('comment-form')
if (commentForm) {
    commentForm.addEventListener("submit", handleFormSubmission)
}

const csrfHeaderName = document.head.querySelector('[name=_csrf_header]').content
const csrfHeaderValue = document.head.querySelector('[name=_csrf]').content

const commentContainer = document.getElementById('comment-control-section')

const backendLocation = "";

/* i18n labels injected via <meta> tags in the template */
const i18nTranslate      = document.head.querySelector('[name=i18n-comment-translate]')?.content      || 'Translate';
const i18nTranslating    = document.head.querySelector('[name=i18n-comment-translating]')?.content    || 'Translating...';
const i18nShowOriginal   = document.head.querySelector('[name=i18n-comment-show-original]')?.content  || 'Show original';

function escapeHtml(text) {
    const div = document.createElement('div');
    div.appendChild(document.createTextNode(text));
    return div.innerHTML;
}

async function handleFormSubmission(event) {
    event.preventDefault()
    const textVal = document.getElementById('comment').value

    fetch(`${backendLocation}/api/comment`, {
        method: 'POST', headers: {
            'Content-type': 'application/json',
            'Accept': 'application/json',
            [csrfHeaderName]: csrfHeaderValue
        }, body: JSON.stringify({
            routeId: routeId,
            comment: textVal
        })
    }).then(res => {
        if (!res.ok) {
            return res.json().then(err => { throw new Error(err.message || 'Comment failed: ' + res.status) })
        }
        return res.json()
    }).then(data => {
        document.getElementById('comment').value = ""
        const node = commentAsNode(data, true)
        commentContainer.appendChild(node)
        attachTranslateButton(node)
    }).catch(err => {
        console.error('Comment submission error:', err.message)
    })
}

fetch(`${backendLocation}/api/${routeId}/comments`, {
    headers: { "Accept": "application/json" }
}).then(res => {
    if (!res.ok) throw new Error('Failed to load comments: ' + res.status)
    return res.json()
}).then(data => {
    for (let comment of data) {
        const node = commentAsNode(comment, false)
        commentContainer.appendChild(node)
        attachTranslateButton(node)
    }
}).catch(err => console.error('Error loading comments:', err.message))

function commentAsNode(comment, approval) {
    const wrapper = document.createElement('div')
    wrapper.id = 'comment-' + comment.commentId
    wrapper.className = 'comment-item'

    const author = document.createElement('div')
    author.className = 'comment-author'
    author.textContent = comment.authorName
    wrapper.appendChild(author)

    const text = document.createElement('p')
    text.className = 'comment-text'
    text.dataset.original = comment.comment
    text.textContent = comment.comment
    wrapper.appendChild(text)

    const date = document.createElement('div')
    date.className = 'comment-date'
    date.textContent = new Date(comment.created).toLocaleString()
    wrapper.appendChild(date)

    if (approval) {
        const pending = document.createElement('span')
        pending.className = 'small text-danger'
        pending.textContent = ' [Waiting for approval from MODERATOR or ADMIN]'
        wrapper.appendChild(pending)
    }

    return wrapper
}

function attachTranslateButton(commentNode) {
    const textEl = commentNode.querySelector('.comment-text')
    if (!textEl) return

    const btn = document.createElement('button')
    btn.className = 'btn btn-ghost btn-sm'
    btn.style.cssText = 'margin-top:var(--sp-xs); font-size:.75rem; padding:.2rem .6rem;'
    btn.innerHTML = '<i class="fas fa-language"></i> ' + escapeHtml(i18nTranslate)

    let translated = false

    btn.addEventListener('click', async function () {
        if (translated) {
            textEl.textContent = textEl.dataset.original
            btn.innerHTML = '<i class="fas fa-language"></i> ' + escapeHtml(i18nTranslate)
            translated = false
            return
        }

        btn.disabled = true
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> ' + escapeHtml(i18nTranslating)

        try {
            const result = await window.translateToEnglish(textEl.dataset.original)
            textEl.textContent = result
            btn.innerHTML = '<i class="fas fa-undo"></i> ' + escapeHtml(i18nShowOriginal)
            translated = true
        } catch {
            btn.innerHTML = '<i class="fas fa-language"></i> ' + escapeHtml(i18nTranslate)
        } finally {
            btn.disabled = false
        }
    })

    commentNode.appendChild(btn)
}
