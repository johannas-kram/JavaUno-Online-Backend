let stompClient = null;
let wasConnected = false;
let cC;

function checkConnected(){
    const connected = stompClient !== null && stompClient.connected !== undefined && stompClient.connected;
    if(connected){
        wasConnected = connected;
    } else {
        if(wasConnected){
            clearInterval(cC);
            self.location.reload();
        }
    }
}

cC = setInterval('checkConnected()', 1000);

function connectPush(pushUuid, callback, callBackArgument) {
    if(stompClient != null){
        console.log('refuse to connect push. already connected.');
        return;
    }
    const socket = new SockJS('/api/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/api/push/'+pushUuid, async function (message) {
            await handleMessage(message);
        });
        if(callback != null){
            callback(callBackArgument);
        }
    });
}

function disconnectPush(){
    clearInterval(cC);
    stompClient.disconnect();
    stompClient = null;
}

async function handleMessage(message){
    let waitFor = 0;
    while(joinGameRunning){
        waitFor++;
        await sleep();
    }
    console.log('Wait for joinGame to be finished for ' + waitFor + ' times');
    doPushActions(message);
}

function doPushActions(message){
    const messageType = message.body.replace(/:.*$/ms, '');
    pushActions[messageType](message);
}

const doPushActionStartedGame = function(message){
    const index = parseInt(message.body.replace(/^started-game:(\d)$/, '$1'));
    app.gameState.game.gameLifecycle = 'RUNNING';
    app.gameState.game.currentPlayerIndex = index;
    app.timeLeftPercent = 100;
    app.winner = -1;
    showTurnToast(app.gameState.game.currentPlayerIndex);
    showToast('Es geht los');
    updateView();
};

const doPushActionAddedPlayer = function(message){
    app.winner = -1;
    if(message.body.startsWith('added-player')){
        const index = parseInt(message.body.replace(/^added-player:(\d).*$/, '$1'));
        if(isNotMe(index)){
            let name = message.body.replace(/^added-player:\d:(.*)$/, '$1');
            name = getPlayerName(name, index);
            showToast(name + ' macht mit');
        }
    }
    updateView();
};

const  doPushActionRemovedPlayer = function(message){
    app.winner = -1;
    if(message.body.startsWith('removed-player')){
        const index = parseInt(message.body.replace(/removed-player:/, ''));
        if(isNotMe(index)){
            let name = app.gameState.players[index].name;
            name = getPlayerName(name, index).replace(/^Spieler\s+(\d+)$/, 'Der ehemalige Spieler $1');
            showToast(name + ' ist gegangen oder wurde entfernt');
        }
        if(isNotMe(index)){
            app.gameState.players.splice(index, 1);
        } else {
            reset();
        }
    }
};

const doPushActionBotifiedPlayer = function(message){
    const index = parseInt(message.body.replace(/^botified-player:(.*?)$/, '$1'));
    app.botifyPlayerPending = false;
    if(isMe(index)){
        reset();
    } else {
        let name = app.gameState.players[index].name;
        name = name !== '' ? name : `Spieler ${index+1}`;
        app.gameState.players[index].bot = true;
        app.gameState.players[index].botifyPending = false;
        showInformationDialog(`${name} hat das Spiel verlassen und wurde zu einem Bot.`);
    }
};

const doPushActionPutCard = function(message){
    const topCardJson = message.body.replace(/^put-card:(\{.*\}).*$/, '$1');
    const topCard = JSON.parse(topCardJson);
    if(app.gameState.game.gameLifecycle === 'RUNNING') {
        const cardCount = app.gameState.players[app.gameState.game.currentPlayerIndex].cardCount - 1;
        if(!topCard.jokerCard && cardCount > 0){
            startCountdown();
        }
    }
    if(app.currentView === 'running' && !isMyTurn()){
        modificationTransitionWrapper(updateView, topCard);
    }
};

const doPushActionDrawnCard = function(message){
    app.drawnCards = 1;
    if(app.currentView === 'running') {
        if(message.body.endsWith(':countdown')){
            startCountdown();
        }
        if(!isMyTurn()){
            modificationTransitionWrapper(updateView, null);
        }
    }
};

const doPushActionDrawnCards = function(message){
    app.drawnCards = parseInt(message.body.replace(/^drawn-cards:([^:]*?):([^:]*?)$/, '$1'));
    app.drawReason = message.body.replace(/^drawn-cards:([^:]*?):([^:]*?)$/, '$2');
    if(app.currentView === 'running') {
        if(!isMyTurn()){
            modificationTransitionWrapper(updateView, null);
        }
    }
};

const doPushActionKeptCard = function(){
    if(app.currentView === 'running') {
        startCountdown();
    }
};

const doPushActionSelectedColor = function(message){
    if(app.currentView === 'running') {
        if(!isMyTurn()){
            app.gameState.game.desiredColor = message.body.replace(/selected-color:/, '');
        }
        startCountdown();
    }
};

const doPushActionSaidUno = function(message){
    const cardCount = parseInt(message.body.replace(/^said-uno:(.*?)$/, '$1'));
    console.log('cardCount: ' + cardCount);
    console.log('message: ' + message.body);
    if(app.currentView === 'running' || app.previousView === 'running'){
        const index = app.gameState.game.currentPlayerIndex;
        if(cardCount === 1){
            showLargeToast('Uno');
        } else {
            showToast('Uno (Lüge)');
        }
        app.gameState.players[index].unoSaid = true;
    }
};

const doPushActionNextTurn = function(message){
    if(app.currentView === 'running' || app.previousView === 'running'){
        app.gameState.game.turnState = '';
        alreadySaidUno = false;
        stopCountdownAnimation();
        const index = parseInt(message.body.replace(/next-turn:/, ''));
        app.gameState.game.currentPlayerIndex = index;
        showTurnToast(index);
    }
    updateView();
};

const  doPushActionFinishedGame = function(message) {
    clearTimeout(aC);
    aC = null;
    const party = parseInt(message.body.replace(/finished-game:/, ''));
    if((app.currentView === 'running' || app.currentView === 'set_players' ) && party === app.gameState.game.party){
        app.finished = true;
        fixRemainingFloatingClonesAfterStopParty();
        updateView();
    }
    app.stopPartyRequested = false;

};

const  doPushActionEnd = function() {
    showToast('Spiel beendet. Danke für\'s Spielen');
    reset();
};

const  doPushActionSwitchIn = function(message) {
    handlePushSwitchIn(message);
};

const  doPushActionSwitchFinished = function(message) {
    handlePushSwitchFinished(message);
};

const doPushActionRequestStopParty = function(message){
    const index = parseInt(message.body.replace(/^request-stop-party:(.*?)$/, '$1'));
    app.gameState.players[index].stopPartyRequested = true;
    let name = app.gameState.players[index].name;
    name = getPlayerName(name, index);
    if(isNotMe(index)){
        showToast(`${name} hat angefragt, diese Runde zu beenden.`);
    }
};

const doPushActionRevokeRequestStopParty = function(message){
    const index = parseInt(message.body.replace(/^revoke-request-stop-party:(.*?)$/, '$1'));
    app.gameState.players[index].stopPartyRequested = false;
    let name = app.gameState.players[index].name;
    name = getPlayerName(name, index);
    if(isNotMe(index)) {
        showToast(`${name} hat die Anfrage zurück genommen, diese Runde zu beenden.`);
    }
};

const doPushActionStopParty = function(message){
    clearTimeout(aC);
    aC = null;
    const party = parseInt(message.body.replace(/stop-party:/, ''));
    if(app.currentView === 'running' && party === app.gameState.game.party){
        app.currentView = 'set-players';
        fixRemainingFloatingClonesAfterStopParty();
        app.previousView = '';
        updateView();
    }
    if(app.previousView === 'running' && party === app.gameState.game.party){
        app.previousView = 'set-players';
    }
    app.stopPartyRequested = false;
};

const doPushActionRequestBotifyPlayer = function (message){
    const uuid = message.body.replace(/request-botify-player:/, '');
    if(app.playerUuid === uuid){
        showTimedCancelDialog(`Jemand möchte dich aus dem Spiel entfernen. 
        Ist das für dich ok? Du hast 10 Sekunden Zeit, diesen Vorgang abzubrechen.`, cancelBotify, 10);
    }
    updateView();
};

const doPushActionCancelBotifyPlayer = function (message){
    const uuid = message.body.replace(/cancel-botify-player:/, '');
    const pendingUuid = app.playerToBotify != null ? app.playerToBotify.publicUuid : '';
    if(pendingUuid === uuid){
        app.botifyPlayerPending = false;
        app.playerToBotify = null;
        showInformationDialog('Der Spieler hat den Prozess abgebrochen.');
    }
    updateView();
};

const doPushActionChatMessage = function (message){
    handlePushActionChatMessage(message);
};

function showTurnToast(index){
    let name = app.gameState.players[index].name;
    name = getPlayerName(name, index);
    if(isMyTurn()){
        showToast('Du bist dran, ' + name);
    } else {
        showToast(name + ' ist dran');
    }
}

function startCountdown(){
    if(app.currentView === 'running' && aC === null){
        app.gameState.game.turnState = 'FINAL_COUNTDOWN';
        startCountdownAnimation();
    }
}

function updateView(){
    if(app.currentView === 'chat' || app.currentView === 'switch-device'){
        stopProcessingAnimation();
        return;
    }
    if(app.playerUuid !== ''){
        loadGame();
    } else {
        loadGameWithoutPlayer();
    }
}

const pushActions = {
    'started-game': doPushActionStartedGame,
    'added-player': doPushActionAddedPlayer,
    'removed-player': doPushActionRemovedPlayer,
    'botified-player': doPushActionBotifiedPlayer,
    'put-card': doPushActionPutCard,
    'drawn-card': doPushActionDrawnCard,
    'drawn-cards': doPushActionDrawnCards,
    'kept-card': doPushActionKeptCard,
    'selected-color': doPushActionSelectedColor,
    'said-uno': doPushActionSaidUno,
    'next-turn': doPushActionNextTurn,
    'finished-game': doPushActionFinishedGame,
    'end': doPushActionEnd,
    'switch-in': doPushActionSwitchIn,
    'switch-finished': doPushActionSwitchFinished,
    'request-stop-party': doPushActionRequestStopParty,
    'revoke-request-stop-party': doPushActionRevokeRequestStopParty,
    'stop-party': doPushActionStopParty,
    'request-botify-player': doPushActionRequestBotifyPlayer,
    'cancel-botify-player': doPushActionCancelBotifyPlayer,
    'chat-message': doPushActionChatMessage
};
