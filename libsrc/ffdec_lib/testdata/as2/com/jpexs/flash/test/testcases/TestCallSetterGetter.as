class com.jpexs.flash.test.testcases.TestCallSetterGetter {
	public var myobj : com.jpexs.flash.test.testcases.TestSetterGetter;
	
	public function testSetterCall(){
		this.myobj.myvar = 5;
	}
	public function testGetterCall() : Number {
		return this.myobj.myvar;
	}
	
	public function testStatGetterCall() : Number {
		return com.jpexs.flash.test.testcases.TestSetterGetter.mystatvar;
	}
	
	public function testStatSetterCall(val:Number) {
		com.jpexs.flash.test.testcases.TestSetterGetter.mystatvar = 6;
	}
}