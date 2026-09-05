const backendLocation = ""

const csrfTokenName = document.head.querySelector('[name=_csrf_header]').content
const csrfTokenValue = document.head.querySelector('[name=_csrf]').content

function approveRejectCommentJS(commentId, routeId) {
    makeToggleApproveRejectRequest(deleteAndRemoveCommentByIdJS(commentId, routeId))
}

function approveRejectAllCommentsJS(commentFullId) {
    makeApproveRejectAllRequest(commentFullId)
}

function deleteCommentJS(commentId) {
    makeDeleteRequest(deleteCommentByIdJS(commentId))
}

function deleteAllCommentsJS(commentFullId) {
    makeDeleteAllRequest(commentFullId)
}

function deleteCommentByIdJS(id) {
    let s = id.split('-')
    let prefix = s[0]
    let commentShortId = s[2]
    let routeId = s[3]

    let tagIdToDel = prefix + '-comment-' + commentShortId

    document.getElementById(tagIdToDel).remove()

    adjustCountersDecrement(prefix + '-curr-route-' + routeId,
        'all-' + prefix + '-comments')

    return tagIdToDel
}

function deleteAndRemoveCommentByIdJS(commentId, routeId) {
    // commentId: new-approve-3, old-reject-5
    // routeId: 7
    let prefix = commentId.split('-')[0]
    let commentShortId = commentId.split('-')[2]
    let tagIdToDel = prefix + '-comment-' + commentShortId

    let targetElement = document.getElementById(tagIdToDel)

    let clone = targetElement.parentElement.cloneNode(true)
    targetElement.parentElement.remove()

    if (prefix === 'new') {
        let parent = document.getElementById('old-collapse-' + routeId)

        let lastElement = parent.lastElementChild
        parent.insertBefore(clone, lastElement)

        clone.firstElementChild.setAttribute('id', 'old-comment-' + commentShortId)
        let btnSwitch = document.getElementById('new-approve-' + commentShortId)
        btnSwitch.nextElementSibling.setAttribute('id', 'old-delete-' + commentShortId)

        btnSwitch.setAttribute('id', 'old-reject-' + commentShortId)

        let firstChild = btnSwitch.firstElementChild
        firstChild.innerText = 'Reject'
        firstChild.classList.remove('bg-success')
        firstChild.classList.add('bg-warning')

        adjustCounters('old-curr-route-' + routeId, prefix + '-curr-route-' + routeId,
            'all-' + prefix + '-comments', 'all-old-comments')
    }

    if (prefix === 'old') {
        let parent = document.getElementById('new-collapse-' + routeId)

        let lastElement = parent.lastElementChild
        parent.insertBefore(clone, lastElement)

        clone.firstElementChild.setAttribute('id', 'new-comment-' + commentShortId)
        let btnSwitch = document.getElementById('old-reject-' + commentShortId)
        btnSwitch.nextElementSibling.setAttribute('id', 'new-delete-' + commentShortId)

        btnSwitch.setAttribute('id', 'new-approve-' + commentShortId)

        let firstChild = btnSwitch.firstElementChild
        firstChild.innerText = 'Approve'
        firstChild.classList.remove('bg-warning')
        firstChild.classList.add('bg-success')

        adjustCounters('new-curr-route-' + routeId, prefix + '-curr-route-' + routeId,
            'all-' + prefix + '-comments', 'all-new-comments')
    }

    return tagIdToDel
}

function makeDeleteRequest(tagClassToDel) {

    let commentIdToDel = tagClassToDel.split('-')[2]

    fetch(`${backendLocation}/superuser/comments/${commentIdToDel}`, {
        method: 'DELETE', headers: {

            'Content-type': 'application/json',
            'Accept': 'application/json',
            [csrfTokenName]: csrfTokenValue
        }
    }).then(res => {
    })
}

function makeDeleteAllRequest(commentFullId) {
    let s = commentFullId.split('-')
    let prefix = s[0]
    let routeId = s[3]

    let endpoint = `${backendLocation}/superuser/comments/route/${routeId}?approved=`
    if (prefix === 'new') {
        endpoint += 'false'
    } else {
        endpoint += 'true'
    }

    fetch(endpoint, {
        method: 'DELETE', headers: {

            'Content-type': 'application/json',
            'Accept': 'application/json',
            [csrfTokenName]: csrfTokenValue
        }
    }).then(res => res.json())
        .then(commentIds => {
            for (let i in commentIds) {
                deleteCommentByIdJS(prefix + '--' + commentIds[i] + '-' + routeId)
            }
        })
}

function makeApproveRejectAllRequest(commentFullId) {
    let s = commentFullId.split('-')
    let prefix = s[0]
    let middle = 'reject'
    if (prefix === 'new') {
        middle = 'approve'
    }
    let routeId = s[3]

    let endpoint = `${backendLocation}/superuser/comments/route/${routeId}?approved=`
    if (prefix === 'new') {
        endpoint += 'false'
    } else {
        endpoint += 'true'
    }

    fetch(endpoint, {
        method: 'PATCH', headers: {

            'Content-type': 'application/json',
            'Accept': 'application/json',
            [csrfTokenName]: csrfTokenValue
        }
    }).then(res => res.json())
        .then(commentIds => {

            for (let i in commentIds) {
                deleteAndRemoveCommentByIdJS(prefix + '-' + middle + '-' + commentIds[i], routeId)
            }
        })
}

function makeToggleApproveRejectRequest(tagClassToDel) {

    let commentIdToApprove = tagClassToDel.split('-')[2]

    fetch(`${backendLocation}/superuser/comments/${commentIdToApprove}`, {
        method: 'PATCH', headers: {

            'Content-type': 'application/json',
            'Accept': 'application/json',
            [csrfTokenName]: csrfTokenValue
        }
    }).then(res => {
    })
}

function adjustCounters(toIncrementId, toDecrementId, toDecrementHeaderId, toIncrementHeaderId) {

    adjustCountersDecrement(toDecrementId, toDecrementHeaderId)
    adjustCountersIncrement(toIncrementId, toIncrementHeaderId)
}

function adjustCountersDecrement(toDecrementId, toDecrementHeaderId) {
    // adjust routes
    let toDecrement = document.getElementById(toDecrementId)
    let decrementText = toDecrement.innerText
    toDecrement.innerText = changeNumber(decrementText, 1, false, false)

    // adjust headers
    let headerToDecr = document.getElementById(toDecrementHeaderId)
    let headerToDecrementText = headerToDecr.innerText
    headerToDecr.innerText = changeNumber(headerToDecrementText, 1, false, false)
}

function adjustCountersIncrement(toIncrementId, toIncrementHeaderId) {
    // adjust routes
    let toIncrement = document.getElementById(toIncrementId)
    let incrementText = toIncrement.innerText
    toIncrement.innerText = changeNumber(incrementText, 1, true, false)

    // adjust headers
    let headerToIncrement = document.getElementById(toIncrementHeaderId)
    let headerToIncrementText = headerToIncrement.innerText
    headerToIncrement.innerText = changeNumber(headerToIncrementText, 1, true, false)
}

function changeNumber(targetText, withNum, incrementFlag, makeZeroFlag) {

    let extractedNumber = targetText.replace(/.*\[(\d+)\]/g, "$1")

    let newNumber = extractedNumber.replace(/(\d+)+/g, function (match, number) {
        if (makeZeroFlag) {
            return 0
        } else {
            if (incrementFlag) {
                return parseInt(number) + parseInt(withNum)
            } else {
                return parseInt(number) - parseInt(withNum)
            }
        }
    })

    return targetText.replace(/\d+/g, newNumber)
}

function toggleArrowComments(arg) {
    let s = arg.split('-')
    let prefix = s[1]
    let routeId = s[2]

    let allChevron = document.getElementsByClassName('chevron-' + prefix)
    let allExpand = document.getElementsByClassName('expand-' + prefix)

    let currChevron = document.getElementById('chevron-arrow-' + prefix + '-' + routeId)
    let currExpand = document.getElementById('expand-arrow-' + prefix + '-' + routeId)

    let currChevronContains = currChevron.classList.contains('d-none')

    for (let i = 0; i < allChevron.length; i++) {
        allChevron[i].classList.remove('d-none')
        allExpand[i].classList.add('d-none')
    }

    if (currChevronContains) {
        currChevron.classList.remove('d-none')
        currExpand.classList.add('d-none')
    } else {
        currChevron.classList.add('d-none')
        currExpand.classList.remove('d-none')
    }
}

function toggleArrowUsers(arg) {
    let s = arg.split('-')
    let userType = s[1] // user type

    let allChevron = document.getElementsByClassName('chevron')
    let allExpand = document.getElementsByClassName('expand')

    let currChevron = document.getElementById('chevron-arrow-' + userType)
    let currExpand = document.getElementById('expand-arrow-' + userType)

    let currChevronCont = currChevron.classList.contains('d-none')

    for (let i = 0; i < allChevron.length; i++) {
        allChevron[i].classList.remove('d-none')
        allExpand[i].classList.add('d-none')
    }

    if (currChevronCont) {
        currChevron.classList.remove('d-none')
        currExpand.classList.add('d-none')
    } else {
        currChevron.classList.add('d-none')
        currExpand.classList.remove('d-none')
    }
}

function toggleUserPerm(userId, from, to) {

    fetch(`${backendLocation}/superuser/permissions/${userId}?` + new URLSearchParams({
        from: from,
        to: to,
    }), {
        method: 'PATCH', headers: {
            'Content-type': 'application/json',
            'Accept': 'application/json',
            [csrfTokenName]: csrfTokenValue
        }
    }).then(res => {
    })
}

// Role → button config: id-suffix, CSS colour class, icon class, label
const ROLE_BTN = {
    admin:     { cls: 'perm-btn--to-admin',     icon: 'fa-user-shield', label: 'Admin' },
    moderator: { cls: 'perm-btn--to-moderator', icon: 'fa-user-tie',   label: 'Moderator' },
    regular:   { cls: 'perm-btn--to-regular',   icon: 'fa-user',       label: 'Regular' },
}

function _setBtn(el, toRole) {
    let cfg = ROLE_BTN[toRole]
    // remove all colour variants, set the right one
    el.classList.remove('perm-btn--to-admin', 'perm-btn--to-moderator', 'perm-btn--to-regular')
    el.classList.add(cfg.cls)
    el.innerHTML = `<i class="fas ${cfg.icon}"></i> ${cfg.label}`
}

function deleteAndToggleUserPermByIdJS(userId, from, to) {

    let target = document.getElementById('user-' + userId)
    let clone = target.cloneNode(true)
    target.remove()

    // insert after last user row in the target section (before filter/header divs)
    let parent = document.getElementById('collapse-' + to)
    // find the last .perm-users-table__row inside the collapse, or fall back to appending
    let rows = parent.querySelectorAll('.perm-users-table__row')
    if (rows.length > 0) {
        rows[rows.length - 1].after(clone)
    } else {
        parent.appendChild(clone)
    }

    // update class on the row itself so filter still works
    clone.classList.remove('user-body-' + from)
    clone.classList.add('user-body-' + to)

    // The 6 possible transitions: rewire the two action buttons
    // Each button id is: userId-currentRole-targetRole
    if (from === 'admin') {
        let makeModerator = document.getElementById(userId + '-admin-moderator')
        let makeRegular   = document.getElementById(userId + '-admin-regular')
        if (to === 'moderator') {
            makeModerator.setAttribute('id', userId + '-moderator-admin')
            _setBtn(makeModerator, 'admin')
            makeRegular.setAttribute('id', userId + '-moderator-regular')
        } else if (to === 'regular') {
            makeModerator.setAttribute('id', userId + '-regular-moderator')
            _setBtn(makeModerator, 'moderator')
            makeRegular.setAttribute('id', userId + '-regular-admin')
            _setBtn(makeRegular, 'admin')
        }
    } else if (from === 'moderator') {
        let makeAdmin   = document.getElementById(userId + '-moderator-admin')
        let makeRegular = document.getElementById(userId + '-moderator-regular')
        if (to === 'admin') {
            makeAdmin.setAttribute('id', userId + '-admin-moderator')
            _setBtn(makeAdmin, 'moderator')
            makeRegular.setAttribute('id', userId + '-admin-regular')
        } else if (to === 'regular') {
            makeAdmin.setAttribute('id', userId + '-regular-moderator')
            _setBtn(makeAdmin, 'moderator')
            makeRegular.setAttribute('id', userId + '-regular-admin')
            _setBtn(makeRegular, 'admin')
        }
    } else if (from === 'regular') {
        let makeModerator = document.getElementById(userId + '-regular-moderator')
        let makeAdmin     = document.getElementById(userId + '-regular-admin')
        if (to === 'moderator') {
            makeModerator.setAttribute('id', userId + '-moderator-admin')
            _setBtn(makeModerator, 'admin')
            makeAdmin.setAttribute('id', userId + '-moderator-regular')
            _setBtn(makeAdmin, 'regular')
        } else if (to === 'admin') {
            makeModerator.setAttribute('id', userId + '-admin-moderator')
            makeAdmin.setAttribute('id', userId + '-admin-regular')
            _setBtn(makeAdmin, 'regular')
        }
    }

    let toDecrement = document.getElementById('cnt-' + from)
    toDecrement.innerText = changeNumber(toDecrement.innerText, 1, false, false)

    let toIncrement = document.getElementById('cnt-' + to)
    toIncrement.innerText = changeNumber(toIncrement.innerText, 1, true, false)
}

function changePerm(wrappedId) {
    let s = wrappedId.split('-')
    let userId = s[0]
    let from = s[1]
    let to = s[2]

    toggleUserPerm(userId, from, to)
    deleteAndToggleUserPermByIdJS(userId, from, to)
}

function filterByInfix(userRole) {
    let users = document.querySelectorAll('.user-body-' + userRole);
    let search = document.querySelector('#search-' + userRole).value.toLowerCase();

    for (let i = 0; i < users.length; i++) {
        if (search === '') {
            users[i].classList.remove('d-none');
        } else {
            let username = (users[i].dataset.username || '').toLowerCase();
            if (username.includes(search)) {
                users[i].classList.remove('d-none');
            } else {
                users[i].classList.add('d-none');
            }
        }
    }
}