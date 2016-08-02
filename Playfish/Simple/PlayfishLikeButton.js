if( typeof Playfish == "undefined" ) { Playfish = {}; }

// --- Firebugx.js -----//
if (!window.console || !console.firebug) {
    window.console = {};
    var names = ["log", "debug", "info", "warn", "error", "assert", "dir", "dirxml", "group", "groupEnd", "time", "timeEnd", "count", "trace", "profile", "profileEnd"];
    for (var i = 0; i < names.length; ++i) { window.console[names[i]] = function() {} }
}

/**
 *  Constructor for Playfish.LikeButton widget
 *  @param {String} flashNodeId  the DOM id of the flash widget to attach to [default: flashcontent]
 *  @param {Object} params       Additional Params for the like button URL - will selectivly overwrite defaults in source
 *  @param {Object} attrs        Additional Attributes for the like button tag - will selectivly overwrite defaults in source
 *  @return {Playfish.LikeButton}
 */
Playfish.LikeButton = function( flashNodeId, params, attrs ) {
    var instance = {
        //----- User Config Variables -----//
        logging: true,     // {Boolean} Enable Logging

        flashNodeId: "flashcontent",  // {String}  ID of flashcontent DIV, overridable via constructor param
        params: {          // {Object} Params for like button URL
            href: "http://apps.facebook.com/crazyplanets/?pf_ref=sb",
            layout: "standard",
            show_faces: true,
            width: 450,
            height: 80,
            action: "like",
            colorscheme: "light"
        },
        attrs: {       // {Object} Attrs for like button tag
            scrolling: "no",
            allowTransparency: true,
            style: "position: absolute; border: none; overflow: hidden;"  // width/height to be auto-prepended from this.params
        },

        tagName: "iframe", // {String} TagName for like button
        srcPrefix: "https://www.facebook.com/plugins/like.php?", // {String} script prefix

        //----- Instance Variables -----//
        flashcontent: null,           // {Element} reference to flash parent div, defined in init
        node: null,                   // {Element} reference to like button Elemente


        //----- Functions -----//

        /**
         *  Creates an element from this.tagName, this.attrs, this.params
         *  @return {Element}
         */
        buildElement: function() {
            this.attrs.style = this.params.height + "px; " + this.params.width + "px; " + this.attrs.style;

            var srcParams = [];
            for( var paramName in this.params ) {
                srcParams.push( paramName + "=" + escape(this.params[paramName]) );
            }
            var src = this.srcPrefix + srcParams.join("&amp;");

            var node = document.createElement(this.tagName);
            for( var attrName in this.attrs ) {
                node.setAttribute(attrName, this.attrs[attrName]);
            }
            node.setAttribute("src", src);
            return node;
        },


        /**
         *  Renders the like button to the page
         */
        render: function() {
            if( !this.flashcontent ) { console.error("Playfish.LikeButton.render: flashcontent #" + this.flashNodeId + " does not exist"); }
            if( this.node && this.flashcontent ) {
                this.flashcontent.removeChild(this.node); // There can be only one
            }
            this.node = this.buildElement();
            this.flashcontent.appendChild( this.node );

            if( this.logging ) { console.log( "Playfish.LikeButton.render", this.flashcontent ); }
            return this.node;
        },

        show: function() {
            if( this.node ) {
                this.node.style.display = "block";
            } else {
                this.attrs.style = this.attrs.style.replace(/display:\s*\w+\s*;?/, '') + "display: block;";
            }
            if( this.logging ) { console.log( "Playfish.LikeButton.show", this.node ); }
        },
        hide: function() {
            if( this.node ) {
                this.node.style.display = "none";
            } else {
                this.attrs.style = this.attrs.style.replace(/display:\s*\w+\s*;?/, '') + "display: none;";
            }
            if( this.logging ) { console.log( "Playfish.LikeButton.hide", this.node ); }
        },

        /**
         *  Sets the absolute position of the like button tag, relative to this.flashcontent
         *  @param top  {Number} offset in px
         *  @param left {Number} offset in px
         */
        setPosition: function( top, left ) {
            top  = String(top).replace(/\D+/g,'')  + "px";
            left = String(left).replace(/\D+/g,'') + "px";

            if( this.node ) {
                if( this.node.style.position != "none" ) {
                    this.node.style.position = "absolute";
                }
                this.node.style.top  = top;
                this.node.style.left = left;
            } else {
                this.attrs.style = this.attrs.style.replace(/(top|left):\s*\w+\s*;?/g, '') + "top: "+top+"; left:"+left+";";
            }
            if( this.logging ) { console.log( "Playfish.LikeButton.setPosition", top, left, this.node ); }
        }
    }

    //----- Playfish.LikeButton init() -----//

    if( flashNodeId ) { instance.flashNodeId = flashNodeId; }
    instance.flashcontent = document.getElementById( instance.flashNodeId );

    if( !instance.flashcontent ) { console.error("Playfish.LikeButton.Constructor: flashNodeId: ", instance.flashNodeId, " does not exist on page", document); }

    if( params && typeof params == "object" ) {
        for( var key in params ) {
            instance.params[key] = params[key];
        }
    }
    if( attrs && typeof attrs == "object" ) {
        for( var key in attrs ) {
            instance.attrs[key] = attrs[key];
        }
    }

    if( instance.logging ) { console.log( "Playfish.LikeButton.Constructor", instance ); }
    return instance;
};

// Script Init Function
(function() {
    likeButton = new Playfish.LikeButton("flashcontent", { action: "Unlike", href: "http://apps.facebook.com/crazyplanets/?pf_ref=sb" } );
    likeButton.hide();
    likeButton.render();
    likeButton.setPosition(100,100);
    likeButton.show();
})();
