
function setGame(data){
    app.gameUuid = data.gameUuid;
    localStorage.setItem('gameUuid', app.gameUuid);
    self.location.replace('/');
}

function setGameState(data){
    setTimeout('setReadMessages()', 200);
    if(app.finished){
        setTimeout(function(){
            app.finished = false;
            app.currentView = 'set_players';
            app.winner = data.game.currentPlayerIndex;
            setGameState(data);
        }, 1200);
        return;
    }
    app.gameState = data;
    app.stopPartyRequested = app.gameState.players[app.gameState.myIndex].stopPartyRequested;
    app.currentView = data.game.gameLifecycle.toLowerCase();
    joinGameRunning = false;
    if(app.gameState.game.turnState === 'FINAL_COUNTDOWN' && app.gameState.game.gameLifecycle === 'RUNNING' && aC === null){
        if(localStorage.getItem('sayUno') !== null && localStorage.getItem('sayUno') === '1'){
            sayUno();
        }
        setTimeout('startCountdownAnimation()', 200);
    }
    if(isMyTurn() && app.gameState.game.turnState === 'DRAW_PENALTIES' && app.gameState.players[app.gameState.game.currentPlayerIndex].drawPenalties === 2){
        showToast(app.gameState.ownCards.length > 1 ? 'Du hast zu früh „Uno“ gesagt.' : 'Du hast vergessen „Uno“ zu sagen.');
    }
    app.gameLoadedWithPlayer = true;
}

function setGameStateWithoutPlayer(data){
    app.gameState = data;
    app.currentView = 'join';
}

function loadGame(){
    const path = '/gameState/get/' + app.gameUuid + '/' + app.playerUuid;
    doGetRequest(path, setGameState);
}

function loadGameWithoutPlayer(){
    const path = '/gameState/get/' + app.gameUuid;
    doGetRequest(path, setGameStateWithoutPlayer);
}

function createGame() {
    if(app.tokenLockedGameCreate){
        return;
    }
    app.btnCreateGameDisabled = true;
    doPostRequest('/game/create/'+app.token, {}, setGame);
}

function startGame(){
    app.timeLeftPercent = 100;
    localStorage.setItem('gameUuid', app.gameUuid);
    localStorage.setItem('playerUuid', app.playerUuid);
    doPostRequest('/game/start/' + app.gameUuid, {}, loadGame);
}

function reset(){
    localStorage.removeItem('gameUuid');
    localStorage.removeItem('playerUuid');
    localStorage.removeItem('invitation');
    localStorage.removeItem('sayUno');
    localStorage.removeItem('readMessages');
    self.location.replace('/');
}

function handleInvitation(){
    if(location.hash.startsWith("#game:")){
        const gameUuid = location.hash.replace(/^#game:/, '');
        localStorage.setItem('gameUuid', gameUuid);
        localStorage.setItem('invitation', '1');
        location.replace('/');
    }
}

function handleTokenLink(){
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    if(token === null){
        return false;
    }
    if(isTokenPatternValid(token)){
        localStorage.setItem('token', token);
    }
    self.location.replace('/');
    return true;
}

function setHandleTokenAndEnableView(data){
    setHandleToken(data)
    document.getElementById('javaUno').style.display = 'block';
}

function setHandleToken(data){
    stopProcessingAnimation();
    const enabled = data.message === 'on';
    if(!enabled){
        return;
    }
    app.enableTokenizedGameCreate = true;
    const token = localStorage.getItem('token');
    app.tokenValidPattern = isTokenPatternValid(token);
    if(!app.tokenValidPattern){
        localStorage.removeItem('token');
        app.tokenLockedGameCreate = true;
        return;
    }
    app.token = token;
}

function handleToken(){
    doGetRequest('/game/tokenized-game-create-enabled', setHandleTokenAndEnableView)
}

function isTokenPatternValid(token){
    if(token === null){
        return false;
    }
    const tokenRegex = "^([a-zA-Z0-9_-]{11})\\.([a-zA-Z0-9_-]{11})$";
    return new RegExp(tokenRegex).test(token);
}

function setRequestStopParty(){
    stopProcessingAnimation();
    if(app.currentView === 'running'){
        app.stopPartyRequested = true;
    }
}

function confirmRequestStopParty(){
    showConfirmationDialog('Bist du sicher, dass das laufende Spiel beendet werden soll? ' +
        'Die anderen müssen diesen Wunsch ebenfalls äußern.', requestStopParty, null);
}

function requestStopParty(){
    app.dialog = null;
    let path = '/player/request-stop-party/' + app.gameUuid + '/' + app.playerUuid;
    doPostRequest(path, {}, setRequestStopParty);
}

function setRevokeRequestStopParty(){
    stopProcessingAnimation();
    app.stopPartyRequested = false;
}

function revokeRequestStopParty(){
    let path = '/player/revoke-request-stop-party/' + app.gameUuid + '/' + app.playerUuid;
    doPostRequest(path, {}, setRevokeRequestStopParty);
}

function getPlayersListInOrder(){
    app.gameState.players.forEach(e => e.index = app.gameState.players.indexOf(e));
    if(app.gameState.game.reversed){
        return app.gameState.players.slice().reverse();
    }
    return app.gameState.players;
}

function topCardInSingleList(){
    const topCard = app.gameState.game.topCard;
    topCard.desiredColor = app.gameState.game.desiredColor !== null ? app.gameState.game.desiredColor.toLowerCase() : '';
    return [topCard];
}

function playerInSingleList(player){
    return [player];
}

function init(){
    const invitation = localStorage.getItem('invitation');
    if(invitation != null && invitation === '1'){
        app.invitation = true;
    }
    const gameUuid = localStorage.getItem('gameUuid');
    if(gameUuid == null){
        app.currentView = 'start';
        handleToken();
        return false;
    }
    getReadMessages();
    app.gameUuid = gameUuid;
    connectPush(gameUuid, null, null);
    const playerUuid = localStorage.getItem('playerUuid');
    if(playerUuid != null){
        app.playerUuid = playerUuid;
        app.loadGame();
    } else {
        app.loadGameWithoutPlayer();
    }
    return true;
}

window.addEventListener("load", function() {
    loadTheme();
    if(handleSwitchDevice()){
        return;
    }
    if(handleTokenLink()){
        return;
    }
    handleInvitation();
    if(init()){
        document.getElementById('javaUno').style.display = 'block';
    }
});

document.addEventListener('click', function(e) {
    if(document.activeElement.toString() !== '[object HTMLInputElement]' && document.activeElement.toString() !== '[object HTMLTextAreaElement]'){
        document.activeElement.blur();
        setTimeout('document.activeElement.blur()', 100);
    }
});

function loadTheme(){
    let theme = localStorage.getItem('theme');
    if(theme !== 'dark'){
        theme = 'light';
    } else {
        app.darkMode = true;
    }
    setTheme(theme);
}

function toggleTheme(){
    app.darkMode = !app.darkMode;
    const theme = app.darkMode ? 'dark' : 'light';
    setTheme(theme);
}

function setTheme(theme){
    document.querySelector('html').setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
}
