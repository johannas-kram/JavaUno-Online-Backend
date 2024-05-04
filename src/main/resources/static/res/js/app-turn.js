let sayUnoRequestRunning = false;
let alreadySaidUno = false;

function getCardImage(card) {
    switch(card.cardType){
        case 'NUMBER':
            return getNumberCardImage(card);
        case 'SKIP':
        case 'REVERSE':
        case 'DRAW_2':
            return getActionCardImage(card);
        case 'JOKER':
        case 'DRAW_4':
            return getJokerCardImage(card);
    }
}

function getNumberCardImage(card){
    const color = card.color.toLowerCase();
    const number = card.value;
    return `${color}${number}`;
}

function getActionCardImage(card){
    const color = card.color.toLowerCase();
    const action = card.cardType.toLowerCase().replace('_', '');
    return `${color}${action}`;
}

function getJokerCardImage(card){
    return  card.cardType.toLowerCase().replace('draw_4', 'jokerdraw4');
}

function put(card, index){
    if(isPutAllowed(card, index)){
        app.actionLock = true;
        app.putCardIndex = index;
        const data = {
            card: card,
            cardIndex: index,
            gameUuid: app.gameUuid,
            playerUuid: app.playerUuid
        };
        localStorage.setItem('gameUuid', app.gameUuid);
        localStorage.setItem('playerUuid', app.playerUuid);
        doPostRequest('/turn/put', data, putCallback);
    }
}

function putDrawn(){
    const index = app.gameState.ownCards.length - 1;
    const card = app.gameState.ownCards[index];
    put(card, index);
}

function draw(){
    if(isDrawAllowed()){
        app.actionLock = true;
        const path = prepareAction(app.gameState.game.turnState.startsWith('DRAW_') ? 'draw-multiple' : 'draw');
        doPostRequest(path, {}, drawCallback);
    }
}

function keep(){
    const path = prepareAction('keep');
    doPostRequest(path, {}, loadGame);
}

function selectColor(color){
    const path = '/turn/select-color/' + app.gameUuid + '/' + app.playerUuid + '/' + color;
    doPostRequest(path, {}, loadGame);
}

function sayUno(){
    sayUnoRequestRunning = true;
    alreadySaidUno = true;
    localStorage.setItem('sayUno', '1');
    const path = prepareAction('say-uno');
    doPostRequest(path, {}, loadGame);
}

function next() {
    if(!isMyTurn()){
        return;
    }
    if(!sayUnoRequestRunning){
        app.gameState.game.currentPlayerIndex = -1;
        app.gameState.game.turnState = '';
        const path = '/turn/next/' + app.gameUuid + '/' + app.playerUuid;
        doPostRequest(path, {}, nullCallback);
    } else {
        setTimeout('next()', 1000);
    }
}

function isPutAllowed(card, index){
    if(app.actionLock){
        return false;
    }
    if(!isMyTurn()){
        return false;
    }
    if(!isPlayable(card)){
        return false;
    }
    if(app.gameState.game.turnState === 'PUT_DRAWN'){
        return index === (app.gameState.ownCards.length-1);
    }
    return app.gameState.game.turnState === 'PUT_OR_DRAW' || app.gameState.game.turnState === 'DRAW_DUTIES_OR_CUMULATIVE';
}

function isDrawAllowed() {
    if(app.actionLock){
        return false;
    }
    if(!isMyTurn()){
        return false;
    }
    if(app.gameState.game.turnState === 'PUT_DRAWN'){
        return false;
    }
    if(app.gameState.game.turnState === 'SELECT_COLOR'){
        return false;
    }
    return app.gameState.game.turnState !== 'FINAL_COUNTDOWN';
}

function isSayUnoAllowed() {
    if(!isMyTurn() || alreadySaidUno){
        return false;
    }
    return app.gameState.game.turnState === 'SELECT_COLOR' || app.gameState.game.turnState === 'FINAL_COUNTDOWN';
}

function isMyTurn(){
    return isPlayersTurn(app.gameState.myIndex);
}

function isMe(index){
    return index === app.gameState.myIndex && app.gameState.myIndex >= 0;
}

function isNotMe(index){
    return index !== app.gameState.myIndex && app.gameState.myIndex >= 0;
}

function isPlayersTurn(index){
    return app.gameState.game.currentPlayerIndex === index && app.gameState.myIndex >= 0;
}

function prepareAction(action){
    localStorage.setItem('gameUuid', app.gameUuid);
    localStorage.setItem('playerUuid', app.playerUuid);
    return '/turn/' + action + '/' + app.gameUuid + '/' + app.playerUuid;
}

function drawCallback(){
    modificationTransitionWrapper(loadGame, null);
    setTimeout(function(){app.actionLock = false}, 1000);
}

function putCallback(data){
    modificationTransitionWrapper(loadGame, data.card);
    setTimeout(function(){app.actionLock = false}, 1000);
}

function isPlayable(card){
    if(app.gameState.game.turnState === 'DRAW_DUTIES_OR_CUMULATIVE' && !isCumulative(app.gameState.game.topCard, card)){
        return false;
    }
    return isMatch(app.gameState.game.topCard, card);
}

function isCumulative(topCard, card){
    return topCard.drawValue === card.drawValue;
}

function isMatch(topCard, card){
    if(card.jokerCard){
        return true;
    }
    switch(topCard.cardType){
        case 'NUMBER':
            return card.value === topCard.value || card.color === topCard.color;
        case 'SKIP':
        case 'REVERSE':
        case 'DRAW_2':
            return card.cardType === topCard.cardType || card.color === topCard.color;
        case 'JOKER':
        case 'DRAW_4':
            return card.color === app.gameState.game.desiredColor;
    }
    return false;
}

function nullCallback(data){
    data = null;
}
