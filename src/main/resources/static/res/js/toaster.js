/***********************************************

 "toaster.js"

 Created by Michael Cheng on 05/27/2015 14:24
 http://michaelcheng.us/
 michael@michaelcheng.us
 --All Rights Reserved--
 edited for javaUno by me:
  * multiple toasters are positioned one above the other instead of stacked positioning
  * big toasts (with fully centered position)
  * variable colors (for dark and light theme)
 original version is here: https://github.com/mlcheng/js-toast

 ***********************************************/

"use strict";

/**
 * The iqwerty namespace
 */
var iqwerty = iqwerty || {};
var positionOffsets = [];

/**
 * my positioning settings
 */
const initialPosition = -5;
const firstPosition = 5;
const positionOffset = 35;


/**
 * Toasts are here
 */
iqwerty.toast = (function() {
    function Toast() {
        /**
         * The duration of the toast, in milliseconds
         * @type {Number}
         */
        var _duration = 3000;
        this.getDuration = function() {
            return _duration;
        };
        this.setDuration = function(time) {
            _duration = time;
            return this;
        };

        /**
         * The toast element
         * @type {Object}
         */
        var _toastStage = null;
        this.getToastStage = function() {
            return _toastStage;
        };
        this.setToastStage = function(toastStage) {
            _toastStage = toastStage;
            return this;
        };

        /**
         * The text inside the toast
         * @type {String}
         */
        var _text = null;
        this.getText = function() {
            return _text;
        };
        this.setText = function(text) {
            _text = text;
            return this;
        };

        /**
         * The text element inside the toast
         * @type {Object}
         */
        var _textStage = null;
        this.getTextStage = function() {
            return _textStage;
        };
        this.setTextStage = function(textStage) {
            _textStage = textStage;
            return this;
        };


        /**
         * Specifies whether or not the style is user defined. If stylize() is called by the user, the toast will not use default styles. Otherwise, default styles will be applied
         * @type {Boolean}
         */
        this.stylized = false;
    };

    /**
     * Specifies whether or not the stylesheet exists in the document head
     * @type {Boolean}
     */
    Toast.prototype.styleExists = false;

    /**
     * Initialize the animations for the toast, including fade/slide in, and fade/slide out. Add the styles to a style element in the head.
     * @return Returns nothing
     */
    Toast.prototype.initializeAnimations = function() {
        // don't do anything if styles/animations already exist inside document
        if(Toast.prototype.styleExists) return;



        var style = document.createElement("style");
        style.classList.add(iqwerty.toast.identifiers.CLASS_STYLESHEET);

        style.innerHTML = "." + iqwerty.toast.identifiers.CLASS_SLIDE_IN +
            "{opacity: 1; bottom: " + firstPosition + "px}" +

            "." + iqwerty.toast.identifiers.CLASS_SLIDE_OUT +
            "{opacity: 0; bottom: " + initialPosition + "px}" +

            "." + iqwerty.toast.identifiers.CLASS_ANIMATED +
            "{transition: opacity " + iqwerty.toast.style.TOAST_ANIMATION_SPEED + "ms, bottom " + iqwerty.toast.style.TOAST_ANIMATION_SPEED + "ms;}";


        // add the styles to the document head
        document.head.appendChild(style);

        // specify in the prototype that the style exists in the document already, to avoid creating styles again next time
        Toast.prototype.styleExists = true;
    };

    /**
     * Generate the toast and set the stages
     * @return {Object} Returns the Toast object
     */
    Toast.prototype.generate = function() {
        var toastStage = document.createElement("div");
        var textStage = document.createElement("span");
        textStage.innerHTML = this.getText();
        toastStage.appendChild(textStage);

        this.setToastStage(toastStage);
        this.setTextStage(textStage);

        // initialize animation styles for the toast
        this.initializeAnimations();

        return this;
    };

    /**
     * Show the toast
     * @return {Object} Returns the Toast object
     */
    Toast.prototype.show = function(large) {
        if(this.getToastStage() == null) {
            this.generate();
        }



        // stylize the toast if it isn't user defined
        if(!this.stylized) {
            this.stylize();
        }

        var body = document.getElementById('toasts');
        var before = body.firstChild;

        // use classes to animate the toast
        this.getToastStage().classList.add(iqwerty.toast.identifiers.CLASS_ANIMATED);
        this.getToastStage().classList.add(iqwerty.toast.identifiers.CLASS_SLIDE_OUT);

        // insert into the dom
        body.insertBefore(this.getToastStage(), before);

        // a hack to "redraw"; without this, the next class will get immediately applied without transitioning
        this.getToastStage().offsetHeight;

        // switch classes; slide the toast up
        this.getToastStage().classList.add(iqwerty.toast.identifiers.CLASS_SLIDE_IN);
        this.getToastStage().classList.remove(iqwerty.toast.identifiers.CLASS_SLIDE_OUT);



        //positioning
        this.getToastStage().style.bottom = large ? '50%' : getPositionOffset() + 'px';
        if(large){
            this.getToastStage().style.fontSize = '4rem';
            this.getToastStage().style.borderRadius = '18px';
            this.getToastStage().style.transform = 'translate(-50%, 50%)';
        }
        this.getToastStage().style.position = 'absolute';

        // hide the toast after the specified timeout
        setTimeout(this.hide.bind(this), this.getDuration());

        return this;
    };

    /**
     * Hide the toast
     * @return {Object} Returns the Toast object
     */
    Toast.prototype.hide = function() {

        var body = document.getElementById('toasts');

        if(this.getToastStage() == null) return;

        removePositionOffset(this.getToastStage());

        this.getToastStage().style.bottom = '';
        this.getToastStage().classList.remove(iqwerty.toast.identifiers.CLASS_SLIDE_IN);
        this.getToastStage().classList.add(iqwerty.toast.identifiers.CLASS_SLIDE_OUT);

        setTimeout(function() {
            body.removeChild(this.getToastStage());
            this.setToastStage(null);
            this.setText(null);
            this.setTextStage(null);
        }.bind(this), iqwerty.toast.style.TOAST_ANIMATION_SPEED);

        return this;
    };

    /**
     * Stylize the toast with defaults, or specify an object that contains the custom style
     * @param  {Object} style A literal object containing the custom style, e.g. toast.stylize({background: "pink", color: "#ff00ff"})
     * @return {Object}       Returns the Toast object
     */
    Toast.prototype.stylize = function(style) {
        if(this.getToastStage() == null) {
            this.generate();
        }

        var toastStage = this.getToastStage();
        toastStage.setAttribute("style", iqwerty.toast.style.defaultStyle);


        // apply custom styles if specified
        if(arguments.length == 1) {
            var s = Object.keys(style);
            s.forEach(function(value, index, array) {
                toastStage.style[value] = style[value];
            });
        }



        this.stylized = true;


        return this;
    };


    return {
        Toast: Toast,


        style: {
            /**
             * The default styles for the toast. Override these in Toast.stylize()
             * @type {String}
             */
            defaultStyle: ""+
            "background: var(--toaster-bgcolor);" +
            "box-shadow: 0 0 10px var(--toaster-shadow-color);" +
            "z-index: 99999;" +
            "border-radius: 3px;" +
            "color: var(--toaster-text-color);" +
            "padding: 5px;" +
            "font-size: 0.9rem;" +
            "word-break: keep-all;" +
            "white-space: nowrap;" +
            "margin: 0 auto;" +
            "text-align: center;" +
            "position: fixed;" +
            "left: 50%;" +
            "transform: translateX(-50%);",

            /**
             * The speed of the toast animation, i.e. how long it takes to fade in/out. Preferably not more than 500
             * @type {Number}
             */
            TOAST_ANIMATION_SPEED: 400
        },

        /**
         * A list of constants that define some identifiers for the Toast
         * @type {Object}
         */
        identifiers: {
            CLASS_STYLESHEET: "iqwerty_toast_stylesheet",
            CLASS_ANIMATED: "iqwerty_toast_animated",
            CLASS_SLIDE_IN: "iqwerty_toast_slide_in",
            CLASS_SLIDE_OUT: "iqwerty_toast_slide_out"
        }
    };
})();

/**
 * positioning
 */

function getPositionOffset(){
    const offset = getMaxPositionOffset() + positionOffset;
    positionOffsets.push(offset);
    return offset;
}

function getMaxPositionOffset(){
    let max = initialPosition;
    for(let i = 0; i < positionOffsets.length; i++){
        if(positionOffsets[i] > max){
            max = positionOffsets[i];
        }
    }
    return max;
}

function removePositionOffset(toastStage){
    const offset = parseInt(toastStage.style.bottom.replace(/^.*?(\d+)px.*$/, '$1'));
    const index = positionOffsets.indexOf(offset);
    positionOffsets.splice(index, 1);
}