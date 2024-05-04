import QrScanner from '/res/js/qr/qr-scanner.min.js';

QrScanner.WORKER_PATH = '/res/js/qr/qr-scanner-worker.min.js';

const qrBg = document.getElementById('qrScanBg');
const qrVideo = document.getElementById('qrScanVideo');
const qrStartBtn = document.getElementById('qrScanBtn');
const qrStopBtn = document.getElementById('qrScanStopBtn');

function setResult(result) {
    handleQrScanResult(result);
    qrStop();
}

QrScanner.hasCamera().then(hasCamera => app.hasCamera = hasCamera);

const scanner = new QrScanner(qrVideo, result => setResult(result), error => {
    console.log(error);
});

function qrStart(){
    qrBg.style.display = 'block';
    qrVideo.style.display = 'block';
    qrStopBtn.style.display = 'block';
    scanner.start();
}

function qrStop(){
    qrBg.style.display = 'none';
    qrVideo.style.display = 'none';
    qrStopBtn.style.display = 'none';
    scanner.stop();
}

qrStartBtn.addEventListener('click', function(){
    qrStart();
});

qrStopBtn.addEventListener('click', function(){
    qrStop();
});
