class com.jpexs.flash.test.testcases.TestSetterGetter {
	
	private var _myvar = 1;
	
	private static var _mystvar = 2;
	
	private var _myvarsetonly = 3;
	
	private var _myvargetonly = 4;
	
	
	public static function get mystvar(){
		return _mystvar;
	}
	
	public static function set mystvar(val){
		_mystvar = val;
	}
	
	public function get myvar(){
		return _myvar;
	}
	
	public function set myvar(val){
		_myvar = val;
	}
	
	public function get myvargetonly(){
		return this._myvargetonly;
	}
	
	public function set myvarsetonly(val){		
		this._myvarsetonly = val;
	}
	
	public function classic(){
		trace("okay");
	}
}