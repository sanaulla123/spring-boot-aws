function show_success(title, message){
    toastr.success(message, title);
}

function show_info(title, message){
    toastr.info(message, title);
}

function show_error(title, message){
    toastr.error(message, title);
}

function show_closable_error(title, message){
    toastr.options = {
        "closeButton": true,
        "progressBar": false
    };
    toastr.error(message, title);
}

function show_warning(title, message){
    toastr.warning(message, title);
}