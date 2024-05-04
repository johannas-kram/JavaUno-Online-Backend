const noSuchGameMessage = config.noSuchGameMessage;
const noSuchPlayerMessage = config.noSuchPlayerMessage;
const invalidTokenMessage = features.invalidTokenMessage;
const fileReadErrorMessage = features.fileReadErrorMessage;

let rI;

function handleRequestSuccess(response, callback) {
    if(response.url.indexOf('say-uno') >= 0){
        sayUnoRequestRunning = false;
        localStorage.removeItem('sayUno');
    }
    callback(response.data);
    if(callback === setGameState || callback === setGameStateWithoutPlayer){
        stopProcessingAnimation();
    }
    clearInterval(rI);
}

function handleRequestError(response) {
    if(response.status === 502){
        app.currentView = 'backend-down';
        document.getElementById('javaUno').style.display = 'block';
        rI = setInterval('self.location.reload()', 5000);
    }
    if(response.url.indexOf('say-uno') >= 0){
        setTimeout('sayUno()', 1000)
    } else {
        stopProcessingAnimation();
    }
    if(response.data.message !== undefined){
        console.error("Request-Error: " + response.data.message);
        if(response.data.message === noSuchGameMessage){
            reset();
        }
        if(response.data.message === noSuchPlayerMessage){
            localStorage.removeItem('playerUuid');
            self.location.reload();
        }
        if(response.data.message === invalidTokenMessage){
            localStorage.removeItem('token');
            app.btnCreateGameDisabled = false;
            app.tokenLockedGameCreate = true;
            showErrorDialog('Fehler: Ungültiger Token');
        }
        if(response.data.message === fileReadErrorMessage){
            showErrorDialog('Fehler: Token konnte nicht validiert werden, wegen eines Datei-Lesefehlers im Backend. ' +
                'Versuche es später erneut und/oder teile mir diesen Fehler mit.');
        }
    } else {
        console.error("Request-Error: responseObject: " + JSON.stringify(response));
    }
}

function doGetRequest(path, callback){
    startProcessingAnimation();
    app.$http.get(config.apiBase+path).then(function (response) {
        handleRequestSuccess(response, callback);
    }, handleRequestError);
}

function doPostRequest(path, data, callback){
    startProcessingAnimation();
    app.$http.post(config.apiBase+path, JSON.stringify(data)).then(function (response) {
        handleRequestSuccess(response, callback);
    }, handleRequestError);
}

function doDeleteRequest(path, callback){
    startProcessingAnimation();
    app.$http.delete(config.apiBase+path).then(function (response) {
        handleRequestSuccess(response, callback);
    }, handleRequestError);
}
