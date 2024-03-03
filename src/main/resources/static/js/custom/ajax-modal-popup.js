$(document).ready(function () {
    $('#modal-confirm').on('show.bs.modal', function (event) {

        let triggerModalBtn = $(event.relatedTarget)
        let btnFunc = triggerModalBtn.data('func')
        let msgAction = triggerModalBtn.data('msg-action').toUpperCase()
        let routeName = triggerModalBtn.data('route')
        let extractedId = triggerModalBtn.attr('id-transfer')
        let modal = $(this)

        modal.find('#modal-text').text('' +
            'Are you sure you want to '
            + msgAction +
            ' all comments for route '
            + routeName)

        let confirmBtn = modal.find('.modal-confirm-btn')

        confirmBtn.attr('id', extractedId)
        confirmBtn.attr('onclick', btnFunc)
    })
})