class MyClass {

	function testNoReturn(){
		var a = 5;
		var b = a + 27;
		trace("hi:"+b);
	}
  
	function testSimpleReturn(){
		var a = 5;
		var b = 5 * 6;      
		return "bagr"+b;
	}

	function testReturns(){
		var a = 10;
		if(a > 2){
			a++;
			for(var i=0;i<100;i++){
				if(a + i == 27){    
					return a + 7;
				}
				i+= 27;
			}
		} else if (a == 4){
			return 4;
		}
		return 3;
	}
  
	function testSomeReturns(){
		var a = 5;
		if(a < 10){
			return a;
		}
	}
}