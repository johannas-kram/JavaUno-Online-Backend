
function ownCardsBeforeEnter(element){
    document.querySelector('#own-cards-container').appendChild(element);
    const elementRectangle = element.children[0].getBoundingClientRect();
    const drawPileRectangle = document.querySelector('#drawPile').getBoundingClientRect();
    const leftDifference = drawPileRectangle.left - elementRectangle.left;
    const topDifference = drawPileRectangle.top - elementRectangle.top;
    document.querySelector('#own-cards-container').removeChild(element);
    element.style.width='';
    element.children[0].style.cssText = `transform:translate(${leftDifference}px,${topDifference}px) rotateY(180deg);`;
}

function ownCardsEnter(element, done){
    setTimeout(function (){doOwnCardsEnter(element, done)}, 50);
}

function doOwnCardsEnter(element, done){
    element.children[0].style.cssText = `transform: translate(0,0) rotateY(0deg)`;
    setTimeout(function (){
        done();
        resetCardStyle(element.children[0]);
    }, 500);
}

function ownCardsLeave(element, done){
    const elementRectangle = element.children[0].getBoundingClientRect();
    const topCardRectangle = document.querySelector('#topCard').getBoundingClientRect();
    const leftDifference = topCardRectangle.left - elementRectangle.left;
    const topDifference = topCardRectangle.top - elementRectangle.top;
    element.children[0].style.cssText = `transform: translate(${leftDifference}px,${topDifference}px);`;
    element.children[0].children[0].style.cssText = 'filter:brightness(var(--primary-brightness-filter));';
    element.style.cssText = 'width:0.1px;margin:0;';
    setTimeout(done, 500);
}

function topCardBeforeEnter(element){
    if(isMyTurn()){
        element.style.display = 'none';
        return;
    }
    const player = app.gameState.players[app.gameState.game.currentPlayerIndex];
    const uuid = player.publicUuid;
    const playersPile = document.querySelector(`#players .player._${uuid} .cards`);
    document.querySelector('#topCard').appendChild(element);
    const elementRectangle = element.getBoundingClientRect();
    const playersPileRectangle = playersPile.getBoundingClientRect();
    const leftDifference = playersPileRectangle.left - elementRectangle.left;
    const topDifference = playersPileRectangle.top - elementRectangle.top;
    element.style.cssText = `transform:translate(${leftDifference}px,${topDifference}px) rotateY(180deg);`;
    document.querySelector('#topCard').removeChild(element);
    const wrapper = document.querySelector(`#players .player._${uuid} .cards .floating-wrapper-cards`);
    const clone = wrapper.cloneNode(true);
    enableFloating(wrapper, clone, false);
}

function topCardEnter(element, done){
    removeBack();
    if(!isMyTurn()){
        setTimeout(function (){doTopCardEnter(element, done)}, 50);
        return;
    }
    setTimeout(function (){
        element.style.display = '';
        done();
    }, 500);
}

function doTopCardEnter(element, done){
    element.style.cssText = `transform: translate(0,0) rotateY(0deg)`;
    setTimeout(function(){
        done();
        const uuid = app.gameState.players[app.gameState.game.currentPlayerIndex].publicUuid;
        const wrapper = document.querySelector(`#players .player._${uuid} .cards .floating-wrapper-cards`);
        const clone = document.querySelector('.floating-wrapper-cards.float');
        disableFloating(wrapper, clone);
    }, 500);
}

function cardCountBeforeEnter(element){
    const uuid = app.gameState.players[app.gameState.game.currentPlayerIndex].publicUuid;
    if(isMyTurn()){
        document.querySelector(`#players .player._${uuid} .cards-count-wrapper`).appendChild(element);
        element.parentElement.querySelectorAll(`.cards`).forEach(e => e.style.position='absolute');
        document.querySelector(`#players .player._${uuid} .cards-count-wrapper`).removeChild(element);
        element.style.cssText = 'position:absolute;z-index:-1';
        return;
    }
    document.querySelector(`#players .player._${uuid} .cards-count-wrapper`).appendChild(element);
    element.parentElement.querySelectorAll(`.cards`).forEach(e => e.style.position='absolute');
    const elementRectangle = element.getBoundingClientRect();
    const drawPileRectangle = document.querySelector(`#drawPile`).getBoundingClientRect();
    let leftDifference = drawPileRectangle.left - elementRectangle.left;
    let topDifference = drawPileRectangle.top - elementRectangle.top;
    element.style.cssText = `position:absolute;transform:translate(${leftDifference}px,${topDifference}px);`;
    const wrapper = element.querySelector('.floating-wrapper-cards');
    const clone = createFloatingClone(wrapper)
    document.querySelector(`#topCard`).style.cssText = 'z-index:-1;';
    document.querySelector(`#drawPile`).style.cssText = 'position:relative;z-index:-1;';
    document.querySelectorAll(`#players .player:not(._${uuid})`).forEach(e => e.style.cssText='position:relative;z-index:-1');
    element.parentElement.children[0].querySelectorAll('.floating-wrapper-cards *').forEach(e => e.style.display='none');
    enableFloating(wrapper, clone, true);
    enableDrawCount(element);
    document.querySelector(`#players .player._${uuid} .cards-count-wrapper`).removeChild(element);
}

function cardCountEnter(element, done){
    if(!isMyTurn()){
        setTimeout(function (){doCardCountEnter(element, done)}, 50);
        return;
    }
    setTimeout(function (){
        setTimeout(function(){
            element.style.cssText = '';
        }, 50);
        done();
    }, 500);
}

function doCardCountEnter(element, done){
    element.style.cssText = 'position:absolute;transform:translate(0,0)';
    setTimeout(function(){
        setTimeout(done, 200);
        element.style.cssText = '';
        document.querySelector(`#topCard`).style.cssText = '';
        document.querySelector(`#drawPile`).style.cssText = '';
        document.querySelectorAll('#players .player').forEach(e => e.style.cssText='');
        const wrapper = element.querySelector('.floating-wrapper-cards');
        const clone = document.querySelector('.floating-wrapper-cards.float');
        disableFloating(wrapper, clone);
        disableDrawCount(element);
    }, 500);
}

function cardCountLeave(element, done){
    setTimeout(done, 500);
}

function resetCardStyle(element){
    element.style.cssText = 'transition: none';
    setTimeout(function(){element.style.cssText=''}, 50);
}

function enableFloating(wrapper, clone, draw){
    clone.querySelector('.count').classList.remove('red');
    if(draw && app.playerWasInUnoState){
        console.debug('setting red again');
        clone.querySelector('.count').classList.add('red');
    }
    app.playerWasInUnoState = false;
    const wrapperRectangle = wrapper.getBoundingClientRect();
    const top = wrapperRectangle.top;
    const left = wrapperRectangle.left;
    const width = wrapperRectangle.right - left;
    const height = wrapperRectangle.bottom - top;
    wrapper.querySelector('button').style.display = 'none';
    clone.style.cssText = `position:fixed;top:${top}px;left:${left}px;width:${width}px;height:${height}px;`;
    clone.classList.add('float');
    document.querySelector('body').appendChild(clone);
}

function disableFloating(wrapper, clone){
    document.querySelector('body').removeChild(clone);
    wrapper.querySelector('button').style.display = '';
}

function fixRemainingFloatingClonesAfterStopParty(){
    document.querySelectorAll('.floating-wrapper-draw.float,.floating-wrapper-cards.float')
        .forEach(e=>document.querySelector('body').removeChild(e));
}

function enableDrawCount(element){
    const drawCount = document.createElement('div');
    drawCount.classList.remove('red');
    drawCount.classList.add('draw', 'count', app.drawReason);
    drawCount.textContent = app.drawnCards > 1 ? ''+app.drawnCards : '';
    element.querySelector('.count').style.visibility = 'hidden';
    if(app.drawnCards > 9){
        drawCount.classList.add('more');
    }
    element.querySelector('.count').parentElement.appendChild(drawCount);
}

function disableDrawCount(element){
    const drawCount = element.querySelector('.draw.count')
    element.querySelector('.count').parentElement.removeChild(drawCount);
    element.querySelector('.count').style.visibility = '';
    app.drawnCards = 1;
    app.drawReason = 'regular';
}

function createFloatingClone(wrapper){
    const clone = wrapper.cloneNode(true);
    const countElement = clone.querySelector('.count');
    let count = parseInt(countElement.textContent) - app.drawnCards;
    countElement.textContent = ''+count;
    if(count > 9){
        countElement.classList.add('more');
    } else {
        countElement.classList.remove('more');
    }
    return clone;
}

function removeBack(){
    const player = app.gameState.players[app.gameState.game.currentPlayerIndex];
    const uuid = player.publicUuid;
    const playersPile = document.querySelector(`#players .player._${uuid} .cards`);
    if(player.cardCount === 0){
        const remove = function(){playersPile.classList.add('none', isMyTurn() ? 'animated' : 'instantly')};
        setTimeout(remove, isMyTurn() ? 0 : 50);
        removeCount(playersPile);
        if(!isMyTurn()){
            removeCount(document.querySelector('.floating-wrapper-cards.float'));
        }
    }
}

function removeCount(element){
    const countElement = element.querySelector('.count');
    countElement.textContent = '';
}

function modificationTransitionWrapper(callback, topCard){
    if(topCard != null){
        doPutCardModification(topCard);
        setTimeout(callback, 400);
        return;
    }
    const currentPlayer = app.gameState.players[app.gameState.game.currentPlayerIndex];
    if(currentPlayer.cardCount === 1 && currentPlayer.unoSaid && !isMyTurn()){
        app.playerWasInUnoState = true;
    }
    callback();
}

function doPutCardModification(topCard){
    app.gameState.game.topCard = topCard;
    app.gameState.players[app.gameState.game.currentPlayerIndex].cardCount -= 1;
    if(isMyTurn() && app.putCardIndex >= 0){
        app.gameState.ownCards.splice(app.putCardIndex, 1);
        app.putCardIndex = -1;
    }
    if(topCard.jokerCard){
        app.gameState.game.desiredColor = null;
    }
}