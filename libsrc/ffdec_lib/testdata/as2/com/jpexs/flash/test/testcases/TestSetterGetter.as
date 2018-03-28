class com.jpexs.flash.test.testcases.TestSetterGetter {
	
	private var priv_myvar:Number;
	
	private static var priv_mystatvar: Number;
	
	private var priv_myreadonly : Number = 1;
	
	private var priv_mywriteonly : Number = 2;
	
	public function get myvar(){
		return this.priv_myvar;
	}
	
	public function set myvar(val){
		this.priv_myvar = val;
	}
	
	public static function get mystatvar(){
		return TestSetterGetter.priv_mystatvar;
	}
	
	public static function set mystatvar(val:Number){
		TestSetterGetter.priv_mystatvar = val;
	}
	
	public function get myreadonly(){
		return this.priv_myreadonly;
	}
	
	public function set mywriteonly(val:Number) {
		this.priv_mywriteonly = val;
	}
}