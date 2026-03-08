class com.jpexs.flash.test.testcases.TestUnpopped {
	
    public var a;        
    public var c = true;        
    
	public function run() {
		a;
        trace("b");
        if (c) {
            trace("c");
        } 
	}
}