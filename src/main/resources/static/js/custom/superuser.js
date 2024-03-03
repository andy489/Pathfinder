const backendLocation = "http://localhost:8080"

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

function deleteAndToggleUserPermByIdJS(userId, from, to) {

    let target = document.getElementById('user-' + userId)
    let clone = target.parentElement.cloneNode(true)
    target.parentElement.remove()

    let parent = document.getElementById('collapse-' + to)
    let lastElement = parent.lastElementChild
    lastElement.after(clone)

    if (from === 'admin') {
        let makeModerator = document.getElementById(userId + '-' + from + '-' + 'moderator')
        let makeRegular = document.getElementById(userId + '-' + from + '-' + 'regular')

        if (to === 'moderator') {
            makeModerator.setAttribute('id', userId + '-' + to + '-' + from)
            let firstChildMod = makeModerator.firstElementChild

            firstChildMod.classList.remove('bg-success')
            firstChildMod.classList.add('bg-warning')
            firstChildMod.innerText = 'Make admin'

            makeRegular.setAttribute('id', userId + '-' + to + '-' + 'regular')
        } else if (to === 'regular') {
            makeModerator.setAttribute('id', userId + '-' + to + '-' + 'moderator')
            let firstChildMod = makeModerator.firstElementChild

            firstChildMod.classList.remove('bg-success')
            firstChildMod.classList.add('bg-warning')

            makeRegular.setAttribute('id', userId + '-' + 'regular' + '-' + 'admin')
            let firstChildReg = makeRegular.firstElementChild
            firstChildReg.classList.remove('bg-success')
            firstChildReg.classList.add('bg-danger')
            firstChildReg.innerText = 'Make admin'
        }
    }

    if (from === 'moderator') {
        let makeAdmin = document.getElementById(userId + '-' + from + '-' + 'admin')
        let makeRegular = document.getElementById(userId + '-' + from + '-' + 'regular')

        makeAdmin.setAttribute('id', userId + '-' + to + '-' + from)
        let firstChildAdm = makeAdmin.firstElementChild

        if (to === 'admin') {
            firstChildAdm.classList.remove('bg-warning')
            firstChildAdm.classList.add('bg-success')
            firstChildAdm.innerText = 'Make moderator'

            makeRegular.setAttribute('id', userId + '-' + to + '-' + 'regular')

        } else if (to === 'regular') {
            firstChildAdm.innerText = 'Make moderator'

            makeRegular.setAttribute('id', userId + '-' + to + '-' + 'admin')
            let firstChildReg = makeRegular.firstElementChild
            firstChildReg.classList.remove('bg-success')
            firstChildReg.classList.add('bg-danger')
            firstChildReg.innerText = 'Make admin'
        }
    }

    if (from === 'regular') {
        let makeModerator = document.getElementById(userId + '-' + from + '-' + 'moderator')
        let makeAdmin = document.getElementById(userId + '-' + from + '-' + 'admin')

        if (to === 'moderator') {
            makeModerator.setAttribute('id', userId + '-' + to + '-' + 'admin')
            let firstChildMod = makeModerator.firstElementChild
            firstChildMod.innerText = 'Make admin'
        } else if (to === 'admin') {
            makeModerator.setAttribute('id', userId + '-' + to + '-' + 'moderator')
            let firstChildMod = makeModerator.firstElementChild
            firstChildMod.classList.remove('bg-warning')
            firstChildMod.classList.add('bg-success')
        }

        makeAdmin.setAttribute('id', userId + '-' + to + '-' + 'regular')
        let firstChildAdm = makeAdmin.firstElementChild
        firstChildAdm.classList.remove('bg-danger')
        firstChildAdm.classList.add('bg-success')
        firstChildAdm.innerText = 'Make regular'
    }

    let toDecrement = document.getElementById('cnt-' + from)
    let decrementText = toDecrement.innerText
    toDecrement.innerText = changeNumber(decrementText, 1, false, false)

    let toIncrement = document.getElementById('cnt-' + to)
    let incrementText = toIncrement.innerText
    toIncrement.innerText = changeNumber(incrementText, 1, true, false)
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

    if (search !== '') {
        for (let i = 0; i < users.length; i++) {
            let username = users[i].firstElementChild.firstElementChild.firstElementChild.firstElementChild
                .textContent.toLowerCase();

            if (username.match(search) == null) {
                users[i].classList.add('d-none')
            }
        }
    } else {
        for (let i = 0; i < users.length; i++) {
            users[i].classList.remove('d-none')
        }
    }
}