// @author James McGuigan (james.mcguigan@gmail.com)
//
// ********** Part 1 **********//
// JavaScript:
// Sample 1 below contains a JavaScript implementation of a basic Component class, 
// with an instantiation of it. Unfortunately, it doesn't work; list any errors or 
// problems that are preventing it from successfully running.
//
var Component = function (config) {
    // 1. need to declare `property` as `var property`, else it is a global variable
    // 2. need to validate that config is not null
    for (property in config) {
        // 3. typo in propety, semicolon would be nice
        this[propety] = config[property]
    }
}
// 4. Lack of `new` keyword before Array is acceptable assuming Array() has not been redefined
// 5. Extra comma at end of Array() is a syntax error
// 6. Semicolon is technically optional, but may mess up after javascript minification
var list = Array (
    "Item 1",
    "Item 2",
    "Item 3", )
// 7. Lack of `new` keyword before Component will not create a new object, Component has no return value.
// 8. Missing curly { } brackets around config variable 
var instance = Component(id: "XF-254", list: list);


//********** Part 2 **********/

/**
 *  Change the code so that the Component class implements the Publish/Subscribe pattern.
 *  Clients should be able to register for noti"cations using simple keys, e.g.
 *  propertyChanged, user.loggedOut, or any other event that might make sense in an application.
 *  
 *  @param {Hash} config
 */
var Component = function ( config ) {
    this.values    = {}; // {Hash} key/value data pairs
    this.listeners = {}; // {Hash<Array>} Listeners Array
    
    if( config ) {
        for( var key in config ) {
            this.setProperty( key, config[key] );
        }
    }
}                
/**
 *  Sets the property value and triggers any listeners any associated key
 *  @param {String} key   the key for the property
 *  @param {String} value the value for the property
 */
Component.prototype.setProperty = function(key, value) {
    this.values[key] = value;
    this.triggerListener(key);
};
/**
 *  Triggers any listeners associated with a key
 *  @param {String} key   key to trigger
 */
Component.prototype.triggerListener = function(key) {
    if(!( key in this.values ) { return; } // Don't trigger for non-existant keys

    var value = this.values[key];
    if( this.listeners[key] instanceof Array ) {
        for( var i=0, n=this.listeners[key].length; i<n; i++ ) {
            if( this.listeners[key][i] instanceof Function ) {
                this.listeners[key][i](value, key);
            }
        }
    }
};
/**
 *  Registers a callback for a given key
 *  @param {String}   key       key to register
 *  @param {Function} callback  function( value, key )
 */
Component.prototype.registerListener = function(key, callback) {
    if(!( this.listeners[key] instanceof Array )) {
        this.listeners[key] = [];
    }    
    if( callback instanceof Function ) {
        this.listeners[key].push(callback);
    }
};
/**
 *  Unregisters all instances of a callback listener associated with a key
 *  @param {String}   key        key to match
 *  @param {Function} callback   callback instance to remove
 */
Component.prototype.unregisterListener = function(key, callback) {
        if( this.listeners[key] instanceof Array ) {
            // Callback may be registered multiple times
            var index;
            while( (index = this.listeners[key].indexOf(callback)) != -1 ) {
                this.listeners[key].splice(index, 1);
            }
        }
    }
}
                    


//********** Part 3 **********//
// Extending your answer to Part 2, suppose we now want to allow clients to receive notications 
// even if they registered themselves after such notication had been broadcast. 
// Provide a simple implementation, noting any potential performance concerns.

// If all you want is the "last" notification, if set, for a current key
// This runs in constant time and with no additional memory
ComponentLastNotification = function( config ) {
    Component.prototype.constructor.apply(this, arguments);
}
ComponentLastNotification.prototype = new Component;
ComponentLastNotification.prototype.constructor = Component;

ComponentLastNotification.prototype.registerListener = function(key, callback) {
    Component.prototype.registerListener.apply(this, arguments);
    if( key in this.values && callback instanceof Function ) {
        callback(this.values[key], key);
    }
}

// If however you want a full track record of every notification
ComponentFullNotification = function( config ) {
    this.archive = {}; // {Hash<Array>}
    Component.prototype.constructor.apply(this, arguments);
}
ComponentFullNotification.prototype = new Component;
ComponentFullNotification.prototype.constructor = ComponentFullNotification;

// This will require memory storage for every property ever registered. Array.push runs in constant time
ComponentFullNotification.prototype.setProperty = function(key, value) {
    Component.prototype.setProperty.apply(this, arguments);
    
    if(!( this.archive[key] instanceof Array )) {
        this.archive[key] = [];
    }                          
    this.archive[key].push(value);
}
// Linear O(N) time to loop over this.archive on register
ComponentFullNotification.prototype.registerListener = function(key, callback) {
    Component.prototype.registerListener.apply(this, arguments);

    if( var key in this.archive && this.archive[key] instanceof Array && callback instanceof Function ) {
        for( var i=0, n=this.archive[key].length; i<n; i++ ) {
            callback(this.archive[key][i], key);
        }
    }
}



//********** Part 4 **********//
// Define a CustomComponent that extends Component. 
// It should have a setValue method which broadcasts an event when it is called, specifying the old and new values.

CustomComponent = function( config ) {
    this.old = {}; // {Hash}
    Component.prototype.constructor.apply(this, arguments);
}
CustomComponent.prototype = new Component;
CustomComponent.prototype.constructor = CustomComponent;
   
CustomComponent.prototype.setProperty = function(key, value) {
    this.old[key] = this.values[key];
    this.values[key] = value;
    this.triggerListener(key);
}
// Alternative implementation would be to extend ComponentFullNotification: var old = this.archive[this.archive.length-1];
Component.prototype.triggerListener = function(key) {
    if(!( key in this.values ) { return; } // Don't trigger for non-existant keys

    var value = this.values[key];
    var old   = this.old[key];
    if( this.listeners[key] instanceof Array ) {
        for( var i=0, n=this.listeners[key].length; i<n; i++ ) {
            if( this.listeners[key][i] instanceof Function ) {
                this.listeners[key][i](value, old);
            }
        }
    }
};



//********** Discuss **********//
// - How might your implementation change to allow for different base classes to implement the Publish/Subscribe pattern? 
// Use a mixin pattern.
// jQuery.extend( BaseClass.prototype, Component.prototype ); 
// Ensure Component.prototype.constructor is explictly called in the BaseClass constructor, and manually watch out for variable namespace conflicts
//
// - How might your implementation change to support wildcard tokens in the message key, e.g. app.*.log? 
// Split key on the . symbol and recursively build a json tree to store your values (rather than using a one dimentional hash)
// As part of your tree walking routine, if the segment === "*" then loop over all existing keys on that branch 

// - How might your implementation change to support a limited number of notifications for a given event?
// Change your data structure to: this.listeners[key] = [{ callback: function() {}, remainingCallbacks: 5 }]
// decrement remainingCallbacks on each successful trigger event and remove it from the list after it hits 0

// Please find the attached files EventManager and UidEventManager where I solved this problem properly for a production enviroment, with exaustive unit tests


