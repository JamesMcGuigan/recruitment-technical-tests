if( typeof Playfish == "undefined" ) { Playfish = {}; }

/**
 *  Creates a new instance of a like button widget
 *  @param {Hash} options    @see Playfish.LikeButton.prototype.defaults
 *  @author James McGuigan <james.mcguigan@gmail.com>
 */
Playfish.LikeButton = function( options ) {
    this.klass = "Playfish.LikeButton";

    this.options = this.extend( {}, this.defaults, options );
    this.el = {
        parentNode: null, // {Element} reference to flash parent div, defined by parentNodeId
        button:     null  // {Element} reference to like button Element
    };
    this.px = {
        top:  null,       // {Number} current px positon top, as offset from parent
        left: null        // {Number} current px positon left, as offset from parent
    };

    this._loaded = false;           // {Boolean} has the iframe finsihed loading
    this.onLoadCallbackStack = [];  // {Array}   list of callbacks waiting for iframe to load

    // Don't do logging if console not defined
    this.logging      = !!(this.options.logging      && typeof console !== "undefined" && typeof console.log   === "function" );
    this.errorLogging = !!(this.options.errorLogging && typeof console !== "undefined" && typeof console.error === "function" );

    // Locate flash
    this.el.parentNode = document.getElementById( this.options.parentNodeId );
    if( !this.el.parentNode ) {
        if( this.errorLogging ) { console.error("Playfish.LikeButton.prototype.Constructor: parentNodeId: ", this.options.parentNodeId, " does not exist on page", document); }
        return this; // Without a parent there is not much we can do
    }

    // Init
    this.hide();
    this.render();
    this.setPosition(this.options.position.top, this.options.position.left);
    // this.show(); // No buttons are initially visible.

    if( this.logging ) { console.log( "Playfish.LikeButton.prototype.Constructor(", options, ") = ", this ); }
    return this;
};
Playfish.LikeButton.prototype.destroy = function() {
    this._destroyed = true;
    if( this.el.parentNode && this.el.button ) {
        this.el.parentNode.removeChild(this.el.button);
    }
    for( var key in this.el ) {
        this.el[key] = null;
    }
};


/**
 *  These are the default options and form the spec for what can be passed into the constructor
 *  This hash is shared across all classes and thus should not be modified inline
 */
Playfish.LikeButton.prototype.defaults = {
    //----- User Config Variables -----//
    logging:       false,           // {Boolean} Enable Logging
    errorLogging:  true,            // {Boolean} Enable Logging of errors

    parentNodeId:  "flashcontent",  // {String}  ID of flashcontent DIV, overridable via constructor param
    tagName:       "iframe",        // {String} TagName for like button
    srcPrefix:     "http://www.facebook.com/plugins/like.php", // {String} script prefix

    //---- Initial offset position for element -----//
    position: {
        top:  100,
        left: 100
    },

    //----- srcParams for like button URL, concatinated to form a url ----//
    srcParams: {
        href:        "http://apps.facebook.com/crazyplanets/?pf_ref=sb",
        layout:      "standard",
        show_faces:  true,
        width:       450,        // {Number} also used as an HTML attribute
        height:      80,         // {Number} also used as an HTML attribute
        action:      "like",
        colorscheme: "light"
    },

    //----- HTML attrs for like button tag ----/i/
    attrs: {
        scrolling: "no",
        allowTransparency: true,
        style: "position: absolute; border: none; overflow: hidden;"  // width/height to be auto-prepended from this.options.srcParams
    }
};



/**
 *  Builds the like button and renders it to the page, removing any previous elements rendered by this instance
 *  @return {Element}      the element rendered to the page
 */
Playfish.LikeButton.prototype.render = function() {
    if( !this.el.parentNode && this.errorLogging ) { console.error("Playfish.LikeButton.prototype.render: flashNode #" + this.options.parentNodeId + " does not exist"); }
    if( this.el.button && this.el.parentNode ) {
        this.el.parentNode.removeChild(this.el.button); // There can be only one
    }
    this.el.button = this.buildElement();
    this.el.parentNode.appendChild( this.el.button );

    if( this.el.button && this.el.button.nodeName === "IFRAME" ) {
        var self = this;
        this.el.button.onload = function() {
            self._loaded = true;
            var callback;
            while( callback = self.onLoadCallbackStack.shift() ) {
                if( callback instanceof Function ) {
                    callback(this);
                }
            }
        };
    }

    if( this.logging ) { console.log( "Playfish.LikeButton.prototype.render", this.el.parentNode ); }
    return this.el.button;
};


/**
 *  Builds an element based up this.options.srcParams and this.options.attrs, called by this.render
 *  @return {Element} the built element
 */
Playfish.LikeButton.prototype.buildElement = function() {
    this.options.attrs.style = this.options.srcParams.height + "px; " + this.options.srcParams.width + "px; " + this.options.attrs.style;

    var node = document.createElement(this.options.tagName);
    for( var attrName in this.options.attrs ) {
        node.setAttribute(attrName, this.options.attrs[attrName]);
    }
    var src = this.buildUrl( this.options.srcPrefix, this.options.srcParams );
    node.setAttribute("src", src);
    return node;
};


/**
 *  Builds a url based on a string prefix and parameter hash
 *  paramHash values are escaped and combined as such:  key1=value1&amp;key2=value2
 *  Function is smart enough to know if a ? needs to be added before the query string
 *
 *  @param  {String} url         base url
 *  @param  {Hash}   paramHash   [optional] key/value pairs to be encoded into the queryString
 *  @return {String}             the fully encoded url
 */
Playfish.LikeButton.prototype.buildUrl = function( url, paramHash ) {
    var srcParams = [];
    var seperator = "";
    if( typeof paramHash === "object" ) {
        for( var paramName in paramHash ) {
            srcParams.push( paramName + "=" + escape(paramHash[paramName]) );
        }
        if( srcParams.length ) {
            seperator = (url.match(/\?$/)) ? ""
                      : (url.match(/\?/))  ? "&amp;"
                                           : "?";
        }
    }
    var src = url + seperator + srcParams.join("&amp;");
    return src;
};


/**
 *  Changes the url to be liked
 *  @param  {String} url         url to be liked
 */
Playfish.LikeButton.prototype.setLikeUrl = function( url ) {
    if( url ) {
        this.options.srcParams.href = url;
        if( this.el.button ) {
            this.el.button.setAttribute("src", this.buildUrl(this.options.srcPrefix, this.options.srcParams) );
        }
    }
    if( this.logging ) { console.log( "Playfish.LikeButton.prototype.setLikeUrl", url ); }
};

/**
 *  Sets this.el.button to display: block
 *  If element has not previously been rendered, then modifies this.options.attrs.style to display: block
 *
 *  @param  {String} url         [optional] new url to like
 */
Playfish.LikeButton.prototype.show = function( url ) {
    if( url ) {
        this.setLikeUrl( url );
    }

    if( this.el.button ) {
        this.el.button.style.display = "block";
    } else {
        this.options.attrs.style = "display: block; " + this.options.attrs.style.replace(/display:\s*\w+\s*;?/, '');
    }
    if( this.logging ) { console.log( "Playfish.LikeButton.prototype.show", this.el.button ); }
};

/**
 *  Sets this.el.button to display: none
 *  If element has not previously been rendered, then modifies this.options.attrs.style to display: none
 */
Playfish.LikeButton.prototype.hide = function() {
    if( this.el.button ) {
        this.el.button.style.display = "none";
    } else {
        this.options.attrs.style = "display: none; " + this.options.attrs.style.replace(/display:\s*\w+\s*;?/, '');
    }
    if( this.logging ) { console.log( "Playfish.LikeButton.prototype.hide", this.el.button ); }
};

/**
 *  Sets the absolute position of the like button tag, relative to this.el.parentNode
 *  @param top  {Number} offset in px
 *  @param left {Number} offset in px
 */
Playfish.LikeButton.prototype.setPosition = function( top, left ) {
    // If the parentNode is not position: absolute|relative, then the offset is fixed in px on render, it won't get auto-updated if the parent moves
    // The alternative would be to add position:relative to the parentNode, but this may break the page styling for other position: absolute elements
    // We are also assuming here that the top left of the flash app is aligned with the top left of the parent div, no logic here to extract the offset of the child flash element

    this.px.top  = (typeof top  === "number") ? top  : Number( String(top).replace(/[^\d\.]+/g,'')   );
    this.px.left = (typeof left === "number") ? left : Number( String(left).replace(/[^\d\.]+/g,'')  );

    top  = this.px.top  + this.el.parentNode.offsetTop  + "px";
    left = this.px.left + this.el.parentNode.offsetLeft + "px";

    if( this.el.button ) {
        if( this.el.button.style.position != "none" ) {
            this.el.button.style.position = "absolute";
        }
        this.el.button.style.top  = top;
        this.el.button.style.left = left;
    } else {
        this.options.attrs.style = this.options.attrs.style.replace(/(top|left):\s*\w+\s*;?/g, '') + "top: "+top+"; left:"+left+";";
    }
    if( this.logging ) { console.log( "Playfish.LikeButton.prototype.setPosition", top, left, this.el.button ); }
};


/**
 *  Animates the position of the like button
 *
 *  @param {Number}   options.top       where to start the animation from [default: current positon]
 *  @param {Number}   options.left      where to start the animation from [default: current positon]
 *  @param {Number}   options.moveTop   where to end the animation, relative px from options.top  [default: 100]
 *  @param {Number}   options.moveLeft  where to end the animation, relative px from options.left [default: 100]
 *  @param {Number}   options.endTop    where to end the animation, grid offset from parent, overrides options.moveTop
 *  @param {Number}   options.endLeft   where to end the animation, grid offset from parent, overrides options.moveLeft
 *  @param {Number}   options.stepTop   number of px to move the button in each frame
 *  @param {Number}   options.stepLeft  number of px to move the button in each frame
 *  @param {Number}   options.delay     milliseconds to wait between each frame
 *  @param {Boolean}  options._recurse  [internal] flag to mark that the function is recursing
 *  @param {Function} callback          [optional] function(this) to call once the animation is complete
 */
Playfish.LikeButton.prototype.animate = function( options, callback ) {
    options = options || {};

    if( !options._recurse ) { // speed optimization
        options = this.extend({
            top:      this.px.top,
            left:     this.px.left,
            stepTop:  1,
            stepLeft: 1,
            moveTop:  100,
            moveLeft: 100,
            delay:    10
        }, options);
        if( typeof options.endTop  === "undefined" ) { options.endTop  = options.top  + options.moveTop;  }
        if( typeof options.endLeft === "undefined" ) { options.endLeft = options.left + options.moveLeft; }
    }

    var self = this;
    setTimeout(function() {
        var isEndTop  = false;
        var isEndLeft = false;
        if( options.stepTop > 0 && options.top >= options.endTop
         || options.stepTop < 0 && options.top <= options.endTop
         || options.stepTop == 0 ) {
            isEndTop = true;
         } else {
             var maxMin = ( options.stepTop > 0 ) ? "min" : "max";
             options.top = Math[maxMin]( options.top + options.stepTop, options.endTop );
        }

        if( options.stepLeft > 0 && options.left >= options.endLeft
         || options.stepLeft < 0 && options.left <= options.endLeft
         || options.stepLeft == 0 ) {
            isEndLeft = true;
         } else {
             var maxMin = ( options.stepLeft > 0 ) ? "min" : "max";
             options.left = Math[maxMin]( options.left + options.stepLeft, options.endLeft );
        }

        self.setPosition( options.top, options.left );

        if(!( isEndTop && isEndLeft )) {
            options._recurse = true;
            self.animate( options, callback );
        } else {
            options._recurse = false;
            if( callback instanceof Function ) {
                callback(this);
            }
        }
    }, options.delay);
};


/**
 *  Does a deep recusrive extend of all N args onto target
 *  Modifies target inplace and also returns target
 *  If target is not of type "object", then copy of extended empty hash is returned
 *
 *  @param {Hash}     target   object to extend
 *  @param {Hash...}  args     N arguments to copy values from, leftmost first
 *  @param {Hash}              returns modified target param (also modified inplace)
 */
Playfish.LikeButton.prototype.extend = function( target, args ) {
    if( typeof target !== "object" ) { target = {}; }

    for( var i=1, n=arguments.length; i<n; i++ ) {
        var options = arguments[i];
        if( typeof options !== "object" ) { continue; }
        for( var key in options ) {
            if( typeof target[key] === "object" && typeof options[key] === "object" ) {
                target[key] = this.extend( {}, target[key], options[key] );
            } else {
                target[key] = options[key];
            }
        }
    }
    return target;
};

/**
 *  Calls a callback but delays execution until after iframe has finsihed loading
 *  @param {Function} callback   function(this)
 */
Playfish.LikeButton.prototype.afterLoad = function( callback ) {
    if( callback instanceof Function ) {
        if( this._loaded ) {
            callback(this);
        } else {
            this.onLoadCallbackStack.push( callback );
        }
    }
    if( this.logging ) { console.log( "Playfish.LikeButton.prototype.afterLoad", callback, this ); }
};
