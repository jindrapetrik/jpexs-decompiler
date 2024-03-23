class com.jpexs.flash.test.testcases.TestReturnInConstructor {
			
		public function TestReturnInConstructor(){
			var k = 3;
			if (k == 3) {
				trace("A");
				return;
			}
			trace("B");
		}
		
		public function func() {
			var k = 3;
			if (k == 3) {
				trace("A");
				return undefined;
			}
			trace("B");
			return 5;
		}
}