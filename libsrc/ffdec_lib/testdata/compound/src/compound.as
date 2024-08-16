package {
    public function trace(s:int) {
        //print something
    }
}

package mypkg {
    var x:int = 7;
    var a:int = 1;    
    if (x >= 5) {
        a += 100;
    } else {
        a += 200;
    }
    var b:int = a + 10;
}

package mypkg2 {
    var x:int = 7;
    var a:int = 1;    
    if (x >= 5) {
        a += 100;
    } else {
        a += 200;
    }
    var b:int = a + 10;
}

package mypkg3 {
    trace(29);
}

include "mypkg/MyClass.as"
include "mypkg/MyClass2.as"

var y:int = 1;
var c:int = 1;
if (y >= 5) {
    c += 100;
} else {
    c += 100;
}

