const localeStorageEntriesToTransport = ['sayUno', 'readMessages'];

function prepareSwitchDevice(){
    app.previousView = app.currentView;
    app.currentView = 'switch-device';
    app.pendingRemoveAfterSwitch = false;
}

function abortSwitchDevice(){
    app.currentView = app.previousView;
    app.previousView = '';
}

function prepareSwitchOut(){
    const url = getSwitchOutUrl();
    app.qr = genQr(url);
    app.showSwitchOutQr = true;
    app.pendingRemoveAfterSwitch = true;
}

function abortSwitchOut(){
    app.qr = null;
    app.showSwitchOutQr = false;
    app.pendingRemoveAfterSwitch = false;
}

function setPreparedSwitchIn(pushUuid){
    app.qr = genQr(`${app.protocol}://${app.hostname}/?switch=in&pushUuid=${pushUuid}`);
    app.showSwitchInQr = true;
    app.pendingSwitch = true;
    app.pendingRemoveAfterSwitch = true;
}

function prepareSwitchIn(){
    const pushUuid = generateUUID();
    app.pushUuid = pushUuid;
    connectPush(pushUuid, setPreparedSwitchIn, pushUuid);
}

function abortSwitchIn(){
    app.qr = null;
    app.showSwitchInQr = false;
    disconnectPush();
    app.pendingSwitch = false;
    app.pendingRemoveAfterSwitch = false;
}

function handleSwitchDevice(){
    const urlParams = new URLSearchParams(window.location.search);
    const switchParam = urlParams.get('switch');
    if(switchParam != null && switchParam === 'out'){
        return switchOut(urlParams);
    }
    if(switchParam != null && switchParam === 'in'){
        return switchIn(urlParams);
    }
}

function setSwitchFinished(){
    self.location.replace('/');
}

function switchOut(urlParams){
    let gameUuid = localStorage.getItem('gameUuid');
    let playerUuid = localStorage.getItem('playerUuid');
    if((gameUuid != null && gameUuid !== '') || (playerUuid != null && playerUuid !== '')){
        app.refreshPageOnDialogClose = true;
        showErrorDialog('Hier ist bereits ein Spiel. Wechsel kann nicht erfolgen.');
        return false;
    }
    gameUuid = urlParams.get('gameUuid');
    playerUuid = urlParams.get('playerUuid');
    if(gameUuid != null && gameUuid !== '' && playerUuid != null && playerUuid !== ''){
        for(let i = 0; i < localeStorageEntriesToTransport.length; i++) {
            const entry = localeStorageEntriesToTransport[i];
            const value = urlParams.get(entry);
            if(value !== null && value !== ''){
                localStorage.setItem(entry, value);
            }
        }
        localStorage.setItem('gameUuid', gameUuid);
        localStorage.setItem('playerUuid', playerUuid);
        doPostRequest(`/switch/finished/${gameUuid}/${playerUuid}`, {}, setSwitchFinished);
        return true;
    }
}

function setSwitchIn(pushUuid){
    let path = `/switch/in/${pushUuid}/${app.gameUuid}/${app.playerUuid}`;
    for(let i = 0; i < localeStorageEntriesToTransport.length; i++){
        const entry = localeStorageEntriesToTransport[i];
        const value = localStorage.getItem(entry);
        const empty = value === null || value === '';
        const toAdd = empty ? 'empty' : value;
        path += `/${toAdd}`;
    }
    doPostRequest(path, {}, nullCallback);
}

function switchIn(urlParams){
    const pushUuid = urlParams.get('pushUuid');
    const gameUuid = localStorage.getItem('gameUuid');
    const playerUuid = localStorage.getItem('playerUuid');
    app.pendingRemoveAfterSwitch = true;
    app.gameUuid = gameUuid;
    app.playerUuid = playerUuid;
    if(pushUuid !== null && pushUuid !== '' && gameUuid !== null && gameUuid !== '' && playerUuid !== null && playerUuid !== ''){
        connectPush(gameUuid, setSwitchIn, pushUuid);
        return true;
    } else {
        app.refreshPageOnDialogClose = true;
        showErrorDialog('Hier ist kein Spiel. Wechsel kann nicht erfolgen.');
        return false;
    }
}

function removeSwitchedGameFromHere(data){
    const myIndex = data.myIndex;
    const pendingPlayerIndex = app.pendingPlayerIndex;
    if(myIndex === pendingPlayerIndex && app.pendingRemoveAfterSwitch){
        reset();
    }
}

function handlePushSwitchIn(message){
    const values = [];
    const gameUuid = message.body.replace(/switch-in:([^:]+):([^:]+):([^:]+):([^:]+)$/, '$1');
    const playerUuid = message.body.replace(/switch-in:([^:]+):([^:]+):([^:]+):([^:]+)$/, '$2');
    values[0] = message.body.replace(/switch-in:([^:]+):([^:]+):([^:]+):([^:]+)$/, '$3');
    values[1] = message.body.replace(/switch-in:([^:]+):([^:]+):([^:]+):([^:]+)$/, '$4');
    if(app.pendingSwitch){
        localStorage.setItem('gameUuid', gameUuid);
        localStorage.setItem('playerUuid', playerUuid);
        for(let i = 0; i < localeStorageEntriesToTransport.length; i++) {
            const entry = localeStorageEntriesToTransport[i];
            const value = values[i];
            if(value !== null && value !== 'empty'){
                localStorage.setItem(entry, value);
            }
        }
        doPostRequest(`/switch/finished/${gameUuid}/${playerUuid}`, {}, function(){self.location.replace('/')});
    }
}

function handlePushSwitchFinished(message){
    app.pendingPlayerIndex = parseInt(message.body.replace(/switch-finished:/, ''));
    const path = `/gameState/get/${app.gameUuid}/${app.playerUuid}`;
    doGetRequest(path, removeSwitchedGameFromHere);
}

function copySwitchLink(){
    const url = getSwitchOutUrl();
    copy(url);
    app.currentView = app.previousView;
    app.previousView = '';
    showInformationDialog('Link wurde kopiert. Rufe die Adresse nun auf dem Ziel-Gerät im Ziel-Browser auf.');
}

function generateUUID() {
    let d = new Date().getTime();
    let uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        let r = (d + Math.random()*16)%16 | 0;
        d = Math.floor(d/16);
        return (c=='x' ? r : (r&0x3|0x8)).toString(16);
    });
    return uuid;
}

function getSwitchOutUrl(){
    let url = `${app.protocol}://${app.hostname}/?switch=out&gameUuid=${app.gameUuid}&playerUuid=${app.playerUuid}`;
    for(let i = 0; i < localeStorageEntriesToTransport.length; i++){
        const entry = localeStorageEntriesToTransport[i];
        const value = localStorage.getItem(entry);
        if(value !== null && value !== ''){
            url += `&${entry}=${value}`;
        }
    }
    return url;
}
