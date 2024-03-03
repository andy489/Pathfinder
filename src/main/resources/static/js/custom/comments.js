const routeId = document.getElementById('curr-route-id').value
const commentForm = document.getElementById('comment-form')
commentForm.addEventListener("submit", handleFormSubmission)

const csrfHeaderName = document.head.querySelector('[name=_csrf_header]').content
const csrfHeaderValue = document.head.querySelector('[name=_csrf]').content

const commentContainer = document.getElementById('comment-control-section')

const backendLocation = "http://localhost:8080";

async function handleFormSubmission(event) {
    event.preventDefault()
    const routeId = document.getElementById('route-id').value
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
    }).then(res => res.json())
        .then(data => {
            document.getElementById('comment').value = ""
            commentContainer.innerHTML += commentAsHtml(data, true)
        })
}

fetch(`${backendLocation}/api/${routeId}/comments`, {
    headers: {
        "Accept": "application/json"
    }
}).then(res => res.json())
    .then(data => {
        for (let comment of data) {
            commentContainer.innerHTML += commentAsHtml(comment, false)
        }
    })

function commentAsHtml(comment, approval) {
    let commentHtml = `<div id="comment-${comment.commentId}">\n`
    commentHtml += `<h5>${comment.authorName}</h5>\n`
    commentHtml += `<p class="mb-0">${comment.comment}</p>\n`
    let createdTime = new Date(comment.created).toLocaleString();
    let modifiedTime = new Date(comment.modified).toLocaleString();

    commentHtml += `<span class="small mt-0">Created: ${createdTime}</span>`
    // commentHtml += `,<span class="small">Last modified: ${modifiedTime}\n</span>`

    if(approval){
        commentHtml += `.<span class="small text-danger"> [Waiting for approval from MODERATOR or ADMIN]\n</span>`
    }

    commentHtml += `<p></p>`

    // commentHtml += `<button class="btn btn-danger" onclick="deleteComment(${comment.commentId})">Delete</button>\n`
    commentHtml += `<hr/>`
    commentHtml += "</div>\n"

    return commentHtml
}

