class com.jpexs.flash.test.testcases.TestSuperSetterGetter extends com.jpexs.flash.test.testcases.TestSetterGetter {
	
	private var _myvar2 = 1;
	
	public function get myvar2(){
		return _myvar2;
	}
	
	public function set myvar2(val){
		_myvar2 = val;
	}

	public function testThisGetSet(){
		this.myvar2 = 2;
        trace(this.myvar2);
        this.myvar2();
        new this.myvar2();
        this.myvar2++
        trace(this.myvar2++);
        trace(++this.myvar2);
	}
    
    public function testThisParentGetSet(){
		this.myvar = 2;
        trace(this.myvar);
        this.myvar();
        new this.myvar();
        this.myvar++
        trace(this.myvar++);
        trace(++this.myvar);
	}
    
    
    public function testSuperGetSet() {
        super.myvar = 3;
        trace(super.myvar);
        super.myvar();
        new super.myvar();
        delete super.myvar;
        super.myvar++
        trace(super.myvar++);
        trace(++super.myvar);
    }
}