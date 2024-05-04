function showChat(){
    app.previousView = app.currentView;
    app.currentView = 'chat';
    setReadMessages();
    setTimeout('enableChatScrolling()', 200);
}

function hideChat(){
    resetInputHeight();
    app.currentView = app.previousView;
    app.previousView = '';
    updateView();
}

function setSendMessage(){
    stopProcessingAnimation();
    app.message = '';
    setTimeout('setInputHeight()', 200);
}

function sendMessage(){
    const path = `/game/chat/send-message`;
    const data = {
        content: app.message,
        gameUuid: app.gameUuid,
        playerUuid: app.playerUuid
    }
    doPostRequest(path, data, setSendMessage);
    document.querySelector('#chat-view textarea').focus();
}

function setReadMessages(){
    app.readMessages = app.gameState.game.messages.length;
    localStorage.setItem("readMessages", app.readMessages);
}

function getReadMessages(){
    let readMessages = localStorage.getItem('readMessages');
    if(readMessages === null){
        readMessages = '0';
    }
    app.readMessages = parseInt(readMessages);
    localStorage.setItem("readMessages", app.readMessages);
}

function enableChatScrolling(){
    setTimeout('setInputHeight()', 100);
    setTimeout('scrollToChatEnd()', 200);
    document.querySelector('#chat-view textarea').addEventListener('click', waitAndScrollToChatEnd);
    document.querySelector('#chat-view textarea').addEventListener('input', waitAndScrollToChatEnd);
    document.querySelector('#chat-view textarea').addEventListener('input', setInputHeight);
}

function waitAndScrollToChatEnd(){
    setTimeout('scrollToChatEnd()', 200);
}

function scrollToChatEnd(){
    const element = document.querySelector("html");
    element.scrollTo(0,element.scrollHeight);
}

function getMessageDirection(playerPublicUuid){
    const myPublicUuid = app.gameState.players[app.gameState.myIndex].publicUuid;
    return (playerPublicUuid === myPublicUuid) ? 'out' : 'in';
}

function getSenderName(playerPublicUuid){
    const myPublicUuid = app.gameState.players[app.gameState.myIndex].publicUuid;
    return (playerPublicUuid === myPublicUuid) ? 'Du' : getPlayerNameByPublicUuid(playerPublicUuid);
}

function getPlayerNameByPublicUuid(publicUuid) {
    const players = app.gameState.players;
    for(let i = 0; i < players.length; i++){
        const player = players[i];
        if(player.publicUuid === publicUuid){
            const name = player.name;
            return getPlayerName(name, i);
        }
    }
    return "[Entfernter Spieler]";
}

function getMessageClock(time){
    const date = new Date(time);
    const hours = "0" + date.getHours();
    const minutes = "0" + date.getMinutes();
    return `${hours.substr(-2)}:${minutes.substr(-2)}`;
}

function handlePushActionChatMessage(message){
    const playerPublicUuid = message.body.replace(/chat-message:([^:]+):([^:]+):(.+)$/ms, '$1');
    const time = parseInt(message.body.replace(/chat-message:([^:]+):([^:]+):(.+)$/ms, '$2'));
    const content = message.body.replace(/chat-message:([^:]+):([^:]+):(.+)$/ms, '$3');
    const messageData = {
        playerPublicUuid: playerPublicUuid,
        content: content,
        time: time
    }
    app.gameState.game.messages.push(messageData);
    if(app.gameState.players[app.gameState.myIndex].publicUuid !== playerPublicUuid){
        const name = getPlayerNameByPublicUuid(playerPublicUuid);
        showToast(`${name} schreibt: ${content}`);
    }
    if(app.currentView === 'chat'){
        setReadMessages();
        setTimeout('scrollToChatEnd()', 250);
    }
}

function setInputHeight(){
    const textarea = document.querySelector('#chat-view textarea');
    textarea.style.height = 'auto';
    const height = textarea.scrollHeight;
    textarea.style.height = `${height}px`;
    document.querySelector('#chat-view button').style.height = `${height}px`;
    const threshold = textarea.clientWidth > 780 ? 140 : 100;
    textarea.style.overflow = height > threshold ? 'hidden scroll' : 'hidden';
    document.querySelector('#chat-view #chatPlaceholder').style.height = `${height+9}px`;
}

function resetInputHeight(){
    const textarea = document.querySelector('#chat-view textarea');
    textarea.style.height = '';
    textarea.style.overflow = '';
    document.querySelector('#chat-view #chatPlaceholder').style.height = '';
    document.querySelector('#chat-view button').style.height = '';
}

function getMessageLines(message){
    return message.content.split('\n');
}
