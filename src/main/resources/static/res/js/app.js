Vue.config.devtools = config.vueDevToolsEnabled;

const gameState = {
    success: true,
    message: 'success',
    game: null,
    players: [],
    cards: [],
    myIndex: -1
};

const data = {
    winner: -1,
    showInvitationQr: false,
    showSwitchOutQr: false,
    showSwitchInQr: false,
    qr: '',
    gameUuid: '',
    playerUuid: '',
    invitation: false,
    currentView: '',
    previousView: '',
    name: '',
    botName: '',
    gameState: gameState,
    timeLeftPercent: 100,
    btnCreateGameDisabled: false,
    btnJoinGameDisabled: false,
    processing: false,
    processingEnd: false,
    enableTokenizedGameCreate: false,
    token: 'empty',
    tokenLockedGameCreate: false,
    hostname: config.siteHostname,
    protocol: config.siteProtocol,
    hasCamera: false,
    pendingRemoveAfterSwitch: false,
    pendingSwitch: false,
    pendingPlayerIndex: -1,
    pushUuid: '',
    dialog: null,
    refreshPageOnDialogClose: false,
    stopPartyRequested: false,
    botifyPlayerPending: false,
    playerToBotify: null,
    message: '',
    readMessages: 0,
    gameLoadedWithPlayer: false,
    darkMode: false,
    actionLock: false,
    putCardIndex: -1,
    drawnCards: 1,
    drawReason: 'regular',
    finished: false,
    playerWasInUnoState: false
};

const methods = {
    createGame: function(){createGame()},
    joinGame: function () {joinGame()},
    leaveGame: function () {leaveGame()},
    addBot: function () {addBot()},
    removeBot: function (player) {removeBot(player)},
    removeBotInGame: function (player){removeBotInGame(player)},
    reset: function (){reset()},
    loadGame: function (){loadGame()},
    loadGameWithoutPlayer: function (){loadGameWithoutPlayer()},
    startGame: function (){startGame()},
    getPlayerName: function(name, index){ return getPlayerName(name, index)},
    copyLink: function(){copyLink()},
    shareLink: function(){shareLink()},
    showInvitationQrCode: function () {showInvitationQrCode()},
    getCardImage: function(card) { return getCardImage(card) },
    put: function(card, index) { put(card, index) },
    putDrawn: function() { putDrawn() },
    draw: function() { draw() },
    keep: function() { keep() },
    selectColor: function(color) { selectColor(color) },
    sayUno: function() { sayUno() },
    isPutAllowed: function(card, index) { return isPutAllowed(card, index) },
    isDrawAllowed: function() { return isDrawAllowed() },
    isSayUnoAllowed: function() { return isSayUnoAllowed() },
    isMyTurn: function() { return isMyTurn() },
    isPlayersTurn: function(index) { return isPlayersTurn(index) },
    isPlayerInRow: function(index, row) { return isPlayerInRow(index, row) },
    scanQr: function() { scanQr() },
    prepareSwitchDevice: function (){ prepareSwitchDevice() },
    abortSwitchDevice: function (){ abortSwitchDevice() },
    prepareSwitchOut: function (){ prepareSwitchOut() },
    abortSwitchOut: function (){ abortSwitchOut() },
    prepareSwitchIn: function (){ prepareSwitchIn() },
    abortSwitchIn: function (){ abortSwitchIn() },
    copySwitchLink: function(){copySwitchLink()},
    confirmLeaveRunningGame: function(){confirmLeaveRunningGame();},
    confirmRequestStopParty: function(){confirmRequestStopParty()},
    revokeRequestStopParty: function(){revokeRequestStopParty()},
    getPlayerClasses: function (player){ return getPlayerClasses(player)},
    confirmRequestBotifyPlayer: function(player){confirmRequestBotifyPlayer(player)},
    showChat: function(){showChat()},
    hideChat: function(){hideChat()},
    sendMessage: function(){sendMessage()},
    getMessageDirection: function(playerPublicUuid){ return getMessageDirection(playerPublicUuid)},
    getSenderName: function(playerPublicUuid){ return getSenderName(playerPublicUuid)},
    getMessageClock: function(time) {return getMessageClock(time)},
    getMessageLines: function (message) {return getMessageLines(message)},
    hideDialog : function(){hideDialog()},
    toggleTheme: function(){toggleTheme()},
    ownCardsBeforeEnter: function(element){ownCardsBeforeEnter(element)},
    ownCardsEnter: function(element, done){ownCardsEnter(element, done)},
    ownCardsLeave: function(element, done){ownCardsLeave(element, done)},
    topCardBeforeEnter: function(element){topCardBeforeEnter(element)},
    topCardEnter: function(element, done){topCardEnter(element, done)},
    cardCountBeforeEnter: function(element){cardCountBeforeEnter(element)},
    cardCountEnter: function(element, done){cardCountEnter(element, done)},
    cardCountLeave: function (element, done){cardCountLeave(element, done)},
    playerInSingleList: function (player){return playerInSingleList(player)}
};

const computed = {
    getPlayersListInOrder: function(){return getPlayersListInOrder()},
    topCardInSingleList: function (){return topCardInSingleList()}
}

app = new Vue({
    el: '#app',
    data: data,
    methods: methods,
    computed: computed
});

function hasTouch() {
    return 'ontouchstart' in document.documentElement
        || navigator.maxTouchPoints > 0
        || navigator.msMaxTouchPoints > 0;
}

if(!hasTouch()){
    document.querySelector('body').setAttribute('class', 'hover');
    document.querySelector('html').setAttribute('class', 'custom-scrollbars');
}

function showErrorDialog(text){
    app.dialog = {
        classes: 'error',
        confirm: false,
        timedCancel: false,
        secondsLeft: 0,
        confirmCallback: null,
        cancelCallback: null,
        text: text
    }
}

function showInformationDialog(text){
    app.dialog = {
        classes: '',
        confirm: false,
        timedCancel: false,
        secondsLeft: 0,
        confirmCallback: null,
        cancelCallback: null,
        text: text
    }
}

function showConfirmationDialog(text, confirmCallback){
    app.dialog = {
        classes: '',
        confirm: true,
        timedCancel: false,
        secondsLeft: 0,
        confirmCallback: confirmCallback,
        cancelCallback: null,
        text: text
    }
}
let rTCDSL;

function showTimedCancelDialog(text, cancelCallback, time){
    app.dialog = {
        classes: '',
        confirm: false,
        timedCancel: true,
        secondsLeft: time,
        confirmCallback: null,
        cancelCallback: cancelCallback,
        text: text
    }
    rTCDSL = setInterval('reduceTimedConfirmationDialogSecondsLeft()', 1000);
}

function reduceTimedConfirmationDialogSecondsLeft(){
    if(--app.dialog.secondsLeft <= 0){
        clearInterval(rTCDSL);
    }
}

function hideDialog(){
    app.dialog = null;
    if(app.refreshPageOnDialogClose){
        self.location.replace('/');
    }
}

function copy(text){
    const textarea = document.createElement('textarea');
    document.querySelector('body').appendChild(textarea);
    textarea.value = text;
    textarea.select();
    textarea.setSelectionRange(0, 500); /*For mobile devices*/
    document.execCommand("copy");
    document.querySelector('body').removeChild(textarea);
}

function sleep(){
    return new Promise(resolve => setTimeout(resolve, 50));
}
