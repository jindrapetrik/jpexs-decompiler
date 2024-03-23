class com.jpexs.MyClass {
    var v;
    static var sv;
    var v2;  
    static var sv2;
    
    var f;
    
    var c;
    
    var d;    
	
	var _v3;
	static var _sv3;    
	
    var init_v = 2;
    static var sinit_v = 3;	
    
    function testVar() {
        this.v = 1;
    }
    
    function getV2() {
        return v2;
    }
    
    static function getSV2() {
        return sv2;
    }
    
    function callF() {
        f();
    }
    
    function constructC() {
        new c();
    }
    
    function deleteD() {
        delete d;
    }
	
	function set v3(val) {
		this._v3 = val;
	}
	
	function get v3() {
		return this._v3;
	}
	
	static function set sv3(val) {
		MyClass._sv3 = val;
	}
	
	static function get sv3() {
		return MyClass._sv3;
	}
	
	function setV3() {
		this.v = this.v3;
		MyClass.sv = MyClass.sv3;
	}
}
