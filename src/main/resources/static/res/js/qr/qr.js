
function genQr(data){
    const typeNumber = 0;
    const errorCorrectionLevel = 'L';
    const qr = qrcode(typeNumber, errorCorrectionLevel);
    qr.addData(data);
    qr.make();
    return qr.createDataURL(8, 32, '');
}

function handleQrScanResult(result){
    if(result.indexOf(app.protocol+'://'+app.hostname+'/') >= 0){
        self.location.href = result;
    } else {
        console.log('skipped redirection to qr code target. target is cross-site');
    }
}

function scanQr(){
    document.getElementById('qrScanBtn').click();
}
