
package tests_classes 
{
    import tests.TestHello;

    public class TestScriptInitializer 
	{                             	        
         	private static var sa:int = 5;
            
            if (Math.random() * 10 >= 5) {
                sa += 100;
            } else {
                sa += 200;
            }

            private static const sc:int = Math.floor(Math.random() * 50) + sa + x;        	

            private static var sb:int = sa + 20;
            if (sb > 100) {
                sb += 10;
            } else {
                sb += 20;
            }

            private static var sv:int;

            for each (sv in [1,3,5])
            {
                trace(sv);
            }
                      
            public function test() : void {
                const x:int = 5;
                
                var th:TestHello = new TestHello();
            }
	}    
}

import tests.TestHello;

var x:int = Math.random() * 100;

var a:int = 5;
if (Math.random() * 10 >= 5) {
    a += 100;
} else {
    a += 200;
}

const c:int = Math.floor(Math.random() * 50) + a;        	

var b:int = a + 20;
if (b > 100) {
    b += 10;
} else {
    b += 20;
}

var v:int;

for each (v in [1,3,5])
{
    trace(v);
}       

TestHello;
