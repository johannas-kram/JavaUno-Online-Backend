let aC = null;
let start;

function animateCountdown(){
    const now = new Date().getTime();
    let diff = now-start;

    if(diff > 3000){
        diff = 3000;
    }

    app.timeLeftPercent = 100 - 100/3000*diff;

    if(app.timeLeftPercent <= 0){
        next();
    }
}

function startCountdownAnimation() {
    start = new Date().getTime();
    aC = setInterval('animateCountdown()', 10);
}


function stopCountdownAnimation(){
    if(aC !== null){
        clearInterval(aC);
        aC = null;
    }
    app.timeLeftPercent = 100;
}
