class com.jpexs.flash.test.testcases.TestVarsMethods {
	
		public var instVar:Number = 1;
		public static var statVar:Number = 2;
	
	
		public function TestVarsMethods(){
			trace("constructor");
		}
		
		public function instMethod() {
			trace("instance method");
		}
		
		public static function statMethod() {
			trace("static method");
		}
}