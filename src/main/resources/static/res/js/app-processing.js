let countDownToProcessingFalse;
let countDownToProcessingEndFalse;

function startProcessingAnimation(){
    clearTimeout(countDownToProcessingFalse);
    clearTimeout(countDownToProcessingEndFalse);
    app.processingEnd = false;
    app.processing = true;
}

function stopProcessingAnimation(){
    app.processingEnd = true;
    countDownToProcessingFalse = setTimeout(function () {app.processing = false}, 500);
    countDownToProcessingEndFalse = setTimeout(function () {app.processingEnd = false}, 2000);
}