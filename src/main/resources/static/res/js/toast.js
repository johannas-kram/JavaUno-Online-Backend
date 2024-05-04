
function showToast(text){
    let toast = new iqwerty.toast.Toast();
    toast = toast.setText(sanitize(text)).setDuration(3000);
    toast.show(false);
}

function showLargeToast(text){
    let toast = new iqwerty.toast.Toast();
    toast = toast.setText(sanitize(text)).setDuration(3000);
    toast.show(true);
}

function sanitize(text){
    return text.replaceAll('<', '&lt;').replaceAll('>', '&gt;');
}
