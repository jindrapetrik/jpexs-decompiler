class com.jpexs.TestCallSetterGetter {
	public var myobj : com.jpexs.TestSetterGetter;
	
	public function testSetterCall(){
		this.myobj.myvar = 5;
	}
	public function testGetterCall() : Number {
		return this.myobj.myvar;
	}
	
	public function testStatGetterCall() : Number {
		return com.jpexs.TestSetterGetter.mystatvar;
	}
	
	public function testStatSetterCall(val:Number) {
		com.jpexs.TestSetterGetter.mystatvar = 6;
	}
}