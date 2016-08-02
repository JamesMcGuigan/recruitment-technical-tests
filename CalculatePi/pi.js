var _ = require("underscore");
var timeout = 10 * 1000;

var methods = {
    "Lines Around a Circle": {
        lines: 8,
        diameter: 2,
        nextPi: function() {
           var point = {
                x: Math.cos(2*Math.PI / this.lines),
                y: Math.sin(2*Math.PI / this.lines)
           }
           if( point.x < 1 ) {
               var pointDistance = Math.sqrt( Math.pow(1-point.x,2) + Math.pow(point.y,2) );
               var circumfrence = pointDistance * this.lines;
               pi = circumfrence / this.diameter;
               this.lines = this.lines * 1.1;
           } else {
                throw new Error("max resolution reached");
           }
           return pi;
        }
    },
    "Gregory-Leibniz": {
        numerator:   4,
        denominator: 1,
        sign:        1,
        fractions:   10000,
        nextPi: function() {
            var addition = 0;
            for( var i=0; i<this.fractions; i++ ) {
                addition += this.sign * this.numerator/this.denominator
                this.denominator += 2;
                this.sign = this.sign * -1;
            }
            pi = pi + addition;
            return pi;
        }
    },
}
              
var agreedDigits = function(numbers) { 
    var n = 1;
    var match = true;
    while( match === true && n <= 21 ) {
        for( var i = 1; i < numbers.length; i++ ) {
            //console.log( n, numbers,  Number(numbers[0]).toPrecision(n), Number(numbers[i]).toPrecision(n) )
            if( !isNaN(numbers[i]) && Number(numbers[0]).toPrecision(n) !== Number(numbers[i]).toPrecision(n) ) {
                match = false;
                break;
            } 
        }
        n++;
    }
    return n - 2;
}


var times = {};
var maxTestCount = 3;
var testCount = 0;

for( methodName in methods ) {
    var start = Date.now();
    var pi = 0.0;
    times[methodName] = [{ pi: pi.toPrecision(1), dp: dp, seconds: (Date.now() - start)/1000 }]

    var maxDp = 0;
    while( true ) {
        try {
            var sample = _(5).times(function(n){ return methods[methodName].nextPi() });
            var dp = agreedDigits(sample);
            //console.log(dp, sample);
            if( dp > maxDp ) {
                times[methodName][dp] = { pi: pi.toPrecision(dp+1),  dp: dp, seconds: (Date.now() - start)/1000 };
                console.log(methodName, times[methodName][dp]);
                maxDp = dp;
            }
        } catch(e) {
            console.log(e)
            break;
        }
        if( Date.now() - start > timeout ) { break; }
    }
}

process.exit();
